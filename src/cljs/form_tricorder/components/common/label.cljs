(ns form-tricorder.components.common.label
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]
   ["@radix-ui/react-label" :as LabelPrimitive]))

(def r) ;; hotfix for linting error in let+

(def Root (.-Root LabelPrimitive))

;; Note: `.peer` class must be set on controls linked to label
(defnc Label
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d Root
      {:class (unite className class
                     (css :font-size-sm :weight-normal :line-h-none
                          [".peer:disabled ~ &"
                           {:cursor "not-allowed"
                            :opacity "0.7"}]))
       :ref ref
       & r})))

