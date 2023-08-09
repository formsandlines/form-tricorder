(ns form-tricorder.functions
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   ; ["react" :as react]
   [formform.calc :as calc]
   [formform.expr :as expr]
   [formform.io :as io]
   ["/form-svg$default" :as form-svg]
   [clojure.math]
   [form-tricorder.components.mode-ui :as mode-ui]
   [form-tricorder.utils :as utils :refer [clj->js*]]))


(defn expr->json
  [expr]
  (clj->js* (io/uniform-expr {:legacy? true} expr)))

(defmulti gen-component (fn [func-id _] func-id))

(defmethod gen-component :default
  [func-id _]
  (d/pre {:style {:font-family "monospace"}}
         (str (ex-info "Unknown function"
                       {:func-id func-id}))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; EDN

(defnc F-EDN
  [{:keys [expr]}]
  (d/pre {:style {:font-family "monospace"}}
         (d/code (prn-str expr))))

(defnc F-EDN--init
  [args]
  (let [expr (refx/use-sub [:expr])]
    ($ F-EDN {:expr expr
              & args})))

(defmethod gen-component :edn
  [_ args]
  ($ F-EDN--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON

(defnc F-JSON
  [{:keys [expr]}]
  (d/pre {:style {:font-family "monospace"}}
         (d/code (.stringify js/JSON (expr->json expr)
                             js/undefined 2))))

(defnc F-JSON--init
  [args]
  (let [expr (refx/use-sub [:expr])]
    ($ F-JSON {:expr expr
               & args})))

(defmethod gen-component :json
  [_ args]
  ($ F-JSON--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Value table

(defnc F-Vtable
  [{:keys [results varorder]}]
  (let [results (map (fn [[intpr res]]
                       {:id (random-uuid)
                        :intpr intpr
                        :result res})
                     results)]
    (d/table
     {:style {:font-family "monospace"}}
     (d/thead
      (d/tr
       (for [x varorder
             :let [x (str x)]]
         (d/th {:key x}
               x))
       (d/th "Result")))
     (d/tbody
      (for [{:keys [id intpr result]} results]
        (d/tr
         {:key id}
         (for [[i v] (map-indexed vector intpr)]
           (d/td {:key (str id "-" i)}
                 (str v) "\u00a0"))
         (d/td (str result))))))))

(defnc F-Vtable--init
  [args]
  (let [varorder (refx/use-sub [:varorder])
        results  (refx/use-sub [:value])]
    (d/div {:class "Vtable"}
           ($ mode-ui/Calc {:current-varorder varorder
                            :debug-origin "Vtable"
                            :set-varorder
                            #(refx/dispatch
                              [:changed-varorder {:next-varorder %}])})
           ($ F-Vtable {:results results
                        :varorder varorder
                        & args}))))

(defmethod gen-component :vtable
  [_ args]
  ($ F-Vtable--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; vmap

(defn gen-margins
  [{:keys [gap-bounds gap-growth]
    :or {gap-bounds [0.1 ##Inf] gap-growth 0.3}}
   dim]
  (let [growth-fn (fn [part-dim]
                    (min (max (* part-dim gap-growth)
                              (first gap-bounds))
                         (second gap-bounds)))]
    (mapv growth-fn (range 0 dim))))

(def KRGB {:N "#000000"
           :U "#FF0000"
           :I "#00FF00"
           :M "#0000FF"})

(def const->coords {:N [0 0]
                    :U [0 1]
                    :I [1 0]
                    :M [1 1]
                    :_ [0 0]})

(defnc F-Vmap
  [{:keys [vmap scale-to-fit? cellsize padding margins stroke-width
           colors bg-color stroke-color label]
    :or {scale-to-fit? false padding 0 stroke-width 0.5
         colors KRGB bg-color nil stroke-color nil label nil}}]
  (let [dim       (calc/vmap-dimension vmap)
        vmap      (if (> dim 0) vmap {:_ vmap})
        cellsize  (if (nil? cellsize)
                    12
                    cellsize)
        margins   (reverse (take dim (if (nil? margins)
                                       (utils/geom-seq 0.1 2.0)
                                       margins)))
        sum-gaps  (fn [margins] (apply + (map (fn [n i]
                                                (* n (utils/pow-nat 2 i)))
                                              margins (range))))
        vmap-size (+ (* (utils/pow-nat 2 dim) cellsize)
                     (* (sum-gaps margins) cellsize))
        vmap-diag (clojure.math/sqrt (* (* vmap-size vmap-size) 2))
        full-size (+ vmap-diag padding)]

    (d/svg {:width  (if scale-to-fit? "100%" full-size)
            :height (if scale-to-fit? "100%" full-size)
            :view-box (let [shift (+ (/ padding 2))]
                        (str "-" shift " -" shift
                             " " full-size " " full-size))}
           (when (some? bg-color)
             (d/rect {:x (str "-" (/ padding 2))
                      :y (str "-" (/ padding 2))
                      :width  full-size
                      :height full-size
                      :fill   bg-color}))
           (d/g {:transform
                 (str "translate(" 0 "," (/ vmap-diag 2) ")"
                      "scale(" cellsize ") "
                      "rotate(" -45 ") ")}
                ((fn f [vmap dim margins]
                   (for [[k v] vmap]
                     (d/g {:key (str "dim" dim "_" (name k))
                           :transform
                           (let [coords (const->coords k)
                                 gaps   (if (> (count margins) 1)
                                          (sum-gaps (rest margins))
                                          0)
                                 shift  (+ (utils/pow-nat 2 dim)
                                           (first margins)
                                           gaps)
                                 x (* (coords 0) shift)
                                 y (* (coords 1) shift)]
                             (str "translate(" x "," y ")"))}
                          (if (> dim 0)
                            (f v (dec dim) (rest margins))
                            (d/rect
                             {:width  1
                              :height 1
                              :fill (colors v)
                              & (if (nil? stroke-color)
                                  {}
                                  {:stroke-width (/ stroke-width cellsize)
                                   :stroke stroke-color})})))))
                 vmap (dec dim) margins))

               ;; ! TODO
           (when (some? label)
             (d/g {:transform (str "translate(" 0 "," vmap-diag ")")}
                  (d/text {}
                          label))))))

(defnc F-Vmap--init
  [args]
  (let [varorder (refx/use-sub [:varorder])
        vmap     (refx/use-sub [:vmap])]
    (d/div {:class "Vmap"}
           ($ mode-ui/Calc {:current-varorder varorder
                            :debug-origin "Vmap"
                            :set-varorder
                            #(refx/dispatch
                              [:changed-varorder {:next-varorder %}])})
           ($ F-Vmap {:vmap vmap
                      & args}))))

(defmethod gen-component :vmap
  [_ args]
  ($ F-Vmap--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Depth tree

(defn remove-children
  [parent]
  (let [children (when (.. parent hasChildNodes)
                   (.. parent -children))]
    (doseq [el children] (.remove el))))


(defnc F-Depthtree
  [{:keys [expr]}]
  (let [id   (str "depthtree" (random-uuid))
        json (expr->json expr)]
    (hooks/use-effect
     [expr]
     (remove-children (.. js/document (getElementById id)))
     (form-svg "tree" json
               (clj->js
                {:parentId id})))
    (d/div
     {:class "Output"
      :id id})))

(defnc F-Depthtree--init
  [args]
  (let [expr (refx/use-sub [:expr])]
    ($ F-Depthtree {:expr expr
                    & args})))

(defmethod gen-component :depthtree
  [_ args]
  ($ F-Depthtree--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph notation

(defnc F-Graphs
  [{:keys [expr]}]
  (let [id   (str "graph" (random-uuid))
        json (expr->json expr)]
    (hooks/use-effect
      [expr]
      (remove-children (.. js/document (getElementById id)))
      (form-svg "pack" json
                (clj->js
                  {:parentId id})))
    (d/div
      {:class "Output"
       :id id})))

(defnc F-Graphs--init
  [args]
  (let [expr (refx/use-sub [:expr])]
    ($ F-Graphs {:expr expr
                 & args})))

(defmethod gen-component :graphs
  [_ args]
  ($ F-Graphs--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Hooks notation

(defnc F-Hooks
  [{:keys [expr]}]
  (let [id   (str "hooks_" (random-uuid))
        json (expr->json expr)]
    (hooks/use-effect
     [expr]
     (remove-children (.. js/document (getElementById id)))
     (form-svg "gsbhooks" json
               (clj->js
                {:parentId id})))
    (d/div
     {:class "Output"
      :id id})))

(defnc F-Hooks--init
  [args]
  (let [expr (refx/use-sub [:expr])]
    ($ F-Hooks {:expr expr
                & args})))

(defmethod gen-component :hooks
  [_ args]
  ($ F-Hooks--init {& args}))


