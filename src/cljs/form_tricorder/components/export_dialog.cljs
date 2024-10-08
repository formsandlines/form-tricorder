(ns form-tricorder.components.export-dialog
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ;; ["react" :as react]
   ;; [garden.color :as gc]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.stitches-config :refer [styled css keyframes]]
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
           "color-mix(in srgb, $colors$inner_fg 60%, transparent)"
           ;; :backdrop-filter "blur(3px)"
           :position "fixed"
           :inset 0
           :animation (str overlayShow
                           " 150ms cubic-bezier(0.16, 1, 0.3, 1)")}))

(def Content
  (styled (.-Content Dialog)
          (let [shadow "0 3px 5px 0px color-mix(in srgb, $colors$outer_fg 30%, transparent)"]
            {:background-color "$outer_bg"
             :position "fixed"
             :top "50%"
             :left "50%"
             :width "90vw"
             :transform "translate(-50%, -50%)"
             :padding "25px"
             :animation (str contentShow
                             " 150ms cubic-bezier(0.16, 1, 0.3, 1)")
             :border-radius "$radii$3"
             :box-shadow shadow
             :color "$colors$outer_fg"
             })))

(def Title
  (styled (.-Title Dialog)
          {}))

(def Description
  (styled (.-Description Dialog)
          {}))

(def Close
  (styled (.-Close Dialog)
          {}))

(def button-base-styles
  {:all "unset"
   :cursor "pointer"
   :touch-action "manipulation"
   :position "relative"
   :display "inline-flex"
   :align-items "center"
   :justify-content "center"
   :white-space "nowrap"

   "&:focus"
   {:outline "solid"}
   "&:focus-visible"
   {:outline "none"}
   "&:hover"
   {:cursor "pointer"}
   "&:disabled"
   {:cursor "not-allowed"
    :background "none"}})

(def button-modal-styles
  {:padding "8px 16px"
   :border "1.5px solid $colors$outer_m200"
   :border-radius "$3"
   :font-size "$2"

   :variants
   {:variant
    {:secondary
     {:background "none"
      :color "$outer_fg"
      "&:hover"
      {
       :border-color "color-mix(in srgb, $outer_m200, black 10%)"
       :background-color "color-mix(in srgb, white 20%, transparent)"}}

     :primary
     {:font-weight "$medium"
      :background-color "$outer_m200"
      :color "$outer_bg"
      "&:hover"
      {:color "white"
       :border-color "white"
       :background-color "color-mix(in srgb, $outer_m200, black 10%)"}}}}})

(def ModalButton
  (styled "button"
          (css (utils/merge-deep
                button-base-styles
                button-modal-styles))))

(def modal-actions-styles
  (css {:display "flex"
        :gap "$4"
        :justify-content "end"
        :margin-top "$4"
        }))

(defnc ExportDialog
  [{:keys []}]
  ($d Root
    ;; {:default-open true}
    ($d Trigger
      {:as-child true}
      (d/button
        "Export"))
    ($d Portal
      ($d Overlay
        {})
      ($d Content
        {}
        ($d Title
          "Export figure…")
        ($d Description
          "Set some options.")
        (d/div
          {:class (str "ModalActions " (modal-actions-styles))}
          ($d Close
            {:as-child true}
            ($d ModalButton
              {:variant "secondary"}
              "Cancel"))
          ($d Close
            {:as-child true}
            ($d ModalButton
              {:variant "primary"
               :on-click (fn [e]
                           (js/console.log "clicked download"))}
              "Download")))))))
