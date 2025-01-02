(ns form-tricorder.components.output-area
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.view-pane :refer [ViewPane]]
   [form-tricorder.utils :refer [log]]
   ["@devbookhq/splitter$default" :as Splitter]))

(defnc OutputArea
  []
  ;; ? cache component in state
  (let [orientation (rf/subscribe [:frame/orientation])
        windows (rf/subscribe [:frame/windows])
        *sizes (hooks/use-ref (array 50 50))]
    (d/div
      {:class (css "OutputArea"
                   :p-0 :rounded-sm :bg
                   {:height "100%"
                    ;; :overflow-y "auto"
                    :box-sizing "border-box"
                    ;; :border "1px solid lightgray"
                    ;; :height "auto"
                    ;; :height "600px" ;; ! must be fixed because gutter-style
                    }
                   ;; ["& > div"
                   ;;  {:align-items "stretch"}]
                   )}
      (let [$item-styles (css {:height "100%"
                               :width "100%"
                               :overflow-y "auto"})]
        (case windows
          ;; single view
          1 (d/div {:class $item-styles}
              ($ ViewPane {:id 0
                           :only-child? true}))
          ;; split views
          2 ($d Splitter
              {:gutterClassName (css "outer"
                                     {:position "relative"}
                                     ["&:hover > *"
                                      {:background-color "var(--col-m16)"}]
                                     ["&::before"
                                      :bg
                                      {:content "\"\""
                                       :position "absolute"
                                       :width "1px"
                                       :height "100%"}]
                                     ["&[dir=Vertical]::before"
                                      {:width "100%"
                                       :height "1px"}])
               :draggerClassName (css "outer"
                                      {:background-color "var(--col-m11)"
                                       :position "relative"
                                       :z-index "1"})
               :minWidths (array 100 100)
               :minHeights (array 100 100)
               :initialSizes @*sizes
               :onResizeFinished (fn [_ newSizes] (reset! *sizes newSizes))
               :direction (case orientation
                            :cols "Horizontal"
                            :rows "Vertical"
                            (throw (ex-info "Invalid frame orientation"
                                            {:split-orientation orientation})))}
              (d/div {:class $item-styles}
                ($ ViewPane {:id 0}))
              (d/div {:class $item-styles}
                ($ ViewPane {:id 1})))
          (throw (ex-info "Invalid view count" {:view-count windows})))))))

