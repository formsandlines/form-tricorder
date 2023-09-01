(ns form-tricorder.components.function-menu
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.model :as model :refer [modes]]
   [form-tricorder.utils :refer [log style> css>]]
   ["@radix-ui/react-menubar" :as Menubar]))


(def Root
  (style> (.-Root Menubar)
          {:display "flex"
           :column-gap "4px"
           :boxSizing "border-box"}))

(def Menu
  (style> (.-Menu Menubar)
          {}))

(def Trigger
  (style> (.-Trigger Menubar)
          (let [shadow "0 1px 1px 0px rgba(0,0,0, .4)"]
            {:flex "1"
             :display "inline-block"
             ; :outline "none"
             :border "none"
             :padding "22px 6px 4px 4px"
             :fontFamily "$base"
             :fontWeight "$normal"
             :fontSize "$3"
             :textAlign "right"
             :color "$outer_fg"
             :cursor "pointer"
             :boxShadow (str "inset 0 -20px 20px -8px $colors$fmenu_base"
                             ", " shadow)
             :borderRadius "$2"
             "&:focus"
             {}
             "&:hover"
             {:boxShadow (str "inset 0 -20px 20px -8px $colors$fmenu_glow"
                              ", " shadow)}
             :variants
             {:type
              (into {}
                    (for [{:keys [id color]} modes]
                      [id {:backgroundColor (:base color)}]))}})))

(def Portal
  (style> (.-Portal Menubar)
          {}))

(def Content
  (style> (.-Content Menubar)
          {}))

(def Item
  (style> (.-Item Menubar)
          {}))


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

