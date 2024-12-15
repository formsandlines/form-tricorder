(ns form-tricorder.components.export-dialog
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ["react" :as react]
   ;; [garden.color :as gc]
   [form-tricorder.utils :refer [let+]]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.stitches-config :refer [styled css keyframes]]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   ["@radix-ui/react-dialog" :as Dialog]
   [form-tricorder.utils :as utils]))

(def Root
  (styled (.-Root Dialog)
          {}))

(def Trigger
  (styled (.-Trigger Dialog)
          {}))

(def Portal
  (styled (.-Portal Dialog)
          {}))

(def overlayShow
  (keyframes
   {:0%   {:opacity 0}
    :100% {:opacity 1}}))

(def contentShow
  (keyframes
   {:0%   {:opacity 0
           :transform "translate(-50%, -48%) scale(.96)"}
    :100% {:opacity 1
           :transform "translate(-50%, -50%) scale(1)"}}))

(def Overlay
  (styled (.-Overlay Dialog)
          {
           :background-color
           "color-mix(in srgb, $colors$inner-fg 60%, transparent)"
           ;; :backdrop-filter "blur(3px)"
           :position "fixed"
           :z-index "98"
           :inset 0
           :animation (str overlayShow
                           " 150ms cubic-bezier(0.16, 1, 0.3, 1)")}))

(def Content
  (styled (.-Content Dialog)
          (let [shadow "0 3px 5px 0px color-mix(in srgb, $colors$outer-fg 30%, transparent)"]
            {:background-color "$outer-bg"
             :position "fixed"
             :z-index "99"
             :top "50%"
             :left "50%"
             :width "90vw"
             :transform "translate(-50%, -50%)"
             :padding "25px"
             :animation (str contentShow
                             " 150ms cubic-bezier(0.16, 1, 0.3, 1)")
             :border-radius "$3"
             :box-shadow shadow
             :color "$outer-fg"})))

(def Title
  (styled (.-Title Dialog)
          {:font-size "$lg"
           :margin-bottom "$4"}))

(def Description
  (styled (.-Description Dialog)
          {}))

(def Close
  (styled (.-Close Dialog)
          {}))


(def modal-actions-styles
  (css {:display "flex"
        :gap "$4"
        :justify-content "end"
        :margin-top "$4"
        }))

(def export-preview-styles
  (css (let [opc1 "26%"
             opc2 "75%"
             col "$n12"
             size 60]
         {:display "flex"
          :justify-content "center"
          :align-items "center"
          :padding "$8 $2"
          :background-color "$n14"
          :border-radius "$2"
          ;; Checkerboard pattern to mock transparency
          ;; Source: https://gist.github.com/dfrankland/f6fed3e3ccc42e3de482b324126f9542?permalink_comment_id=5160713#gistcomment-5160713
          :background-image
          (str "linear-gradient(45deg, " col " " opc1 ", transparent " opc1 "),"
               "linear-gradient(135deg, " col " " opc1 ", transparent " opc1 "),"
               "linear-gradient(45deg, transparent " opc2 ", " col " " opc2 "),"
               "linear-gradient(135deg, transparent " opc2 ", " col " " opc2 ")")
          :background-size (str size "px " size "px")
          :background-position (str "0 0, "
                                    (/ size 2) "px 0, "
                                    (/ size 2) "px -" (/ size 2) "px, "
                                    "0px " (/ size 2) "px")})))

(def modal-options-styles
  (css {:display "flex"
        :margin-top "$6"
        "& > *"
        {:flex 1
         :border-left "$borderWidths$1 solid $outer-border"
         :padding "0 $4"
         "&:first-child"
         {:border-left "none"
          :padding-left 0}
         "&:last-child"
         {:padding-right 0}
         "h3"
         {:font-size "$sm"
          :color "$outer-border-fg"
          :margin-bottom "$4"}}
        ".FileFormat"
        {:display "flex"
         :gap "$10"
         "& > *"
         {:display "flex"
          :align-items "center"
          :gap "$3"}}}))

(defnc ExportPreview
  [{:keys [vis-id vis-props]} ref]
  {:wrap [(react/forwardRef)]}
  (let [dna (vis-props :dna)]
    (hooks/use-effect
      []
      (let [webc-el @ref]
        (aset webc-el "dna" dna)))
    (d/div
      {:class (str "ExportPreview " (export-preview-styles))}
      ($ vis-id
         {:ref ref
          & vis-props}))))

(defnc ExportDialog
  [{:keys [save-svg save-img get-svg-el vis-id vis-props]}]
  (let [export-ref (hooks/use-ref nil)
        [format set-format] (hooks/use-state "svg")]
    ($d Root
      {:default-open true}
      ($d Trigger
        {:as-child true}
        ($ Button
           {:variant "outline"
            :layer "inner"
            :size "sm"}
           "Export"))
      ($d Portal
        ($d Overlay
          {})
        ($d Content
          {}
          ($d Title
            "Export figure…")
          ;; ($d Description
          ;;   "Set some options.")
          ($ ExportPreview
             {:vis-id vis-id
              :vis-props vis-props
              :ref export-ref})
          (d/div
            {:class (str "ModalOptions " (modal-options-styles))}
            (d/div
              (d/h3 "File format:")
              ($ RadioGroup
                 {:class "FileFormat"
                  ;; :defaultValue "svg"
                  :value format
                  :onValueChange set-format}
                 (d/div
                   ($ RadioGroupItem
                      {:id "png"
                       :layer "outer"
                       :value "png"})
                   ($ Label
                      {:htmlFor "png"}
                      "PNG"))
                 (d/div
                   ($ RadioGroupItem
                      {:id "svg"
                       :layer "outer"
                       :value "svg"})
                   ($ Label
                      {:htmlFor "svg"}
                      "SVG")))
              (d/div
                (d/h3
                  "Dimensions:")
                (d/div
                  )))
            (d/div
              (d/h3 "Appearance:"))
            (d/div
              (d/h3 "Caption:")))
          (d/div
            {:class (str "ModalActions " (modal-actions-styles))}
            ($d Close
              {:as-child true}
              ($ Button
                 {:variant "outline"}
                 "Cancel"))
            ($d Close
              {:as-child true
               :on-click
               (fn [e]
                 (let [el (.. export-ref -current)
                       make-filename (fn [ext] (str (utils/get-timestamp) "_"
                                                   vis-id ext))]
                   (js/setTimeout
                    (fn []
                      (let [svg-el (get-svg-el el)]
                        (case format
                          "png" (save-img svg-el (make-filename ".png") {})
                          "svg" (save-svg svg-el (make-filename ".svg") {}))))
                    1000)))}
              ($ Button
                 "Download"))))))))
