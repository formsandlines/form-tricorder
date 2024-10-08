(ns form-tricorder.components.app-toolbar
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.stitches-config :refer [styled css]]
   [form-tricorder.icons :refer [SunIcon MoonIcon SwapIcon
                                 ViewVerticalIcon
                                 ViewHorizontalIcon
                                 SourceIcon]]
   ["@radix-ui/react-toolbar" :as Toolbar]
   #_["@radix-ui/react-icons" :refer []]))

;; Shared styles

(def itemStyles
  {:flex "0 0 auto"})

(def linkStyles
  {})

(def buttonStyles
  {:outline "none"
   :cursor "pointer"
   :border "none"
   :padding "$1" ; "0.2rem"
   :border-radius "$1"
   :background "none"
   :color "$colors$outer_fg"

   "&:focus"
   {:outline "solid"}
   "&:focus-visible"
   {:outline "none"}
   "&:hover"
   {:background-color "$colors$inner_bg"
    :color "$colors$outer_m200"}
   "&:disabled"
   {:cursor "not-allowed"
    :background "none"}})


;; Styled Components

(def Root
  (styled (.-Root Toolbar)
          {:display "flex"
           :min-width "max-content"
           :height "100%"
           :column-gap "$1" ; "4px"
           :align-items "stretch"
           :color "$colors$outer_fg"

           "& .icon"
           {:width "$icon-toolbar" ; "1.2rem" ; "auto"
            :height "100%"
            :fill "$colors$outer_m200"}
           "& *:hover > .icon"
           {:fill "$colors$outer_m200"}
           "& *:disabled > .icon"
           {:fill "$colors$outer_n200"}}))

(def Button
  (styled (.-Button Toolbar)
          (merge itemStyles
                 buttonStyles
                 {})))

(def Separator
  (styled (.-Separator Toolbar)
          (merge itemStyles
                 {:width "$px"
                  :background-color "$colors$outer_n200"
                  :margin "0 $1"})))

(def ToggleGroup
  (styled (.-ToggleGroup Toolbar)
          (merge itemStyles
                 {:display "flex"
                  :min-width "max-content"
                  :column-gap "$1" ; "4px"
                  })))

(def ToggleItem
  (styled (.-ToggleItem Toolbar)
          (merge itemStyles
                 buttonStyles
                 {})))

; (def Link
;   (styled (.-Link Toolbar)
;           (merge itemStyles
;                  linkStyles)))

(def SourceLink
  (styled (.-Link Toolbar)
          (merge itemStyles
                 {:display "block"
                  :padding "$1 0"})))

;; Components

(defnc AppToolbar
  []
  (let [view-split? (> (rf/subscribe [:frame/windows]) 1)
        appearance (rf/subscribe [:theme/appearance])
        frame-orientation (rf/subscribe [:frame/orientation])
        handle-frame-orientation
        #(rf/dispatch [:frame/set-orientation {:next-orientation %}])
        handle-swap
        #(rf/dispatch [:views/swap])
        handle-toggle-appearance
        #(rf/dispatch [:theme/set-appearance {:next-appearance %}])]
    ($d Root {:orientation "horizontal"
              :aria-label "App toolbar"}
        ($d ToggleGroup
          {:type "single"
           :value (name frame-orientation)
           :on-value-change #(handle-frame-orientation (keyword %))}
          ($d ToggleItem
            {:value "cols"
             :disabled (= frame-orientation :cols)}
            ($ ViewVerticalIcon))
          ($d ToggleItem
            {:value "rows"
             :disabled (= frame-orientation :rows)}
            ($ ViewHorizontalIcon)))
        ($d Button
          {:disabled (not view-split?)
           :on-click (fn [_] (handle-swap))}
          ($ SwapIcon))
        ($d Separator)
        ($d ToggleGroup
          {:type "single"
           :value (name appearance)
           :on-value-change #(handle-toggle-appearance (keyword %))}
          ($d ToggleItem
            {:value "light"
             :disabled (= appearance :light)}
            ($ SunIcon))
          ($d ToggleItem
            {:value "dark"
             :disabled (= appearance :dark)}
            ($ MoonIcon)))
        ($d Separator)
        ($d Button {:on-click (fn [_] (js/console.log "Clicked about"))}
            "about")
        ($d Button {:on-click (fn [_] (js/console.log "Clicked help"))}
            "help")
        ($d Separator)
        ($d SourceLink
          {:href "https://github.com/formsandlines/form-tricorder"
           :target "_blank"}
          ($ SourceIcon)))))

