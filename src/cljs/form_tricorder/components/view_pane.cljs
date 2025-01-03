(ns form-tricorder.components.view-pane
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.function-tabs :refer [FunctionTabs]]
   [form-tricorder.components.common.button :refer [Button]]
   ["@radix-ui/react-icons" :refer [Cross2Icon]]
   ))

(defnc ViewPane
  [{:keys [id only-child?]}]
  (let [{:keys [func-id]} (rf/subscribe [:views/->view id])
        handle-change-view #(rf/dispatch
                             [:views/set-func-id {:next-id %
                                                  :view-index id}])
        handle-remove-view #(rf/dispatch
                             [:views/remove {:view-index id}])]
    (d/div
      {:class (css "ViewPane"
                   {:position "relative"
                    :overflow-y "auto"
                    :display "flex"})}
      (d/div
        {:class "ViewPaneControls"}
        (when-not only-child?
          ($ Button
             {:class (css :top-4 :right-4
                          {:position "absolute"
                           :z-index "1"
                           :outline "0.5rem solid var(--col-bg)"})
              :variant :secondary
              :size :icon-sm
              :on-click handle-remove-view}
             ($d Cross2Icon)))
        ($ FunctionTabs
           {:func-id func-id
            :handle-change-view handle-change-view})))))
