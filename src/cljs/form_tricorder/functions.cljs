(ns form-tricorder.functions
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   ;; ["react" :as react]
   [formform.calc :as calc]
   [formform.io :as io]
   [clojure.math]
   [clojure.test.check.generators] ;; <- BAD
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.web-components.core]
   [form-tricorder.components.mode-ui :as mode-ui]
   [form-tricorder.utils :as utils :refer [css> clj->js*]]))


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
  (let [styles (css> {:font-family "$mono"
                      :background-color "$inner_hl"})]
    (d/pre {:class (styles)}
           (d/code (prn-str expr)))))

(defnc F-EDN--init
  [args]
  (let [expr (rf/subscribe [:input/expr])]
    ($ F-EDN {:expr expr
              & args})))

(defmethod gen-component :edn
  [_ args]
  ($ F-EDN--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON

(defnc F-JSON
  [{:keys [expr]}]
  (let [styles (css> {:font-family "$mono"
                      :font-size "$1"
                      :background-color "$inner_hl"})]
    (d/pre {:class (styles)}
           (d/code (.stringify js/JSON (expr->json expr)
                               js/undefined 2)))))

(defnc F-JSON--init
  [args]
  (let [expr (rf/subscribe [:input/expr])]
    ($ F-JSON {:expr expr
               & args})))

(defmethod gen-component :json
  [_ args]
  ($ F-JSON--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Value table

(defnc F-Vtable--init
  [_]
  (let [ref (hooks/use-ref nil)
        varorder (rf/subscribe [:input/varorder])
        results  (rf/subscribe [:input/->value])]
    (hooks/use-effect
      [results]
      (let [webc-el @ref]
        (aset webc-el "results" results)))
    (d/div {:class "Vtable"}
      ($ mode-ui/Calc {:current-varorder varorder
                       :debug-origin "Vtable"
                       :set-varorder
                       #(rf/dispatch
                         [:input/changed-varorder {:next-varorder %}])})
      ($ "ff-vtable" {:ref ref
                      :varorder varorder}))))

(defmethod gen-component :vtable
  [_ args]
  ($ F-Vtable--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; vmap visualization

(defnc F-Vmap--init
  [_]
  (let [ref (hooks/use-ref nil)
        varorder (rf/subscribe [:input/varorder])
        vmap     (rf/subscribe [:input/->vmap])]
    (hooks/use-effect
      [vmap]
      (let [webc-el @ref]
        (aset webc-el "vmap" vmap)))
    (d/div {:class "Vmap"}
      ($ mode-ui/Calc {:current-varorder varorder
                       :debug-origin "Vmap"
                       :set-varorder
                       #(rf/dispatch
                         [:input/changed-varorder {:next-varorder %}])})
      ($ "ff-vmap" {:ref ref}))))

(defmethod gen-component :vmap
  [_ args]
  ($ F-Vmap--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph Visualization

(defnc F-Graph--init
  [{:keys [type]}]
  (let [ref (hooks/use-ref nil)
        expr       (rf/subscribe [:input/expr])
        appearance (rf/subscribe [:theme/appearance])
        theme (if (= :dark appearance) "dark" "light")]
    (hooks/use-effect
      [expr appearance]
      (let [webc-el @ref]
        (aset webc-el "expr" expr)))
    ($ "ff-fgraph" {:ref ref
                    :type type
                    :theme theme})))

(defmethod gen-component :depthtree
  [_ args]
  ($ F-Graph--init {:type "tree" & args}))

(defmethod gen-component :graphs
  [_ args]
  ($ F-Graph--init {:type "pack" & args}))

(defmethod gen-component :hooks
  [_ args]
  ($ F-Graph--init {:type "gsbhooks" & args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SelFi CA

(comment
  (def KRGB {:N "#000000"
             :U "#FF0000"
             :I "#00FF00"
             :M "#0000FF"})
  
  (defn sys-ini
    [ini-ptn res]
    (condp ini-ptn =
      :random (vec (repeatedly res calc/rand-const))
      (throw (ex-info "Unknown ini pattern" {}))))

  (defn sys-next
    [gen-prev rules umwelt]
    (let [p 0
          q (dec (count gen-prev))]
      (into []
            (for [i (range (count gen-prev))]
              (let [L (if (> i p) (- i 1) q)
                    E i
                    R (if (< i q) (+ i 1) p)]
                (rules
                 (mapv gen-prev
                       (condp = umwelt
                         :e   [E]
                         :lr  [L R]
                         :ler [L E R]
                         (throw (ex-info "Invalid cell neighbourhood" {}))))))))))

  (defn emulate
    [rules umwelt num evolution]
    (let [prev-gen (last evolution)
          next-gen (sys-next prev-gen rules umwelt)]
      (if (<= num 1)
        evolution
        (recur rules umwelt (dec num) (conj evolution next-gen)))))

  (defnc F-Selfi
    [{:keys [res vis-limit ini-ptn rules umwelt cell-size]}]
    (let [canvas-ref (hooks/use-ref nil)
          ;; evolution is cached in app-db to prevent long delays when
          ;; component gets remounted (cannot use-memo here)
          evol-cache (rf/subscribe [:cache/retrieve :selfi-evolution])
          evolution (if evol-cache
                      evol-cache
                      (rf/dispatch
                       [:cache/update
                        {:key :selfi-evolution
                         :update-fn #(emulate rules umwelt vis-limit
                                              [(sys-ini ini-ptn res)])}]))
          draw (hooks/use-callback
                 :auto-deps
                 (fn [context cw ch]
                   (.clearRect context 0 0 cw ch)
                   (aset context "fillStyle" "black")
                   (.fillRect context 0 0 cw ch)
                   (doseq [[i gen] (map-indexed vector evolution)
                           [j val] (map-indexed vector gen)
                           :let [x (* j cell-size)
                                 y (* i cell-size)]]
                     (aset context "fillStyle" (KRGB val))
                     (.fillRect context x y cell-size cell-size))))]
      (hooks/use-effect
        [draw]
        (let [canvas @canvas-ref
              cw (.-width canvas)
              ch (.-height canvas)
              context (.getContext canvas "2d")]
          (draw context cw ch)))
      (d/canvas {:ref canvas-ref
                 :width  (* res cell-size)
                 :height (* vis-limit cell-size)}))))

(defnc F-Selfi--init
  [_]
  (let [ref (hooks/use-ref nil)
        rules-fn (rf/subscribe [:input/->selfi-rules-fn])
        umwelt   (rf/subscribe [:input/->selfi-umwelt])
        varorder (rf/subscribe [:input/varorder])
        ;; dna      (rf/subscribe [:input/->dna])
        ]
    (hooks/use-effect
      [rules-fn umwelt]
      (let [webc-el @ref]
        (aset webc-el "rules"  rules-fn)
        (aset webc-el "umwelt" umwelt)))
    (d/div {:class "Selfi"}
      ($ mode-ui/Calc {:current-varorder varorder
                       :debug-origin "Selfi"
                       :set-varorder
                       #(rf/dispatch
                         [:input/changed-varorder {:next-varorder %}])})
      ($ "ff-selfi" {:ref ref
                     :res      100
                     :iniptn   :random
                     :vislimit 200
                     :cellsize 4}))))

(defmethod gen-component :selfi
  [_ args]
  ($ F-Selfi--init {& args}))
