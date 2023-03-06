(ns form-tricorder.views.output-area
  (:require
    [reagent.core :as r]
    [reagent.dom :as d]
    [form-tricorder.components.splitview :refer [Root Divider Handle Pane]]
    [form-tricorder.utils :refer [clj->js*]]
    ["/stitches.config" :refer (styled css)]))

(def splitview-root-style
  (-> {:width "100%"
       :height "400px"
       :backgroundColor "gainsboro"}
      clj->js*
      css))

(def splitview-divider-style
  (let [diam "2px"]
    (-> {:backgroundColor "blue"
         "&[data-orientation=horizontal]"
         {:width "100%"
          :height diam}
         "&[data-orientation=vertical]"
         {:height "100%"
          :width diam}}
        clj->js*
        css)))

(def splitview-handle-style
  (let [diam   13
        len    40
        dshift (- (int (/ diam 2)))
        lshift (- (int (/ len 2)))]
    (-> {:backgroundColor "violet"
         "&[data-orientation=horizontal]"
         {:left "50%"
          :width len
          :height diam
          :margin-left lshift
          :margin-top dshift}
         "&[data-orientation=vertical]"
         {:top "50%"
          :width diam
          :height len
          :margin-left dshift
          :margin-top lshift}}
        clj->js*
        css)))

(def splitview-pane-style
  (-> {:border "1px solid black"}
      clj->js*
      css))


(defn OutputArea
  [{:keys []}]
  [Root {:props {:id "OutputArea"
                 :class (splitview-root-style)}
         :horizontal? false}
   [Divider {:props {:id "OutputDivider"
                     :class (splitview-divider-style)}}
    [Handle {:props {:id "OutputDividerHandle"
                     :class (splitview-handle-style)}}]]
   [Pane {:props {:id "Output_a"
                  :class (splitview-pane-style)}}
    "A"]
   [Pane {:props {:id "Output_b"
                  :class (splitview-pane-style)}}
    "B"]])


