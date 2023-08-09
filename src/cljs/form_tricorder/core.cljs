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
   [form-tricorder.functions :as func]
   [form-tricorder.utils :refer [log clj->js*]]
   [form-tricorder.components.header :refer [Header]]
   [form-tricorder.components.formula-input :refer [FormulaInput]]
   [form-tricorder.components.function-menu :refer [FunctionMenu]]
   [form-tricorder.components.output-area :refer [OutputArea]]
   ["react-dom/client" :as rdom]))


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
  
  
