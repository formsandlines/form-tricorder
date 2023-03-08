(ns form-tricorder.core
  (:require
    [formform.calc :as calc]
    [form-tricorder.events :as events]
    [form-tricorder.subs :as subs]
    [form-tricorder.effects :as effects]
    [form-tricorder.views.app-toolbar :refer [AppToolbar]]
    [form-tricorder.views.function-menubar :refer [FunctionMenubar]]
    [form-tricorder.views.output-area :refer [OutputArea]]
    [reagent.core :as r]
    [reagent.dom :as d]
    [re-frame.core :as rf]
    ["/stitches.config" :refer (css)]))


(defn views-test
  []
  (let [views*    (r/atom [{:mode "a" :func "a" :active true}
                           {:mode "c" :func "b" :active true}])
        set-views (fn [v] (reset! views* v))
        style     (-> {:backgroundColor "darkgray"}
                      clj->js
                      css)]
    (fn []
      [:div {:class (style)}
       [AppToolbar {:views*    views*
                    :set-views set-views}]
       [FunctionMenubar {:views*    views*
                         :set-views set-views}]
       [OutputArea {:views*    views*
                    :set-views set-views}]])))

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


