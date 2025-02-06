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
   [form-tricorder.utils :as utils :refer [let+ unite]]))


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


(defnc InterpretationFilterUI
  [{:keys [interpr-filter filter-interpr-handler varorder]}]
  (let [{:keys [neg-op? op terms-filter vals-filter]} interpr-filter]
    (d/div
     {:class (css "InterpretationFilter"
                  :gap-4
                  {:display "flex"
                   :align-items "center"})}
     (d/div
      {:class "InterpretationFilterNegOp"}
      ($ Toggle
         {:variant :outline
          :size :sm
          ;; :style {:height "var(--sz-2)"}
          :pressed neg-op?
          :onPressedChange (fn [b] (filter-interpr-handler
                                   (assoc interpr-filter :neg-op? b)))}
         "¬"))
     (d/div
      {:class (css "InterpretationValsFilter"
                   :gap-2
                   {:display "flex"
                    :align-items "center"})}
      ($ Button
         {:variant :outline
          :size :sm
          :style {:height "var(--sz-2)"}
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
          :variant :outline
          :size :sm}
         (for [c utils/consts]
           ($ ToggleGroupItem
              {:key (str "filter-interpr-vals-" (name c))
               :class ($$toggle-const-styles c)
               :value (name c)}
              (d/i (utils/pp-val c))))))
     (d/div
      {:class "InterpretationFilterOp"}
      ($ ToggleGroup
         {:type "single"
          :value (name op)
          :onValueChange (fn [s] (filter-interpr-handler
                                 (assoc interpr-filter :op (keyword s))))
          :orientation "vertical"
          :group-variant :joined
          :variant :outline
          :size :sm}
         (for [[k s label] [[:intersects "∩" "intersects"]
                            [:subseteq "⊇" "is subset of"]
                            [:equal "=" "is equal to"]]]
           ($ ToggleGroupItem
              {:key (str "filter-interpr-op" (name k))
               :title label
               :value (name k)}
              s))))
     (d/div
      {:class (css "InterpretationTermsFilter"
                   :gap-2
                   {:display "flex"})}
      (for [[i filter] (map-indexed vector terms-filter)
            :let [v (varorder i)]]
        (d/div
         {:key (str "filter-interpr-term-" i)
          :class (css :pr-2
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
                     (str i)))))))))


(defnc ResultsFilterUI
  [{:keys [results-filter filter-results-handler]}]
  (d/div
   {:class (css "ResultsFilter"
                :gap-2
                {:display "flex"
                 :align-items "center"})}
   ($ Button
      {:variant :outline
       :size :sm
       :style {:height "var(--sz-2)"}
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
           (d/i (utils/pp-val c)))))))

(defnc ValueFilter
  [{:keys [varorder]}]
  (let [results-filter (rf/subscribe [:modes/results-filter])
        interpr-filter (rf/subscribe [:modes/interpr-filter])
        set-filtered-results #(rf/dispatch [:modes/set-results-filter
                                            {:next-results-filter %}])
        set-filtered-interpr #(rf/dispatch [:modes/set-interpr-filter
                                            {:next-interpr-filter %}])
        ;; _ (println results-filter)
        ]
    (d/div
     {:class (css "ValueFilter"
                  :gap-2
                  {:display "flex"
                   :flex-direction "column"})}
     nil
     ($ InterpretationFilterUI
        {:interpr-filter interpr-filter
         :filter-interpr-handler set-filtered-interpr
         :varorder (vec varorder)})
     ($ ResultsFilterUI
        {:results-filter results-filter
         :filter-results-handler set-filtered-results})
     )))
