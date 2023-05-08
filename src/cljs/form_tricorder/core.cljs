(ns form-tricorder.core
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [formform.calc :as calc]
   [formform.expr :as expr]
   [formform.io :as io]
   [form-tricorder.model :as model :refer [modes]]
   [form-tricorder.functions :as func]
   [form-tricorder.utils :refer [log]]
   ["react-dom/client" :as rdom]
   ["/stitches.config" :refer (css)]))


(defnc FormulaInput
  [{:keys [set-expr]}]
  (let [[input set-input] (hooks/use-state "")
        apply-input #(set-expr (io/read-expr input))]
    (d/div
     {:class "FormulaInput"
      :style {:display "flex"}}
     (d/input
      {:value input
       :on-change (fn [e] (do (.preventDefault e)
                              (set-input (.. e -target -value))))
       :on-key-press (fn [e] (when (= "Enter" (.-key e))
                               (apply-input)))
       :style {:flex "1 1 auto"}})
     (d/button
      {:on-click (fn [e] (apply-input))}
      "apply"))))

(defnc FunctionMenu
  [{:keys [set-value value]}]
  (let [handle-change (fn [e] (set-value (.. e -target -value)))
        checked? (fn [s] (if (= value s) true ""))]
    (d/div
     {:class "FunctionMenu"
      :style {:display "flex"
              :gap 10}}
     (for [{:keys [id label items]} modes]
       (d/fieldset
        {:style {:flex (if (= id "more") "none" "1 1 0%")
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
  [{:keys [expr func-id]}]
  (let [Output (func/gen-component (keyword func-id) expr)]
    (d/div
     {:class "OutputArea"
      :style {:border "1px solid lightgray"
              :padding 10
              :margin "10px 0"}}
     ($ Output {}))))

(defnc App
  []
  (let [[expr set-expr] (hooks/use-state
                         ;; dummy expression
                         [['a :M]
                          (expr/seq-re :<r
                                       [:- 'a ['b]]
                                       (expr/seq-re :<..r :M 'x)
                                       :U)
                          'b])
        [func-id set-func-id] (hooks/use-state "hooks")]
    (d/div
     {:class "App"
      :style {:margin "2rem 2rem"}}
     (d/h1
      {:style {:margin-bottom 10}}
      "FORM tricorder")
     ($ FormulaInput {:set-expr set-expr})
     ($ FunctionMenu {:set-value set-func-id
                      :value func-id})
     ($ OutputArea {:expr expr
                    :func-id func-id}))))


(defonce root
  (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init! []
  ; (refx/dispatch-sync [:initialize-db])
  (.render root ($ App)))


(comment)
  
  
