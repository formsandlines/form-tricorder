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
 :views
 (fn [db _]
   (:views db)))

(refx/reg-sub
 :view
 (fn [db [_ index]]
   ;; {:pre [(< index (count (:views db)))]}
   ;; fails silently (sometimes views update doesn’t catch on after remove)
   (get (:views db) index)))

(refx/reg-sub
 :frame
 (fn [db _]
   (:frame db)))

(refx/reg-sub
 :appearance
 (fn [db _]
   (get-in db [:theme :appearance])))

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

;; ? maybe replace with :dna sub
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
 :dna
 :<- [:expr-data]
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
 :vmap
 :<- [:value]
 (fn [value _]
   (println "computing vmap")
   (cond
     (nil? value) (throw (ex-info "Unknown expression value!" {}))
     :else (->> value (into {}) calc/vdict->vmap))))


(comment
  
  (expr/eval-all {:varorder ['b 'a]} [['a] 'b] {})
  (expr/op-get (expr/eval->expr-all {:varorder ['b 'a]} [['a] 'b] {}) :dna)
  
  )
