(ns form-tricorder.core
  (:require
   [helix.core :refer [defnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [formform.calc :as calc]
   [formform.expr :as expr]
   [formform.io :as io]
   [form-tricorder.utils :refer [log]]
   ["react-dom/client" :as rdom]
   ["/stitches.config" :refer (css)]))

(defnc FormulaInput
  [{:keys [set-expr]}]
  (let [[input set-input] (hooks/use-state "((a) b)")
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
        checked? (fn [s] (if (= value s) true ""))
        func-data [{:func-id "edn"
                    :label "EDN data"}
                   {:func-id "vtable"
                    :label "Value table"}]]
    (d/div
      {:class "FunctionMenu"}
      (for [{:keys [func-id label]} func-data]
        (d/label
          (d/input
            {:type "radio"
             :name "func"
             :value func-id
             :checked (checked? func-id)
             :on-change handle-change})
          label)))))

(defnc OutputArea
  [{:keys [expr func]}]
  (let [apply-func (fn [func-id expr]
                     (case func-id
                       "edn"    (str expr)
                       "vtable" (str (:results (expr/eval-all expr)))
                       (throw (ex-info "Unknown function" {}))))]
    (d/div
      {:class "OutputArea"
       :style {:border "1px solid lightgray"
               :padding 10
               :margin "10px 0"}}
      (d/pre
        {:style {:font-family "monospace"}}
        func ": "
        (str (apply-func func expr))))))

(defnc App
  []
  (let [[expr set-expr] (hooks/use-state nil)
        [func set-func] (hooks/use-state "edn")]
    (d/div
     {:class "App"
      :style {:margin "2rem 2rem"}}
     (d/h1
      {:style {:margin-bottom 10}}
      "FORM tricorder")
     ($ FormulaInput {:set-expr set-expr})
     ($ FunctionMenu {:set-value set-func
                      :value func})
     ($ OutputArea {:expr expr
                    :func func}))))


(defonce root
  (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init! []
  ; (refx/dispatch-sync [:initialize-db])
  (.render root ($ App)))



