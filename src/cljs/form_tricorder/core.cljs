(ns form-tricorder.core
  (:require
    [refx.alpha :as refx]
    [helix.core :refer [defnc fnc $ <> provider]]
    [helix.hooks :as hooks]
    [helix.dom :as d]
    [formform.calc :as calc]
    [formform.expr :as expr]
    [formform.io :as io]
    [form-tricorder.events :as events]
    [form-tricorder.subs :as subs]
    [form-tricorder.effects :as effects]
    [form-tricorder.model :as model :refer [modes]]
    [form-tricorder.functions :as func]
    [form-tricorder.utils :refer [log]]
    ["react-dom/client" :as rdom]
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
  [{:keys [handle-change value]}]
  (let [checked? (fn [s] (if (= value s) true ""))]
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
          (d/label
           {:key id
            :style {:display "block"}}
           (d/input
            {:type "radio"
             :name "func"
             :value id
             :checked (checked? id)
             :on-change handle-change})
           label)))))))

(defnc OutputArea
  [{:keys [func-id]}]
  ;; ? cache component in state
  (let [mode-id (model/func->mode func-id)]
    (d/div
     {:class "OutputArea"
      :style {:border "1px solid lightgray"
              :padding 10
              :margin "10px 0"}}
     (d/p mode-id)
     (func/gen-component func-id {}))))

(defnc App
  []
  (let [func-id (refx/use-sub [:func-id])]
    (d/div
      {:class "App"
       :style {:margin "2rem 2rem"}}
      (d/h1
        {:style {:margin-bottom 10}}
        "FORM tricorder")
      ($ FormulaInput {:apply-input #(refx/dispatch
                                       [:changed-formula
                                        {:next-formula %}])})
      ($ FunctionMenu {:handle-change
                       (fn [e] (refx/dispatch
                                 [:set-func-id
                                  {:next-id (.. e -target -value)}]))
                       :value (when func-id (name func-id))})
      ($ OutputArea {:func-id func-id}))))

(defonce root
  (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init! []
  (refx/dispatch-sync [:initialize-db])
  (.render root ($ App)))


(comment
  (let [expr 'a]
    (meta (-> expr
              expr/=>*
              (expr/op-get :dna)
              calc/dna->vdict
              calc/vdict->vmap))))
  
  
