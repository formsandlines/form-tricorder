(ns form-tricorder.core
  (:require
   [helix.core :refer [defnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [formform.calc :as calc]
   [formform.expr :as expr]
   [formform.io :as io]
   ["react-dom/client" :as rdom]
   ["/stitches.config" :refer (css)]))

(defnc Input
  [{:keys [set-expr]}]
  (let [[input set-input] (hooks/use-state "((a) b)")
        apply-input #(set-expr (io/read-expr input))]
    (d/div
     {:class "Input"
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

(defnc Output
  [{:keys [expr]}]
  (d/div
   {:class "Output"
    :style {:border "1px solid lightgray"
            :padding 10
            :margin "10px 0"}}
   (d/pre
    {:style {:font-family "monospace"}}
    (str expr))))

(defnc App
  []
  (let [[expr set-expr] (hooks/use-state nil)]
    (d/div
     {:class "App"
      :style {:margin "2rem 2rem"}}
     (d/h1
      {:style {:margin-bottom 10}}
      "FORM tricorder")
     ($ Input {:set-expr set-expr})
     ($ Output {:expr expr}))))


(defonce root
  (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init! []
  ; (refx/dispatch-sync [:initialize-db])
  (.render root ($ App)))



