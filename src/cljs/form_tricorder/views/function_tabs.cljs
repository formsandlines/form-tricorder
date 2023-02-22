(ns form-tricorder.views.function-tabs
  (:require
    [reagent.core :as r]
    [reagent.dom :as d]
    [form-tricorder.utils :refer [clj->js*]]
    ["@radix-ui/react-tabs" :as Tabs]
    ["/stitches.config" :refer (styled css)]))

(def Root
  (styled (.-Root Tabs)
          (clj->js*
            {:display "flex"
             :gap "10px"
             :padding "6px"
             })))

(def TabList
  (styled (.-List Tabs)
          (clj->js*
            {:display "flex"
             :flexDirection "column"
             :gap "2px"
             :flex "flex-none"
             :boxSizing "border-box"})))

(def Trigger
  (styled (.-Trigger Tabs)
          (clj->js*
           {:flex "flex-none"
            :outline "none"
            :border "none"
            :borderRadius "4px"
            :width "30px"
            :height "30px"
            "&[data-state=active]" {:backgroundColor "lightgray"}
            :variants
            {:type {:a {} :b {} :c {}}
             :subtype {:a {} :b {} :c {}}}
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

(def Content
  (styled (.-Content Tabs)
          (clj->js*
            {:flex "1 1 auto"
             :border "1px solid lightgray"
             })))


(defn FunctionTabs
  [{:keys [mode func fn-change-handler func-content]}]
  [:> Root {:value func
            :onValueChange fn-change-handler
            :activationMode "manual"
            :orientation "vertical"}
   [:> TabList 
    [:> Trigger {:type mode
                 :subtype "a"
                 :value "a"}
     "A"]
    [:> Trigger {:type mode
                 :subtype "b"
                 :value "b"}
     "B"]
    [:> Trigger {:type mode
                 :subtype "c"
                 :value "c"}
     "C"]]
   [:> Content {:value "a"}
    [(get-in func-content [mode "a"])]]
   [:> Content {:value "b"}
    [(get-in func-content [mode "b"])]]
   [:> Content {:value "c"}
    [(get-in func-content [mode "c"])]]])



