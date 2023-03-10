(ns form-tricorder.events
  (:require [refx.alpha :as refx]))


;; ---- Event handler -------------------------------------------

(refx/reg-event-db
  :initialize-db
  (fn [_ _]
    {:test/answer 42}))

(refx/reg-event-fx
  :test/event
  (fn [{:keys [db]} [_ {:keys [new-answer]}]]
    {:db (assoc db :test/answer new-answer)
     :fx [[:test-effect! "BOOM!"]]}))
