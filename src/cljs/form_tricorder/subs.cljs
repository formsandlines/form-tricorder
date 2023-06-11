(ns form-tricorder.subs
  (:require
   [formform.calc :as calc]
   [formform.expr :as expr]
   [refx.alpha :as refx]))


(refx/reg-sub
 :test/subs
 (fn [db]
   (:test/answer db)))

(refx/reg-sub
 :func-id
 (fn [db]
   (:func-id db)))


(refx/reg-sub
 :expr
 (fn [db _]
   (get-in db [:input :expr] :not-found)))

(refx/reg-sub
 :varorder
 (fn [db _]
   (get-in db [:input :varorder] nil)))

(refx/reg-sub
 :varorder-permutations
 :<- [:expr]
 (fn [expr _]
   (println "computing permutations")
   (cond
     (= :not-found expr) (throw (ex-info "Expression data missing!" {}))
     :else (expr/permute-vars (expr/find-vars expr {:ordered? true})))))

(refx/reg-sub
 :value
 :<- [:expr]
 :<- [:varorder]
 (fn [[expr varorder] _]
   (println "computing value")
   (cond
     (= :not-found expr) (throw (ex-info "Expression data missing!" {}))
     (nil? varorder)     (throw (ex-info "Unknown variable ordering!" {}))
     :else (:results (expr/eval-all {:varorder varorder} expr {})))))

(refx/reg-sub
 :vmap
 :<- [:value]
 (fn [value _]
   (println "computing vmap")
   (cond
     (nil? value) (throw (ex-info "Unknown expression value!" {}))
     :else (->> value (into {}) calc/vdict->vmap))))


(comment
  
  (expr/eval-all {:varorder ['b 'a]} [['a] 'b] {})
  
  )
