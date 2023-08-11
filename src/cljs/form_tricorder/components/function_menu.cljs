(ns form-tricorder.components.function-menu
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.model :as model :refer [modes]]
   [form-tricorder.utils :refer [log clj->js*]]
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


(defnc FunctionMenu
  [{:keys [handle-click]}]
  ($d Root
      {:class "FunctionMenu"}
      (for [{:keys [id label items]} modes
            :let [id-str (name id)]]
        ($d Menu {:key id-str}
            ($d Trigger {:type id-str} label)
            ($d Portal
                ($d Content
                    (for [{:keys [id label]} items
                          :let [id-str (name id)]]
                      ($d Item
                          {:key id-str
                           :onSelect 
                           (fn [e] (let [win-e  (.-event js/window)
                                         shift? (if win-e
                                                  (.-shiftKey win-e)
                                                  false)]
                                     (handle-click id shift?)))}
                          label))))))))

