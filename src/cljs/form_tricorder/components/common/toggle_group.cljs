(ns form-tricorder.components.common.toggle-group
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+ unite]]
   [form-tricorder.components.common.toggle :refer [$$styles]]
   ["react" :as react]
   ["@radix-ui/react-toggle-group" :as ToggleGroupPrimitive]))

(def r) ;; hotfix for linting error in let+

(def Root (.-Root ToggleGroupPrimitive))
(def Item (.-Item ToggleGroupPrimitive))

(def ToggleGroupContext
  (react/createContext {:size :default
                        :variant :default}))

(defnc ToggleGroup
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className variant size children]
          :rest r} props]
    ($d Root
      {:class (unite (css :gap-1
                          {:display "flex"
                           :align-items "center"
                           :justify-content "center"})
                     className class)
       :ref ref
       & r}
      (provider
       {:context ToggleGroupContext
        :value {:variant variant
                :size size}}
       children))))

(defnc ToggleGroupItem
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className variant size children]
          :rest r} props
         context (react/useContext ToggleGroupContext)]
    ($d Item
      {:class (unite ($$styles (or (:variant context) variant)
                               (or (:size context) size))
                     className class)
       :ref ref
       & r}
      children)))
