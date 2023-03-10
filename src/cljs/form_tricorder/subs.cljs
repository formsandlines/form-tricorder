(ns form-tricorder.subs
  (:require [refx.alpha :as refx]))


(refx/reg-sub
  :test/subs
  (fn [db]
    (:test/answer db)))

