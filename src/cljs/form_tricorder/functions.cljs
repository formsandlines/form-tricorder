(ns form-tricorder.functions
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [garden.core :as garden]
   ["react" :as react]
   [shadow.css :refer (css)]
   [clojure.math]
   [clojure.string :as string]
   ["@radix-ui/react-icons" :as radix-icons]
   [form-tricorder.re-frame-adapter :as rf]
   [formform-vis.core :refer [->attr]]
   [formform-vis.utils-dom :refer [save-svg save-img]]
   [form-tricorder.icons :refer [PerspectivesExpandIcon
                                 PerspectivesCollapseIcon]]
   [form-tricorder.components.export-dialog
    :refer [ExportDialog ExportPreview ExportOptions
            ExportGroup ExportItem]]
   [form-tricorder.components.common.input :refer [Input]]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.copy-trigger :refer [CopyTrigger]]
   [form-tricorder.components.value-filter :refer [ValueFilter]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   ;; [form-tricorder.components.common.toggle-group
   ;;  :refer [ToggleGroup ToggleGroupItem]]
   [form-tricorder.components.common.checkbox :refer [Checkbox]]
   [form-tricorder.components.common.select
    :refer [Select SelectTrigger SelectValue SelectItem SelectContent
            SelectGroup SelectLabel]]
   [form-tricorder.utils :as utils :refer [let+]]))

(def r) ;; hotfix for linting error in let+


(defnc Function
  [{:keys [children]}]
  (d/div
    {:class (css "inner"
                 :gap-8
                 {:display "flex"
                  :flex-direction "column"
                  :height "100%"
                  :width "100%"}
                 ["& > *:last-child"
                  {:overflow "auto"}])}
    children))

(defnc FuncOpts
  [{:keys [children]}]
  (d/div
    {:class (css ;; "outer"
              :border-col
              :p-2 :rounded
              :gap-2
              {:border "1px dashed"
               ;; :position "relative"
               :display "flex"
               :width "fit-content"})}
    children
    (d/label
      {:class (css :fg-muted)}
      ($ radix-icons/MixerHorizontalIcon))
    ;; (d/label
    ;;   {:class (css
    ;;             :font-size-xs :fg-muted :ml-1
    ;;             {:margin-top "-0.3rem"}
    ;;             ;; {:position "absolute"
    ;;             ;;  :top "0"
    ;;             ;;  :right "0"}
    ;;             )}
    ;;   "Options")
    ))


(defmulti gen-component (fn [func-id _] func-id))

(defmethod gen-component :default
  [func-id _]
  (d/pre {:style {:font-family "monospace"}}
         (str (ex-info "Unknown function"
                       {:func-id func-id}))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; EDN

(defnc F-EDN
  [{:keys [edn-str]}]
  (d/pre {:class (css :font-mono)}
    (d/code edn-str)))

(defnc F-EDN--init
  [args]
  (let [expr (rf/subscribe [:input/expr])
        edn-str (prn-str expr)]
    ($ Function
       ($ FuncOpts
          ($ CopyTrigger
             {:text-to-copy edn-str}
             ($ Button
                {:variant :outline
                 :size :icon}
                ($ radix-icons/CopyIcon))))
       (d/div
         ($ F-EDN {:edn-str edn-str
                   & args})))))

(defmethod gen-component :edn
  [_ args]
  ($ F-EDN--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; JSON

(defnc F-JSON
  [{:keys [expr-json-str]}]
  (d/pre {:class (css :font-mono :text-xs)}
    (d/code expr-json-str)))

(defnc F-JSON--init
  [args]
  (let [expr-json (rf/subscribe [:input/->expr-json])
        expr-json-str (.stringify js/JSON expr-json
                                  js/undefined 2)]
    ($ Function
       ($ FuncOpts
          ($ CopyTrigger
             {:text-to-copy expr-json-str}
             ($ Button
                {:variant :outline
                 :size :icon}
                ($ radix-icons/CopyIcon))))
       (d/div
         ($ F-JSON {:expr-json-str expr-json-str
                    & args})))))

(defmethod gen-component :json
  [_ args]
  ($ F-JSON--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Value table

(def vtable-css
  (garden/css
   [[":host"
     {:font-family "var(--font-mono)"
      :font-size "var(--fs-1)"}]
    ["table"
     {:padding-bottom "var(--sp-6)"}]
    ["th"
     {:position "sticky"
      :background-color "var(--col-bg)"
      :top "0"
      :font-weight "var(--weight-medium)"
      :border-top "1px solid var(--col-fg)"
      :border-bottom "1px solid var(--col-fg)"}]
    ["tr:hover td"
     {:background-color "var(--col-n3)"}]
    ["td"
     {:border-top "1px solid var(--col-bg-muted)"}]]))

(defnc F-VTable
  [{:keys [results varorder]}]
  (let [ref (hooks/use-ref nil)]
    (hooks/use-effect
     [results]
     (let [webc-el @ref]
       (aset webc-el "results" results)))
    ($ :ff-vtable {:ref ref
                   :styles (->attr vtable-css)
                   :varorder (->attr varorder)})))

(defn results->tsv
  [results varorder]
  (->> results
       (map (fn [[intpr res]]
              (str (string/join ";" (map utils/pp-val intpr))
                   ";"
                   (utils/pp-val res))))
       (cons (str (string/join ";" (map utils/pp-var varorder))
                  ";Result"))
       (string/join "\n")))

(defnc F-Vtable--init
  [_]
  (let [varorder (rf/subscribe [:input/varorder])
        filtered-results (rf/subscribe [:input/->filtered-results])]
    ;; (println filtered-results)
    ($ Function
       ($ FuncOpts
          ($ CopyTrigger
             {:copy-handler (hooks/use-memo
                              [filtered-results varorder]
                              (fn [_ report-copy-status]
                                (let [csv (results->tsv filtered-results
                                                        varorder)]
                                  (utils/copy-to-clipboard
                                   csv report-copy-status))))}
             ($ Button
                {:variant :outline
                 :size :md}
                ($ radix-icons/CopyIcon)
                (d/span {:class (css :ml-2)} "CSV")))
          (when varorder
            ($ ValueFilter
               {:varorder varorder})))
       (d/div
         ($ F-VTable {:results filtered-results
                      :varorder varorder})))))

(defmethod gen-component :vtable
  [_ args]
  ($ F-Vtable--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; vmap visualization

(defnc VmapPsps
  [{:keys [data varorder set-make-export-preview]}]
  (let [webc-ref (hooks/use-ref nil)]
    (hooks/use-effect
      [data]
      (let [webc-el @webc-ref]
        (aset webc-el "varorder" varorder)
        (aset webc-el "vmapPsps" data)))
    (d/div
      {:class "VmapPsps outer"}
      ($ :ff-vmap-psps {:ref webc-ref
                        "bg-color" (->attr "var(--col-bg)")
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
      {:class "Vmap outer"}
      ($ :ff-vmap {:ref webc-ref
                   "bg-color" (->attr "var(--col-bg)")
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
        vmap (when-not psps? (rf/subscribe [:input/->filtered-vmap]))
        vmap-psps (when psps? (rf/subscribe [:input/->filtered-vmap-psps]))]
    ($ Function
       ($ FuncOpts
          ($ Toggle {:variant :outline
                     :on-click (fn [_] (set-psps? (fn [b] (not b))))}
             ($ (if psps? PerspectivesCollapseIcon PerspectivesExpandIcon))
             (d/span {:class (css :ml-1)}
               "Perspectives"))
          ($ F-Vmap--export
             {:data (if psps? vmap-psps vmap)
              :varorder varorder
              :psps? psps?})
          (when varorder
            ($ ValueFilter
               {:varorder varorder})))
       (d/div
         ($ (if psps? VmapPsps Vmap)
            {:data (if psps? vmap-psps vmap)
             :varorder varorder})))))

(defmethod gen-component :vmap
  [_ args]
  ($ F-Vmap--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; formDNA

(defnc F-FDNA
  [{:keys [dna]}]
  (d/div {:class (css :font-mono :font-size-xs)}
         (d/code
          {:class (css {:word-wrap "break-word"
                        ;; :letter-spacing "0.05em"
                        :letter-spacing "0.01em" ;; !TEMP
                        :display "flex"}
                       ["& > *"
                        {:padding "0.1rem 0"}])}
          (d/span "::")
          (d/span
           {:class (css "dna"
                        {:display "inline-flex"
                         :flex-wrap "wrap"}
                        ["& > span"
                         {:padding "0 0.1rem"}]
                        ["& > span:nth-child(odd)"
                         {:color "var(--col-n29)"
                          :background-color "var(--col-n3)"}]
                        ["& > span:nth-child(even)"
                         {:color "var(--col-m29)"
                          :background-color "var(--col-m3)"}])}
           (for [[group i] (map vector (partition-all 4 dna)
                                (range))
                 :let [s (-> (string/join "" group)
                             string/lower-case ;; !TEMP
                             )]]
             (d/span
              {:key (str i)}
              s))))))

(defn code->str
  [c]
  (case c
    "const" "Constants (nuim)"
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
        dna-view (rf/subscribe [:input/->filtered-dna-view (keyword code)])
        dna-view (if (not= "const" code)
                   (mapv #(if (< % 0) "_" %) dna-view)
                   dna-view)
        varorder (rf/subscribe [:input/varorder])]
    ($ Function
       ($ FuncOpts
          ($ EncodingSel {:current-code code
                          :set-code set-code})
          ($ CopyTrigger
             {:text-to-copy (str "::"
                                 (-> (string/join "" dna-view)
                                     string/lower-case ;; !TEMP
                                     ))}
             ($ Button
                {:variant :outline
                 :size :icon}
                ($ radix-icons/CopyIcon)))
          (when varorder
            ($ ValueFilter
               {:varorder varorder})))
       (d/div
         ($ F-FDNA {:dna dna-view
                    & args})))))

(defmethod gen-component :fdna
  [_ args]
  ($ F-FDNA--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Graph Visualization

(defnc F-Graph
  [{:keys [expr-json graph-type graph-style theme compact-reentry?]}]
  (let [ref (hooks/use-ref nil)]
    (hooks/use-effect
     [expr-json theme graph-style]
     (let [webc-el @ref]
       (aset webc-el "json" expr-json)))
    ($ :ff-fgraph {:ref ref
                   :type (->attr graph-type)
                   "compact-reentry" compact-reentry?
                   :styleclass (->attr (name graph-style))
                   :theme (->attr theme)})))

(defnc F-Graph--init
  [{:keys [graph-type]}]
  (let [expr-json  (rf/subscribe [:input/->expr-json])
        appearance (rf/subscribe [:theme/appearance])
        graph-style (rf/subscribe [:modes/graph-style])
        [compact-reentry? set-compact-reentry?] (hooks/use-state false)
        theme (if (= :dark appearance) "dark" "light")]
    ($ Function
       (when (= graph-type "pack")
         ($ FuncOpts
            (d/div
              {:class (css {:display "flex"
                            :align-items "center"})}
              ($ Label
                 {:htmlFor "styleclass-radio"}
                 "Style:")
              ($ RadioGroup
                 {:id "styleclass-radio"
                  :class (css "StyleClass"
                              :gap-4 :ml-2 :p-2 :rounded
                              :border :border-col
                              {:display "inline-flex"
                               :align-items "center"}
                              ["& > *"
                               :gap-2
                               {:display "flex"
                                :align-items "center"}])
                  :value (name graph-style)
                  :onValueChange #(rf/dispatch [:modes/set-graph-style
                                                {:next-graph-style
                                                 (keyword %)}])}
                 (d/div
                   ($ RadioGroupItem
                      {:id "styleclass-basic"
                       :value "basic"})
                   ($ Label
                      {:htmlFor "styleclass-basic"}
                      "Basic"))
                 (d/div
                   ($ RadioGroupItem
                      {:id "styleclass-gestalt"
                       :value "gestalt"})
                   ($ Label
                      {:htmlFor "styleclass-gestalt"}
                      "Gestalt"))))))
       (when (= graph-type "gsbhooks")
         ($ FuncOpts
            ($ Toggle {:variant :outline
                       :on-click (fn [_] (set-compact-reentry? #(not %)))}
               ;; ($ (if compact-reentry?
               ;;      PerspectivesCollapseIcon PerspectivesExpandIcon))
               ;; (d/span {:class (css :ml-1)})
               "Compact Re-Entries")))
       (d/div
         ($ F-Graph {:expr-json expr-json
                     :graph-type graph-type
                     :graph-style graph-style
                     :compact-reentry? compact-reentry?
                     :theme theme})))))

(defmethod gen-component :depthtree
  [_ args]
  ($ F-Graph--init {:graph-type "tree" & args}))

(defmethod gen-component :graphs
  [_ args]
  ($ F-Graph--init {:graph-type "pack" & args}))

(defmethod gen-component :hooks
  [_ args]
  ($ F-Graph--init {:graph-type "gsbhooks" & args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SelFi CA

(defnc F-Selfi
  [{:keys [rules-fn umwelt]}]
  (let [ref (hooks/use-ref nil)]
    (hooks/use-effect
     [rules-fn umwelt]
     (let [webc-el @ref]
       (aset webc-el "rules"  rules-fn)
       (aset webc-el "umwelt" umwelt)))
    ($ :ff-selfi {:ref ref
                  :res (->attr 100)
                  "ini-ptn" (->attr :random)
                  "vis-limit" (->attr 200)
                  :cellsize (->attr 4)})))

(defnc F-Selfi--init
  [_]
  (let [rules-fn (rf/subscribe [:input/->selfi-rules-fn])
        umwelt   (rf/subscribe [:input/->selfi-umwelt])]
    ($ Function
       (d/div
         ($ F-Selfi {:rules-fn rules-fn
                     :umwelt umwelt})))))

(defmethod gen-component :selfi
  [_ args]
  ($ F-Selfi--init {& args}))
