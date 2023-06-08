(ns form-tricorder.mode-ui
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   [formform.calc :as calc]
   [formform.expr :as expr]
   ; [formform.io :as io]
   [clojure.math]
   [form-tricorder.utils :as utils :refer [clj->js*]]))

(defnc Calc
  [{:keys [value]}]
  (let []
    (when value
      (d/div
       (d/label "Variable interpretation order:")
       (d/select
        {}
        (for [varorder []]
          (d/option {:value "x"})))))))




(comment

  (calc/dna->vdict (expr/op-get (expr/=>* [['a] 'b]) :dna))
  (into {} (:results (expr/eval-all [['a] 'b])))

  (->> (expr/eval-all [['a] 'b])
       :results
       (into {})
       calc/vdict->vmap)

  (rseq (mapv second (:results (expr/eval-all [['a] 'b]))))


  )
