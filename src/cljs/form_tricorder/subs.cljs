(ns form-tricorder.subs
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  :test/subs
  (fn [db]
    (:test/answer db)))

