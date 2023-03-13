(ns form-tricorder.components.function-menubar
  (:require
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.data :refer [modes]]
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
            "&:focus" {:boxShadow "0 0.5px 1px 0.5px red"}
            :variants
            {:type
             (into {}
                   (for [{:keys [id color]} modes]
                     [id {:backgroundColor (:base color)
                          "&:hover" {:backgroundColor (:hover color)}}]))}})))

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
  ($d Root
      (for [{mode-id :id mode-lbl :label items :items} modes]
        ($d Menu {:key mode-id}
            ($d Trigger {:type mode-id} mode-lbl)
            ($d Portal
                ($d Content
                    (for [{func-id :id func-lbl :label} items]
                      ($d Item
                          {:key func-id
                           :onSelect (fn [_]
                                       (set-views
                                        #(update % 0 assoc
                                                 :mode-id mode-id
                                                 :func-id func-id)))}
                          func-lbl))))))))

