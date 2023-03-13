(ns form-tricorder.components.input-area
  (:require
    [helix.core :refer [defnc $ <>]]
    [helix.hooks :as hooks]
    [helix.dom :as d]
    [form-tricorder.utils :refer [clj->js*]]
    ["/stitches.config" :refer (css)]))


(defnc InputArea [{:keys [submit-handler]}]
  (let [[text set-text] (hooks/use-state "")]
    (d/div
     {:class "InputArea"}
     (d/input
      {:value text
       :on-change (fn [e]
                    (.preventDefault e)
                    (set-text (.. e -target -value)))})
     (d/button
       {:on-click (fn [_] (submit-handler text))}
       "submit"))))
