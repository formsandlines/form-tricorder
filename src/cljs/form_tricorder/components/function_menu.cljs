(ns form-tricorder.components.function-menu
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.model :as model :refer [modes]]))

(defnc FunctionMenu
  [{:keys [handle-click]}]
  (d/div
   {:class "FunctionMenu"
    :style {:display "flex"
            :gap 10}}
   (for [{:keys [id label items]} modes]
     (d/fieldset
      {:key id
       :style {:flex (if (= id "more") "none" "1 1 0%")
               :padding 4
               :border "1px solid black"}}
      (d/legend label)
      (for [{:keys [id label]} items]
        (d/button
         {:key id
          :on-click (fn [e] (handle-click id (.-shiftKey e)))}
         label))))))
