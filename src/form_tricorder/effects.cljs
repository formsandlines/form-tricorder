(ns form-tricorder.effects
  (:require
    [re-frame.core :as rf]))

(rf/reg-fx
 :test-effect!
 (fn [msg]
   (js/console.log msg)))

