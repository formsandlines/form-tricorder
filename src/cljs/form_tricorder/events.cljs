(ns form-tricorder.events
  (:require
    [refx.alpha :as refx]
    [formform.expr :as expr]
    [formform.io :as io]))


;; ---- Event handler -------------------------------------------

(refx/reg-event-db
  :initialize-db
  (fn [_ _]
    {:input {:formula nil
             :expr [['a :M]
                    (expr/seq-re
                      :<r
                      [:- 'a ['b]]
                      (expr/seq-re :<..r :M 'x)
                      :U)
                    'b]}
     :func-id :vmap
     :modes {:calc-config nil} 
     :cache {}}))

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
                (assoc m
                       :formula next-formula
                       :expr (io/read-expr next-formula))
                m)))))

(refx/reg-event-db
  :set-func-id
  (fn [db [_ {:keys [next-id]}]]
    (assoc db :func-id (if (keyword? next-id) next-id (keyword next-id)))))

(refx/reg-event-db
  :update-cache
  (fn [db [_ {:keys [update-fn]}]]
    (update db :cache update-fn)))
