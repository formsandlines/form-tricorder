(ns form-tricorder.subs
  (:require
   [formform.calc :as calc]
   [formform.expr :as expr]
   [refx.alpha :as refx]))


;; Conventions for clarity:
;; - qualify keys with by the app-db entry the sub depends on
;; - prepend `->` to sub key when the sub computes derived data

(refx/reg-sub
 :views/->view
 (fn [db [_ index]]
   ;; {:pre [(< index (count (:views db)))]}
   ;; fails silently (sometimes views update doesn’t catch on after remove)
   (get (:views db) index)))


(refx/reg-sub
 :frame/orientation
 (fn [db _]
   (get-in db [:frame :orientation])))

(refx/reg-sub
 :frame/windows
 (fn [db _]
   (get-in db [:frame :windows])))


(refx/reg-sub
 :theme/appearance
 (fn [db _]
   (get-in db [:theme :appearance])))


(refx/reg-sub
 :cache/retrieve
 (fn [db [_ key]]
   (get-in db [:cache key :val])))


(refx/reg-sub
 :input/expr
 (fn [db _]
   (get-in db [:input :expr] :not-found)))

(refx/reg-sub
 :input/varorder
 (fn [db _]
   (get-in db [:input :varorder] nil)))

(refx/reg-sub
 :input/->expr-data
 (fn [db _]
   (let [input (:input db)]
     [(get input :expr :not-found)
      (get input :varorder nil)])))

(refx/reg-sub
 :input/->sorted-varorder
 :<- [:input/->expr-data]
 (fn [[_ varorder] _]
   (println "sorting: " varorder)
   (sort varorder)))

(refx/reg-sub
 :input/->varorder-permutations
 :<- [:input/->sorted-varorder]
 (fn [sorted-varorder _]
   (println "computing permutations")
   (let [permutations (expr/permute-vars sorted-varorder)]
     permutations)))


;; ? maybe replace with :dna sub
(refx/reg-sub
 :input/->value
 :<- [:input/->expr-data]
 (fn [[expr varorder] _]
   (when (= :not-found expr)
     (throw (ex-info "Expression data missing!" {})))
   (when (nil? varorder)
     (throw (ex-info "Unknown variable ordering!" {})))
   (let [value (expr/eval-all {:varorder varorder} expr {})]
     (println "computing value")
     (:results value))))

(refx/reg-sub
 :input/->dna
 :<- [:input/->expr-data]
 (fn [[expr varorder] _]
   (when (= :not-found expr)
     (throw (ex-info "Expression data missing!" {})))
   (when (nil? varorder)
     (throw (ex-info "Unknown variable ordering!" {})))
   (let [formDNA (if (expr/formDNA? expr)
                   expr
                   (expr/eval->expr-all {:varorder varorder} expr {}))]
     (println "computing value")
     (expr/op-get formDNA :dna))))


(refx/reg-sub
 :input/->vmap
 :<- [:input/->value]
 (fn [value _]
   (println "computing vmap")
   (cond
     (nil? value) (throw (ex-info "Unknown expression value!" {}))
     :else (->> value (into {}) calc/vdict->vmap))))


(refx/reg-sub
 :input/->selfi-rules-fn
 :<- [:input/->dna]
 (fn [dna _]
   (println "computing ca rules function")
   (cond
     (nil? dna) (throw (ex-info "Invalid formDNA" {}))
     :else (partial calc/dna-get dna))))

(refx/reg-sub
 :input/->selfi-umwelt
 :<- [:input/varorder]
 (fn [varorder _]
   (println "computing ca umwelt")
   (cond
     (nil? varorder) (throw (ex-info "Invalid variable ordering" {}))
     :else (condp = (count varorder)
             1 :e
             2 :lr
             3 :ler
             4 :-lr+
             5 :-ler+
             (throw (ex-info "Invalid variable count" {}))))))
