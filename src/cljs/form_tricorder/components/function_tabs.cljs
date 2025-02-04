(ns form-tricorder.components.function-tabs
  (:require
    [helix.core :refer [defnc fnc $ <>]]
    [helix.dom :as d :refer [$d]]
    [helix.hooks :as hooks]
    [shadow.css :refer (css)]
    ;; [form-tricorder.re-frame-adapter :as rf]
    [form-tricorder.model :as model]
    [form-tricorder.icons :refer [function-icon]]
    [form-tricorder.functions :as func]
    [form-tricorder.utils :refer [log unite]]
    ["@radix-ui/react-tabs" :as Tabs]))



(def Root (.-Root Tabs))
(def TabList (.-List Tabs))
(def Content (.-Content Tabs))
(def Trigger (.-Trigger Tabs))

(def $trigger-styles-base
  (css
    :rounded-sm :text-xs :weight-normal :transition-colors
    :size-12 :p-0 :fg
    {:flex-shrink "0"
     :touch-action "manipulation"
     :display "inline-flex"
     :justify-content "center"
     :align-items "center"
     :white-space "nowrap"}
    ["&:focus-visible"
     :outline-none :ring-inset]
    ["&:disabled"
     {:pointer-events "none"
      :opacity "0.5"}]
    ["&:hover"
     {:cursor "pointer"}]
    ["&[data-state=active]"
     {:background-color "var(--col-n3)"}]
    ["&[data-state=active]:hover"
     {:cursor "default"}]))

(def $$trigger-style-variants
  {:expr (css {:background-color "var(--col-tab-expr)"}
              ["&[data-state=inactive]:hover"
               {:background-color "var(--col-tab-expr-hover)"}])

   :eval (css {:background-color "var(--col-tab-eval)"}
              ["&[data-state=inactive]:hover"
               {:background-color "var(--col-tab-eval-hover)"}])

   :emul (css {:background-color "var(--col-tab-emul)"}
              ["&[data-state=inactive]:hover"
               {:background-color "var(--col-tab-emul-hover)"}])})

(def text-only
  {:edn "EDN" :json "JSON" :fdna "DNA"})

(defn $$trigger-styles
  [mode-id func-id]
  (unite $trigger-styles-base
         (get $$trigger-style-variants mode-id)
         (if (text-only func-id)
           (css :h-6)
           (css :h-12))))

(defnc FunctionTabs
  [{:keys [func-id handle-change-view]}]
  (let [mode-id (model/func->mode func-id)
        mode    (model/modes-map mode-id)]
    ($d Root {:class (css "ModeFunctionTabs"
                          :gap-6
                          {:display "flex"
                           :height "100%"
                           :width "auto"})
              :value (name func-id)
              :onValueChange #(handle-change-view (keyword %))
              :activationMode "manual"
              :orientation "vertical"}
        ($d TabList
          {:class (css :gap-2
                       {:display "flex"
                        :flex-direction "column"
                        :overflow-y "auto"
                        :flex-shrink "0"})}
          (for [{:keys [id label]} (:items mode)
                :let [id-str (name id)]]
            ($d Trigger
              {:class   ($$trigger-styles mode-id id)
               :key     id-str
               :title   label
               :value   id-str}
              (or (function-icon id)
                  (text-only id)))))
        (for [{:keys [id]} (:items mode)
              :let [id-str (name id)]]
          ($d Content
            {:class (css ;; :pb-10
                         {:height "100%"
                          :width "100%"
                          :overflow-x "auto"}
                         ["&:focus-visible"
                          :outline-none])
             :key   id-str
             :value id-str}
            (func/gen-component func-id {}))))))


