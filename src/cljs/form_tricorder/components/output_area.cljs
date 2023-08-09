(ns form-tricorder.components.output-area
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.components.view-pane :refer [ViewPane]]
   [form-tricorder.utils :refer [log clj->js*]]
   ["@devbookhq/splitter$default" :as Splitter]
   ["/stitches.config" :refer (css)]))

(def gutter-styles
  (-> {:position "relative"
       "&:hover > *" {:backgroundColor "#333"}
       "&::before" {:content ""
                    :position "absolute"
                    :width 1
                    :height "100%"
                    :backgroundColor "#888"}
       "&[dir=Vertical]::before" {:width "100%"
                                  :height 1}}
      clj->js*
      css))

(def dragger-styles
  (-> {:backgroundColor "#666"
       :position "relative"
       :z-index 999}
      clj->js*
      css))

(defnc OutputArea
  [{:keys [views split-orientation]}]
  ;; ? cache component in state
  (let [*sizes (hooks/use-ref (array 50 50))
        change-view-handler #(refx/dispatch
                              [:views/set-func-id {:next-id %2
                                                   :view-index %1}])
        remove-view-handler #(refx/dispatch
                              [:views/remove {:view-index %}])]
    (d/div
     {:class "OutputArea"
      :style {:border "1px solid lightgray"
              :padding 10
              :margin "10px 0"
              :height "600px" ;; ! must be fixed because gutter-style
              }}
     (case (count views)
       ;; single view
       1 ($ ViewPane {:id   0
                      :view (first views)
                      :handle-change-view (partial change-view-handler 0)
                      :handle-remove-view nil})
       ;; split views
       2 ($d Splitter
             {:gutterClassName (gutter-styles)
              :draggerClassName (dragger-styles)
              :minWidths (array 100 100)
              :minHeights (array 100 100)
              :initialSizes @*sizes
              :onResizeFinished (fn [_ newSizes] (reset! *sizes newSizes))
              :direction (case split-orientation
                           :cols "Horizontal"
                           :rows "Vertical"
                           (throw (ex-info "Invalid split orientation"
                                           {:split-orientation
                                            split-orientation})))}
             ($ ViewPane {:id   0
                          :view (first views)
                          :handle-change-view (partial change-view-handler 0)
                          :handle-remove-view remove-view-handler})
             ($ ViewPane {:id   1
                          :view (second views)
                          :handle-change-view (partial change-view-handler 1)
                          :handle-remove-view remove-view-handler}))
       (throw (ex-info "Invalid view count" {:views views}))))))

