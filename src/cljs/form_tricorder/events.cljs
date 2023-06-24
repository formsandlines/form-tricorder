(ns form-tricorder.events
  (:require
    [refx.alpha :as refx]
    [formform.expr :as expr]
    [formform.io :as io]))


;; ---- Event handler -------------------------------------------

(refx/reg-event-db
  :initialize-db
  (fn [_ _]
    (let [fml  "(a :M) {@ a (b), {..@ :M, x}, :U} b"
          expr (io/read-expr fml)
          varorder (expr/find-vars expr {:ordered? true})]
      {:input {:formula fml
               :expr expr
               :varorder varorder}
       :func-id :vtable
       :modes {:calc-config nil}})))

(refx/reg-event-db
 :changed-formula
 (fn [db [_ {:keys [next-formula]}]]
   (-> db
       (update :input
               (fn [{:keys [formula expr] :as m}]
                 (if-not (= formula next-formula)
                   (let [next-expr (io/read-expr next-formula)
                         next-varorder
                         (let [vars (expr/find-vars next-expr {:ordered? true})
                               current-varorder (get-in db [:input :varorder])]
                           (if (= (sort current-varorder) vars)
                             current-varorder
                             vars))]
                     (assoc m
                            :formula  next-formula
                            :expr     next-expr
                            :varorder next-varorder))
                   m))))))

(refx/reg-event-db
 :changed-varorder
 (fn [db [_ {:keys [next-varorder]}]]
   (update db :input
           #(if-not (= (:varorder %) next-varorder)
              (assoc % :varorder next-varorder)
              %))))

(refx/reg-event-db
 :set-func-id
 (fn [db [_ {:keys [next-id]}]]
   (assoc db :func-id (if (keyword? next-id) next-id (keyword next-id)))))

(refx/reg-event-db
 :update-cache
 (fn [db [_ {:keys [update-fn]}]]
   (update db :cache update-fn)))

