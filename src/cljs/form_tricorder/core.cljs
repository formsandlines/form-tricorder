(ns form-tricorder.core
  (:require
   [helix.core :refer [defnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   ["react-dom/client" :as rdom]
   ["/stitches.config" :refer (css)]))


(defnc App
  []
  (d/h1 "FORM tricorder"))


(defonce root
  (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init! []
  ; (refx/dispatch-sync [:initialize-db])
  (.render root ($ App)))



