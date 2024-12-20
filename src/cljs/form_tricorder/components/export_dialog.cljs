(ns form-tricorder.components.export-dialog
  {:shadow.css/include ["form_tricorder/keyframes.css"]}
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ["react" :as react]
   [shadow.css :refer (css)]
   ;; [garden.color :as gc]
   [form-tricorder.utils :refer [let+]]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.stitches-config :as st]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   ["@radix-ui/react-dialog" :as Dialog]
   [form-tricorder.utils :as utils]))

(def Root (.-Root Dialog))
(def Trigger (.-Trigger Dialog))
(def Portal (.-Portal Dialog))
(def Overlay (.-Overlay Dialog))
(def Content (.-Content Dialog))
(def Title (.-Title Dialog))
(def Description (.-Description Dialog))
(def Close (.-Close Dialog))


(defnc ExportPreview
  [{:keys [vis-id vis-props]} ref]
  {:wrap [(react/forwardRef)]}
  (let [dna (vis-props :dna)]
    (hooks/use-effect
      []
      (let [webc-el @ref]
        (aset webc-el "dna" dna)))
    (d/div
      {:class
       (css "ExportPreview"
            :px-2
            :py-8
            :rounded-sm
            :checkerboard
            {:display "flex"
             :justify-content "center"
             :align-items "center"})}
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
               {:variant :outline
                :size :sm}
               "Export"))
        ($d Portal
            ($d Overlay
                {:class (css "inner" :overlay-bg)})
            ($d Content
                {:class
                 (css "outer"
                      :bg :fg :p-8-5 :rounded-md
                      :overlay-content
                      {:width "90vw"})}
                ($d Title
                    {:class
                     (css :text-lg :mb-4)}
                    "Export figure…")
                ;; ($d Description
                ;;   "Set some options.")
                ($ ExportPreview
                   {:vis-id vis-id
                    :vis-props vis-props
                    :ref export-ref})
                (d/div
                 {:class
                  (css "ModalOptions"
                       :mt-6
                       {:display "flex"}
                       ["& > *"
                        :py-0 :px-4 :border-l :border-col
                        {:flex "1"}]
                       ["& > *:first-child"
                        :pl-0
                        {:border-left "none"}]
                       ["& > *:last-child"
                        :pr-0]
                       ["& > * h3"
                        :font-size-sm :border-col :mb-4]
                       ["& .FileFormat"
                        :gap-10
                        {:display "flex"}]
                       ["& .FileFormat > *"
                        :gap-3
                        {:display "flex"
                         :align-items "center"}])}
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
                   {:class (css :mt-4)}
                   (d/h3
                    "Dimensions:")
                   (d/div
                    )))
                 (d/div
                  (d/h3 "Appearance:"))
                 (d/div
                  (d/h3 "Caption:")))
                (d/div
                 {:class
                  (css "ModalActions"
                       :gap-4 :mt-4
                       {:display "flex"
                        :justify-content "end"})}
                 ($d Close
                     {:as-child true}
                     ($ Button
                        {:variant :outline}
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
