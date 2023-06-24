(ns form-tricorder.mode-ui
  (:require
   [refx.alpha :as refx]
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
  [{:keys [current-varorder set-varorder]}]
  (println "curr. varorder: " current-varorder)
  (let [sorted-varorder (sort current-varorder)
        permutations (refx/use-sub
                      [:varorder-permutations]
                       ; [:varorder-permutations sorted-varorder]
                      )
        ; permutations    (hooks/use-memo
        ;                   (utils/use-custom-compare-memoize
        ;                     [sorted-varorder] =)
        ;                   (do (println "calc permutations")
        ;                       (println sorted-varorder)
        ;                       (expr/permute-vars sorted-varorder)))
        ]
    (when current-varorder
      (d/div
       (d/label "Variable interpretation order:")
       (d/select
        {:default-value current-varorder
         :on-change (fn [e]
                      (set-varorder (edn/read-string (.. e -target -value))))}
        (for [varorder permutations
              :let [label (reduce #(str %1 " " %2) varorder)]]
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
