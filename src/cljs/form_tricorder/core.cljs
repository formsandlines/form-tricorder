(ns form-tricorder.core
  (:require
    [formform.calc :as calc]
    [form-tricorder.events :as events]
    [form-tricorder.subs :as subs]
    [form-tricorder.effects :as effects]
    ; [form-tricorder.views.radix-test :refer [PopoverDemo Button]]
    [form-tricorder.views.app-toolbar :refer [AppToolbar]]
    [form-tricorder.views.function-menubar :refer [FunctionMenubar]]
    ; [form-tricorder.views.function-radio-group :refer [FunctionRadioGroup]]
    [form-tricorder.views.function-tabs :refer [FunctionTabs]]
    [form-tricorder.views.output-area :refer [OutputArea]]
    [form-tricorder.components.splitview-test :refer [SplitView]]
    [reagent.core :as r]
    [reagent.dom :as d]
    [re-frame.core :as rf]
    ["/stitches.config" :refer (css)]
    ; ["/components/Button/Button" :refer [Button]]
    ))


(defn fa-a [] [:div [:h2 "Content of A.a"]])
(defn fa-b [] [:div [:h2 "Content of A.b"]])
(defn fa-c [] [:div [:h2 "Content of A.c"]])

(defn fb-a [] [:div [:h2 "Content of B.a"]])
(defn fb-b [] [:div [:h2 "Content of B.b"]])
(defn fb-c [] [:div [:h2 "Content of B.c"]])

(defn fc-a [] [:div [:h2 "Content of C.a"]])
(defn fc-b [] [:div [:h2 "Content of C.b"]])
(defn fc-c [] [:div [:h2 "Content of C.c"]])

(defn testc [props content]
  [:div {:class "testc"}
   content])

(defn views-test
  []
  (let [state (r/atom {:mode "a" :func "a"})
        style (-> {:backgroundColor "darkgray"}
                  clj->js
                  css)
        func-content {"a" {"a" fa-a "b" fa-b "c" fa-c}
                      "b" {"a" fb-a "b" fb-b "c" fb-c}
                      "c" {"a" fc-a "b" fc-b "c" fc-c}}
        make-on-select (fn [{:keys [type subtype] :as m}]
                         (fn [_]
                           (js/console.log m)
                           (swap! state assoc
                                  :mode type
                                  :func subtype)))
        fn-change-handler (fn [v]
                            (swap! state assoc :func v))]
    (fn []
      [:div {:class (style)}
       [AppToolbar]
       [FunctionMenubar {:on-select make-on-select}]
       [FunctionTabs (assoc @state
                            :fn-change-handler fn-change-handler
                            :func-content func-content)]
       [:div {:style {:height "400px"}}
        [SplitView
         {}
         [:div {:style {:height "100%"
                        :backgroundColor "lightblue"}} "A"]
         [:div {:style {:height "100%"
                        :backgroundColor "lightgreen"}} "B"]]]
       #_[OutputArea {}]
       #_[testc {:x 12} [:p "Hallo"]]])))

(defn re-frame-test
  []
  (let [*answer (rf/subscribe [:test/subs])]
    [:<>
     [:p "The answer is: " @*answer]
     [:button
      {:on-click (fn [e] (rf/dispatch [:test/event 
                                       {:new-answer "within yourself"}]))}
      "Click me!"]]))

(defn formform-test
  []
  [:<>
   [:p "Testing formform.calc:"]
   [:pre (str (calc/rel [:N :U :I :M :N :N :I :I :N :U :N :U :N :N :N :N :N :U :I :M :N :U :I :M :N :U :N :U :N :U :N :U :N :U :I :M :N :N :I :I :N :U :I :M :N :N :I :I :N :U :I :M :N :U :I :M :N :U :I :M :N :U :I :M]
                        [:M :I :U :N :I :M :N :U :U :N :M :I :N :U :I :M]))]])

(defn root
  []
  [:div#root
   ; [re-frame-test]
   ; [formform-test]
   ; [PopoverDemo]
   ; [:> Button "Styled"]
   [views-test]
   ])

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (d/render [root]
            (.getElementById js/document "app")))

(defn ^:export init! []
  (rf/dispatch-sync [:initialize-db])
  (mount-root))


(comment
  (js/console.log (+ 1 2))
  
  )


