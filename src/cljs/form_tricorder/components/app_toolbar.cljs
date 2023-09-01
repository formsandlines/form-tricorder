(ns form-tricorder.components.app-toolbar
  (:require
    [refx.alpha :as refx]
    [helix.core :refer [defnc fnc $ <> provider]]
    [helix.hooks :as hooks]
    [helix.dom :as d :refer [$d]]
    [form-tricorder.utils :refer [style> css>]]
    [form-tricorder.icons :refer [SunIcon MoonIcon SwapIcon
                                  ViewVerticalIcon
                                  ViewHorizontalIcon
                                  SourceIcon]]
    ["@radix-ui/react-radio-group" :as RadioGroup]
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
   :padding "0.2rem"
   :border-radius "2px"
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
  (style> (.-Root Toolbar)
          {:display "flex"
           :min-width "max-content"
           :height "100%"
           :column-gap "4px"
           :align-items "stretch"
           :color "$colors$outer_fg"

           "& .icon"
           {:width "1.2rem" ; "auto"
            :height "100%"
            :fill "$colors$outer_m200"}
           "& *:hover > .icon"
           {:fill "$colors$outer_m200"}
           "& *:disabled > .icon"
           {:fill "$colors$outer_n200"}}))

(def Button
  (style> (.-Button Toolbar)
          (merge itemStyles
                 buttonStyles
                 {})))

(def Button-splitview
  (style> (.-Button Toolbar)
          (merge itemStyles
                 buttonStyles
                 {})))

(def Separator
  (style> (.-Separator Toolbar)
          (merge itemStyles
                 {:width "1px"
                  :background-color "$colors$outer_n200"
                  :margin "0 4px"})))

(def ToggleGroup
  (style> (.-ToggleGroup Toolbar)
          (merge itemStyles
                 {:display "flex"
                  :min-width "max-content"
                  :column-gap "4px"})))

(def ToggleItem
  (style> (.-ToggleItem Toolbar)
          (merge itemStyles
                 buttonStyles
                 {})))

; (def Link
;   (style> (.-Link Toolbar)
;           (merge itemStyles
;                  linkStyles)))

(def SourceLink
  (style> (.-Link Toolbar)
          (merge itemStyles
                 {:display "block"
                  :padding "0.2rem 0"})))

;; Components

(defnc AppToolbar
  [{:keys [view-split?]}]
  (let [appearance (refx/use-sub [:appearance])
        handle-split-orientation
        #(refx/dispatch [:views/set-split-orientation {:next-orientation %}])
        handle-swap
        #(refx/dispatch [:views/swap])
        handle-toggle-appearance
        #(refx/dispatch [:theme/set-appearance {:next-appearance %}])]
    ($d Root {:orientation "horizontal"
              :aria-label "App toolbar"}
        ($d Button-splitview
            {:disabled (not view-split?)
             :on-click (fn [_] (handle-split-orientation :cols))}
            ($ ViewVerticalIcon))
        ($d Button-splitview
            {:disabled (not view-split?)
             :on-click (fn [_] (handle-split-orientation :rows))}
            ($ ViewHorizontalIcon))
        ($d Button-splitview
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

