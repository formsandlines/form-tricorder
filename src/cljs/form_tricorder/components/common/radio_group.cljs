(ns form-tricorder.components.common.radio-group
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]
   ["@radix-ui/react-radio-group" :as RadioGroupPrimitive]
   [form-tricorder.components.common.toggle :as toggle]
   [form-tricorder.components.common.toggle-group :as toggle-group]
   ["lucide-react" :as icons]
   ;; ["@radix-ui/react-icons" :refer [CircleIcon]]
   ))

(def r) ;; hotfix for linting error in let+

(def Root (.-Root RadioGroupPrimitive))
(def Item (.-Item RadioGroupPrimitive))
(def Indicator (.-Indicator RadioGroupPrimitive))

(def RadioGroupContext
  (react/createContext {:size :default
                        :variant :default}))

(def toggle-group-variants
  (set (keys (:variant toggle-group/$$group-variants))))

(def $group-base nil)

(def $$group-variants
  (update toggle-group/$$group-variants :variant assoc
          :bullets (css {:display "grid"
                         :gap "2"})))

(defn $$group-styles
  [group-variant]
  (unite (if (toggle-group-variants group-variant)
           toggle-group/$group-base
           $group-base)
         (get-in $$group-variants [:variant (or group-variant :bullets)])))

(defnc RadioGroup
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className group-variant variant size children]
          :rest r} props]
    ($d Root
      {:class (unite ($$group-styles group-variant)
                     className class "peer")
       :orientation "vertical"
       :ref ref
       & r}
      (provider
       {:context RadioGroupContext
        :value {:variant variant
                :size size}}
       children))))


(def toggle-variants
  (set (keys (:variant toggle/$$variants))))

(def $base nil)

(def $$variants
  (update toggle/$$variants :variant assoc
          :bullet (css
                    :size-icon-sm :rounded-full :border ; :fg-primary
                    {:aspect-ratio "1 / 1"
                     :color "var(--col-bg-primary)"
                     :border-color "var(--col-bg-primary)"}
                    ["&:focus-visible"
                     :outline-none :ring]
                    ["&:disabled"
                     {:cursor "not-allowed"
                      :opacity "0.5"}])))

(defn $$styles
  [variant size]
  (unite (if (toggle-variants variant)
           toggle/$base
           $base)
         (get-in $$variants [:variant (or variant :bullet)])
         (get-in $$variants [:size (or size nil)])))

(defnc RadioGroupItem
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className variant size children]
          :rest r} props
         context (react/useContext RadioGroupContext)]
    ($d Item
      {:class (unite ($$styles (or (:variant context) variant)
                               (or (:size context) size))
                     className class "peer") ;; `.peer` is for labels, etc.
       :ref ref
       & r}
      (if (toggle-variants (:variant context))
        children
        ($d Indicator
          {:class (css {:display "flex"
                        :align-items "center"
                        :justify-content "center"})}
          ($d icons/Circle
            {:class (css {:height "0.625rem" ;; 2.5
                          :width  "0.625rem" ;; 2.5
                          :fill "currentColor"
                          :color "currentColor"})}))))))
