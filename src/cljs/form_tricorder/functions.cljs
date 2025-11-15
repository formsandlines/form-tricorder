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
   [formform.emul :as emul]
   [form-tricorder.re-frame-adapter :as rf]
   [formform-vis.core :refer [->attr]]
   [formform-vis.components.automaton :refer [make-state!]]
   [form-tricorder.icons :refer [PerspectivesExpandIcon
                                 PerspectivesCollapseIcon]]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.copy-trigger :refer [CopyTrigger]]
   [form-tricorder.components.value-filter :refer [ValueFilter]]
   [form-tricorder.components.export-dialog :refer [ExportPreview]]
   [form-tricorder.components.image-export :refer [ImageExportVmap
                                                   ImageExportGraph]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   ;; [form-tricorder.components.common.toggle-group
   ;;  :refer [ToggleGroup ToggleGroupItem]]
   [form-tricorder.components.common.select
    :refer [Select SelectTrigger SelectValue SelectItem SelectContent]]
   [form-tricorder.utils :as utils :refer [unite]]))

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
      ($ radix-icons/MixerHorizontalIcon))))

(defnc FuncOptsGroup
  [{:keys [dir children]}]
  (d/div
    {:class (unite
             (css :gap-2 {:display "flex"
                          :align-items "start"})
             (case dir
               :row (css {:flex-direction "row"})
               :column (css {:flex-direction "column"})
               (throw (ex-info "invalid flex direction" {}))))}
    children))

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
     {:font-family "var(--font-sans)"
      :position "sticky"
      :background-color "var(--col-bg)"
      :top "0"
      :font-weight "var(--weight-medium)"
      :border-top "1px solid var(--col-fg)"
      :border-bottom "1px solid var(--col-fg)"}]
    ["tr:hover td"
     {:background-color "var(--col-n3)"}]
    ["th, td"
     {:padding "0.1rem 0.4rem"}]
    ["th:first-child, td:first-child"
     {:padding "0.1rem 0.6rem 0.1rem 0"
      :color "var(--col-fg-muted)"}]
    ["th:last-child, td:last-child"
     {:padding "0.1rem 0 0.1rem 0.6rem"
      :text-align "right"}]
    [".result-arrow"
     {:color "var(--col-fg-muted)"}]
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
          ($ FuncOptsGroup
             {:dir :column}
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
                  {:varorder varorder}))))
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
  [{:keys [vis-id vis-data negative? bg-color padding scale
           default-caption? custom-caption-input]} ref]
  {:wrap [(react/forwardRef)]}
  (let [{:keys [psps? data varorder]} vis-data]
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
           "fig-bg-color" (->attr bg-color)}))))

(defnc F-Vmap--init
  []
  (let [[psps? set-psps?] (hooks/use-state false)
        varorder (rf/subscribe [:input/varorder])
        vmap (when-not psps? (rf/subscribe [:input/->filtered-vmap]))
        vmap-psps (when psps? (rf/subscribe [:input/->filtered-vmap-psps]))]
    ($ Function
       ($ FuncOpts
          ($ FuncOptsGroup
             {:dir :column}
             ($ FuncOptsGroup
                {:dir :row}
                ($ Toggle {:variant :outline
                           :disabled (< (count varorder) 2)
                           :on-click (fn [_] (set-psps? (fn [b] (not b))))}
                   ($ (if psps?
                        PerspectivesCollapseIcon PerspectivesExpandIcon))
                   (d/span {:class (css :ml-1)}
                           "Perspectives"))
                ($ ImageExportVmap
                   {:vis-id (if psps? "ff-vmap-psps" "ff-vmap")
                    :vis-data {:data (if psps? vmap-psps vmap)
                               :varorder varorder
                               :psps? psps?}
                    :get-svg-el #(.. % -shadowRoot
                                     (getElementById (if psps?
                                                       "psps-figure"
                                                       "vmap-figure")))
                    :preview-cmp F-Vmap-preview}))
             (when varorder
               ($ ValueFilter
                  {:varorder varorder}))))
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
          ($ FuncOptsGroup
             {:dir :column}
             ($ FuncOptsGroup
                {:dir :row}
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
                      ($ radix-icons/CopyIcon))))
             (when varorder
               ($ ValueFilter
                  {:varorder varorder}))))
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

(defnc F-Graph-preview
  [{:keys [vis-id vis-data negative? bg-color padding scale
           default-caption? custom-caption-input]} ref]
  {:wrap [(react/forwardRef)]}
  (let [{:keys [expr-json graph-type graph-style theme compact-reentry?]}
        vis-data]
    (hooks/use-effect
     [expr-json]
     (let [webc-el @ref]
       (aset webc-el "json" expr-json)))
    ($ ExportPreview
       {:class (if negative?
                 (css {:color-scheme "dark"})
                 (css {:color-scheme "light"}))}
       ($ vis-id
          {:ref ref
           :type (->attr graph-type)
           "compact-reentry" compact-reentry?
           :styleclass (->attr (name graph-style))
           :theme (->attr (if negative? "dark" "light"))
           "fig-scale" scale
           "fig-padding" (->attr padding)
           "fig-bg-color" (->attr bg-color)
           ;; "full-svg" (->attr true)
           ;; "no-caption" (->attr (not default-caption?))
           ;; :label (->attr custom-caption-input)
           ;; :styles (->attr vmap-export-css)
           ;; "caption-attrs"
           ;; (->attr {:font-family "ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, \"Liberation Mono\", \"Courier New\", monospace"
           ;;          :font-size "12px"
           ;;          :fill (if negative? "#ffffff" "#000000")})
           }))))

(defnc F-Graph--init
  [{:keys [graph-type]}]
  (let [expr-json  (rf/subscribe [:input/->expr-json])
        appearance (rf/subscribe [:theme/appearance])
        system-color-scheme (rf/subscribe [:theme/system-color-scheme])
        graph-style (rf/subscribe [:modes/graph-style])
        [compact-reentry? set-compact-reentry?] (hooks/use-state false)
        theme (if (= :system appearance)
                system-color-scheme
                (name appearance))]
    ($ Function
       ($ FuncOpts
          ($ FuncOptsGroup
             {:dir :row}
             (case graph-type
               "pack"
               ($ FuncOptsGroup
                  {:dir :column}
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
                          "Gestalt")))))
               "gsbhooks"
               ($ FuncOptsGroup
                  {:dir :column}
                  ($ Toggle {:variant :outline
                             :on-click (fn [_] (set-compact-reentry? #(not %)))}
                     ;; ($ (if compact-reentry?
                     ;;      PerspectivesCollapseIcon PerspectivesExpandIcon))
                     ;; (d/span {:class (css :ml-1)})
                     "Compact Re-Entries"))
               nil)
             ($ ImageExportGraph
                {:vis-id "ff-fgraph"
                 :vis-data {:expr-json expr-json
                            :graph-type graph-type
                            :graph-style graph-style
                            :compact-reentry? compact-reentry?
                            :theme theme}
                 :get-svg-el #(.. % -shadowRoot
                                  (getElementById "chart"))
                 :preview-cmp F-Graph-preview})))
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

;; first (error-prone) attempt, complects web component state with React:
#_
(defnc F-Automaton
  [{:keys [ca-spec cell-size buffer-size run?]} :as props]
  (let [webc-ref (hooks/use-ref nil)
        ;; ca-ref (hooks/use-ref (emul/create-ca ca-spec 0))
        ;; run-ref (hooks/use-ref run?)
        ]
    (hooks/use-effect
     :once
     (let [webc-el @webc-ref
           ca (emul/create-ca ca-spec 0)
           _ (make-state! webc-el)]
       ;; (aset webc-el "ca" @ca-ref)
       ;; (aset webc-el "run?" @run-ref)
       (aset webc-el "ca" ca)
       (aset webc-el "run?" run?)
       (fn []
         ;; (reset! ref nil)
         ;; nil
         #_
         (when webc-el
           (aset webc-el "ca" nil)
           ;; (.disconnect webc-el)
           ;; (when (.-parentNode webc-el)
           ;;   (.removeChild (.-parentNode webc-el) webc-el))
           ;; (reset! ref nil)
           ))))
    ($ :ff-automaton {:ref webc-ref
                      "cell-size" (->attr cell-size)
                      "buffer-size" (->attr buffer-size)})))

;; second attempt, isolates web component state:
#_
(defnc F-Automaton
  [{:keys [ca-spec cell-size buffer-size run?]}]
  (let [container-ref (hooks/use-ref nil)]
    (hooks/use-effect
      :once
      (let [container @container-ref
            webc-el (js/document.createElement "ff-automaton")
            ca (emul/create-ca ca-spec 0)
            _ (make-state! webc-el)]
        (.setAttribute webc-el "cell-size" (->attr cell-size))
        (.setAttribute webc-el "buffer-size" (->attr buffer-size))
        (aset webc-el "ca" ca)
        (aset webc-el "run?" run?)
        (.appendChild container webc-el)

        (fn []
          (println "Cleanup React")
          (when-let [parent (.-parentNode webc-el)]
            (.removeChild parent webc-el)))))
    (d/div {:ref container-ref})))

;; third (unrefined) attempt, allows web component to react on prop change:
(defnc F-Automaton
  [props]
  (let [container-ref (hooks/use-ref nil)
        webc-ref (hooks/use-ref nil)]
    (hooks/use-effect
      [props]
      (when (and (not @webc-ref) @container-ref)
        ;; (println "F-Automaton effect! " (:ca-spec props))
        ;; imperatively create web component and set initial props
        (let [{:keys [ca-spec res cell-size buffer-size run?]} props
              webc-el (js/document.createElement "ff-automaton")
              ca (emul/create-ca ca-spec res 0)
              _ (make-state! webc-el)]
          (.setAttribute webc-el "cell-size" (->attr cell-size))
          (.setAttribute webc-el "buffer-size" (->attr buffer-size))
          (aset webc-el "ca" ca)
          (aset webc-el "run?" run?)
          ;; store reference to web component and append it to container
          (reset! webc-ref webc-el)
          (.appendChild @container-ref webc-el)))

      ;; update properties when props change
      (when @webc-ref
        ;; TODO
        nil)

      ;; cleanup (is this necessary? seems to prevent detached components)
      #_
      (fn []
        ;; (println "Cleanup React")
        (let [webc-el @webc-ref]
          (reset! webc-ref nil)
          (when-let [parent (.-parentNode webc-el)]
            (.removeChild parent webc-el)))))
    (d/div {:ref container-ref})))

(defn ini->str
  [ini]
  (str ini))

(defnc IniSel
  [{:keys [current-ini set-ini]}]
  (let [inis-available [:random :ball]]
    ($d Select
      {:id "automaton-ini-select"
       :value current-ini
       :onValueChange (fn [v] (set-ini v))}
      ($ SelectTrigger
         {:style {:width "10rem"}}
         ($d SelectValue
           (ini->str current-ini)))
      ($ SelectContent
         {:class "inner"}
         (for [ini inis-available
               :let [label (ini->str ini)]]
           ($ SelectItem
              {:key (str ini)
               :value ini}
              label))))))

(defnc F-Selfi--init
  [_]
  (let [[ini set-ini] (hooks/use-state :ball)
        ini-spec (case ini
                   :ball [:figure :n (emul/ini-patterns :ball) :center]
                   :random [:random])
        ca-spec (rf/subscribe [:input/->ca-selfi ini-spec])
        [reset-key set-reset-key] (hooks/use-state 0)
        ;; prevents unmounting effect on initial render
        initial-render? (hooks/use-ref true)]
    (println "SELFI " reset-key)
    (hooks/use-effect
     [ca-spec]
     ;; when CA changes, the automaton component must be remounted,
     ;; so we change its `key` (identity) to trick React into unmounting
     ;; the “old” and mounting the “new” component
     (if @initial-render?
       (reset! initial-render? false)
       (do
         ;; (println "RESET KEY Selfi")
         (set-reset-key inc))))
    ($ Function
       (when ca-spec
         ($ FuncOpts
            ($ IniSel {:current-ini ini
                       :set-ini set-ini})))
       (when ca-spec
         ($ F-Automaton {:key reset-key
                         :ca-spec ca-spec
                         :run? true
                         :res [100]
                         :cell-size 4
                         :buffer-size 200})))))

(defmethod gen-component :selfi
  [_ args]
  ($ F-Selfi--init {& args}))


(defnc F-Mindform--init
  [_]
  (let [ca-spec (rf/subscribe [:input/->ca-mindform
                               [:random]
                               #_
                               [:figure :n (emul/ini-patterns :ball) :center]
                               ])
        [reset-key set-reset-key] (hooks/use-state 0)
        initial-render? (hooks/use-ref true)]
    (println "MINDFORM " reset-key)
    (hooks/use-effect
     [ca-spec]
     (if @initial-render?
       (reset! initial-render? false)
       (do
         ;; (println "RESET KEY Mindform")
         (set-reset-key inc))))
    ($ Function
       (when ca-spec
         ($ F-Automaton {:key reset-key
                         :ca-spec ca-spec
                         :run? true
                         :res [151 151]
                         :cell-size 4
                         :buffer-size 1})))))

(defmethod gen-component :mindform
  [_ args]
  ($ F-Mindform--init {& args}))


(defnc F-Lifeform--init
  [_]
  (let [ca-spec (rf/subscribe [:input/->ca-lifeform])
        [reset-key set-reset-key] (hooks/use-state 0)
        initial-render? (hooks/use-ref true)]
    (println "LIFEFORM " reset-key)
    (hooks/use-effect
     [ca-spec]
     (if @initial-render?
       (reset! initial-render? false)
       (do
         ;; (println "RESET KEY")
         (set-reset-key inc))))
    ($ Function
       (when ca-spec
         ($ F-Automaton {:key reset-key
                         :ca-spec ca-spec
                         :run? true
                         :res [151 151]
                         :cell-size 4
                         :buffer-size 1})))))

(defmethod gen-component :lifeform
  [_ args]
  ($ F-Lifeform--init {& args}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SelFi CA

#_#_#_
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
