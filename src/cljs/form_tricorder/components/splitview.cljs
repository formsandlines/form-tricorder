(ns form-tricorder.components.splitview
  (:require
   [reagent.core :as r]
    [reagent.dom :as d]
   [form-tricorder.utils :refer [assocp]]))

(defn Pane
  [{:keys [props]} content]
  (fn [{:keys [size horizontal?]} _]
    [:div
     (merge props
            {:data-orientation (if horizontal? "horizontal" "vertical")
             :style (merge
                     (if horizontal?
                       {:height size}
                       {:width size})
                     {:maxWidth "100%"})})
     content]))

(defn Divider
  [{:keys [props horizontal?]} handle]
  (fn [{:keys [shift drag-handler]} _]
    [:div
     (merge props
            {:aria-orientation (if horizontal? "horizontal" "vertical")
             :data-orientation (if horizontal? "horizontal" "vertical")
             :role "separator"
             :decorative false
             :draggable true
             :horizontal? horizontal?
             :onDrag drag-handler
             :style (merge
                     (if horizontal?
                       {:top shift}
                       {:left shift})
                     {:flex-direction (if horizontal? "column" "row")
                      :position "absolute"
                      :zIndex "998"})})
     (assocp handle
             :horizontal? horizontal?)]))

(defn Handle
  [{:keys [props]}]
  (fn [{:keys [drag-handler horizontal?]}]
    [:div
     (merge props
            {:data-orientation (if horizontal? "horizontal" "vertical")
               ;:onDrag drag-handler
             :style {:position "relative"
                     :zIndex "999"}})]))

(defn Root
  [{:keys [props horizontal?]} divider pane1 pane2]
  (let [state          (r/atom {:ratio 0.3})
        ratio->percent #(-> % (* 100) (str "%"))]
    (fn [{:keys []} _ _ _]
      (let [{:keys [ratio]} @state]
        [:div
         (merge props
                {:style {:display "flex"
                         :flex-direction (if horizontal? "column" "row")
                         :position "relative"}})
         (assocp divider
                 :horizontal? horizontal?
                 :shift (->> ratio ratio->percent)
                 :drag-handler (fn [e]
                                 (let [size  (.. e -target -parentElement
                                                 -clientWidth)
                                       ratio (/ (.-clientX e)
                                                size)]
                                   (.preventDefault e)
                                   (swap! state #(assoc % :ratio ratio)))))
         (assocp pane1
                 :horizontal? horizontal?
                 :size (->> ratio ratio->percent))
         (assocp pane2
                 :horizontal? horizontal?
                 :size (->> ratio (- 1.0) ratio->percent))]))))


