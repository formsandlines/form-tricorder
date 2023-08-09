(ns form-tricorder.components.header
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]))


(defnc ViewControls
  [{:keys [view-split? handle-split-orientation handle-swap]}]
  (d/div
   {:class "ViewControls"
    :style {:display "flex"}}
   (d/div {:style {:display "flex" :margin-left 6}}
          (d/button
           {:on-click (fn [_] (handle-split-orientation :cols))
            :disabled (not view-split?)}
           "Split cols")
          (d/button
           {:on-click (fn [_] (handle-split-orientation :rows))
            :disabled (not view-split?)}
           "Split rows"))
   (d/div {:style {:display "flex" :margin-left 6}}
          (d/button
           {:on-click (fn [_] (handle-swap))
            :disabled (not view-split?)}
           "Swap"))))

(defnc Header
  [{:keys [view-split?]}]
  (d/div
   {:class "Header"
    :style {:display "flex"
            :margin-bottom 10}}
   (d/div
    {:style {:class "Title"
             :flex-grow 1}}
    (d/h1 "FORM tricorder"))
   ($ ViewControls {:view-split? view-split?
                    :handle-split-orientation
                    #(refx/dispatch [:views/set-split-orientation
                                     {:next-orientation %}])
                    :handle-swap
                    #(refx/dispatch [:views/swap])})))
