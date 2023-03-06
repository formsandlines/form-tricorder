(ns form-tricorder.components.splitview-test
  (:require
    [reagent.core :as r]
    [reagent.dom :as d]
    ["react" :as react]))

;; Inspired by:
;; https://blog.theodo.com/2020/11/react-resizeable-split-panels/

(defn LeftPanel
  [{:keys [left-width set-left-width]} children]
  (let [!left-ref (react/useRef nil)]
    (react/useEffect
     (fn []
       (when (nil? left-width)
         (set-left-width (.. !left-ref -current -clientWidth )))
       (fn []
         (set-left-width nil)))
     [set-left-width])
    [:div
     {:ref !left-ref
      :style {:width left-width
              :height "100%"}}
     children]))

(defn Divider
  [{:keys [mouse-down-handler touch-start-handler
           mouse-move-handler touch-move-handler
           mouse-up-handler]}]
  (react/useEffect
   (fn []
     (.addEventListener js/document "mousemove" mouse-move-handler)
     (.addEventListener js/document "touchmove" touch-move-handler)
     (.addEventListener js/document "mouseup" mouse-up-handler)
     (fn []
       (.removeEventListener js/document "mousemove" mouse-move-handler)
       (.removeEventListener js/document "touchmove" touch-move-handler)
       (.removeEventListener js/document "mouseup" mouse-up-handler)))
   [])
  [:div.divider-hitbox
   {:on-mouse-down mouse-down-handler
    :on-touch-start touch-start-handler
    :on-touch-end mouse-up-handler ;; ? needed
    :style {:cursor "col-resize"
            :align-self "stretch"
            :display "flex"
            :align-items "center"
            :padding "0 1rem"}}
   [:div.divier
    {:style {:width 2
             :height "100%"
             :margin "1rem"
             :border "2px solid #808080"}}]])

(defn on-move
  [state SplitView-el clientX min-width]
  (let [{:keys [left-width separator-x-pos dragging]} @state]
    (when (and dragging left-width separator-x-pos)
      (let [new-left-width (+ left-width (- clientX
                                            separator-x-pos))]
        (swap! state assoc :separator-x-pos clientX)
        (if (< new-left-width min-width)
          (swap! state assoc :left-width min-width)
          (if-let [SplitView-width
                   (when SplitView-el
                     (let [w (.-clientWidth SplitView-el)]
                       (when (> new-left-width
                                (- w min-width))
                         w)))]
            (swap! state assoc :left-width (- SplitView-width
                                              min-width))
            (swap! state assoc :left-width new-left-width)))))))

(defn SplitView
  [_ left right]
  (let [state (r/atom {:left-width 100
                       :separator-x-pos nil
                       :dragging false})
        !SplitView-ref (atom nil)
        min-width 50
        set-left-width (fn [n] (swap! state assoc :left-width n))
        on-mouse-down
        (fn [e]
          (swap! state assoc :separator-x-pos (.-clientX e))
          (swap! state assoc :dragging true))
        on-touch-start
        (fn [e]
          (swap! state assoc :separator-x-pos (aget e "touches" 0 "clientX"))
          (swap! state assoc :dragging true))
        on-mouse-move
        (fn [e]
          (when (:dragging @state)
            (.preventDefault e)
            (on-move state @!SplitView-ref
                     (.-clientX e) min-width)))
        on-touch-move
        (fn [e]
          (on-move state @!SplitView-ref
                   (aget e "touches" 0 "clientX") min-width))
        on-mouse-up
        (fn [_]
          (swap! state assoc :separator-x-pos nil)
          (swap! state assoc :dragging false))]
    (fn [props _ _]
      (let [{:keys [left-width]} @state]
        [:div.splitView
         (merge props
                {:ref   (fn [el] (reset! !SplitView-ref el))
                 :style {:height "100%"
                         :display "flex"
                         :flex-direction "row"
                         :align-items "flex-start"
                         :backgroundColor "white"}})
         [:f> LeftPanel {:left-width left-width
                         :set-left-width set-left-width}
          left]
         [:f> Divider {:mouse-down-handler on-mouse-down
                       :touch-start-handler on-touch-start
                       :mouse-move-handler on-mouse-move
                       :touch-move-handler on-touch-move
                       :mouse-up-handler on-mouse-up}]
         [:div.rightPane
          {:style {:flex 1
                   :height "100%"}}
          right]]))))




