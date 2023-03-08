(ns form-tricorder.views.output-area
  (:require
   [form-tricorder.utils :refer [clj->js*]]
   [form-tricorder.views.function-tabs :refer [FunctionTabs]]
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


(defn OutputPane [{:keys [id style views* set-views]}]
  (let [value-change-handler (fn [v]
                               (set-views (update @views* id
                                                  assoc :func v)))]
    [:div.OutputPane
     {:style style}
     [FunctionTabs
      {:view (@views* id)
       :value-change-handler value-change-handler}]]))

(defn OutputArea [{:keys [views* set-views]}]
  (let [sizes* (atom (array 50 50))]
    (fn [_]
      (let [active-views (filter :active @views*)]
        [:div.OutputArea
         {:style {:height "400px"}}
         (condp == (count active-views)
           1 [OutputPane {:id        0
                          :style     {:backgroundColor "orange"}
                          :views*    views*
                          :set-views set-views}]
           2 [:> Splitter
              {:gutterClassName (gutter-styles)
               :draggerClassName (dragger-styles)
               :minWidths (array 100 100)
               :minHeights (array 100 100)
               :initialSizes @sizes*
               :onResizeFinished (fn [_ newSizes] (reset! sizes* newSizes))
               :direction "Horizontal"}
              [OutputPane {:id        0
                           :views*    views*
                           :set-views set-views}]
              [OutputPane {:id        1
                           :views*    views*
                           :set-views set-views}]]
           (assert "Must have at least one active view."))]))))
