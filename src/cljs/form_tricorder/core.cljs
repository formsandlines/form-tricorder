(ns form-tricorder.core
  (:require
    [formform.calc :as calc]
    [form-tricorder.events :as events]
    [form-tricorder.subs :as subs]
    [form-tricorder.effects :as effects]
    [form-tricorder.views.radix-test :refer [PopoverDemo Button]]
    [reagent.core :as r]
    [reagent.dom :as d]
    [re-frame.core :as rf]
    ; ["/components/Button/Button" :refer [Button]]
    ))

; (defn js-test
;   []
;   [:> Button])

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
   [:h1 "FORM tricorder"]
   [:p [:span {:style {:color "red"}} "Work in progress…"]]
   [re-frame-test]
   [formform-test]
   [PopoverDemo]
   [:> Button "Styled"]])

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


