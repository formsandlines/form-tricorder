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
  (css> {:position "relative"
         :overflow-y "auto"
         :display "flex"}))

(def close-button-styles
  (css> {:position "absolute"
         :top "$4"
         :right "$4"}))

(defnc ViewPane
  [{:keys [id only-child?]}]
  (let [{:keys [func-id]} (refx/use-sub [:views/->view id])
        handle-change-view #(refx/dispatch
                             [:views/set-func-id {:next-id %
                                                  :view-index id}])
        handle-remove-view #(refx/dispatch
                             [:views/remove {:view-index id}])]
    (d/div
      {:class (str "ViewPane " (styles))}
      (d/div
        {:class "ViewPaneControls"}
        (when-not only-child?
          (d/button
            {:class (close-button-styles)
             :on-click handle-remove-view}
            "[x]"))
        ($ FunctionTabs
          {:func-id func-id
           :handle-change-view handle-change-view})))))
