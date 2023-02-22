(ns form-tricorder.views.function-radio-group
  (:require
    [reagent.core :as r]
    [reagent.dom :as d]
    [form-tricorder.utils :refer [clj->js*]]
    ["@radix-ui/react-radio-group" :as RadioGroup]
    ["/stitches.config" :refer (styled css)]))

(def Root
  (styled (.-Root RadioGroup)
          (clj->js*
            {:display "flex"
             :flexDirection "column"
             :gap "2px"
             :padding "6px"
             :boxSizing "border-box"})))

(def Item
  (styled (.-Item RadioGroup)
          (clj->js*
           {:flex "flex-none"
            :outline "none"
            :border "none"
            :borderRadius "4px"
            :width "30px"
            :height "30px"
            "&[data-state=checked]" {:backgroundColor "lightgray"}
            :variants
            {:type {:a {}
                    :b {}
                    :c {}}
             :subtype {:a {}
                       :b {}
                       :c {}}}
            :compoundVariants
            [{:type "a"
              :subtype "a"
              :css {:backgroundColor "teal"}}
             {:type "a"
              :subtype "b"
              :css {:backgroundColor "green"}}
             {:type "a"
              :subtype "c"
              :css {:backgroundColor "olive"}}
             {:type "b"
              :subtype "a"
              :css {:backgroundColor "cyan"}}
             {:type "b"
              :subtype "b"
              :css {:backgroundColor "blue"}}
             {:type "b"
              :subtype "c"
              :css {:backgroundColor "navy"}}
             {:type "c"
              :subtype "a"
              :css {:backgroundColor "orange"}}
             {:type "c"
              :subtype "b"
              :css {:backgroundColor "red"}}
             {:type "c"
              :subtype "c"
              :css {:backgroundColor "violet"}}]})))

(def Indicator
  (styled (.-Indicator RadioGroup)
          (clj->js*
            {})))

(defn FunctionRadioGroup
  [{:keys [mode func fn-change-handler]}]
  [:> Root {:value func
            :onValueChange fn-change-handler
            :orientation "vertical"}
   [:> Item {:type mode
             :subtype "a"
             :value "a"}
    [:> Indicator] "A"]
   [:> Item {:type mode
             :subtype "b"
             :value "b"}
    [:> Indicator] "B"]
   [:> Item {:type mode
             :subtype "c"
             :value "c"}
    [:> Indicator] "C"]])



