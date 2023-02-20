(ns form-tricorder.events
  (:require
    [re-frame.core :as rf]))


;; ---- Event handler -------------------------------------------

(rf/reg-event-db
  :initialize-db
  (fn [_ _]
    {:test/answer 42}))

(rf/reg-event-fx
  :test/event
  (fn [{:keys [db]} [_ {:keys [new-answer]}]]
    {:db (assoc db :test/answer new-answer)
     :fx [[:test-effect! "BOOM!"]]}))
