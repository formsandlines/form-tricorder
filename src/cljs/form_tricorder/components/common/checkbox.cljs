(ns form-tricorder.components.common.checkbox
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+]]
   [form-tricorder.stitches-config :as st]
   ["react" :as react]
   ["@radix-ui/react-checkbox" :as CheckboxPrimitive]
   ["lucide-react" :refer [Check]]
   ;; ["@radix-ui/react-icons" :refer [CheckIcon]]
   ;; ["@stitches/react" :refer [css]]
   ))

(def r) ;; hotfix for linting error in let+


(def Root
  (st/styled (.-Root CheckboxPrimitive)
          {:height "$icon-sm"
           :width "$icon-sm"
           :flex-shrink 0
           :border-radius "$sm"
           :border-width "$1"
           
           "&:focus-visible"
           {:_outlineNone []}
           "&:disabled"
           {:cursor "not-allowed"
            :opacity "0.5"}

           :variants
           {:layer
            {:outer
             {:border-color "$outer-primary"
              "&:focus-visible"
              {:_ringOuter []}
              "&[data-state=checked]"
              {:background-color "$outer-primary"
               :color "$outer-primary-fg"}}

             :inner
             {:border-color "$inner-primary"
              "&:focus-visible"
              {:_ringInner []}
              "&[data-state=checked]"
              {:background-color "$inner-primary"
               :color "$inner-primary-fg"}}}}

           :defaultVariants
           {:layer :outer}}))

(def Indicator
  (st/styled (.-Indicator CheckboxPrimitive)
          {:display "flex"
           :align-items "center"
           :justify-content "center"
           :color "currentColor"}))

(def IconCheck
  (st/styled Check
          {:height "$icon-sm"
           :width "$icon-sm"}))


(defnc Checkbox
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer]
          :rest r} props]
    ($d Root
      {:class (str (string/join " " (remove nil? [className class]))
                   " peer") ;; peer is a tailwind thing
       :layer (or layer js/undefined)
       :ref ref
       & r}
      ($d Indicator
        ($d IconCheck)))))
