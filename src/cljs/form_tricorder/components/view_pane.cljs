(ns form-tricorder.components.view-pane
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.function-tabs :refer [FunctionTabs]]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.stitches-config :as st]
   ["@radix-ui/react-icons" :refer [Cross2Icon]]
   ))

(def styles
  (st/css {:position "relative"
        :overflow-y "auto"
        :display "flex"}))

(def close-button-styles
  (st/css {:position "absolute"
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
          ($ Button
             {:st/css (clj->js {:position "absolute"
                             :width "$6"
                             :height "$6"
                             :top "$4"
                             :right "$4"})
              :variant "secondary"
              :size "icon"
              :layer "inner"
              :on-click handle-remove-view}
             ($d Cross2Icon)))
        ($ FunctionTabs
           {:func-id func-id
            :handle-change-view handle-change-view})))))
