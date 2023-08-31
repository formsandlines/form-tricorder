(ns form-tricorder.components.app-toolbar
  (:require
    [refx.alpha :as refx]
    [helix.core :refer [defnc fnc $ <> provider]]
    [helix.hooks :as hooks]
    [helix.dom :as d :refer [$d]]
    [form-tricorder.utils :refer [style> css>]]
    [form-tricorder.icons :refer [SunIcon MoonIcon SwapIcon
                                  ViewVerticalIcon
                                  ViewHorizontalIcon]]
    ["@radix-ui/react-radio-group" :as RadioGroup]
    ["@radix-ui/react-toolbar" :as Toolbar]
    #_["@radix-ui/react-icons" :refer []]))


;; Shared styles

(def itemStyles
  {:flex "0 0 auto"
   :color "$colors$outer_fg"
   :alignItems "center"
   "&:focus"
   {:outline "solid"}
   "&:hover"
   {}})

(def linkStyles
  {:display "inline-flex"})

(def buttonStyles
  {:outline "none"
   :cursor "pointer"
   :border "none"
   :padding "0.2rem"
   :background "none"
   :borderRadius "2px"
   "& svg"
   {:fill "$colors$outer_m200"}
   "&:hover"
   {:backgroundColor "$colors$inner_bg"
    :color "$colors$outer_m200"
    "& svg"
    {:fill "$colors$outer_m200"}}
   "&:disabled"
   {:cursor "not-allowed"
    :background "none"
    ; :color "$colors$outer_bg"
    "& svg"
    {:fill "$colors$outer_n200"}}})


;; Styled Components

(def Root
  (style> (.-Root Toolbar)
          {:display "flex"
           :minWidth "max-content"
           :column-gap "4px"
           :padding "4px"

           "& a"
           {:textDecoration "none"}}))

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
          {:width "1px"
           :backgroundColor "$colors$outer_n200"
           :margin "0 4px"}))

(def Link
  (style> (.-Link Toolbar)
          (merge itemStyles
                 linkStyles)))

(def AppLink
  (style> (.-Link Toolbar)
          (merge itemStyles
                 linkStyles
                 {:margin-right "auto"})))

(def ToggleGroup
  (style> (.-ToggleGroup Toolbar)
          {}))

(def ToggleItem
  (style> (.-ToggleItem Toolbar)
          (merge buttonStyles
                 {})))

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
        ($d AppLink {:href "https://tricorder.formform.dev"}
            "FORM tricorder")
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
        ($d Link
            {:href "https://github.com/formsandlines/form-tricorder"
             :target "_blank"}
            "src"))))

