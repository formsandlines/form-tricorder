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
      {:class (css "ViewPane" :px-4 :pt-4 :pb-8
                   {:display "flex"
                    :height "100%"
                    :width "100%"})}
      (d/div
        {:class (css "ViewPaneControls"
                     {:height "100%"
                      :width "100%"
                      ;; :overflow "hidden"
                      :position "relative"})}
        (when-not only-child?
          ($ Button
             {:class (css :top-0 :right-0
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
