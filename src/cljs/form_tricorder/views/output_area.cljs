(ns form-tricorder.views.output-area
  (:require
   [form-tricorder.utils :refer [clj->js*]]
   [form-tricorder.views.function-tabs :refer [FunctionTabs]]
   ["@spectrum-web-components/split-view/sp-split-view.js"]
   ["@spectrum-web-components/theme/sp-theme.js"]
   ["@spectrum-web-components/theme/src/themes.js"]
   ["/stitches.config" :refer (css globalCss)]))


(def view-style
  (-> {:height "200px"
       :width "100%"}
      clj->js*
      css))

; (def splitter-style
;   (-> {; "*" {:color "red"}
;        ":host #gripper" {:border "3px solid yellow"
;                          :color "red"}}
;       clj->js*
;       globalCss))

(defn OutputPane [{:keys [id style views* set-views]}]
  (let [value-change-handler (fn [v]
                               (set-views (update @views* id
                                                  assoc :func v)))]
    [:div.OutputPane
     {:style style}
     [FunctionTabs
      {:view (@views* id)
       :value-change-handler value-change-handler}]]))

(defn OutputArea [{:keys [views* set-views]}]
  (let [active-views (filter :active @views*)]
    [:div.OutputArea
     {:style {:height "400px"}}
     (condp == (count active-views)
       1 [OutputPane {:id        0
                      :style     {:backgroundColor "orange"}
                      :views*    views*
                      :set-views set-views}]
       2 [:<>
          ; {:class (str (view-style))}
          [:> "sp-theme"
           {"scale" "medium"
            "color" "dark"
            "style" {}
            }
           [:> "sp-split-view"
            {"style" {:height "200px"
                      :width "100%"}
             "label" "Output splitview"
             "resizable" true
             ; "vertical" true
             "primary-min" "50"
             "secondary-min" "50" }
            [OutputPane {:id        0
                         :views*    views*
                         :set-views set-views}]
            [OutputPane {:id        1
                         :views*    views*
                         :set-views set-views}]]]]
       (assert "Must have at least one active view."))]))




