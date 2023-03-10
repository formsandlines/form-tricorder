(ns form-tricorder.views.app-toolbar
  (:require
    [helix.core :refer [defnc $ <>]]
    [helix.hooks :as hooks]
    [helix.dom :as d :refer [$d]]
    [form-tricorder.utils :refer [clj->js*]]
    ["@radix-ui/react-radio-group" :as RadioGroup]
    ["@radix-ui/react-toolbar" :as Toolbar]
    ["/stitches.config" :refer (styled)]))


;; Shared styles

(def groupStyles
  {:display "flex"
   :gap "4px"})

(def itemStyles
  {:flex "0 0 auto"
   :color "white"
   :alignItems "center"})

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

(def Root-splitview
  (styled (.-Root RadioGroup)
          (clj->js*
           (merge groupStyles
                  {}))))

(def Item-splitview
  (styled (.-Item RadioGroup)
          (clj->js*
           (merge itemStyles
                  buttonStyles
                  {}))))

(def Indicator-splitview
  (styled (.-Indicator RadioGroup)
          (clj->js*
           {})))

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

(def ToggleItem
  (styled (.-ToggleItem Toolbar)
          (clj->js*
           (merge itemStyles
                  {:border "1px solid black"
                   :background "none"
                   :outline "none"}))))


;; Components

(defnc SplitviewRadioGroup
  [{:keys [mode value-change-handler]}]
  ($d Root-splitview
      {:value mode
       :onValueChange value-change-handler
       :orientation "vertical"}
      ($d Item-splitview {:value "single-1"}
          ($d Indicator-splitview) "1")
      ($d Item-splitview {:value "single-2"}
          ($d Indicator-splitview) "2")
      ($d Item-splitview {:value "split-hz"}
          ($d Indicator-splitview) "◨")
      ($d Item-splitview {:value "split-vt"}
          ($d Indicator-splitview) "⬓")))

(defnc DarkmodeRadioGroup
  [{:keys [mode value-change-handler]}]
  ($d Root-darkmode 
      {:value mode
       :onValueChange value-change-handler
       :orientation "vertical"}
      ($d Item-darkmode {:value "dark"}
          ($d Indicator-darkmode) "🌙")
      ($d Item-darkmode {:value "light"}
          ($d Indicator-darkmode) "🔅")))

(defnc AppToolbar 
  [{:keys [views set-views]}]
  ($d Root {}
      ($d AppLink {:href "https://tricorder.formform.dev"}
          "FORM tricorder")
      ($ SplitviewRadioGroup 
         {:mode :split1
          :value-change-handler (fn [_] (js/console.log "View change"))})
      ($d Button-splitview 
          {:on-click (fn [_] (set-views #(let [[v1 v2] %]
                                           [v2 v1])))}
          "⇄")
      ($d Separator)
      ($ DarkmodeRadioGroup 
         {:mode "🌙"
          :value-change-handler (fn [_] (js/console.log "Mode change"))})
      ($d Separator)
      ($d Button {:on-click (fn [_] (js/console.log "Clicked about"))}
          "about")
      ($d Button {:on-click (fn [_] (js/console.log "Clicked help"))}
          "help")
      ($d Separator)
      ($d Link 
          {:href "https://github.com/formsandlines/form-tricorder"
           :target "_blank"}
          "src")))
