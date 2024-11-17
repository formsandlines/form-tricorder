(ns form-tricorder.components.function-menu
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ;; [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.model :as model :refer [modes]]
   [form-tricorder.stitches-config :refer [styled css]]
   [form-tricorder.utils :refer [log]]
   [form-tricorder.icons :refer [function-icon]]
   ["@radix-ui/react-menubar" :as Menubar]))


(def Root
  (styled (.-Root Menubar)
          {:display "flex"
           :column-gap "$2" ; "4px"
           :box-sizing "border-box"}))

(def Menu
  (styled (.-Menu Menubar)
          {}))

(def Trigger
  (styled (.-Trigger Menubar)
          (let [shadow "0 1px 1px 0px rgba(0,0,0, .4)"]
            {:flex "1"
             :display "inline-block"
             ;; :outline "none"
             ;; :position "relative"
             :border "none"
             :padding "$7 $2 calc($2 - 1px) $2"
             :font-family "$base"
             :font-weight "$normal"
             :font-size "$2"
             :text-align "right"
             :color "$outer-fg"
             :cursor "pointer"
             ;; :box-shadow (str "inset 0 -20px 20px -8px $colors$outer-fmenu-base"
             ;;                  ", " shadow)
             :border-radius "$2"
             "&:focus"
             {}
             ;; "&:hover"
             ;; {:box-shadow (str "inset 0 -20px 20px -8px $colors$outer-fmenu-glow"
             ;;                   ", " shadow)}
             :variants
             {:type
              (into {}
                    (for [{:keys [id]} modes]
                      [id {:background-color (str "$outer-fmenu-" (name id))
                           :box-shadow (str "inset 0 -20px 20px -8px $colors$outer-fmenu-" (name id) "-shadow, " shadow)
                           "&:hover"
                           {:box-shadow (str "inset 0 -20px 20px -8px $colors$outer-fmenu-" (name id) "-shadow-hover, " shadow)
}
                           }]))}})))

(def Portal
  (styled (.-Portal Menubar)
          {}))

(def Content
  (styled (.-Content Menubar)
          (let [shadow "0 1px 1px 0px rgba(0,0,0, .4)"]
            {:display "flex"
             :flex-direction "column"
             ;; :margin-left "$2"
             ;; :margin-top "$2"
             :width "var(--radix-menubar-trigger-width)"
             :padding "0 0 $5 0"
             :background "$outer-bg"
             :box-shadow shadow
             :border-bottom-left-radius "$2"
             :border-bottom-right-radius "$2"
             })))

(def Item
  (styled (.-Item Menubar)
          {
           :display "flex"
           ;; :justify-content "space-between"
           :margin "$1 0 0 0"
           :padding "$2 $3"
           :font-family "$base"
           :font-size "$1"
           :cursor "pointer"

           "& > *:last-child"
           {:margin-left "$4"
            :margin-right "$4"}

           ;; :variants
           ;; {:type
           ;;  (into {}
           ;;        (for [{:keys [id color]} modes]
           ;;          [id {:background-color (:base color)}]))}

           :variants
           {:type {:a {} :b {} :c {}}
            :subtype {:a {} :b {} :c {}}}
           :compoundVariants
           (vec (for [{mode-id :id items :items} modes
                      {func-id :id color :color} items]
                  {:type (name mode-id)
                   :subtype (name func-id)
                   ;; color is actually always the same for one mode
                   ;; so this is mostly obsolete, but more flexible for now
                   :css {:background-color (str "$inner-tab-" (name mode-id))
                         "&:hover"
                         {:background-color (str "$inner-tab-" (name mode-id) "-hover")}}}))
           }))


(defnc FunctionMenu
  [{:keys [handle-click]}]
  ($d Root
    {:class "FunctionMenu"
     :loop true}
    (for [{mode-id :id label :label items :items} modes
          :let [id-str (name mode-id)]]
      ($d Menu
        {:key id-str}
        ($d Trigger
          {:type id-str} label)
        ($d Portal
          ($d Content
            {:sideOffset 2
             :align "start"
             :alignOffset 0
             :loop true}
            (for [{:keys [id label]} items
                  :let [id-str (name id)]]
              ($d Item
                {:key     id-str
                 :type    (name mode-id)
                 :subtype id-str
                 :onSelect 
                 (fn [e] (let [win-e  (.-event js/window)
                              shift? (if win-e
                                       (.-shiftKey win-e)
                                       false)]
                          (handle-click id shift?)))}
                (d/div {:style {:width "24px"
                                :height "24px"}}
                  (function-icon id))
                (d/div label)))))))))

