(ns form-tricorder.components.function-tabs
  (:require
    [helix.core :refer [defnc fnc $ <>]]
    [helix.dom :as d :refer [$d]]
    [helix.hooks :as hooks]
    ;; [form-tricorder.re-frame-adapter :as rf]
    [form-tricorder.model :as model]
    [form-tricorder.icons :refer [function-icon]]
    [form-tricorder.functions :as func]
    [form-tricorder.stitches-config :as st]
    [form-tricorder.utils :refer [log]]
    ["@radix-ui/react-tabs" :as Tabs]))



(def Root
  (st/styled (.-Root Tabs)
          {:display "flex"
           :gap "$6" ; "0.6rem"
           :padding "$4" ; "0.4rem"
           }))

(def TabList
  (st/styled (.-List Tabs)
          {:display "flex"
           :flex-direction "column"
           :gap "$2" ; "0.4rem"
           :flex "flex-none"}))

(def text-only
  {:edn "EDN" :json "JSON" :fdna "DNA"})

(def Trigger
  (st/styled (.-Trigger Tabs)
          {:flex "flex-none"

           :touch-action "manipulation"
           :display "inline-flex"
           :justify-content "center"
           :align-items "center"
           :white-space "nowrap"
           :border-radius "$md"
           :_text ["$xs"]
           :font-weight "$normal"
           :_transition_colors []
           "&:focus-visible"
           {:_outlineNone []
            :_ringOuter []}
           "&:disabled"
           {:pointer-events "none"
            :opacity "0.5"}
           "&:hover"
           {:cursor "pointer"}
           
           :width "$icon-tab" ; "2.2rem"
           :height "$icon-tab" ; "2.2rem"
           :padding 0
           :color "$inner-fg"
           "&[data-state=active]"
           {:background-color "$n3"
            "&:hover"
            {:cursor "default"}}

           :variants
           {:type {:a {} :b {} :c {}}
            :subtype {:a {} :b {} :c {}}}
           :compoundVariants
           (vec (for [{mode-id :id items :items} model/modes
                      {func-id :id} items]
                  {:type (name mode-id)
                   :subtype (name func-id)
                   ;; color is actually always the same for one mode
                   ;; so this is mostly obsolete, but more flexible for now
                   :css {:height (if (text-only func-id)
                                   "$text-tab"
                                   "$icon-tab")
                         :background-color
                         (str "$inner-tab-" (name mode-id))
                         "&[data-state=inactive]:hover"
                         {:background-color
                          (str "$inner-tab-" (name mode-id) "-hover")}}}))}))

(def Content
  (st/styled (.-Content Tabs)
          {:flex "1 1 auto"
           :padding "0 0 $10 0"}))

(defnc FunctionTabs
  [{:keys [func-id handle-change-view]}]
  (let [mode-id (model/func->mode func-id)
        mode    (model/modes-map mode-id)]
    ($d Root {:class "ModeFunctionTabs"
              :value (name func-id)
              :onValueChange #(handle-change-view (keyword %))
              :activationMode "manual"
              :orientation "vertical"}
        ($d TabList
          (for [{:keys [id label]} (:items mode)
                :let [id-str (name id)]]
            ($d Trigger {:key     id-str
                         :title   label
                         :type    (name mode-id)
                         :subtype id-str
                         :value   id-str}
                (or (function-icon id)
                    (text-only id)))))
        (for [{:keys [id]} (:items mode)
              :let [id-str (name id)]]
          ($d Content {:key   id-str
                       :value id-str}
              (func/gen-component func-id {}))))))


