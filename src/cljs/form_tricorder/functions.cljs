(ns form-tricorder.functions
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [garden.core :as garden]
   ["react" :as react]
   [shadow.css :refer (css)]
   [formform.calc :as calc]
   [formform.expr :as expr]
   [formform.io :as io]
   [clojure.math]
   [clojure.string :as string]
   [form-tricorder.re-frame-adapter :as rf]
   [formform-vis.core]
   [formform-vis.utils :refer [save-svg save-img scale-svg ->attr]]
   [form-tricorder.icons :refer [PerspectivesExpandIcon
                                 PerspectivesCollapseIcon]]
   [form-tricorder.components.export-dialog
    :refer [ExportDialog ExportPreview ExportOptions
            ExportGroup ExportItem]]
   [form-tricorder.components.common.input :refer [Input]]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   [form-tricorder.components.common.checkbox :refer [Checkbox]]
   [form-tricorder.components.common.select
    :refer [Select SelectTrigger SelectValue SelectItem SelectContent
            SelectGroup SelectLabel]]
   [form-tricorder.stitches-config :as st]
   [form-tricorder.utils :as utils :refer [clj->js* let+]]))

(def r) ;; hotfix for linting error in let+

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
  (let [styles (st/css {:font-family "$mono"
                     ;; :background-color "$inner_hl"
                     })]
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
  (let [styles (st/css {:font-family "$mono"
                     :font-size "$1"
                     ;; :background-color "$inner_hl"
                     })]
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
      :border-top "1px solid var(--colors-inner-fg)"
      :border-bottom "1px solid var(--colors-inner-fg)"}]
    ["tr:hover td"
     {:background-color "var(--colors-n3)"}]
    ["td"
     {:border-top "1px solid var(--colors-inner-muted)"}]]))

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


(defnc VmapPsps
  [{:keys [data varorder set-make-export-preview]}]
  (let [webc-ref (hooks/use-ref nil)]
    (hooks/use-effect
      [data]
      (let [webc-el @webc-ref]
        (aset webc-el "varorder" varorder)
        (aset webc-el "vmapPsps" data)))
    (d/div
      {:class "VmapPsps"}
      ($ :ff-vmap-psps {:ref webc-ref
                        "bg-color" (->attr "var(--colors-outer-bg)")
                        :padding (->attr 6)}))))

(defnc Vmap
  [{:keys [data varorder set-make-export-preview]}]
  (let [webc-ref (hooks/use-ref nil)]
    (hooks/use-effect
      [data]
      (let [webc-el @webc-ref]
        (aset webc-el "varorder" varorder)
        (aset webc-el "vmap" data)))
    (d/div
      {:class "Vmap"}
      ($ :ff-vmap {:ref webc-ref
                   "bg-color" (->attr "var(--colors-outer-bg)")
                   :padding (->attr 6)}))))

;; (def vmap-export-css
;;   (garden/css
;;    [[":host"
;;      {:color "var(--col-n31)"
;;       :fill "currentcolor"}]]))

(defnc F-Vmap-preview
  [{:keys [psps? vis-id data varorder negative? bg-color padding scale
           default-caption? custom-caption-input]} ref]
  {:wrap [(react/forwardRef)]}
  (hooks/use-effect
    [varorder data]
    (let [webc-el @ref]
      (aset webc-el "varorder" varorder)
      (aset webc-el (if psps? "vmapPsps" "vmap") data)))
  ($ ExportPreview
     {:class (if negative?
               (css {:color-scheme "dark"})
               (css {:color-scheme "light"}))}
     ($ vis-id
        {:ref ref
         ;; :cellsize (->attr (* scale 12))
         "fig-scale" scale
         "full-svg" (->attr true)
         "no-caption" (->attr (not default-caption?))
         :label (->attr custom-caption-input)
         ;; :styles (->attr vmap-export-css)
         "caption-attrs"
         (->attr {:font-family "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace"
                  :font-size "12px"
                  :fill (if negative? "#ffffff" "#000000")})
         "fig-padding" (->attr padding)
         "fig-bg-color" (->attr bg-color)})))

(defnc F-Vmap--export
  [{:keys [psps? data varorder]}]
  (let [vis-id (if psps? "ff-vmap-psps" "ff-vmap")
        export-ref (hooks/use-ref nil)
        [format set-format] (hooks/use-state "svg")
        [scale set-scale] (hooks/use-state 1.0)
        [negative? set-negative?] (hooks/use-state false)
        [background? set-background?] (hooks/use-state false)
        [padding set-padding] (hooks/use-state 0)
        [bg-color set-bg-color] (hooks/use-state "#ffffff")
        [default-caption? set-default-caption?] (hooks/use-state true)
        [custom-caption? set-custom-caption?] (hooks/use-state false)
        [custom-caption-input set-custom-caption-input] (hooks/use-state "")]
    ($ ExportDialog
       {:title "Export vmap…"
        :on-export
        (fn [e]
          (let [el (.. export-ref -current)
                make-filename (fn [ext] (str (utils/get-timestamp) "_"
                                            vis-id ext))
                get-svg-el #(.. % -shadowRoot
                                (getElementById (if psps?
                                                  "psps-figure"
                                                  "vmap-figure")))]
            (js/setTimeout
             (fn []
               (let [svg-el (get-svg-el el)]
                 (case format
                   "png" (save-img svg-el (make-filename ".png") {})
                   "svg" (save-svg svg-el (make-filename ".svg") {}))))
             1000)))}
       ($ F-Vmap-preview
          {:ref export-ref
           :psps? psps?
           :vis-id vis-id
           :data data
           :varorder varorder
           :scale scale
           :negative? negative?
           :bg-color (when background? bg-color)
           :padding padding
           :default-caption? default-caption?
           :custom-caption-input (when custom-caption? custom-caption-input)})
       ($ ExportOptions
          ($ ExportGroup
             {:orientation :horizontal}
             ($ ExportGroup
                {:orientation :vertical}
                ($ ExportItem
                   {:title "File format:"}
                   ($ RadioGroup
                      {:class (css "FileFormat"
                                   :gap-10
                                   {:display "flex"}
                                   ["& > *"
                                    :gap-3
                                    {:display "flex"
                                     :align-items "center"}])
                       ;; :defaultValue "svg"
                       :value format
                       :onValueChange set-format}
                      (d/div
                        ($ RadioGroupItem
                           {:id "png"
                            :value "png"})
                        ($ Label
                           {:htmlFor "png"}
                           "PNG"))
                      (d/div
                        ($ RadioGroupItem
                           {:id "svg"
                            :value "svg"})
                        ($ Label
                           {:htmlFor "svg"}
                           "SVG"))))
                ($ ExportItem
                   {:title "Scale:"}
                   ;; {:class (css :mt-4)}
                   (let [scales [0.5 1.0 2.0 3.0 4.0 5.0]
                         scale->label (zipmap scales
                                              ["Size ×0.5 (small)"
                                               "Size ×1 (standard)"
                                               "Size ×2 (medium)"
                                               "Size ×3 (large)"
                                               "Size ×4 (extralarge)"
                                               "Size ×5 (print)"])]
                     ($d Select
                       {:id "varorder-select"
                        :value scale
                        :onValueChange (fn [v] (set-scale v))}
                       ($ SelectTrigger
                          ($d SelectValue
                            {:placeholder "Select scale…"}
                            (scale->label scale)))
                       ($ SelectContent
                          {:class "outer"}
                          (for [x scales
                                :let [label (scale->label x)]]
                            ($ SelectItem
                               {:key label
                                :value x}
                               label)))))))
             ($ ExportItem
                {:title "Appearance:"}
                (d/div
                  {:class (css :gap-3
                               {:display "flex"
                                :flex-direction "column"})}
                  (d/div
                    {:class (css :gap-3
                                 {:display "flex"
                                  :align-items "center"})}
                    ($ Checkbox
                       {:id "negative"
                        :checked negative?
                        :onCheckedChange #(set-negative? (not negative?))})
                    ($ Label
                       {:htmlFor "negative"}
                       "negative"))
                  (d/div
                    {:class (css :gap-3
                                 {:display "flex"
                                  :flex-direction "row"})}
                    (d/div
                      {:class (css :gap-3
                                   {:display "flex"
                                    :align-items "center"})}
                      ($ Checkbox
                         {:id "background"
                          :checked background?
                          :onCheckedChange #(set-background?
                                             (not background?))})
                      ($ Label
                         {:htmlFor "background"}
                         "background:"))
                    ;; ? browser support
                    (d/input
                      {:type "color"
                       :value bg-color
                       :onChange #(set-bg-color (.. % -target -value))
                       :disabled (not background?)}))
                  (d/div
                    {:class (css :gap-3
                                 {:display "flex"
                                  :flex-direction "row"
                                  :align-items "center"
                                  :margin-left "var(--sizes-icon-sm)"
                                  :padding-left "var(--sp-3)"})}
                    ($ Label
                       {:htmlFor "padding"}
                       "padding:")
                    ($ Input
                       {:class (css :w-20)
                        :id "padding"
                        :type "number"
                        :step "1"
                        :min "0"
                        :max "99"
                        :value padding
                        :onChange
                        #(set-padding
                          (try (parse-long (.. % -target -value))
                               (catch js/Error e
                                 (js/console.error e))))}))))
             ($ ExportItem
                {:title "Caption:"}
                (d/div
                  {:class (css :gap-3
                               {:display "flex"
                                :flex-direction "column"})}
                  (d/div
                    {:class (css :gap-3
                                 {:display "flex"
                                  :align-items "center"})}
                    ($ Checkbox
                       {:id "default-caption"
                        :checked default-caption?
                        :onCheckedChange #(set-default-caption?
                                           (not default-caption?))})
                    ($ Label
                       {:htmlFor "default-caption"}
                       "variable order"))
                  (d/div
                    {:class (css :gap-3
                                 {:display "flex"
                                  :align-items "center"})}
                    ($ Checkbox
                       {:id "custom-caption"
                        :checked custom-caption?
                        :onCheckedChange #(set-custom-caption?
                                           (not custom-caption?))})
                    ($ Label
                       {:htmlFor "custom-caption"}
                       "custom label:"))
                  ($ Input
                     {:type "text"
                      :value custom-caption-input
                      :disabled (not custom-caption?)
                      :onChange #(set-custom-caption-input
                                  (.. % -target -value))
                      :placeholder "Custom text"}))))))))

(defnc F-Vmap--init
  []
  (let [[psps? set-psps?] (hooks/use-state false)
        varorder (rf/subscribe [:input/varorder])
        vmap (when-not psps? (rf/subscribe [:input/->vmap]))
        vmap-psps (when psps? (rf/subscribe [:input/->vmap-psps]))]
    (d/div
      {:class (css :gap-2
                   {:display "flex"
                    :flex-direction "column"})}
      (d/div
        {:class (css :mb-3 :gap-1
                     {:display "flex"})}
        ($ Toggle {:variant :outline
                   :on-click (fn [_] (set-psps? (fn [b] (not b))))}
           ($ (if psps? PerspectivesCollapseIcon PerspectivesExpandIcon))
           (d/span {:class (css :ml-1)}
             "Perspectives"))
        ($ F-Vmap--export
           {:data (if psps? vmap-psps vmap)
            :varorder varorder
            :psps? psps?}))
      ($ (if psps? VmapPsps Vmap)
         {:data (if psps? vmap-psps vmap)
          :varorder varorder}))))

#_#_
(defnc Title
  [{:keys [children]}]
  (d/h2
    {:class (css :fg-primary)}
    children))

(defnc SlotTest
  [{:keys [title footer children]}]
  (<>
    (d/div {:class (css :bg-primary)}
      (when title title))
    children
    (when footer footer)))

(defmethod gen-component :vmap
  [_ args]
  #_
  ($ SlotTest
     {:title ($ Title "title")
      :footer (d/p "footer")}
     (d/div "test"))
  ($ F-Vmap--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; formDNA

(defnc F-FDNA
  [{:keys [dna]}]
  (let [styles (st/css {:font-family "$mono"
                     :font-size "$xs"
                     "& code" {:word-wrap "break-word"
                               :letter-spacing "0.05em"
                               :display "flex"
                               "& > *"
                               {:padding "0.1rem 0"}
                               ".dna"
                               {:display "inline-flex"
                                :flex-wrap "wrap"
                                "& > span"
                                {:padding "0 0.1rem"}
                                "& > span:nth-child(odd)"
                                {:color "$n29"
                                 :background-color "$n3"}
                                "& > span:nth-child(even)"
                                {:color "$m29"
                                 :background-color "$m3"}}}})]
    (d/div {:class (styles)}
      (d/code
        (d/span "::")
        (d/span
          {:class "dna"}
          (for [[group i] (map vector (partition-all 4 dna) (range))
                :let [s (string/join "" group)]]
            (d/span
              {:key (str i)}
              s)))))))

(defn code->str
  [c]
  (case c
    "const" "Constants (NUIM)"
    (str (string/upper-case c)
         " → 0123")))

(defnc EncodingSel
  [{:keys [current-code set-code]}]
  (let [encodings ["const" "nmui" "nuim"]]
    ($d Select
      {:id "fdna-encoding-select"
       :value current-code
       :onValueChange (fn [v] (set-code v))}
      ($ SelectTrigger
         {:style {:width "10rem"}}
         ($d SelectValue
           (code->str current-code)))
      ($ SelectContent
         {:class "inner"}
         (for [c encodings
               :let [label (code->str c)]]
           ($ SelectItem
              {:key c
               :value c}
              label))))))

(defnc F-FDNA--init
  [args]
  (let [[code set-code] (hooks/use-state "const")
        dna-view (rf/subscribe [:input/->dna-view (keyword code)])]
    (d/div
      {:style {:display "flex"
               :flex-direction "column"
               :gap "1rem"}}
      ($ EncodingSel {:current-code code
                      :set-code set-code})
      ($ F-FDNA {:dna dna-view
                 & args}))))

(defmethod gen-component :fdna
  [_ args]
  ($ F-FDNA--init {& args}))

(comment
  (expr/op-get (expr/eval->expr-all {:varorder nil} [] {}) :dna)
  ;; => "[:fdna [a] ::MIUN]"
  ;; => [:fdna [a] [:M :I :U :N]]
  ,)

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
        ;; dna      (rf/subscribe [:input/->dna])
        ]
    (hooks/use-effect
      [rules-fn umwelt]
      (let [webc-el @ref]
        (aset webc-el "rules"  rules-fn)
        (aset webc-el "umwelt" umwelt)))
    (d/div {:class "Selfi"}
      ($ :ff-selfi {:ref ref
                    :res 100
                    "ini-ptn" :random
                    "vis-limit" 200
                    :cellsize 4}))))

(defmethod gen-component :selfi
  [_ args]
  ($ F-Selfi--init {& args}))
