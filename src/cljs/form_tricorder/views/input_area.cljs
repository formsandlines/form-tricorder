(ns form-tricorder.views.input-area
  (:require
    [helix.core :refer [defnc $ <>]]
    [helix.hooks :as hooks]
    [helix.dom :as d]
    [form-tricorder.utils :refer [clj->js*]]
    ["/stitches.config" :refer (css)]))


(defnc InputArea []
  (let [[input set-input] (hooks/use-state "")]
    (d/div
     {:class "InputArea"}
     (d/input
      {:value input
       :on-change (fn [e]
                    (.preventDefault e)
                    (set-input (.. e -target -value)))}))))
