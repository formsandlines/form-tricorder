(ns form-tricorder.core
  (:require
    [reagent.core :as r]
    [reagent.dom :as d]
    [re-frame.core :as rf]))

(defn root
  []
  [:div#root
   [:h1 "FORM tricorder"]
   [:p "Work in progress…"]])

(defn mount-root []
  (d/render [root] (.getElementById js/document "app")))

(defn ^:export init! []
  (println "Initialized!")
  (mount-root))


(comment
  
  (js/console.log (+ 1 2))
  
  )


