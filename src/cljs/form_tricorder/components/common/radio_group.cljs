(ns form-tricorder.components.common.radio-group
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]
   ["@radix-ui/react-radio-group" :as RadioGroupPrimitive]
   ["lucide-react" :as icons]
   ;; ["@radix-ui/react-icons" :refer [CircleIcon]]
   ;; ["@stitches/react" :refer [css]]
   ))

(def r) ;; hotfix for linting error in let+

(def Root (.-Root RadioGroupPrimitive))

;;         "ring-offset-background focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",

(def Item (.-Item RadioGroupPrimitive))

(def Indicator (.-Indicator RadioGroupPrimitive))

(defnc RadioGroup
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d Root
      {:class (unite className class
                     (css {:display "grid"
                           :gap "2"}))
       :ref ref
       & r})))

(defnc RadioGroupItem
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer]
          :rest r} props]
    ($d Item
        {:class (unite className class
                       (css
                        :size-icon-sm :rounded-full :border ; :fg-primary
                        {:aspect-ratio "1 / 1"
                         :color "var(--col-bg-primary)"
                         :border-color "var(--col-bg-primary)"}
                        ["&:focus-visible"
                         :outline-none :ring]
                        ["&:disabled"
                         {:cursor "not-allowed"
                          :opacity "0.5"}]))
         :ref ref
         & r}
        ($d Indicator
            {:class (css {:display "flex"
                          :align-items "center"
                          :justify-content "center"})}
            ($d icons/Circle
                {:class (css {:height "0.625rem" ;; 2.5
                              :width  "0.625rem" ;; 2.5
                              :fill "currentColor"
                              :color "currentColor"})})))))
