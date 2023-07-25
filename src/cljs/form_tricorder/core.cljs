(ns form-tricorder.core
  (:require
    [refx.alpha :as refx]
    [helix.core :refer [defnc fnc $ <> provider]]
    [helix.hooks :as hooks]
    [helix.dom :as d :refer [$d]]
    [formform.calc :as calc]
    [formform.expr :as expr]
    [formform.io :as io]
    [form-tricorder.events :as events]
    [form-tricorder.subs :as subs]
    [form-tricorder.effects :as effects]
    [form-tricorder.model :as model :refer [modes]]
    [form-tricorder.functions :as func]
    [form-tricorder.utils :refer [log clj->js*]]
    ["react-dom/client" :as rdom]
    ["@devbookhq/splitter$default" :as Splitter]
    ["/stitches.config" :refer (css)]))


(defnc FormulaInput
  [{:keys [apply-input]}]
  (let [[input set-input] (hooks/use-state "")]
    (d/div
     {:class "FormulaInput"
      :style {:display "flex"}}
     (d/input
      {:value input
       :on-change (fn [e] (do (.preventDefault e)
                              (set-input (.. e -target -value))))
       :on-key-press (fn [e] (when (= "Enter" (.-key e))
                               (apply-input input)))
       :style {:flex "1 1 auto"}})
     (d/button
      {:on-click (fn [e] (apply-input input))}
      "apply"))))

(defnc FunctionMenu
  [{:keys [handle-click]}]
  (d/div
   {:class "FunctionMenu"
    :style {:display "flex"
            :gap 10}}
   (for [{:keys [id label items]} modes]
     (d/fieldset
      {:key id
       :style {:flex (if (= id "more") "none" "1 1 0%")
               :padding 4
               :border "1px solid black"}}
      (d/legend label)
      (for [{:keys [id label]} items]
        (d/button
         {:key id
          :on-click (fn [e] (handle-click id (.-shiftKey e)))}
         label))))))


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

(defnc ViewPane
  [{:keys [id view handle-remove-view]}]
  (let [{:keys [func-id]} view
        mode-id (model/func->mode func-id)
        mode    (model/modes-map mode-id)]
    (println mode)
    (d/div
     {:class "ViewPane"
      :style {:height "100%"
              :width "100%"
              :overflow-y "auto"
              :position "relative"
              :display "flex"}}
     (d/div
      {:class "ViewPaneControls"}
      (when handle-remove-view
        (d/button
         {:on-click (fn [_] (handle-remove-view id))
          :style {:position "absolute"
                  :top 0
                  :right 0}}
         "[x]"))
      (d/div
       {:class "ModeFunctionTabs"}
       (d/pre (str mode-id))
       (d/hr)
       (d/ul
        (for [{:keys [id]} (:items mode)]
          (d/li {:key (str id)}
                (str id))))))
     (d/div
      {:class "ViewPaneContent"
       :style {:margin-left 16}}
      (d/pre id)
      (d/code (str view))
      (d/hr)
      (func/gen-component func-id {})))))

(defnc OutputArea
  [{:keys [views split-orientation]}]
  ;; ? cache component in state
  (let [*sizes (hooks/use-ref (array 50 50))]
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
                      :handle-remove-view nil})
       ;; split views
       2 (let [remove-view-handler #(refx/dispatch
                                     [:views/remove {:view-index %}])]
           ($d Splitter
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
                            :handle-remove-view remove-view-handler})
               ($ ViewPane {:id   1
                            :view (second views)
                            :handle-remove-view remove-view-handler})))
       (throw (ex-info "Invalid view count" {:views views}))))))

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

(defnc App
  []
  (let [views             (refx/use-sub [:views])
        split-orientation (refx/use-sub [:split-orientation])]
    (d/div
     {:class "App"
      :style {:margin "2rem 2rem"}}
     ($ Header {:view-split? (> (count views) 1)})
     ($ FormulaInput {:apply-input
                      #(refx/dispatch [:changed-formula
                                       {:next-formula %}])})
     ($ FunctionMenu {:handle-click
                      (fn [func-id alt-view?]
                        (let [view-index (if alt-view? 1 0)]
                          (do
                            (when alt-view? (refx/dispatch [:views/split]))
                            (refx/dispatch [:views/set-func-id
                                            {:next-id    func-id
                                             :view-index view-index}]))))})
     ($ OutputArea {:views views
                    :split-orientation split-orientation}))))

(defonce root
  (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init! []
  (refx/dispatch-sync [:initialize-db])
  (.render root ($ App)))


(comment

  #rtrace (+ 1 2)

  (let [expr 'a]
    (meta (-> expr
              expr/=>*
              (expr/op-get :dna)
              calc/dna->vdict
              calc/vdict->vmap))))
  
  
