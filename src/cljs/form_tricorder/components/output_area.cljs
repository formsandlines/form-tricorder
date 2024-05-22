(ns form-tricorder.components.output-area
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.view-pane :refer [ViewPane]]
   [form-tricorder.utils :refer [log style> css>]]
   ["@devbookhq/splitter$default" :as Splitter]))

(def styles
  (css> {:height "100%"
         ;; :overflow-y "auto"
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

(def item-styles
  (css> {:height "100%"
         :width "100%"
         :overflow-y "auto"
         }))


(defnc OutputArea
  []
  ;; ? cache component in state
  (let [orientation (rf/subscribe [:frame/orientation])
        windows (rf/subscribe [:frame/windows])
        *sizes (hooks/use-ref (array 50 50))]
    (d/div
      {:class (str "OutputArea " (styles))}
      (case windows
        ;; single view
        1 (d/div {:class (item-styles)}
                 ($ ViewPane {:id 0
                              :only-child? true}))
        ;; split views
        2 ($d Splitter
            {:gutterClassName (gutter-styles)
             :draggerClassName (dragger-styles)
             :minWidths (array 100 100)
             :minHeights (array 100 100)
             :initialSizes @*sizes
             :onResizeFinished (fn [_ newSizes] (reset! *sizes newSizes))
             :direction (case orientation
                          :cols "Horizontal"
                          :rows "Vertical"
                          (throw (ex-info "Invalid frame orientation"
                                          {:split-orientation orientation})))}
            (d/div {:class (item-styles)}
                   ($ ViewPane {:id 0}))
            (d/div {:class (item-styles)}
                   ($ ViewPane {:id 1})))
        (throw (ex-info "Invalid view count" {:view-count windows})))
      )))

