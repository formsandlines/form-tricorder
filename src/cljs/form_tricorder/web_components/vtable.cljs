(ns form-tricorder.web-components.vtable
  (:require
   [clojure.math]
   [garden.core :refer [css]]
   [garden.selectors :as s]
   ;; ["/stitches.config" :refer (getCssText)]
   [form-tricorder.utils :as utils :refer [pp-val pp-var css>]]
   [zero.core :as z]
   [zero.config :as zc]
   [zero.component]))

;; (css> {:font-family "$mono"
;;                    :font-size "$1"
;;                    "th"
;;                    {:font-weight "$medium"
;;                     :border-top "1px solid $inner_fg"
;;                     :border-bottom "1px solid $inner_fg"}
;;                    "th, td"
;;                    {:text-align "left"
;;                     :padding "0.4rem 0.2rem"}
;;                    "th:first-child, td:first-child"
;;                    {:padding-left "0"}
;;                    "th:last-child, td:last-child"
;;                    {:text-align "right"
;;                     :padding-right "0"}
;;                    "tr:hover td"
;;                    {:background-color "$inner_hl"}
;;                    "td"
;;                    {:border-top "1px solid $inner_n200"}})

;; ? split to multiple columns

;; (println "\n\n"
;;          (css {:pretty-print? true
;;                :vendors ["webkit"]
;;                :auto-prefix #{:border-radius}}
;;               [[:p
;;                 [:&:hover
;;                  {:font-weight 'normal
;;                   :border-radius "1px"}
;;                  [:a {:color "red"}]]]]))

;; (println (css [[(s/root) {:--text-color "red"}]
;;                [:p {:color "var(--text-color)"}]]))

;; (println (style
;;           {:color "green"
;;            :text-align "center"}
;;           {:color "red"
;;            :text-align "right"}
;;           {:color "blue"}))

(def styles
  (css [[:host
         {:font-family "var(--fonts-mono)"
          :font-size "var(--fontSizes-1)"}]
        ["th"
         {:font-weight "var(--fontWeights-medium)"
          :border-top "1px solid var(--colors-inner_fg)"
          :border-bottom "1px solid var(--colors-inner_fg)"}]
        ["th, td"
         {:text-align "left"
          :padding "0.4rem 0.2rem"}]
        ["th:first-child, td:first-child"
         {:padding-left "0"}]
        ["th:last-child, td:last-child"
         {:text-align "right"
          :padding-right "0"}]
        ["tr:hover td"
         {:background-color "var(--colors-inner_hl)"}]
        ["td"
         {:border-top "1px solid var(--colors-inner_n200)"}]]))

;; (js/console.log styles)

(def stylesheet
  (doto (js/CSSStyleSheet.)
    (.replaceSync styles)))

(defn view
  [{:keys [results varorder]}]
  (let [results (map (fn [[intpr res]]
                       {:id (random-uuid)
                        :intpr intpr
                        :result res})
                     results)]
    [:root>
     {::z/css stylesheet}
     [:table
      [:thead
       [:tr
        (for [x varorder
              :let [s (str x)]]
          [:th {::z/key s}
           (pp-var s)])
        [:th "Result"]]]
      [:tbody
       (for [{:keys [id intpr result]} results]
         [:tr
          {::z/key id}
          (for [[i v] (map-indexed vector intpr)]
            [:td {::z/key (str id "-" i)}
             (pp-val v) "\u00a0"])
          [:td (pp-val result)]])]]]))

(zc/reg-components
 :ff/vtable
 {:props {:results  :field
          :varorder :default}
  :view view
  :inherit-doc-css? true})
