(ns form-tricorder.components.image-export
  (:require
   ;; [clojure.set :as set]
   ;; [clojure.string :as string]
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [formform-vis.utils-dom :refer [save-svg save-img]]
   [form-tricorder.components.export-dialog
    :refer [ExportDialog ExportOptions ExportGroup ExportItem]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.input :refer [Input]]
   [form-tricorder.components.common.checkbox :refer [Checkbox]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   [form-tricorder.components.common.select
    :refer [Select SelectTrigger SelectValue SelectItem SelectContent]]
   [form-tricorder.utils :as utils]))


;; Common export modules:

(defnc ExportSetFileFormat
  [{[format set-format] :format}]
  ($ ExportItem
     {:title "File format:"}
     ($ RadioGroup
        {:class (css "FileFormat"
                     :gap-3
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
            "SVG")))))

(defnc ExportSetScale
  [{[scale set-scale] :scale}]
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
           {:id "scale-select"
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

(defnc ExportSetAppearance
  [{[negative? set-negative?] :negative?
    [background? set-background?] :background?
    [bg-color set-bg-color] :bg-color
    [padding set-padding] :padding}]
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
                    (js/console.error e))))})))))

(defn make-export-handler
  [export-ref vis-id get-svg-el format]
  (fn [_]
    (let [el (.. export-ref -current)
          make-filename (fn [ext] (str (utils/get-timestamp) "_"
                                      vis-id ext))]
      ;; timeout will get cleared in `ExportDialog` component
      (js/setTimeout
       (fn []
         (let [svg-el (get-svg-el el)]
           (case format
             "png" (save-img svg-el (make-filename ".png") {})
             "svg" (save-svg svg-el (make-filename ".svg") {}))))
       1000))))


;; Specialized export components:

(defnc ImageExportVmap
  [{:keys [vis-id vis-data preview-cmp get-svg-el]}]
  (let [export-ref (hooks/use-ref nil)
        [format set-format] (hooks/use-state "svg")
        [scale set-scale] (hooks/use-state 1.0)
        [negative? set-negative?] (hooks/use-state false)
        [background? set-background?] (hooks/use-state false)
        [padding set-padding] (hooks/use-state 4)
        [bg-color set-bg-color] (hooks/use-state "#ffffff")
        [default-caption? set-default-caption?] (hooks/use-state true)
        [custom-caption? set-custom-caption?] (hooks/use-state false)
        [custom-caption-input set-custom-caption-input] (hooks/use-state "")]
    ($ ExportDialog
       {:title "Export graph…"
        :on-export (make-export-handler
                    export-ref vis-id get-svg-el format)}
       ($ preview-cmp
          {:ref export-ref
           :vis-id vis-id
           :vis-data vis-data
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
                ($ ExportSetFileFormat
                   {:format [format set-format]})
                ($ ExportSetScale
                   {:scale [scale set-scale]}))
             ($ ExportSetAppearance
                {:negative? [negative? set-negative?]
                 :background? [background? set-background?]
                 :bg-color [bg-color set-bg-color]
                 :padding [padding set-padding]})
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

(defnc ImageExportGraph
  [{:keys [vis-id vis-data preview-cmp get-svg-el]}]
  (let [export-ref (hooks/use-ref nil)
        [format set-format] (hooks/use-state "svg")
        [scale set-scale] (hooks/use-state 1.0)
        [negative? set-negative?] (hooks/use-state false)
        [background? set-background?] (hooks/use-state false)
        [padding set-padding] (hooks/use-state 4)
        [bg-color set-bg-color] (hooks/use-state "#ffffff")]
    ($ ExportDialog
       {:title "Export graph…"
        :on-export (make-export-handler
                    export-ref vis-id get-svg-el format)}
       ($ preview-cmp
          {:ref export-ref
           :vis-id vis-id
           :vis-data vis-data
           :scale scale
           :negative? negative?
           :bg-color (when background? bg-color)
           :padding padding})
       ($ ExportOptions
          ($ ExportGroup
             {:orientation :horizontal}
             ($ ExportGroup
                {:orientation :vertical}
                ($ ExportSetFileFormat
                   {:format [format set-format]})
                ($ ExportSetScale
                   {:scale [scale set-scale]}))
             ($ ExportSetAppearance
                {:negative? [negative? set-negative?]
                 :background? [background? set-background?]
                 :bg-color [bg-color set-bg-color]
                 :padding [padding set-padding]}))))))

