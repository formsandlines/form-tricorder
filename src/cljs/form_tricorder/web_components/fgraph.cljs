(ns form-tricorder.web-components.fgraph
  (:require
   ;; [garden.core :refer [css]]
   ;; [garden.selectors :as s]
   [zero.core :as z]
   [zero.config :as zc]
   [zero.component]
   [formform.io :as io]
   ;; [form-tricorder.utils :as utils :refer [clj->js*]]
   ["/form-svg$default" :as form-svg]))


(defn expr->json
  [expr]
  (clj->js (io/uniform-expr {:legacy? true} expr)))

;; (def styles
;;   (css []))

;; (js/console.log styles)

;; (def stylesheet
;;   (doto (js/CSSStyleSheet.)
;;     (.replaceSync styles)))

(defn view
  [{:keys [expr type theme]}]
  [:root>
   {;; ::z/css stylesheet
    ::z/on {:render
            (fn [e]
              (let [shadow (.. e -target)
                    el (.querySelector shadow ".Output")
                    json (expr->json expr)]
                (form-svg type json
                          (clj->js
                           {:parent el
                            :styleTheme theme}))))}}
   [:div
    {::z/class "Output"}]])

(zc/reg-components
 :ff/fgraph
 {:props {:expr  :field
          :type  :default
          :theme :default}
  :view view})
