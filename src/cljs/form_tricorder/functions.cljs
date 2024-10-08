(ns form-tricorder.functions
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [garden.core :as garden]
   ;; ["react" :as react]
   [formform.calc :as calc]
   [formform.io :as io]
   [clojure.math]
   [form-tricorder.re-frame-adapter :as rf]
   [formform-vis.core]
   [formform-vis.utils :refer [save-svg]]
   [form-tricorder.components.mode-ui :as mode-ui]
   [form-tricorder.components.export-dialog :refer [ExportDialog]]
   [form-tricorder.stitches-config :refer [css]]
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
  (let [styles (css {:font-family "$mono"
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
  (let [styles (css {:font-family "$mono"
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

(def vtable-css
  (garden/css
   [[":host"
     {:font-family "var(--fonts-mono)"
      :font-size "var(--fontSizes-1)"}]
    ["th"
     {:font-weight "var(--fontWeights-medium)"
      :border-top "1px solid var(--colors-inner_fg)"
      :border-bottom "1px solid var(--colors-inner_fg)"}]
    ["tr:hover td"
     {:background-color "var(--colors-inner_hl)"}]
    ["td"
     {:border-top "1px solid var(--colors-inner_n200)"}]]))

(defnc F-Vtable--init
  [_]
  (let [ref (hooks/use-ref nil)
        varorder (rf/subscribe [:input/varorder])
        results  (rf/subscribe [:input/->value])
        css vtable-css]
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
      ($ :ff-vtable {:ref ref
                     :styles (str \" css \")
                     :varorder varorder}))))

(defmethod gen-component :vtable
  [_ args]
  ($ F-Vtable--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; vmap visualization

;; (def vtable-css
;;   (css [[":host"
;;          {:font-family "var(--fonts-mono)"
;;           :font-size "var(--fontSizes-1)"}]
;;         ["th"
;;          {:font-weight "var(--fontWeights-medium)"
;;           :border-top "1px solid var(--colors-inner_fg)"
;;           :border-bottom "1px solid var(--colors-inner_fg)"}]
;;         ["tr:hover td"
;;          {:background-color "var(--colors-inner_hl)"}]
;;         ["td"
;;          {:border-top "1px solid var(--colors-inner_n200)"}]]))

(defnc F-Vmap--init
  [_]
  (let [[psps? set-psps?] (hooks/use-state false)
        ref (hooks/use-ref nil)
        varorder (rf/subscribe [:input/varorder])
        dna (rf/subscribe [:input/->dna])]
    (hooks/use-effect
      [dna psps?]
      (let [webc-el @ref]
        (aset webc-el "dna" dna)))
    (d/div {:class "Vmap"}
      ($ mode-ui/Calc {:current-varorder varorder
                       :debug-origin "Vmap"
                       :set-varorder
                       #(rf/dispatch
                         [:input/changed-varorder {:next-varorder %}])})
      (if psps?
        ($ :ff-vmap-psps {:ref ref
                          ;; "full-svg" (str true)
                          "bg-color" (str "\"" "var(--colors-outer_bg)" "\"")
                          :padding 6
                          :varorder (str varorder)})
        ($ :ff-vmap {:ref ref
                     ;; "full-svg" (str true)
                     "bg-color" (str "\"" "var(--colors-outer_bg)" "\"")
                     :padding 6
                     :varorder (str varorder)}))
      (d/button {:on-click (fn [_] (set-psps? (fn [b] (not b))))}
        (if psps? "-" "+"))
      ($ ExportDialog)
      (d/button {:on-click
                 (fn [e]
                   (let [el (js/document.createElement "ff-vmap")]
                     (.setAttribute el "full-svg" "true")
                     (aset el "varorder" varorder)
                     (aset el "dna" dna)
                     (.. e -target -parentNode (appendChild el))
                     (js/setTimeout
                      (fn []
                        (let [vmap (.. el -shadowRoot
                                       (getElementById "vmap-figure"))]
                          ;; (js/console.log vmap)
                          (save-svg vmap "test.svg")))
                      1000)
                     
                     ;; (save-svg el "test.svg")
                     ))}
        "Save SVG"))))

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
    ($ :ff-fgraph {:ref ref
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
      ($ :ff-selfi {:ref ref
                    :res 100
                    "ini-ptn" :random
                    "vis-limit" 200
                    :cellsize 4}))))

(defmethod gen-component :selfi
  [_ args]
  ($ F-Selfi--init {& args}))
