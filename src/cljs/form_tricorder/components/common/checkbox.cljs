(ns form-tricorder.components.common.checkbox
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]
   ["@radix-ui/react-checkbox" :as CheckboxPrimitive]
   ["lucide-react" :as icons]
   ;; ["@radix-ui/react-icons" :refer [CheckIcon]]
   ;; ["@stitches/react" :refer [css]]
   ))

(def r) ;; hotfix for linting error in let+

(def $styles
  (css
   :size-icon-sm :border :rounded-sm
   {:flex-shrink 0
    :border-color "var(--col-bg-primary)"}
   ["&[data-state=checked]"
    :bg-primary :fg-primary]
   ["&:focus-visible"
    :outline-none :ring]
   ["&:disabled"
    {:cursor "not-allowed"
     :opacity "0.5"}]))

(def Root (.-Root CheckboxPrimitive))
(def Indicator (.-Indicator CheckboxPrimitive))

(defnc Checkbox
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d Root
      {:class
       (unite className class $styles "peer") ;; `.peer` is for labels, etc.
       :ref ref
       & r}
      ($d Indicator
        {:class (css {:display "flex"
                      :align-items "center"
                      :justify-content "center"
                      :color "currentColor"})}
        ($d icons/Check
          {:class (css :size-icon-sm)})))))
