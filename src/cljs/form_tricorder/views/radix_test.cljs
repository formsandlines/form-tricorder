(ns form-tricorder.views.radix-test
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   ["@radix-ui/react-popover" :as Popover]
   ["/stitches.config" :refer (styled css)]))

;; Need to use `css` for Reagent components:

(def my-button-style
  (css (clj->js
        {:backgroundColor "$accent" ;; access theme colors with `$`
         :color "black"})))

(defn my-button [text]
  (fn [text]
    ;; styles can be overwritten by passing a `css` prop:
    [:button {:class (my-button-style
                      (js-obj "css" (clj->js
                                     {:color "white"})))}
     text]))

;; React components can be wrapped in `styled`:

(def StyledTrigger
  (styled (.-Trigger Popover)
          (clj->js
           {:backgroundColor "red"
            :color "white"
            :borderRadius "4px"
            "&[data-state=open]" (clj->js
                                  {:backgroundColor "orange"})})))

(def StyledContent
  (styled (.-Content Popover)
          (clj->js
           {:backgroundColor "white"
            :borderRadius "4px"
            :padding "20px"
            :width "260px"})))

(def StyledArrow
  (styled (.-Arrow Popover)
          (clj->js
           {:fill "white"})))

(defn PopoverDemo []
  [:> (.-Root Popover)
   ;; rendered element can be changed with `myChild` prop,
   ;; but accessibility and functionality must be added manually:
   [:> StyledTrigger {:asChild true}
    [my-button "My Button"]]
   [:> StyledTrigger "More Info"]
   [:> (.-Portal Popover)
    [:> StyledContent
     "Some more info…"
     [:> StyledArrow]]]])

(def Button
  (styled "button"
          (clj->js
           {:backgroundColor "gainsboro"
            :borderRadius "9999px"
            :fontSize "13px"
            :padding "10px 15px"
            "&:hover" (clj->js
                       {:backgroundColor "lightgray"})})))
