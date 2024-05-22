(ns form-tricorder.components.function-tabs
  (:require
    [helix.core :refer [defnc fnc $ <>]]
    [helix.dom :as d :refer [$d]]
    [helix.hooks :as hooks]
    ;; [form-tricorder.re-frame-adapter :as rf]
    [form-tricorder.model :as model]
    [form-tricorder.functions :as func]
    [form-tricorder.utils :refer [log style> css>]]
    ["@radix-ui/react-tabs" :as Tabs]))



(def Root
  (style> (.-Root Tabs)
          {:display "flex"
           :gap "$6" ; "0.6rem"
           :padding "$4" ; "0.4rem"
           }))

(def TabList
  (style> (.-List Tabs)
          {:display "flex"
           :flex-direction "column"
           :gap "$2" ; "0.4rem"
           :flex "flex-none"}))

(def Trigger
  (style> (.-Trigger Tabs)
          {:flex "flex-none"
           :outline "none"
           :border "none"
           :border-radius "$2"
           :width "$tabIcon" ; "2.2rem"
           :height "$tabIcon" ; "2.2rem"
           :color "$inner_fg"
           :cursor "pointer"
           "&[data-state=active]"
           {:background-color "$inner_n200"}
           "&:focus"
           {:border "1px solid black"}

           :variants
           {:type {:a {} :b {} :c {}}
            :subtype {:a {} :b {} :c {}}}
           :compoundVariants
           (vec (for [{mode-id :id items :items} model/modes
                      {func-id :id color :color} items]
                  {:type (name mode-id)
                   :subtype (name func-id)
                   ;; color is actually always the same for one mode
                   ;; so this is mostly obsolete, but more flexible for now
                   :css {:background-color (:base color)}}))}))

(def Content
  (style> (.-Content Tabs)
          {:flex "1 1 auto"}))

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
                         :type    (name mode-id)
                         :subtype id-str
                         :value   id-str}
                label)))
        (for [{:keys [id]} (:items mode)
              :let [id-str (name id)]]
          ($d Content {:key   id-str
                       :value id-str}
              (func/gen-component func-id {}))))))


