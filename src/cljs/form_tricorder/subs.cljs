(ns form-tricorder.subs
  (:require
   [formform.calc :as calc]
   [formform.expr :as expr]
   [refx.alpha :as refx]))


(refx/reg-sub
 :test/subs
 (fn [db _]
   (:test/answer db)))

(refx/reg-sub
 :func-id
 (fn [db _]
   (:func-id db)))

(refx/reg-sub
 :views
 (fn [db _]
   (:views db)))

; (refx/reg-sub
;  :cache
;  (fn [db]
;    (:cache db)))


(refx/reg-sub
 :expr
 (fn [db _]
   (get-in db [:input :expr] :not-found)))

(refx/reg-sub
 :varorder
 (fn [db _]
   (get-in db [:input :varorder] nil)))

(refx/reg-sub
 :expr-data
 (fn [db _]
   (let [input (:input db)]
     [(get input :expr :not-found)
      (get input :varorder nil)])))


;; Computations

; (refx/reg-sub
;  :active-views
;  :<- [:views]
;  (fn [views _]
;    (filter #(:active? %) views)))

(refx/reg-sub
 :sorted-varorder
 :<- [:expr-data]
 (fn [[_ varorder] _]
   (println "sorting: " varorder)
   (sort varorder)))

(refx/reg-sub
 :varorder-permutations
 :<- [:sorted-varorder]
 (fn [sorted-varorder _]
   (println "computing permutations")
   (let [permutations (expr/permute-vars sorted-varorder)]
     permutations)))

(refx/reg-sub
 :value
 :<- [:expr-data]
 (fn [[expr varorder] _]
   (when (= :not-found expr)
     (throw (ex-info "Expression data missing!" {})))
   (when (nil? varorder)
     (throw (ex-info "Unknown variable ordering!" {})))
   (let [value (expr/eval-all {:varorder varorder} expr {})]
     (println "computing value")
     (:results value))))

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
