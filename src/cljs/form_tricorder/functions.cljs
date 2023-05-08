(ns form-tricorder.functions
  (:require
    [helix.core :refer [defnc fnc $ <> provider]]
    [helix.hooks :as hooks]
    [helix.dom :as d]
    [formform.calc :as calc]
    [formform.expr :as expr]
    [formform.io :as io]
    ["/form-svg$default" :as form-svg]
    [clojure.math]
    [form-tricorder.utils :as utils :refer [clj->js*]]))


(defn expr->json
  [expr]
  (clj->js* (io/uniform-expr {:legacy? true} expr)))

(defmulti gen-component (fn [func-id _] func-id))

(defmethod gen-component :default
  [func-id _]
  (fnc [] (d/pre {:style {:font-family "monospace"}}
                 (str (ex-info "Unknown function"
                               {:func-id func-id})))))

(defmethod gen-component :edn
  [_ expr]
  (fnc [{}]
       (d/pre {:style {:font-family "monospace"}}
              (d/code (str expr)))))

(defmethod gen-component :json
  [_ expr]
  (fnc [{}]
       (d/pre {:style {:font-family "monospace"}}
              (d/code (.stringify js/JSON (expr->json expr)
                                  js/undefined 2)))))

(defmethod gen-component :vtable
  [_ expr]
  (let [{:keys [results varorder]} (expr/eval-all expr)
        results (map (fn [[intpr res]]
                       {:id (random-uuid)
                        :intpr intpr
                        :result res})
                     results)]
    (fnc
      []
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
              (d/td (str result)))))))))


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
                    :M [1 1]})

(defmethod gen-component :vmap
  [_ expr]
  (let [vmap (-> expr
                 expr/=>*
                 (expr/op-get :dna)
                 calc/dna->vdict
                 calc/vdict->vmap)] 
    (fnc
      [{:keys [scale-to-fit? cellsize padding margins stroke-width
               colors bg-color stroke-color label]
        :or {scale-to-fit? false padding 0 stroke-width 0.5
             colors KRGB bg-color nil stroke-color nil label nil}}]
      (let [dim       (:dim (meta vmap))
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
                              label))))))))

; (defmethod gen-component :depth-tree
;   [_ expr]
;   (let [json (clj->js* (io/uniform-expr {:branchname :space
;                                          :use-unmarked? true} expr))]
;     (js/console.log json)
;     (fnc [{}]
;          ; (d/div (str json))
;          (let [; root-ref (hooks/use-ref nil)
;                ]
;            (hooks/use-effect
;              :once
;              (form-svg "tree" json
;                        (clj->js
;                          {:parentId "DepthTree"}))
;              ; (let [viz (form-svg "tree" expr)]
;              ;   (js/console.log viz)
;              ;   (swap! root-ref #(.appendChild (.-current %)
;              ;                                  (.-container viz))))
;              )
;            (d/div
;              {:class "Output"
;               :id "DepthTree"
;               ; :ref root-ref
;               })))))

(defmethod gen-component :depth-tree
  [_ expr]
  (let [id   "depthtree" ; (random-uuid)
        json (expr->json expr)]
    (fnc [{}]
         (hooks/use-effect
          :once
          (form-svg "tree" json
                    (clj->js
                     {:parentId id})))
         (d/div
          {:class "Output"
           :id id}))))

(defmethod gen-component :graph
  [_ expr]
  (let [id   "graph" ; (random-uuid)
        json (expr->json expr)]
    (fnc [{}]
         (hooks/use-effect
          :once
          (form-svg "pack" json
                    (clj->js
                     {:parentId id})))
         (d/div
          {:class "Output"
           :id id}))))

(defmethod gen-component :hooks
  [_ expr]
  (let [id   "hooks" ; (random-uuid)
        json (expr->json expr)]
    (fnc [{}]
         (hooks/use-effect
          :once
          (form-svg "gsbhooks" json
                    (clj->js
                     {:parentId id})))
         (d/div
          {:class "Output"
           :id id}))))
