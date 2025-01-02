(ns form-tricorder.components.function-menu
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   ;; [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.model :as model :refer [modes]]
   [form-tricorder.utils :refer [log unite]]
   [form-tricorder.icons :refer [function-icon]]
   ["@radix-ui/react-menubar" :as Menubar]))


(def Root (.-Root Menubar))
(def Menu (.-Menu Menubar))
(def Portal (.-Portal Menubar))
(def Trigger (.-Trigger Menubar))
(def Content (.-Content Menubar))
(def Item (.-Item Menubar))

(def $trigger-styles-base
  (css :px-2 :pt-7 :font-sans :weight-normal :font-size-2
       :fg :rounded-sm
       {:padding-bottom "calc(var(--sp-2) - 1px)"
        :flex "1"
        :display "inline-block"
        ;; :outline "none"
        ;; :position "relative"
        :border "none"
        :text-align "right"
        :cursor "pointer"}))

(def $$trigger-style-variants
  {:expr (css {:background-color "var(--col-fmenu-expr)"
               :box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-expr-shadow), var(--shadow-fmenu)"}
              ["&:hover"
               {:box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-expr-shadow-hover), var(--shadow-fmenu)"}])

   :eval (css {:background-color "var(--col-fmenu-eval)"
               :box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-eval-shadow), var(--shadow-fmenu)"}
              ["&:hover"
               {:box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-eval-shadow-hover), var(--shadow-fmenu)"}])

   :emul (css {:background-color "var(--col-fmenu-emul)"
               :box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-emul-shadow), var(--shadow-fmenu)"}
              ["&:hover"
               {:box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-emul-shadow-hover), var(--shadow-fmenu)"}])})

(defn $$trigger-styles
  [mode]
  (unite $trigger-styles-base
         (get $$trigger-style-variants mode)))


(def $item-styles-base
  (css :mt-1 :py-2 :px-3 :font-sans :font-size-1
       {:display "flex"
        ;; :justify-content "space-between"
        :cursor "pointer"}
       ["& > *:last-child"
        :mx-4]))

(def $$item-style-variants
  {:expr (css {:background-color "var(--col-tab-expr)"}
              ["&:hover"
               {:background-color "var(--col-tab-expr-hover)"}])

   :eval (css {:background-color "var(--col-tab-eval)"}
              ["&:hover"
               {:background-color "var(--col-tab-eval-hover)"}])

   :emul (css {:background-color "var(--col-tab-emul)"}
              ["&:hover"
               {:background-color "var(--col-tab-emul-hover)"}])})

(defn $$item-styles
  [mode]
  (unite $item-styles-base
         (get $$item-style-variants mode)))


(defnc FunctionMenu
  [{:keys [handle-click]}]
  ($d Root
    {:class (css "FunctionMenu" "outer"
                 :gap-2
                 {:display "flex"
                  :column-gap "$2"
                  :box-sizing "border-box"})
     :loop true}
    (for [{mode-id :id label :label items :items} modes
          :let [id-str (name mode-id)]]
      ($d Menu
        {:key id-str}
        ($d Trigger
          {:class ($$trigger-styles mode-id)}
          label)
        ($d Portal
          ($d Content
            {:class (css "outer"
                         :bg :rounded-b-sm
                         {:display "flex"
                          :flex-direction "column"
                          :width "var(--radix-menubar-trigger-width)"
                          :padding "0 0 var(--space-5) 0"
                          :box-shadow "var(--shadow-fmenu)"})
             :sideOffset 2
             :align "start"
             :alignOffset 0
             :loop true}
            (for [{:keys [id label]} items
                  :let [id-str (name id)]]
              ($d Item
                {:class ($$item-styles mode-id)
                 :key   id-str
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

