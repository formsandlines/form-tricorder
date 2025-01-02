(ns form-tricorder.components.app-toolbar
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.utils :refer [unite]]
   [form-tricorder.icons :refer [SunIcon MoonIcon SwapIcon
                                 ViewVerticalIcon
                                 ViewHorizontalIcon
                                 SourceIcon]]
   ["@radix-ui/react-toolbar" :as Toolbar]
   #_["@radix-ui/react-icons" :refer []]))

(def Root (.-Root Toolbar))

;; TODO: refactor components & styles

(def $common-button-styles
  (css :rounded-sm :transition-colors :p-1
       {:touch-action "manipulation"
        :display "inline-flex"
        :justify-content "center"
        :align-items "center"
        :white-space "nowrap"
        :outline "none"
        :cursor "pointer"
        :border "none"
        :background "none"
        :color "var(--col-bg-primary)" ; "$m27"
        }
       ["&:hover"
        :bg-accent :fg-accent]))

(def $common-item-styles
  (css {:flex "0 0 auto"}
       ;; ["&:focus"
       ;;  {:outline "solid"}]
       ;; ["&:focus-visible"
       ;;  {:outline "none"}]
       ["&:focus-visible"
        :outline-none :ring
        ;; {:_ring [2 "$colors$ring" 1 "$colors$outer-bg"]}
        ]))

(def Button (.-Button Toolbar))
(def $button-styles
  (unite $common-item-styles $common-button-styles))

(def TextButton (.-Button Toolbar))
(def $text-button-styles
  (unite $common-item-styles $common-button-styles
         (css :px-1-5 :py-1
              ["&:disabled"
               {:opacity "0.5"}])))

(def Separator (.-Separator Toolbar))
(def $separator-styles
  (unite $common-item-styles
         (css :my-0 :mx-1
              {:width "1px"
               :background-color "var(--col-n8)"})))

(def ToggleGroup (.-ToggleGroup Toolbar))
(def $toggle-group-styles
  (unite $common-item-styles
         (css :gap-1
              {:display "flex"
               :min-width "max-content"})))

(def ToggleItem (.-ToggleItem Toolbar))
(def $toggle-item-styles
  (unite $common-item-styles $common-button-styles))

(def SourceLink (.-Link Toolbar))
(def $source-link-styles
  (unite $common-item-styles $common-button-styles))

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
    ($d Root
      {:class (css :font-size-sm :fg :gap-1
                   {:display "flex"
                    :min-width "max-content"
                    :height "100%"
                    :align-items "stretch"}
                   ["& .icon"
                    :w-icon-sm ;; 18px ?
                    {:height "100%"
                     :fill "var(--col-m21)"}]
                   ;; ["& *:hover > .icon"
                   ;;  {:fill "$m800"}]
                   ["& *:disabled"
                    {:pointer-events "none"
                     :cursor "not-allowed"}]
                   ["& *:disabled > .icon"
                    {:fill "var(--col-bg-muted)"}])
       :orientation "horizontal"
       :aria-label "App toolbar"}
      ($d ToggleGroup
        {:class $toggle-group-styles
         :type "single"
         :value (name frame-orientation)
         :on-value-change #(handle-frame-orientation (keyword %))}
        ($d ToggleItem
          {:class $toggle-item-styles
           :value "cols"
           :disabled (= frame-orientation :cols)}
          ($ ViewVerticalIcon))
        ($d ToggleItem
          {:class $toggle-item-styles
           :value "rows"
           :disabled (= frame-orientation :rows)}
          ($ ViewHorizontalIcon)))
      ($d Button
        {:class $button-styles
         :disabled (not view-split?)
         :on-click (fn [_] (handle-swap))}
        ($ SwapIcon))
      ($d Separator
        {:class $separator-styles})
      ($d ToggleGroup
        {:class $toggle-group-styles
         :type "single"
         :value (name appearance)
         :on-value-change #(handle-toggle-appearance (keyword %))}
        ($d ToggleItem
          {:class $toggle-item-styles
           :value "light"
           :disabled (= appearance :light)}
          ($ SunIcon))
        ($d ToggleItem
          {:class $toggle-item-styles
           :value "dark"
           :disabled (= appearance :dark)}
          ($ MoonIcon)))
      ($d Separator
        {:class $separator-styles})
      ($d TextButton
        {:class $text-button-styles
         :on-click (fn [_] (js/console.log "Clicked about"))}
        "about")
      ($d TextButton
        {:class $text-button-styles
         :on-click (fn [_] (js/console.log "Clicked help"))}
        "help")
      ($d Separator
        {:class $separator-styles})
      ($d SourceLink
        {:class $source-link-styles
         :href "https://github.com/formsandlines/form-tricorder"
         :target "_blank"}
        ($ SourceIcon)))))

