(ns form-tricorder.components.function-tabs
  (:require
    [helix.core :refer [defnc fnc $ <>]]
    [helix.dom :as d :refer [$d]]
    [helix.hooks :as hooks]
    [form-tricorder.model :as model]
    [form-tricorder.functions :as func]
    [form-tricorder.utils :refer [log style> css>]]
    ["@radix-ui/react-tabs" :as Tabs]))



(def Root
  (style> (.-Root Tabs)
          {:display "flex"
           :gap "10px"
           :padding "6px"
           }))

(def TabList
  (style> (.-List Tabs)
          {:display "flex"
           :flexDirection "column"
           :gap "2px"
           :flex "flex-none"
           :boxSizing "border-box"}))

(def Trigger
  (style> (.-Trigger Tabs)
          {:flex "flex-none"
           :outline "none"
           :border "none"
           :borderRadius "4px"
           :width "30px"
           :height "30px"
           "&[data-state=active]" {:backgroundColor "lightgray"}
           "&:focus" {:border "1px solid black"}
           :variants
           {:type {:a {} :b {} :c {}}
            :subtype {:a {} :b {} :c {}}}
           :compoundVariants
           (vec (for [{mode-id :id items :items} model/modes
                      {func-id :id color :color} items]
                  {:type (name mode-id)
                   :subtype (name func-id)
                   :css {:backgroundColor (:base color)}}))}))

(def Content
  (style> (.-Content Tabs)
          {:flex "1 1 auto"
           :border "1px solid lightgray"
           }))

(defnc FunctionTabs
  [{:keys [view handle-change-view]}]
  (let [{:keys [func-id]} view
        mode-id (model/func->mode func-id)
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
              ;; ! hidden components should be generated on first display
              (func/gen-component func-id {}))))))


