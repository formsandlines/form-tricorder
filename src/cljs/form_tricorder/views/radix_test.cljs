(ns form-tricorder.views.radix-test
  (:require
    [reagent.core :as r]
    [reagent.dom :as d]
    ["@radix-ui/react-popover" :as Popover]
    ["/stitches.config" :refer (styled)]))

(defn PopoverDemo []
  (let [Root (.-Root Popover)
        Trigger (.-Trigger Popover)
        Portal (.-Portal Popover)
        Content (.-Content Popover)
        Arrow (.-Arrow Popover)]
    [:> Root
     [:> Trigger {"className" "PopoverTrigger"}
      "More Info"]
     [:> Portal 
      [:> Content {"className" "PopoverContent"}
       "Some more info…"
       [:> Arrow {"className" "PopoverArrow"}]]]]))

(def Button
  (styled "button"
          (clj->js
           {:backgroundColor "gainsboro"
            :borderRadius "9999px"
            :fontSize "13px"
            :padding "10px 15px"
            "&:hover" (clj->js
                       {:backgroundColor "lightgray"})})))
