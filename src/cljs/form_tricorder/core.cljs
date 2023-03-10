(ns form-tricorder.core
  (:require
    [formform.calc :as calc]
    [form-tricorder.events :as events]
    [form-tricorder.subs :as subs]
    [form-tricorder.effects :as effects]
    [form-tricorder.views.app-toolbar :refer [AppToolbar]]
    [form-tricorder.views.input-area :refer [InputArea]]
    [form-tricorder.views.function-menubar :refer [FunctionMenubar]]
    [form-tricorder.views.output-area :refer [OutputArea]]
    [refx.alpha :as refx]
    [helix.core :refer [defnc $ <>]]
    [helix.hooks :as hooks]
    [helix.dom :as d]
    ["react-dom/client" :as rdom]
    ["/stitches.config" :refer (css)]))


(defnc Test-views
  []
  (let [[views set-views] (hooks/use-state
                            [{:mode "a" :func "a" :active true}
                             {:mode "c" :func "b" :active true}])
        style (-> {:backgroundColor "darkgray"}
                  clj->js
                  css)]
    (d/div 
      {:class (style)}
      "Test ich bin"
      ($ AppToolbar 
         {:views     views
          :set-views set-views})
      ($ InputArea)
      ($ FunctionMenubar 
         {:views     views
          :set-views set-views})
      ($ OutputArea 
         {:views     views
          :set-views set-views}))))

(defnc Test-refx
  []
  (let [answer (refx/use-sub [:test/subs])]
    (<>
      (d/p "The answer is: " answer)
      (d/button
        {:on-click (fn [e] (refx/dispatch
                             [:test/event 
                              {:new-answer "within yourself"}]))}
        "Click me!"))))

(defnc Test-formform
  []
  (<>
    (d/p "Testing formform.calc:")
    (d/pre (str (calc/rel [:N :U :I :M :N :N :I :I :N :U :N :U :N :N :N :N :N :U :I :M :N :U :I :M :N :U :N :U :N :U :N :U :N :U :I :M :N :N :I :I :N :U :I :M :N :N :I :I :N :U :I :M :N :U :I :M :N :U :I :M :N :U :I :M]
                          [:M :I :U :N :I :M :N :U :U :N :M :I :N :U :I :M])))))

(defnc App
  []
  (d/div 
    {:class "App"}
    ; ($ Test-refx)
    ; ($ Test-formform)
    ($ Test-views)))

(defonce root
  (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init! []
  (refx/dispatch-sync [:initialize-db])
  (.render root ($ App)))



