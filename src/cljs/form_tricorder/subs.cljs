(ns form-tricorder.subs
  (:require [refx.alpha :as refx]))


(refx/reg-sub
  :test/subs
  (fn [db]
    (:test/answer db)))

(refx/reg-sub
  :func-id
  (fn [db]
    (:func-id db)))

(refx/reg-sub
  :cache
  (fn [db]
    (:cache db)))

(refx/reg-sub
  :input
  (fn [db]
    (:input db)))
