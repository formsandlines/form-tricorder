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
  (css :px-4 :pt-3 :font-sans :weight-normal :font-size-sm
       :fg :rounded-sm
       {:padding-bottom "calc(var(--sp-2) - 1px)"
        :flex "1"
        ;; :display "inline-block"
        :display "inline-flex"
        :justify-content "space-between"
        ;; :outline "none"
        ;; :position "relative"
        :border "none"
        ;; :text-align "left"
        ;; :text-align "right"
        :cursor "pointer"}
       [:media-min-xs
        :font-size-base
        :pt-6]))

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
  (css :mt-1 :py-2 :pl-3 :pr-0 :font-sans :font-size-1
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

(def $keybind-styles
  (css ["& > .keybind"
        {:opacity "0.25"
         ;; :text-decoration "underline"
         }]))

(defnc FunctionMenu
  [{:keys [handle-select-fn keybind-mode-value]}]
  ($d Root
    {:class (css "FunctionMenu" "outer"
                 :gap-2
                 {:display "flex"
                  :flex-direction "row"
                  ;; :column-gap "$2"
                  :box-sizing "border-box"}
                 ;; [:media-min-xs
                 ;;  {:flex-direction "column"}]
                 )
     :value keybind-mode-value
     :loop true}
    (for [{mode-id :id label :label items :items keybind :keybind} modes
          :let [id-str (name mode-id)]]
      ($d Menu
        {:key id-str
         :value id-str
}
        ($d Trigger
          {:id (str "mode-" id-str)
           :class (unite ($$trigger-styles mode-id)
                         $keybind-styles)}
          (d/span label) (d/span {:class "keybind"} keybind))
        ($d Portal
          ($d Content
            {:class (css "outer"
                         :bg :rounded-b-sm
                         {:display "flex"
                          :z-index "3"
                          :flex-direction "column"
                          :padding "0 0 var(--sp-3) 0"
                          :box-shadow "var(--shadow-fmenu)"}
                         [:media-min-xs
                          {:width "var(--radix-menubar-trigger-width)"}])
             :sideOffset 2
             :align "start"
             :alignOffset 0
             :loop true}
            (for [{:keys [id label keybind]} items
                  :let [id-str (name id)]]
              ($d Item
                {:class ($$item-styles mode-id)
                 :key   id-str
                 :onSelect 
                 (fn [e] (let [window-e (.-event js/window)
                              shift? (if window-e
                                       (.-shiftKey window-e)
                                       false)]
                          (handle-select-fn id shift?)))}
                (d/div {:class (css {:width "24px"
                                     :height "24px"})}
                  (function-icon id))
                (d/div
                  {:class (unite (css :gap-2
                                      {:display "flex"
                                       :flex "1"
                                       :justify-content "space-between"})
                                 $keybind-styles)}
                  (d/span label)
                  (d/span {:class "keybind"} keybind))))))))))

