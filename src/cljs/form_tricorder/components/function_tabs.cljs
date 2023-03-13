(ns form-tricorder.components.function-tabs
  (:require
   [helix.core :refer [defnc $ <> fnc]]
   [helix.hooks :as hooks]
   [form-tricorder.contexts :refer [OutputContext]]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.data :refer [modes modes-map]]
   [form-tricorder.utils :refer [clj->js*]]
   ["@radix-ui/react-tabs" :as Tabs]
   ["/stitches.config" :refer (styled css)]))

(def Root
  (styled (.-Root Tabs)
          (clj->js*
            {:display "flex"
             :gap "10px"
             :padding "6px"
             })))

(def TabList
  (styled (.-List Tabs)
          (clj->js*
            {:display "flex"
             :flexDirection "column"
             :gap "2px"
             :flex "flex-none"
             :boxSizing "border-box"})))

(def Trigger
  (styled (.-Trigger Tabs)
          (clj->js*
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
            (vec (for [{mode-id :id items :items} modes
                       {func-id :id color :color} items]
                   {:type mode-id
                    :subtype func-id
                    :css {:backgroundColor (:base color)}}))})))

(def Content
  (styled (.-Content Tabs)
          (clj->js*
            {:flex "1 1 auto"
             :border "1px solid lightgray"
             })))

(defnc FunctionTabs
  [{:keys [view value-change-handler]}]
  (let [{:keys [mode-id func-id]} view
        output ((hooks/use-context OutputContext) view)
        current-view-el
        (fnc []
             (d/div mode-id "." func-id
                    ": " output))]
    ($d Root {:value func-id
              :onValueChange value-change-handler
              :activationMode "manual"
              :orientation "vertical"}
        ($d TabList
            (for [{:keys [id label]} (-> modes-map (get mode-id) :items)]
              ($d Trigger {:key id
                           :type mode-id
                           :subtype id
                           :value id}
                  label)))
        (for [{:keys [id]} (-> modes-map (get mode-id) :items)]
          ($d Content {:key id
                       :value id}
              ($ current-view-el))))))



; (comment
;   ($d Root {:value "X"
;               :orientation "vertical"}
;         ($d TabList
;             ($d Trigger {:value "X"}
;                 "X")
;             ($d Trigger {:value "Y"}
;                 "Y"))
;         ($d Content {:value "X"}
;             "X content")
;         ($d Content {:value "Y"}
;             "Y content"))
;   )


