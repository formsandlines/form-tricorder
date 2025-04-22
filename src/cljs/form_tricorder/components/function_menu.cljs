(ns form-tricorder.components.function-menu
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   ;; [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.model :as model :refer [modes]]
   [form-tricorder.utils :refer [log unite]]
   [form-tricorder.icons :refer [function-icon]]
   ["@radix-ui/react-icons" :as radix-icons]
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
       ["&:focus-visible"
        :outline-none :ring]
       [:media-min-xs
        :font-size-base
        :pt-6]))

(def $$trigger-style-variants
  {:expr (css {:background-color "var(--col-fmenu-expr)"
               :box-shadow "var(--shadow-fmenu)"
               ;; :box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-expr-shadow), var(--shadow-fmenu)"
               }
              ["&:hover"
               {:box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-expr-shadow), var(--shadow-fmenu)"}
               ;; {:box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-expr-shadow-hover), var(--shadow-fmenu)"}
               ;; {:box-shadow "var(--shadow-fmenu)"}
               ])

   :eval (css {:background-color "var(--col-fmenu-eval)"
               :box-shadow "var(--shadow-fmenu)"
               ;; :box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-eval-shadow), var(--shadow-fmenu)"
               }
              ["&:hover"
               {:box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-eval-shadow), var(--shadow-fmenu)"}
               ;; {:box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-eval-shadow-hover), var(--shadow-fmenu)"}
               ;; {:box-shadow "var(--shadow-fmenu)"}
               ])

   :emul (css {:background-color "var(--col-fmenu-emul)"
               :box-shadow "var(--shadow-fmenu)"
               ;; :box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-emul-shadow), var(--shadow-fmenu)"
               }
              ["&:hover"
               {:box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-emul-shadow), var(--shadow-fmenu)"}
               ;; {:box-shadow "inset 0 -20px 20px -8px var(--col-fmenu-emul-shadow-hover), var(--shadow-fmenu)"}
               ;; {:box-shadow "var(--shadow-fmenu)"}
               ])})

(defn $$trigger-styles
  [mode]
  (unite $trigger-styles-base
         (get $$trigger-style-variants mode)))


(def $item-styles-base
  (css :py-2 :px-0
       {:display "flex"
        :cursor "pointer"}
       ["&:focus-visible"
        :outline-none :ring-inset]))

(def $$item-style-variants
  {:expr (css {:border-color "var(--col-fmenu-expr-shadow)"
               :background-color "var(--col-tab-expr)"}
              ["&:hover"
               {:background-color "var(--col-tab-expr-hover)"}])

   :eval (css {:border-color "var(--col-fmenu-eval-shadow)"
               :background-color "var(--col-tab-eval)"}
              ["&:hover"
               {:background-color "var(--col-tab-eval-hover)"}])

   :emul (css {:border-color "var(--col-fmenu-emul-shadow)"
               :background-color "var(--col-tab-emul)"}
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


(def handle-select-fn
  (fn [func-id alt-view?]
    (let [view-index (if alt-view? 1 0)]
      (when alt-view? (rf/dispatch [:views/split]))
      (rf/dispatch [:views/set-func-id
                    {:next-id    func-id
                     :view-index view-index}]))))

(defnc FunctionMenu
  []
  (let [[keybind-mode-value set-keybind-mode-value] (hooks/use-state nil)]
    (hooks/use-effect
     :once
     (let [handle-key-down
           (fn [e] ;; ? refactor to use model data instead of hardcoding
             (cond
               (.-ctrlKey e)
               (let [shift? (.-shiftKey e)
                     select #(do (handle-select-fn % shift?)
                                 (set-keybind-mode-value nil))
                     [expr-open? eval-open? emul-open?]
                     (map #(= "open"
                              (.. js/document (getElementById %)
                                  (getAttribute "data-state")))
                          ["mode-expr" "mode-eval" "mode-emul"])]
                 (cond
                   expr-open? (case (.-key e)
                                ("h" "H") (select :hooks)
                                ("c" "C") (select :graphs)
                                ("t" "T") (select :depthtree)
                                ("e" "E") (select :edn)
                                ("j" "J") (select :json)
                                nil)
                   eval-open? (case (.-key e)
                                ("t" "T") (select :vtable)
                                ("v" "V") (select :vmap)
                                ("d" "D") (select :fdna)
                                nil)
                   emul-open? (case (.-key e)
                                ("s" "S") (select :selfi)
                                ("m" "M") (select :mindform)
                                ("l" "L") (select :lifeform)
                                nil)
                   :else
                   (case (.-key e)
                     ("x" "X") (set-keybind-mode-value "expr")
                     ("v" "V") (set-keybind-mode-value "eval")
                     ("e" "E") (set-keybind-mode-value "emul")
                     nil)))
               (= (.-key e) "Escape") (set-keybind-mode-value nil)
               :else nil))]
       (.addEventListener js/window "keydown" handle-key-down)
       ;; Cleanup
       #(.removeEventListener js/window "keydown" handle-key-down)))

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
               :value id-str}
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
                                    {:min-width "var(--radix-menubar-trigger-width)"}])
                       :sideOffset 2
                       :align "start"
                       :alignOffset 0
                       :loop true}
                      (for [{:keys [id label keybind]} items
                            :let [id-str (name id)]]
                        (d/div
                         {:key id-str
                          :class (css :mt-1 :font-sans :font-size-1
                                      {:display "flex"})}
                         ($d Item
                             {:class (unite ($$item-styles mode-id)
                                            (css :px-3 :gap-3
                                                 {:flex "1"}))
                              :onSelect (fn [_] (handle-select-fn id false))}
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
                              (d/span {:class "keybind"}
                                      keybind)))
                         ($d Item
                             {:class (unite (css :px-3 :border-l :gap-2
                                                 {:align-items "center"
                                                  :justify-content "space-between"})
                                            ($$item-styles mode-id))
                              :onSelect (fn [] (handle-select-fn id true))}
                             (d/div
                              ($ radix-icons/PlusIcon))
                             (d/div
                              {:class $keybind-styles}
                              (d/span {:class "keybind"} "⇧"))))))))))))

