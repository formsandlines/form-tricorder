(ns form-tricorder.components.varorder-select
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.stitches-config :refer [styled css]]
   ["@radix-ui/react-select" :as Select]
   ["@radix-ui/react-icons" :refer [CheckIcon ChevronDownIcon ChevronUpIcon]]))


(def Root
  (styled (.-Root Select)
          {}))

(def Trigger
  (styled (.-Trigger Select)
          {:display "inline-flex"
           :align-items "center"
           :justify-content "center"
           :padding "0.2rem 0.3rem 0.2rem 0.4rem"
           :gap "0.2rem"

           :font-family "$mono"
           :font-size "$1"
           :background-color "$inner-bg"
           :color "$inner-fg"
           :border "1px solid $inner-border"
           :border-radius "$2"
           :cursor "pointer"}))

(def Portal
  (styled (.-Portal Select)
          {}))

(def Content
  (styled (.-Content Select)
          {:overflow "hidden"

           :font-family "$mono"
           :font-size "$1"
           :background-color "$inner-bg"
           :border "1px solid $inner-border"
           :border-radius "$2"}))

(def Viewport
  (styled (.-Viewport Select)
          {:padding "0.2rem 0.3rem 0.2rem 0.4rem"}))

(def Item
  (styled (.-Item Select)
          {:font "inherit"
           :cursor "pointer"
           :border-radius "$2"
           :display "flex"
           :align-items "center"
           :padding "0.2rem 0.4rem"

           :color "$inner-fg"
           
           "&[data-disabled]"
           {; :color ""
            :pointer-events "none"}
           "&[data-highlighted]"
           {:outline "none"
            :color "$inner-bg"
            :backgroundColor "$inner-accent"}}))

(def ItemText
  (styled (.-ItemText Select)
          {}))

(def ScrollUpButton
  (styled (.-ScrollUpButton Select)
          {}))

(def ScrollDownButton
  (styled (.-ScrollDownButton Select)
          {}))

(def Icon
  (styled (.-Icon Select)
          {}))

(def Arrow
  (styled (.-Arrow Select)
          {}))

(defnc VarorderSelect
  [{:keys [current-varorder permutations value-change-handler display]}]
  ($d Root {:value current-varorder
            :onValueChange value-change-handler}
      ($d Trigger
          ($d (.-Value Select)
              {:placeholder "Select varorder…"}
              (display current-varorder))
          ($d Icon
              ($d ChevronDownIcon)))
      ($d Portal
          ($d Content
              ($d ScrollUpButton
                  ($d ChevronUpIcon))
              ($d Viewport
                  (for [varorder permutations
                        :let [label (display varorder)]]
                    ($d Item
                        {:key label
                         :value varorder}
                        ($d ItemText
                            label))))
              ($d ScrollDownButton
                  ($d ChevronDownIcon))))))

