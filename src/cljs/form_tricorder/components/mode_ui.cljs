(ns form-tricorder.components.mode-ui
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [formform.calc :as calc]
   [formform.expr :as expr]
   [clojure.math]
   [clojure.edn :as edn]
   [form-tricorder.utils :as utils :refer [style> css> pp-var]]
   [form-tricorder.components.varorder-select :refer [VarorderSelect]]))

(def styles
  (css> {:margin-bottom "$6"}))

(defnc Calc
  [{:keys [current-varorder set-varorder debug-origin]}]
  ; TODO: remove debug-origin
  ; (println "Debug origin: " debug-origin)
  ; (println "Current varorder: " current-varorder)
  ; (println "---")
  (let [sorted-varorder (sort current-varorder)
        permutations (refx/use-sub
                      [:varorder-permutations]
                       ; [:varorder-permutations sorted-varorder]
                      )
        pp-varorder #(str %1 " " (pp-var %2))]
    (when current-varorder
      (d/div
       {:class (styles)}
       ; (d/label "Variable interpretation order:")
       ($ VarorderSelect
          {:current-varorder current-varorder
           :permutations permutations
           :display (fn [varorder] (reduce pp-varorder "" varorder))
           :value-change-handler (fn [v] (set-varorder v))})
       #_(d/select
          {:value current-varorder
         ; :default-value current-varorder
           :on-change (fn [e]
                        (set-varorder (edn/read-string (.. e -target -value))))}
          (for [varorder permutations
                :let [label (reduce pp-varorder "" varorder)]]
            (d/option {:key label
                       :value varorder}
                      label)))))))




(comment

  (calc/dna->vdict (expr/op-get (expr/=>* [['a] 'b]) :dna))
  (into {} (:results (expr/eval-all [['a] 'b])))

  (->> (expr/eval-all [['a] 'b])
       :results
       (into {})
       calc/vdict->vmap)

  (rseq (mapv second (:results (expr/eval-all [['a] 'b]))))

  (reduce #(str %1 " " %2) ["a" "b" "c"])

  )
