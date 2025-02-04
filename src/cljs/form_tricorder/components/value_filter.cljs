(ns form-tricorder.components.value-filter
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
   [form-tricorder.components.common.toggle-group
    :refer [ToggleGroup ToggleGroupItem]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   [form-tricorder.utils :as utils :refer [let+ unite]]))


(defnc ValueFilter
  [{:keys [results dna varorder]}]
  (let [[thing set-thing] (hooks/use-state #js ["b"])]
    (<> ($ ToggleGroup
           {:type "multiple"
            :value thing
            :onValueChange (fn [v] (set-thing v))
            :variant :outline
            :size :sm}
           ($ ToggleGroupItem
              {:value "a"}
              "A")
           ($ ToggleGroupItem
              {:value "b"}
              "B")
           ($ ToggleGroupItem
              {:value "c"}
              "C"))
        (d/pre (d/code (str thing))))))
