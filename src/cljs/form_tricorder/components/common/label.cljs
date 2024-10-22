(ns form-tricorder.components.common.label
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+]]
   [form-tricorder.stitches-config :refer [css styled]]
   ["react" :as react]
   ["@radix-ui/react-label" :as LabelPrimitive]
   ;; ["@stitches/react" :refer [css]]
   ))

(def r) ;; hotfix for linting error in let+


;; "text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
(def Root
  (styled (.-Root LabelPrimitive)
          {:font-size "$sm"
           :font-weight "$normal"
           :line-height "$none"

           ".peer:disabled ~ &"
           {:cursor "not-allowed"
            :opacity "0.7"}}))


(defnc Label
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d Root
      {:class (string/join " " (remove nil? [className class]))
       :ref ref
       & r})))

