(ns form-tricorder.components.view-pane
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.function-tabs :refer [FunctionTabs]]
   [form-tricorder.stitches-config :refer [styled css]]
   ))

(def styles
  (css {:position "relative"
        :overflow-y "auto"
        :display "flex"}))

(def close-button-styles
  (css {:position "absolute"
        :top "$4"
        :right "$4"}))

(defnc ViewPane
  [{:keys [id only-child?]}]
  (let [{:keys [func-id]} (rf/subscribe [:views/->view id])
        handle-change-view #(rf/dispatch
                             [:views/set-func-id {:next-id %
                                                  :view-index id}])
        handle-remove-view #(rf/dispatch
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
