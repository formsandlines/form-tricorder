(ns form-tricorder.components.output-area
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.components.view-pane :refer [ViewPane]]
   [form-tricorder.utils :refer [log style> css>]]
   ["@devbookhq/splitter$default" :as Splitter]))

(def styles
  (css> {:height "100%"
         :padding "0" ; "0.2rem"
         :box-sizing "border-box"
         ; :border "1px solid lightgray"
         ; :height "auto"
         ; :height "600px" ;; ! must be fixed because gutter-style
         :border-radius "$3"
         :background-color "$colors$inner_bg"
         ; "& > div"
         ; {:align-items "stretch"}
         }))

(def gutter-styles
  (css> {:position "relative"
         "&:hover > *"
         {:background-color "$outer_hl"}
         "&::before"
         {:content ""
          :position "absolute"
          :width 1
          :height "100%"
          :background-color "$outer_bg"}
         "&[dir=Vertical]::before"
         {:width "100%"
          :height 1}}))

(def dragger-styles
  (css> {:background-color "$outer_m200"
         :position "relative"
         :z-index 999}))

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
     {:class (str "OutputArea " (styles))}
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

