(ns form-tricorder.mode-ui
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [formform.calc :as calc]
   [formform.expr :as expr]
   ; [formform.io :as io]
   [clojure.math]
   [clojure.edn :as edn]
   [form-tricorder.utils :as utils :refer [clj->js*]]))

(defnc Calc
  [{:keys [current-varorder permutations set-varorder]}]
  (when current-varorder
    (d/div
     (d/label "Variable interpretation order:")
     (d/select
      {:on-change (fn [e]
                    (set-varorder (edn/read-string (.. e -target -value))))}
      (for [varorder permutations
            :let [label (reduce #(str %1 " " %2) varorder)]]
        (d/option {:key label
                   :value varorder
                   :default-value (= varorder current-varorder)}
                  label))))))




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
