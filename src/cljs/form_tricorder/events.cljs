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
      :func-id :vmap
      :modes {:calc-config nil}
      :cache {}})))

; (refx/reg-event-fx
;   :test/event
;   (fn [{:keys [db]} [_ {:keys [new-answer]}]]
;     {:db (assoc db :test/answer new-answer)
;      :fx [[:test-effect! "BOOM!"]]}))

(refx/reg-event-db
 :changed-formula
 (fn [db [_ {:keys [next-formula]}]]
   (update db :input
           (fn [{:keys [formula expr] :as m}]
             (if-not (= formula next-formula)
               (let [expr     (io/read-expr next-formula)
                     varorder (expr/find-vars expr {:ordered? true})]
                 (assoc m
                        :formula  next-formula
                        :expr     expr
                        :varorder varorder))
               m)))))

(refx/reg-event-db
 :changed-varorder
 (fn [db [_ {:keys [next-varorder]}]]
   (update db :input
           (fn [{:keys [varorder] :as m}]
             (if-not (= varorder next-varorder)
               (assoc m :varorder next-varorder)
               m)))))

(refx/reg-event-db
 :set-func-id
 (fn [db [_ {:keys [next-id]}]]
   (assoc db :func-id (if (keyword? next-id) next-id (keyword next-id)))))

(refx/reg-event-db
 :update-cache
 (fn [db [_ {:keys [update-fn]}]]
   (update db :cache update-fn)))
