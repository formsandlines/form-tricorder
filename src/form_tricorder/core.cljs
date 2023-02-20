(ns form-tricorder.core
  (:require
    [formform.calc :as calc]
    [reagent.core :as r]
    [reagent.dom :as d]
    [re-frame.core :as rf]))

(defn root
  []
  [:div#root
   [:h1 "FORM tricorder"]
   [:p [:span {:style {:color "red"}} "Work in progress…"]
    [:br] "Testing formform.calc:"]
   [:pre (str (calc/rel [:N :U :I :M :N :N :I :I :N :U :N :U :N :N :N :N :N :U :I :M :N :U :I :M :N :U :N :U :N :U :N :U :N :U :I :M :N :N :I :I :N :U :I :M :N :N :I :I :N :U :I :M :N :U :I :M :N :U :I :M :N :U :I :M]
                   [:M :I :U :N :I :M :N :U :U :N :M :I :N :U :I :M]))]])

(defn mount-root []
  (d/render [root] (.getElementById js/document "app")))

(defn ^:export init! []
  (println "Initialized!")
  (mount-root))


(comment
  (js/console.log (+ 1 2))
  
  )


