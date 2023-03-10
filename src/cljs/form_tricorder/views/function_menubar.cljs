(ns form-tricorder.views.function-menubar
  (:require
    [helix.core :refer [defnc $ <>]]
    [helix.hooks :as hooks]
    [helix.dom :as d :refer [$d]]
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


(defnc FunctionMenubar 
  [{:keys [views set-views]}]
  (let [make-on-select (fn [{:keys [type subtype] :as m}]
                         (fn [e]
                           (set-views
                             #(update % 0 assoc
                                      :mode type :func subtype))))]
    ($d Root
        ($d Menu
            ($d Trigger {:type "a"} "visualize")
            ($d Portal
                ($d Content
                    ($d Item
                        {:onSelect (make-on-select {:type "a" :subtype "a"})}
                        "hooks notation")
                    ($d Item
                        {:onSelect (make-on-select {:type "a" :subtype "b"})}
                        "graph notation")
                    ($d Item
                        {:onSelect (make-on-select {:type "a" :subtype "c"})}
                        "depth tree") )))
        ($d Menu
            ($d Trigger {:type "b"} "calculate")
            ($d Portal
                ($d Content
                    ($d Item
                        {:onSelect (make-on-select {:type "b" :subtype "a"})}
                        "value table")
                    ($d Item
                        {:onSelect (make-on-select {:type "b" :subtype "b"})}
                        "vmap"))))
        ($d Menu
            ($d Trigger {:type "c"} "emulate")
            ($d Portal
                ($d Content
                    ($d Item
                        {:onSelect (make-on-select {:type "c" :subtype "a"})}
                        "SelFi")
                    ($d Item
                        {:onSelect (make-on-select {:type "c" :subtype "b"})}
                        "mindFORM")
                    ($d Item
                        {:onSelect (make-on-select {:type "c" :subtype "c"})}
                        "lifeFORM")))))))
