(ns form-tricorder.components.view-pane
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.components.function-tabs :refer [FunctionTabs]]
   [form-tricorder.utils :refer [css>]]
   ))

(def styles
  (css> {:height "100%"
         :width "100%"
         :overflow-y "auto"
         :position "relative"
         :display "flex"}))

(def close-button-styles
  (css> {:position "absolute"
         :top "$4"
         :right "$4"}))

(defnc ViewPane
  [{:keys [id view handle-change-view handle-remove-view]}]
  (let [{:keys [func-id]} view]
    (d/div
     {:class (str "ViewPane " (styles))}
     (d/div
      {:class "ViewPaneControls"}
      (when handle-remove-view
        (d/button
         {:class (close-button-styles)
          :on-click (fn [_] (handle-remove-view id))}
         "[x]"))
      ($ FunctionTabs
         {:view view
          :handle-change-view handle-change-view})))))
