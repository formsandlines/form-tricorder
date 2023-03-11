(ns form-tricorder.views.function-tabs
  (:require
    [helix.core :refer [defnc $ <> fnc]]
    ; [helix.hooks :as hooks]
    [helix.dom :as d :refer [$d]]
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
            "&:focus" {:border "1px solid black"}
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


(defnc FunctionTabs
  [{:keys [view value-change-handler func-content]}]
  (let [{:keys [mode func]} view
        current-view-el     (fnc [] (d/div mode "." func))]
    ($d Root {:value func
              :onValueChange value-change-handler
              :activationMode "manual"
              :orientation "vertical"}
        ($d TabList
            ($d Trigger {:type mode
                         :subtype "a"
                         :value "a"}
                "A")
            ($d Trigger {:type mode
                         :subtype "b"
                         :value "b"}
                "B")
            ($d Trigger {:type mode
                         :subtype "c"
                         :value "c"}
                "C"))
        ($d Content {:value "a"}
            ($ current-view-el))
        ($d Content {:value "b"}
            ($ current-view-el))
        ($d Content {:value "c"}
            ($ current-view-el)))))



