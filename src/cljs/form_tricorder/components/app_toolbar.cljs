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

;; Styled Components

(def Root
  (styled (.-Root Toolbar)
          {:display "flex"
           :min-width "max-content"
           :height "100%"
           :column-gap "$1" ; "4px"
           :align-items "stretch"
           :font-size "$sm"
           :color "$outer-fg"

           "& .icon"
           {:width "$icon-toolbar" ; "1.2rem" ; "auto"
            :height "100%"
            :fill "$m21"
            }
           ;; "& *:hover > .icon"
           ;; {:fill "$m800"}
           "& *:disabled"
           {:pointer-events "none"
            :cursor "not-allowed"}
           "& *:disabled > .icon"
           {:fill "$outer-muted" ;; $n300
            }}))

(def buttonStyles
  {:touch-action "manipulation"

   :display "inline-flex"
   :justify-content "center"
   :align-items "center"
   :white-space "nowrap"
   :border-radius "$sm"
   :_transition_colors []

   :outline "none"
   :cursor "pointer"
   :border "none"
   :padding "$1" ; "0.2rem"
   :background "none"
   :color "$m27"

   "&:hover"
   {:background-color "$outer-accent" ;; inner-bg
    :color "$outer-accent-fg" ;; outer_m200
    }})

(def itemStyles
  {:flex "0 0 auto"

   ;; "&:focus"
   ;; {:outline "solid"}
   ;; "&:focus-visible"
   ;; {:outline "none"}

   "&:focus-visible"
   {:_outlineNone []
    :_ring [2 "$colors$ring" 1 "$colors$outer-bg"]}})

(def Button
  (styled (.-Button Toolbar)
          (merge itemStyles
                 buttonStyles
                 {})))

(def TextButton
  (styled (.-Button Toolbar)
          (merge itemStyles
                 buttonStyles
                 {:padding "$1 $1-5"

                  "&:disabled"
                  {:opacity "0.5"}})))

(def Separator
  (styled (.-Separator Toolbar)
          (merge itemStyles
                 {:width "$px"
                  :background-color "$n8"
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
                 buttonStyles
                 ;; {:display "block"
                 ;;  :padding "$1 0"}
                 )))

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
        ($d TextButton {:on-click (fn [_] (js/console.log "Clicked about"))}
            "about")
        ($d TextButton {:on-click (fn [_] (js/console.log "Clicked help"))}
            "help")
        ($d Separator)
        ($d SourceLink
          {:href "https://github.com/formsandlines/form-tricorder"
           :target "_blank"}
          ($ SourceIcon)))))

