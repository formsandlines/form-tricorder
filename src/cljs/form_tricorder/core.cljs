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
          :on-click (fn [e]
                      (let [view-id (if (.-shiftKey e) 1 0)]
                        (handle-click id view-id)))}
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
  [{:keys [id view]}]
  (let [{:keys [func-id]} view]
    (d/div
     {:class "ViewPane"
      :style {:height "100%"
              :width "100%"
              :overflow-y "auto"}
      ; :style style
      }
     (<> (d/pre id)
         (d/code (str view))
         (d/hr)
         (func/gen-component func-id {})))))

(defnc OutputArea
  [{:keys []}]
  ;; ? cache component in state
  (let [*sizes (hooks/use-ref (array 50 50))
        views  (refx/use-sub [:views])
        orientation (refx/use-sub [:view-orientation])
        split? (refx/use-sub [:view-split?])]
    (d/div
     {:class "OutputArea"
      :style {:border "1px solid lightgray"
              :padding 10
              :margin "10px 0"
              :height "600px" ;; ! must be fixed because gutter-style
              }}
     ; (d/p mode-id)
     (if split?
       ;; split views
       ($d Splitter
           {:gutterClassName (gutter-styles)
            :draggerClassName (dragger-styles)
            :minWidths (array 100 100)
            :minHeights (array 100 100)
            :initialSizes @*sizes
            :onResizeFinished (fn [_ newSizes] (reset! *sizes newSizes))
            :direction orientation}
           ($ ViewPane {:id   0
                        :view (first views)})
           ($ ViewPane {:id   1
                        :view (second views)}))
       ;; single view
       ($ ViewPane {:id   0
                    :view (first views)})))))

(defnc ViewControls
  [{:keys [handle-change-orientation handle-change-split handle-swap]}]
  (d/div
   {:class "ViewControls"
    :style {:display "flex"}}
   (d/div {:style {:display "flex"}}
          (d/button
           {:on-click (fn [_] (handle-change-split false))}
           "Single")
          (d/button
           {:on-click (fn [_] (handle-change-split true))}
           "Split"))
   (d/div {:style {:display "flex" :margin-left 6}}
          (d/button
           {:on-click (fn [_] (handle-change-orientation "Vertical"))}
           "Split vert.")
          (d/button
           {:on-click (fn [_] (handle-change-orientation "Horizontal"))}
           "Split horiz."))
   (d/div {:style {:display "flex" :margin-left 6}}
          (d/button
           {:on-click (fn [_] (handle-swap))}
           "Swap"))))

(defnc Header
  [{}]
  (d/div
   {:class "Header"
    :style {:display "flex"
            :margin-bottom 10}}
   (d/div
    {:style {:class "Title"
             :flex-grow 1}}
    (d/h1 "FORM tricorder"))
   ($ ViewControls {:handle-change-orientation
                    #(refx/dispatch [:views/change-orientation
                                     {:next-orientation %}])
                    :handle-change-split
                    #(refx/dispatch [:views/change-split
                                     {:split? %}])
                    :handle-swap
                    #(refx/dispatch [:views/swap])})))

(defnc App
  []
  (let [func-id (refx/use-sub [:func-id]) ;; ! obsolete
        ]
    (d/div
     {:class "App"
      :style {:margin "2rem 2rem"}}
     ($ Header {})
     ($ FormulaInput {:apply-input
                      #(refx/dispatch [:changed-formula
                                       {:next-formula %}])})
     ($ FunctionMenu {:handle-click
                      #(refx/dispatch [:set-func-id
                                       {:next-id %1
                                        :view-id %2}])})
     ($ OutputArea {:func-id func-id}))))

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
  
  
