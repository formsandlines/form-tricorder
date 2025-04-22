(ns form-tricorder.components.value-filter
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
   [form-tricorder.components.common.toggle-group
    :refer [ToggleGroup ToggleGroupItem]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   [form-tricorder.utils :as utils :refer [let+ unite]]
   ;; ["@radix-ui/react-icons" :as radix-icons]
   ["lucide-react" :as lucide-icons]
   ["@radix-ui/react-collapsible" :as Collapsible]))

(def r) ;; hotfix for linting error in let+

(def $$toggle-const-styles
  {:N (css ["&[data-state=on]"
            {:background-color "var(--col-const-n)"}]
           ["&[data-state=on]:hover"
            {:background-color "var(--col-const-n-hover)"}])
   :U (css ["&[data-state=on]"
            {:background-color "var(--col-const-u)"}]
           ["&[data-state=on]:hover"
            {:background-color "var(--col-const-u-hover)"}])
   :I (css ["&[data-state=on]"
            {:background-color "var(--col-const-i)"}]
           ["&[data-state=on]:hover"
            {:background-color "var(--col-const-i-hover)"}])
   :M (css ["&[data-state=on]"
            {:background-color "var(--col-const-m)"}]
           ["&[data-state=on]:hover"
            {:background-color "var(--col-const-m-hover)"}])})


(defnc CurlyBrace
  [props]
  (let+ [{:keys [closed?] :or {closed? false}
          :rest r} props]
    (d/svg
     {:style {:fill "currentcolor"}
      ;; :width 5.848
      ;; :height 27.125
      :viewBox "0 0 5.848 27.125"
      & r}
     (d/g 
      {:transform (if closed? "" "translate(5.848 27.125) rotate(180)")}
      (d/path
       {:d "M5.274,13.723a3.936,3.936,0,0,0-2.083,1.539,3.081,3.081,0,0,0-.519,1.833v6.477a3.87,3.87,0,0,1-.306,1.943A3.589,3.589,0,0,1,0,26.873l.056.252A5.071,5.071,0,0,0,3.079,25.99a3.188,3.188,0,0,0,.684-2.531V16.269a3.132,3.132,0,0,1,.071-.727c.238-.9.964-1.231,2.013-1.651v-.364C4.8,13.135,4.072,12.7,3.835,11.806a2.6,2.6,0,0,1-.071-.672V3.665a3.15,3.15,0,0,0-.684-2.52A5,5,0,0,0,.056,0L0,.253A3.629,3.629,0,0,1,2.366,1.607a3.936,3.936,0,0,1,.306,1.959v6.687a3.341,3.341,0,0,0,.364,1.721,3.967,3.967,0,0,0,2.238,1.749"})))))

(def $set-brace-sm
  (css :h-6 :fg-muted))

(def $set-brace
  (css :h-12 :fg-muted))

(def $set-brace-lg
  (css :h-18 :fg-muted))

(defnc InterpretationFilterUI
  [{:keys [interpr-filter varorder
           filter-interpr-handler reset-filter-interpr-handler]}]
  (let [{:keys [neg-op? op terms-filter vals-filter]} interpr-filter
        disabled? (or (empty? varorder) (empty? terms-filter))]
    (d/div
     {:class (css "InterpretationFilter"
                  :gap-3 :border-col :py-3 :px-4 :rounded
                  {:display "flex"
                   :border "1px dashed"
                   :flex-direction "column"
                   :justify-content "stretch"
                   :align-items "start"})}
     (d/div
      {:class (css "InterpretationTermsFilter"
                   :gap-2
                   {:display "flex"
                    :align-items "center"})}
      (d/span
       {:class (css ;; :ml-1
                :fg-muted
                {:white-space "nowrap"})}
       "∀ " (d/i "I") " ∈" (when disabled? " ∅"))
      (when-not disabled?
        (for [[i filter] (map-indexed vector terms-filter)
              :let [v (varorder i)]]
          (<>
           {:key (str "filter-interpr-term-" i)}
           (d/div
            {:class (css :gap-1
                         {:display "flex"
                          :align-items "center"})}
            ($ CurlyBrace
               {:class $set-brace-lg
                :closed? false})
            (d/div
             {:class (css :pr-2
                          {:position "relative"})}
             ($ ToggleGroup
                {:type "multiple"
                 :value (clj->js filter)
                 :onValueChange (fn [arr] (filter-interpr-handler
                                          (assoc-in
                                           interpr-filter
                                           [:terms-filter i]
                                           (into #{} (map keyword) arr))))
                 :class (css :font-mono)
                 :disabled disabled?
                 :orientation "horizontal"
                 :group-variant :value-filter/vmap}
                (for [c utils/consts]
                  ($ ToggleGroupItem
                     {:key (str "filter-results-" (name c))
                      :class ($$toggle-const-styles c)
                      :value (name c)}
                     (d/i (utils/pp-val c)))))
             (d/abbr
              {:class (css :font-size-sm
                           {:position "absolute"
                            :right "0"
                            :bottom "0"})
               :title (str v)}
              "v" (d/sub {:class (css {:margin-left "0.08rem"})}
                         (str i))))
            ($ CurlyBrace
               {:class $set-brace-lg
                :closed? true}))
           (when (< i (dec (count terms-filter)))
             (d/span
              {:class (css :fg-muted)}
              "×")))))
      (d/span
       {:class (css :fg-muted)}
       ": "))
     (d/div
      {:class (css :gap-3
                   {:display "flex"
                    :align-items "center"})}
      (d/div
       {:class "InterpretationFilterNegOp"}
       ($ Toggle
          {:class (css :font-mono :pb-1)
           :variant :outline
           :size :sm
           ;; :style {:max-height "var(--sz-6)"}
           :disabled disabled?
           :pressed neg-op?
           :title "negate set operation"
           :onPressedChange (fn [b] (filter-interpr-handler
                                    (assoc interpr-filter :neg-op? b)))}
          "¬"))
      (d/span
       {:class (css :fg-muted)}
       (d/i "I"))
      (d/div
       {:class "InterpretationFilterOp"}
       ($ RadioGroup
          {:value (name op)
           :onValueChange (fn [s] (filter-interpr-handler
                                  (assoc interpr-filter :op (keyword s))))
           :disabled disabled?
           :orientation "horizontal"
           :group-variant :joined
           :variant :outline
           :size :sm}
          (for [[k s label] [[:intersects "∩" "intersects"]
                             [:subseteq "⊆" "is subset of"]
                             [:equal "=" "is equal to"]]]
            ($ RadioGroupItem
               {:key (str "filter-interpr-op" (name k))
                :class (css :font-mono)
                :title (str "set operation: " label)
                :value (name k)}
               s))))
      (d/div
       {:class (css "InterpretationValsFilter"
                    :gap-2
                    {:display "flex"
                     :align-items "center"})}
       ($ CurlyBrace
          {:class $set-brace
           :closed? false})
       ($ Button
          {:class (css :font-mono)
           :variant :outline
           :size :sm
           :style {:height "var(--sz-2)"}
           :disabled disabled?
           :title "invert toggle selection"
           :onClick (fn [_] (filter-interpr-handler
                            (assoc
                             interpr-filter
                             :vals-filter
                             (set/difference utils/consts-set vals-filter))))}
          "–")
       ($ ToggleGroup
          {:type "multiple"
           :value (clj->js vals-filter)
           :onValueChange (fn [arr] (filter-interpr-handler
                                    (assoc
                                     interpr-filter
                                     :vals-filter
                                     (into #{} (map keyword) arr))))
           :class (css :font-mono)
           :orientation "horizontal"
           :group-variant :joined
           :disabled disabled?
           :variant :outline
           :size :sm}
          (for [c utils/consts]
            ($ ToggleGroupItem
               {:key (str "filter-interpr-vals-" (name c))
                :class ($$toggle-const-styles c)
                :value (name c)}
               (d/i (utils/pp-val c)))))
       ($ CurlyBrace
          {:class $set-brace
           :closed? true}))
      ($ Button
         {:variant :destructive
          :size :sm
          :disabled disabled?
          :title "reset to default settings"
          :onClick reset-filter-interpr-handler}
         "reset")))))


(defnc ResultsFilterUI
  [{:keys [results-filter filter-results-handler reset-filter-results-handler]}]
  (d/div
   {:class (css "ResultsFilter"
                :gap-3 :border-col :py-3 :px-4 :rounded
                {:display "flex"
                 :border "1px dashed"
                 :align-items "center"})}
   (d/span
    {:class (css :fg-muted
                 {:margin-right "-0.2rem"})}
    (d/i "r") " ∈")
   (d/div
    {:class (css :gap-2
                 {:display "flex"
                  :align-items "center"})}
    ($ CurlyBrace
       {:class $set-brace
        :closed? false})
    ($ Button
       {:class (css :font-mono)
        :variant :outline
        :size :sm
        :style {:height "var(--sz-2)"}
        :title "invert toggle selection"
        :onClick (fn [_] (filter-results-handler
                         (set/difference utils/consts-set results-filter)))}
       "–")
    ($ ToggleGroup
       {:type "multiple"
        :value (clj->js results-filter)
        :onValueChange (fn [arr] (filter-results-handler
                                 (into #{} (map keyword) arr)))
        :class (css :font-mono)
        :orientation "horizontal"
        :group-variant :joined
        :variant :outline
        :size :sm}
       (for [c utils/consts]
         ($ ToggleGroupItem
            {:key (str "filter-results-" (name c))
             :class ($$toggle-const-styles c)
             :value (name c)}
            (d/i (utils/pp-val c)))))
    ($ CurlyBrace
       {:class $set-brace
        :closed? true}))
   ($ Button
      {:variant :destructive
       :size :sm
       :title "reset to default settings"
       :onClick reset-filter-results-handler}
      "reset")))

(defnc OptLabel
  [{:keys [children]}]
  (d/label
   {:class (css :font-size-sm)}
   children))

(def Root (.-Root Collapsible))
(def Trigger (.-Trigger Collapsible))
(def Content (.-Content Collapsible))

(defnc ValueFilter
  [{:keys [varorder]}]
  (let [results-filter (rf/subscribe [:modes/results-filter])
        interpr-filter (rf/subscribe [:modes/interpr-filter])
        is-filtered? (rf/subscribe [:modes/->is-filtered? varorder])
        ;; _ (println is-filtered?)
        [open set-open] (hooks/use-state is-filtered?)
        set-filtered-results #(rf/dispatch [:modes/set-results-filter
                                            {:next-results-filter %}])
        reset-filter-results #(rf/dispatch [:modes/reset-results-filter])
        set-filtered-interpr #(rf/dispatch [:modes/set-interpr-filter
                                            {:next-interpr-filter %}])
        reset-filter-interpr #(rf/dispatch [:modes/reset-interpr-filter])]
    ($d Root
        {:class (css "ValueFilter"
                     :gap-2
                     {:display "flex"
                      :align-items "start"
                      :flex-direction "column"})
         :open open
         :onOpenChange set-open}
        nil
        ($d Trigger
            {:class (css ["&:focus-visible"
                          :outline-none :ring])
             :asChild true}
            ($ Toggle
               {:variant :outline
                :size :md}
               ($ lucide-icons/Filter
                  {:className (css {:height "0.8rem"
                                    :width  "0.8rem"
                                    :stroke "currentColor"
                                    :color "currentColor"})})
               (d/span {:class (css :ml-2)} "Filter")))
        ($d Content
            {:class (css
                     {:display "grid"
                      :column-gap "0.6rem"
                      :row-gap "0.4rem"
                      :grid-template-columns "auto 4fr"})}
            ($ OptLabel "Interpr. filter:")
            ($ InterpretationFilterUI
               {:interpr-filter interpr-filter
                :filter-interpr-handler set-filtered-interpr
                :reset-filter-interpr-handler reset-filter-interpr
                :varorder (vec varorder)})
            ($ OptLabel "Results filter:")
            ($ ResultsFilterUI
               {:results-filter results-filter
                :filter-results-handler set-filtered-results
                :reset-filter-results-handler reset-filter-results})))))
