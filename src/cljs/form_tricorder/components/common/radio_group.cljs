(ns form-tricorder.components.common.radio-group
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+]]
   [form-tricorder.stitches-config :as st]
   ["react" :as react]
   ["@radix-ui/react-radio-group" :as RadioGroupPrimitive]
   ["lucide-react" :refer [Circle]]
   ;; ["@radix-ui/react-icons" :refer [CircleIcon]]
   ;; ["@stitches/react" :refer [css]]
   ))

(def r) ;; hotfix for linting error in let+


(def Root
  (st/styled (.-Root RadioGroupPrimitive)
          {:display "grid"
           :gap 2}))

;;         "ring-offset-background focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
(def Item
  (st/styled (.-Item RadioGroupPrimitive)
          {:aspect-ratio "1 / 1"
           :height "$icon-sm" ;; 4
           :width "$icon-sm" ;; 4

           :border-radius "$full"
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
              :color "$outer-primary"

              "&:focus-visible"
              {:_ringOuter []}}

             :inner
             {:border-color "$inner-primary"
              :color "$inner-primary"

              "&:focus-visible"
              {:_ringInner []}}}}

           :defaultVariants
           {:layer :outer}}))

(def Indicator
  (st/styled (.-Indicator RadioGroupPrimitive)
          {:display "flex"
           :align-items "center"
           :justify-content "center"}))

(def IconCircle
  (st/styled Circle
          {:height "0.625rem" ;; 2.5
           :width  "0.625rem" ;; 2.5
           :fill "currentColor"
           :color "currentColor"}))


(defnc RadioGroup
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d Root
      {:class (string/join " " (remove nil? [className class]))
       :ref ref
       & r})))

(defnc RadioGroupItem
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer]
          :rest r} props]
    ($d Item
      {:class (string/join " " (remove nil? [className class]))
       :layer (or layer js/undefined)
       :ref ref
       & r}
      ($d Indicator
        ($d IconCircle)))))
