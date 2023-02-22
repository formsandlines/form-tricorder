(ns form-tricorder.views.function-menubar
  (:require
    [reagent.core :as r]
    [reagent.dom :as d]
    [form-tricorder.utils :refer [clj->js*]]
    ["@radix-ui/react-menubar" :as Menubar]
    ["/stitches.config" :refer (styled css)]))


(def Root
  (styled (.-Root Menubar)
          (clj->js*
            {:display "flex"
             :gap "4px"
             :padding "6px"
             :boxSizing "border-box"
             :width "100%"})))

(def Menu
  (styled (.-Menu Menubar)
          (clj->js*
            {})))

(def Trigger
  (styled (.-Trigger Menubar)
          (clj->js*
           {:flex "1 1 auto"
            :display "inline-block"
            :outline "none"
            :border "none"
            :padding "20px 4px 4px 4px"
            :textAlign "right"
            :color "white"
            :boxShadow "0 0.5px 1px 0.5px black"
            :fontSize "15px"
            :variants
            {:type {:a {:backgroundColor "$teal8"
                        "&:hover" {:backgroundColor "$teal9"}}
                    :b {:backgroundColor "$violet8"
                        "&:hover" {:backgroundColor "$violet9"}}
                    :c {:backgroundColor "$crimson8"
                        "&:hover" {:backgroundColor "$crimson9"}}}}})))

(def Portal
  (styled (.-Portal Menubar)
          (clj->js
            {})))

(def Content
  (styled (.-Content Menubar)
          (clj->js
            {})))

(def Item
  (styled (.-Item Menubar)
          (clj->js
            {})))


(defn FunctionMenubar [{:keys [on-select]}]
  [:> Root
   [:> Menu
    [:> Trigger {:type "a"} "visualize"]
    [:> Portal
     [:> Content
      [:> Item
       {:onSelect (on-select {:type "a" :subtype "a"})}
       "hooks notation"]
      [:> Item 
       {:onSelect (on-select {:type "a" :subtype "b"})}
       "graph notation"]
      [:> Item 
       {:onSelect (on-select {:type "a" :subtype "c"})}
       "depth tree"] ]]]
   [:> Menu
    [:> Trigger {:type "b"} "calculate"]
    [:> Portal
     [:> Content
      [:> Item 
       {:onSelect (on-select {:type "b" :subtype "a"})}
       "value table"]
      [:> Item 
       {:onSelect (on-select {:type "b" :subtype "b"})}
       "vmap"]]]]
   [:> Menu
    [:> Trigger {:type "c"} "emulate"]
    [:> Portal
     [:> Content
      [:> Item 
       {:onSelect (on-select {:type "c" :subtype "a"})}
       "SelFi"]
      [:> Item 
       {:onSelect (on-select {:type "c" :subtype "b"})}
       "mindFORM"]
      [:> Item 
       {:onSelect (on-select {:type "c" :subtype "c"})}
       "lifeFORM"]]]]])
