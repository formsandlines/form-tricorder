(ns form-tricorder.components.app-toolbar
  (:require
    [refx.alpha :as refx]
    [helix.core :refer [defnc fnc $ <> provider]]
    [helix.hooks :as hooks]
    [helix.dom :as d :refer [$d]]
    [form-tricorder.utils :refer [clj->js*]]
    ["@radix-ui/react-radio-group" :as RadioGroup]
    ["@radix-ui/react-toolbar" :as Toolbar]
    ["/stitches.config" :refer (styled css)]
    ))

;; Shared styles

(def groupStyles
  {:display "flex"
   :gap "4px"})

(def itemStyles
  {:flex "0 0 auto"
   :color "white"
   :alignItems "center"
   "&:focus" {:outline "solid"}})

(def linkStyles
  {:textDecoration "none"
   :display "inline-flex"
   "&:hover" {:color "black"}})

(def buttonStyles
  {:outline "none"
   :cursor "pointer"
   :border "1px solid white"
   :background "none"
   "&:hover" {:backgroundColor "black"}})


;; Splitview styles

(def Button-splitview
  (styled (.-Button Toolbar)
          (clj->js*
           (merge itemStyles
                  buttonStyles
                  {}))))


;; Darkmode styles

(def Root-darkmode
  (styled (.-Root RadioGroup)
          (clj->js*
           (merge groupStyles
                  {}))))

(def Item-darkmode
  (styled (.-Item RadioGroup)
          (clj->js*
           (merge itemStyles
                  buttonStyles
                  {}))))

(def Indicator-darkmode
  (styled (.-Indicator RadioGroup)
          (clj->js*
           {})))


;; App Toolbar styles

(def Root
  (styled (.-Root Toolbar)
          (clj->js*
           (merge groupStyles
                  {; :width "100%"
                   :minWidth "max-content"
                   :padding "4px"}))))

(def Button
  (styled (.-Button Toolbar)
          (clj->js*
           (merge itemStyles
                  buttonStyles
                  {}))))

(def Separator
  (styled (.-Separator Toolbar)
          (clj->js*
           {:width "1px"
            :backgroundColor "black"
            :margin "0 4px"})))

(def Link
  (styled (.-Link Toolbar)
          (clj->js*
           (merge itemStyles
                  linkStyles))))

(def AppLink
  (styled (.-Link Toolbar)
          (clj->js*
           (merge itemStyles
                  linkStyles
                  {:marginRight "auto"
                   :color "blue"}))))

(def ToggleGroup
  (styled (.-ToggleGroup Toolbar)
          (clj->js*
           {})))

(def DarkmodeToggleItem
  (styled (.-ToggleItem Toolbar)
          (clj->js*
            (merge buttonStyles
                   {}))))

;; Components

(defnc AppToolbar
  [{:keys [view-split?]}]
  (let [handle-split-orientation
        #(refx/dispatch [:views/set-split-orientation {:next-orientation %}])
        handle-swap
        #(refx/dispatch [:views/swap])]
    ($d Root {:orientation "horizontal"
              :aria-label "App toolbar"}
        ($d AppLink {:href "https://tricorder.formform.dev"}
            "FORM tricorder")
        ($d Button-splitview
            {:disabled (not view-split?)
             :on-click (fn [_] (handle-split-orientation :cols))}
            "◨")
        ($d Button-splitview
            {:disabled (not view-split?)
             :on-click (fn [_] (handle-split-orientation :rows))}
            "⬓")
        ($d Button-splitview
            {:disabled (not view-split?)
             :on-click (fn [_] (handle-swap))}
            "⇄")
        ($d Separator)
        ($d ToggleGroup
            {:type "single"}
            ($d DarkmodeToggleItem
                {:on-click (fn [_] (js/console.log "Mode change"))}
                "🌙"))
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

