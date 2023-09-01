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
   [form-tricorder.utils :refer [log style> css> dark-theme light-theme]]
   [form-tricorder.components.header :refer [Header]]
   [form-tricorder.components.formula-input :refer [FormulaInput]]
   [form-tricorder.components.function-menu :refer [FunctionMenu]]
   [form-tricorder.components.output-area :refer [OutputArea]]
   ["react-dom/client" :as rdom]))

(def body-styles
  (css> {:background-color "$colors$outer_bg"}))

(def styles
  (css> {:display "flex"
         :height "100vh"
         :flex-direction "column"
         :padding "0.6rem"
         :gap "0.4rem"

         :font-family "$base"
         :font-weight "$normal"
         :font-size "$base"
         :color "$colors$outer_fg"
         "& a"
         {
          :color "$colors$outer_m100"
          "&:hover"
          {
           :text-decoration "underline"
           ; :color "$colors$outer_m200"
           }}}))

(def item-styles
  (css> {"&:last-child"
         {:flex "1"
          }}))

(defnc App
  []
  (let [appearance        (refx/use-sub [:appearance])
        views             (refx/use-sub [:views])
        split-orientation (refx/use-sub [:split-orientation])]
    (hooks/use-effect
     :once
     (.add js/document.body.classList body-styles))
    (hooks/use-effect
     [appearance]
     (if (= appearance :dark)
       (do (.add js/document.body.classList dark-theme)
           (.remove js/document.body.classList light-theme))
       (do (.add js/document.body.classList light-theme)
           (.remove js/document.body.classList dark-theme))))
    (d/div
     {:class (str "App " (styles))}
     (d/div
      {:class (item-styles)}
      ($ Header {:view-split? (> (count views) 1)}))
     (d/div
      {:class (item-styles)}
      ($ FormulaInput {:apply-input
                       #(refx/dispatch [:changed-formula
                                        {:next-formula %}])}))
     (d/div
      {:class (item-styles)}
      ($ FunctionMenu {:handle-click
                       (fn [func-id alt-view?]
                         (let [view-index (if alt-view? 1 0)]
                           (do
                             (when alt-view? (refx/dispatch [:views/split]))
                             (refx/dispatch [:views/set-func-id
                                             {:next-id    func-id
                                              :view-index view-index}]))))}))
     (d/div
      {:class (item-styles)}
      ($ OutputArea {:views views
                     :split-orientation split-orientation})))))

(defonce root
  (rdom/createRoot (js/document.getElementById "root")))

(defn ^:export init! []
  (refx/dispatch-sync [:initialize-db])
  (.render root ($ App)))


