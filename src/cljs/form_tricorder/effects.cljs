(ns form-tricorder.effects
  (:require
   [re-frame.core :as rf]))

(rf/reg-fx
 :set-search-params
 (fn [kvs]
   (let [s (.. js/window -location -search)
         search-params ^js (new js/URLSearchParams s)]
     (doseq [[k v] kvs]
       (.set search-params k v))
     (let [path (str (.-pathname js/location) "?" (.toString search-params))]
       ;; (js/console.log path)
       ;; (js/console.log search-params)
       (.. js/window -history (replaceState (js-obj) "" path))))))


(defn copy-to-clipboard
  [text report-copy-status]
  (.then (.. js/navigator -clipboard (writeText text))
         (fn [] (report-copy-status [true text]))
         (fn [err] (report-copy-status [false err]))))

(rf/reg-fx
 :copy-url
 (fn [report-copy-status]
   (let [url (.. js/window -location -href)]
     (copy-to-clipboard url report-copy-status))))

