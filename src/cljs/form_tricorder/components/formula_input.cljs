(ns form-tricorder.components.formula-input
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]))

(defnc FormulaInput
  [{:keys [apply-input]}]
  (let [[input set-input] (hooks/use-state "")]
    (d/div
     {:class "FormulaInput"
      :style {:display "flex"}}
     (d/input
      {:value input
       :on-change (fn [e] (do (.preventDefault e)
                              (set-input (.. e -target -value))))
       :on-key-press (fn [e] (when (= "Enter" (.-key e))
                               (apply-input input)))
       :style {:flex "1 1 auto"}})
     (d/button
      {:on-click (fn [e] (apply-input input))}
      "apply"))))
