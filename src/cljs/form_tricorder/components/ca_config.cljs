(ns form-tricorder.components.ca-config
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [formform.calc :as ff-calc]
   [formform.emul :as ff-emul]
   [formform.emul.core :as ff-emul-core]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.function-opts
    :refer [FuncOpts FuncOptsGroup FuncOptHead
            $$radio-const-styles $$slider-const-styles]]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
   [form-tricorder.components.common.toggle-group
    :refer [ToggleGroup ToggleGroupItem]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   [form-tricorder.components.common.checkbox :refer [Checkbox]]
   [form-tricorder.components.common.input :refer [Input]]
   [form-tricorder.components.common.slider :refer [Slider]]
   [form-tricorder.components.common.slider2d :refer [Slider2D]]
   [form-tricorder.components.common.select
    :refer [Select SelectTrigger SelectValue SelectItem SelectContent]]
   [form-tricorder.utils :as utils :refer [let+ unite]]
   ;; ["@radix-ui/react-icons" :as radix-icons]
   ["lucide-react" :as lucide-icons]
   ["@radix-ui/react-popover" :as PopoverPrimitive]
   ["@radix-ui/react-icons" :as radix-icons]
   ["@radix-ui/react-collapsible" :as Collapsible]))

(def r) ;; hotfix for linting error in let+

(defn ini->str
  [ini]
  (str ini))


(defn ca-type->dim
  [ca-type]
  (case ca-type
    :selfi :1d
    :2d))

(defnc CaOptsResolution
  [{:keys [res set-res dim]}]
  (let [preset-kws [:small :medium :large]
        preset-res [[51 51] [151 151] [301 301]]
        kw->res (zipmap preset-kws preset-res)
        res->kw (zipmap preset-res preset-kws)
        current-kw (if-let [kw (res->kw res)] kw :custom)
        ]
    ($ FuncOptsGroup
       {:dir :column
        :aria-labelledby "ca-res-head"}
       ($ Label {:id "ca-res-head"
                 :html-for "ca-res-preset"} "Resolution:")
       (d/div
        {:class (css :gap-3
                     {:display "flex"})}
        ($d Select
            {:value current-kw
             :onValueChange (fn [kw]
                              (if (= :custom kw)
                                (set-res res)
                                (set-res (kw->res kw))))}
            ($ SelectTrigger
               {:id "ca-res-preset"
                :style {:width "6rem"}}
               ($d SelectValue
                   (name current-kw)))
            ($ SelectContent
               {:class "inner"}
               (for [kw preset-kws]
                 ($ SelectItem
                    {:key (name kw)
                     :value kw}
                    (name kw)))))
        (d/div
         {:class (css :gap-2
                      {:display "flex"
                       :align-items "center"})}
         ($ Input
            {:class (css :w-20)
             :id "ca-res-x"
             :type "number"
             :step "1"
             :min "5"
             :max "800"
             :value (first res)
             :onChange
             (fn [e]
               (let [next-res-x (try
                                  (parse-long (.. e -target -value))
                                  (catch js/Error err
                                    (js/console.error err)))]
                 (set-res [next-res-x (second res)])))})
         (when (= :2d dim)
           (<>
            (d/span "×")
            ($ Input
               {:class (css :w-20)
                :id "ca-res-y"
                :type "number"
                :step "1"
                :min "1"
                :max "800"
                :value (second res)
                :onChange
                (fn [e]
                  (let [next-res-y (try
                                     (parse-long (.. e -target -value))
                                     (catch js/Error err
                                       (js/console.error err)))]
                    (set-res [(first res) next-res-y])))}))))))))


(defnc WeightsPopover
  [{:keys [disabled? rand-weights set-rand-weights children]}]
  (let [[open? set-open?] (hooks/use-state false)]
    ($d (.-Root PopoverPrimitive)
      {:open open?
       :onOpenChange #(set-open? %)
       :modal false}
      ($d (.-Trigger PopoverPrimitive)
        {:asChild true}
        children)
      ($d (.-Portal PopoverPrimitive)
        ($d (.-Content PopoverPrimitive)
          {:align "start"
           :side "bottom"
           :sideOffset 6
           :collisionPadding 10
           :onInteractOutside #(.preventDefault %)
           :sticky "always"
           :class (css "outer"
                       :py-6 :pl-6 :pr-14 :text-sm :shadow-md
                       ;; :bg-popover
                       :fg-popover
                       :border :border-col :rounded-md
                       {
                        ;; :width "70vw"
                        ;; :min-width "260px"
                        ;; :max-width "200px"
                        ;; :max-height "600px"
                        :overflow "auto"
                        :z-index "11"
                        :background-color "color-mix(in srgb, var(--col-bg-popover) 96%, transparent)"})}
          ($d (.-Close PopoverPrimitive)
            {:asChild true}
            ($ Button
               {:class (css :top-4 :right-4
                            {:position "absolute"
                             :outline "0.4rem solid var(--col-bg)"
                             :z-index "12"})
                :variant :secondary
                :size :icon-sm}
               ($d radix-icons/Cross2Icon)))
          (d/div
            {:class (css :w-64 :gap-x-3 :gap-y-1-5 :font-mono :text-sm
                         {:display "grid"
                          ;; :column-gap "0.6rem"
                          ;; :row-gap "0.4rem"
                          :grid-template-columns "auto 1fr auto"})}
            (for [c utils/consts
                  :let [id (str "ini-weights-" (name c))
                        weight (c rand-weights)]]
              (<>
                {:key id}
                ($ Label
                   {:class (css {:font-style "italic"})
                    :htmlFor id}
                   (name c))
                ($ Slider
                   {:class ($$slider-const-styles c)
                    :id id
                    :value [weight]
                    :disabled disabled?
                    :onValueChange (fn [arr]
                                     (let [v (aget arr 0)
                                           next-weights (assoc rand-weights c v)]
                                       (set-rand-weights next-weights)))
                    :min 0
                    :max 1
                    :step 0.05})
                (d/div
                  {:class (css :text-xs :fg-muted)}
                  (str (when weight (.toFixed weight 3))))
                #_
                (d/div (str (utils/pad (utils/<&> "&nbsp;") 3
                                       (str (Math/round (* 100 weight))))
                            (utils/<&> "&thinsp;") "%"))
                )))))))
  )

(defnc CaOptsRandom
  [{:keys [disabled? set-rand-weights rand-weights]}]
  (let [[rand-density set-rand-density] (hooks/use-state
                                         (utils/calc-density rand-weights))]
    ($ FuncOptsGroup
       {:dir :column}
       ($ FuncOptsGroup
          {:class (css :gap-2
                       {:align-items "center"})
           :dir :row}
          ($ FuncOptsGroup
             {:class (css {:align-items "center"})
              :dir :row
              :aria-labelledby "ca-bg-density-head"}
             ($ Label {:id "ca-bg-density-head"
                       :html-for "ca-bg-density"} "Density:")
             ($ Input
                {:class (css :w-24)
                 :id "ca-bg-density"
                 :type "number"
                 :step "0.05"
                 :min "0.0"
                 :max "1.0"
                 :value rand-density
                 :disabled disabled?
                 :onChange
                 (fn [e]
                   (let [next-density (max 0.0
                                           (min 1.0
                                                (parse-double
                                                 (.. e -target -value))))
                         next-weights (utils/apply-density-on-weights
                                       next-density rand-weights)]
                     (set-rand-density next-density)
                     (set-rand-weights next-weights)))}))
          ($ Button
             {:variant :secondary
              :size :sm
              :disabled disabled?
              :title "balance random weights"
              :onClick (fn [_]
                         (set-rand-density (utils/calc-density
                                            utils/equal-weights))
                         (set-rand-weights utils/equal-weights))}
             ($d lucide-icons/Equal
               {:class (css :size-icon-sm)}))
          ($ WeightsPopover
             {:disabled? disabled?
              :rand-weights rand-weights
              :set-rand-weights (fn [next-weights]
                                  (set-rand-density (utils/calc-density
                                                     next-weights))
                                  (set-rand-weights next-weights))}
             ($ Button
                {:variant :primary
                 :size :sm
                 :disabled disabled?
                 :title "set random weights by value"
                 :onClick (fn [_] nil)}
                ($d radix-icons/MixerHorizontalIcon
                  {:class (css :size-icon-sm)})))))))


(def $options-subgrid
  (css :gap-x-2 :gap-y-3
       {:display "grid"
        :align-content "start"
        :grid-template-columns "auto 1fr"}
       ["& > *:first-child, & > *:nth-child(2)"
        {:align-self "center"}]
       ["& > *:nth-child(3), & > *:nth-child(4)"
        {:align-self "start"}]))

(defnc CaOptsIniBackground
  [{:keys [dim ini set-ini reset-ini]}]
  (let [{:keys [bg-type const cycle-vals rand-weights]} (:bg ini)]
    ($ FuncOptsGroup
       {:class (css :gap-6
                    ["& > *:last-child"
                     {:flex "none"}])
        :dir :row}
       ($ RadioGroup
          {:class (css "IniType"
                       :gap-6
                       {:display "flex"})
           :value (name bg-type)
           :aria-label "CA ini type"
           :orientation "horizontal"
           :onValueChange #(set-ini (assoc-in ini [:bg :bg-type] (keyword %)))}
          (d/div
            {:class $options-subgrid}
            ($ RadioGroupItem
               {:id "bg-ini-const"
                :value "constant"})
            ($ Label
               {:htmlFor "bg-ini-const"}
               "Constant:")
            (d/div {:class "empty-grid-cell"})
            ($ RadioGroup
               {:value (name const)
                :id "ca-bg-const"
                :aria-label "CA ini constant"
                :onValueChange (fn [s]
                                 (set-ini
                                  (assoc-in ini [:bg :const] (keyword s))))
                :class (css :font-mono)
                :orientation "horizontal"
                :group-variant :joined
                :variant :outline
                :disabled (not= :constant bg-type)
                :size :sm}
               (for [c utils/consts
                     :let [id (str "ini-bg-const-" (name c))]]
                 ($ RadioGroupItem
                    {:id id
                     :key id
                     :class ($$radio-const-styles c)
                     :value (name c)}
                    (d/i (utils/pp-val c))))))
          (d/div
            {:class $options-subgrid}
            ($ RadioGroupItem
               {:id "bg-ini-random"
                :value "random"})
            ($ Label
               {:htmlFor "bg-ini-random"}
               "Random:")
            (d/div {:class "empty-grid-cell"})
            ($ CaOptsRandom
               {:disabled? (not= :random bg-type)
                :rand-weights rand-weights
                :set-rand-weights
                #(set-ini (assoc-in ini [:bg :rand-weights] %))}))
          (d/div
            {:class $options-subgrid}
            ($ RadioGroupItem
               {:id "bg-ini-cycle"
                :value "cycle"})
            ($ Label
               {:htmlFor "bg-ini-cycle"}
               "Cycling values:")
            (d/div {:class "empty-grid-cell"})
            ($ Input
               {:class (css :w-42)
                :id "ca-bg-cycle"
                :type "text"
                :value (string/join (mapv name cycle-vals))
                :disabled (not= :cycle bg-type)
                :onChange (fn [e]
                            (let [s (.. e -target -value)
                                  xs (mapv (comp keyword str) s)]
                              (when (every? utils/consts-set xs)
                                (set-ini (assoc-in ini [:bg :cycle-vals] xs)))))
                :placeholder "nu | inm | uium | …"})))
       ($ Button
          {:variant :destructive
           :size :sm
           :title "reset to default settings"
           :onClick reset-ini}
          "reset"))))

(defnc CaOptsIniFigureInstances
  [{:keys [dim ini set-ini]}]
  (let [{:keys [copies spacing]} (:figure ini)]
    ($ FuncOptsGroup
       {:class (css :gap-3)
        :dir :column}
       ($ FuncOptHead "Instances:")
       ($ FuncOptsGroup
          {:class (css :gap-3)
           :dir :row}
          ($ FuncOptsGroup
             {:class (css {:align-items "center"})
              :dir :row
              :aria-labelledby "ca-figure-copies-x-head"}
             ($ Label {:id "ca-figure-copies-x-head"
                       :html-for "ca-copies-x"}
                ($d radix-icons/DotsHorizontalIcon
                  {:class (css :size-icon-sm)}))
             ($ Input
                {:class (css :w-20)
                 :id "ca-copies-x"
                 :type "number"
                 :step "1"
                 :min "1"
                 :max "10"
                 :value (first copies)
                 :onChange
                 (fn [e]
                   (let [next-copies-x
                         (try (max 1 (parse-long (.. e -target -value)))
                              (catch js/Error err (js/console.error err)))]
                     (set-ini (assoc-in ini [:figure :copies 0]
                                        next-copies-x))))}))
          ($ FuncOptsGroup
             {:class (css {:align-items "center"})
              :dir :row
              :aria-labelledby "ca-figure-spacing-x-head"}
             ($ Label {:id "ca-figure-spacing-x-head"
                       :html-for "ca-spacing-x"}
                ($d radix-icons/ColumnSpacingIcon
                  {:class (css :size-icon-sm)}))
             ($ Input
                {:class (css :w-20)
                 :id "ca-spacing-x"
                 :type "number"
                 :step "1"
                 :min "0"
                 :max "100"
                 :value (first spacing)
                 :onChange
                 (fn [e]
                   (let [next-spacing-x (try (parse-long (.. e -target -value))
                                             (catch js/Error err
                                               (js/console.error err)))]
                     (set-ini (assoc-in ini [:figure :spacing 0]
                                        next-spacing-x))))})))
       (when (= dim :2d)
         ($ FuncOptsGroup
            {:class (css :gap-3)
             :dir :row}
            ($ FuncOptsGroup
               {:class (css {:align-items "center"})
                :dir :row
                :aria-labelledby "ca-figure-copies-y-head"}
               ($ Label {:id "ca-figure-copies-y-head"
                         :html-for "ca-copies-y"}
                  ($d radix-icons/DotsVerticalIcon
                    {:class (css :size-icon-sm)}))
               ($ Input
                  {:class (css :w-20)
                   :id "ca-copies-y"
                   :type "number"
                   :step "1"
                   :min "1"
                   :max "10"
                   :value (second copies)
                   :onChange
                   (fn [e]
                     (let [next-copies-y
                           (try (max 1 (parse-long (.. e -target -value)))
                                (catch js/Error err (js/console.error err)))]
                       (set-ini (assoc-in ini [:figure :copies 1]
                                          next-copies-y))))}))
            ($ FuncOptsGroup
               {:class (css {:align-items "center"})
                :dir :row
                :aria-labelledby "ca-figure-spacing-y-head"}
               ($ Label {:id "ca-figure-spacing-y-head"
                         :html-for "ca-spacing-y"}
                  ($d radix-icons/RowSpacingIcon
                    {:class (css :size-icon-sm)}))
               ($ Input
                  {:class (css :w-20)
                   :id "ca-spacing-y"
                   :type "number"
                   :step "1"
                   :min "0"
                   :max "100"
                   :value (second spacing)
                   :onChange
                   (fn [e]
                     (let [next-spacing-y (try (parse-long (.. e -target -value))
                                               (catch js/Error err
                                                 (js/console.error err)))]
                       (set-ini (assoc-in ini [:figure :spacing 1]
                                          next-spacing-y))))})))))))

(def align-kws
  [:topleft
   :topcenter
   :topright

   :centerleft
   :center
   :centerright
   
   :bottomleft
   :bottomcenter
   :bottomright])

(def align-kw->pos
  (zipmap
   align-kws
   (let [pos [0.0 0.5 1.0]]
     (for [y pos
           x pos]
       [x y]))))

(def align-kws-1d
  (take 3 (drop 3 align-kws)))

(defn align->icon
  [dim dir]
  (dir (zipmap align-kws
               [lucide-icons/ArrowDownRight
                lucide-icons/ArrowDownFromLine
                lucide-icons/ArrowDownLeft

                lucide-icons/ArrowRightFromLine
                (if (= dim :2d) lucide-icons/Move
                    lucide-icons/UnfoldHorizontal)
                lucide-icons/ArrowLeftFromLine

                lucide-icons/ArrowUpRight
                lucide-icons/ArrowUpFromLine
                lucide-icons/ArrowUpLeft])))

(defnc CaOptsIniFigurePosition
  [{:keys [dim ini set-ini]}]
  (let [{:keys [pos align]} (:figure ini)
        [linked? set-linked?] (hooks/use-state true)
        [slider2d-xy set-slider2d-xy] (hooks/use-state pos)]
    (hooks/use-effect
      [pos]
      (when-not (= slider2d-xy pos)
        (set-slider2d-xy pos)))
    (d/div
      {:class (css :gap-3
                   {:display "grid"
                    :align-content "start"
                    :grid-template-columns "auto auto auto"
                    :grid-template-rows "auto auto"
                    ;; :align-items "center"
                    }
                   ["& > *:nth-child(4), & > *:nth-child(5), & > *:nth-child(6)"
                    {:align-self "center"}]

                   )}
      ($ Label
         {:htmlFor "ca-fig-pos"}
         "Position:")
      (d/div {:class "empty-grid-cell"})
      ($ Label
         {:htmlFor "ca-fig-align"}
         "Align:")
      (if (= dim :2d)
        (d/div
          ($ Slider2D
             {:id "ca-fig-pos"
              :xmin 0.0
              :xmax 1.0
              :ymin 0.0
              :ymax 1.0
              :w 80
              :h 80
              :on-value-change (fn [rx ry]
                                 (set-slider2d-xy
                                  (mapv #(js/parseFloat (.toFixed % 2))
                                        [rx ry])))
              :on-value-commit (fn [rx ry]
                                 (let [new-xy (mapv #(js/parseFloat
                                                      (.toFixed % 2))
                                                    [rx ry])]
                                   (set-slider2d-xy new-xy)
                                   (set-ini
                                    (assoc-in ini [:figure :pos]
                                              new-xy))
                                   (set-linked? false)))
              :xvalue (first slider2d-xy)
              :yvalue (second slider2d-xy)}))
        (d/div
          {:class (css :w-26)}
          ($ Slider
             {:id "ca-fig-pos"
              :value [(first slider2d-xy)]
              :onValueChange (fn [arr]
                               (set-slider2d-xy
                                [(js/parseFloat (.toFixed (aget arr 0) 2))
                                 (second slider2d-xy)]))
              :onValueCommit (fn [arr]
                               (let [new-x (js/parseFloat
                                            (.toFixed (aget arr 0) 2))]
                                 (set-slider2d-xy
                                  [new-x (second slider2d-xy)])
                                 (set-ini
                                  (assoc-in ini [:figure :pos 0]
                                            new-x))
                                 (set-linked? false)))
              :min 0.0
              :max 1.0
              :step 0.01})))
      (d/div
        ($ Toggle
           {:variant :ghost
            :size :icon-sm
            ;; :disabled disabled?
            :pressed linked?
            :title "match position with alignment"
            :onPressedChange (fn [b]
                               (set-linked? b)
                               (set-ini
                                (assoc-in ini [:figure :pos]
                                          (align-kw->pos align))))
            }
           ($d radix-icons/DoubleArrowLeftIcon
             {:class (css :size-icon-sm)})))
      (d/div
        ($ RadioGroup
           {:value (name align)
            :id "ca-fig-align"
            :aria-label "CA ini figure alignment"
            :onValueChange (fn [s]
                             (set-ini
                              (update ini :figure
                                      (fn [m]
                                        (if linked?
                                          (assoc m
                                                 :align (keyword s)
                                                 :pos (align-kw->pos (keyword s)))
                                          (assoc m :align (keyword s)))))))
            :class (css :font-mono)
            :orientation "horizontal"
            :group-variant (if (= dim :2d) :grid :joined)
            :variant :outline
            ;; :disabled disabled?
            :size :icon-sm}
           (for [align-dir (if (= dim :2d) align-kws align-kws-1d)
                 :let [id (str "ini-fig-align-" (name align-dir))]]
             ($ RadioGroupItem
                {:id id
                 :key id
                 :value (name align-dir)}
                ($d (align->icon dim align-dir)
                  {:class (css :size-icon-sm)})))))
      ;; (d/span (name align))
      )))



(def ini-patterns-1d
  (->> utils/extended-ini-patterns-seq
       (remove (fn [[k _]]
                 (string/includes? (name k) "2d")))))

(def ini-patterns-2d
  utils/extended-ini-patterns-seq)

(defnc CaOptsIniFigure
  [{:keys [dim res ini set-ini reset-ini]}]
  (let [{:keys [fig-type pattern rand-res rand-weights rand-decay apply?]}
        (:figure ini)
        ;; [ini-fig-type set-ini-fig-type] (hooks/use-state :pattern)
        ;; [fig-pattern set-fig-pattern] (hooks/use-state :ball)
        ;; [fig-rand-res set-fig-rand-res] (hooks/use-state [5 5])
        ;; [fig-rand-decay set-fig-rand-decay] (hooks/use-state 0.0)
        ]
    (d/div
     {:class $options-subgrid}
     ($ Checkbox
        {:id "figure-ini"
         :checked apply?
         :aria-label "Enable CA figure ini?"
         :onCheckedChange #(set-ini (assoc-in ini
                                              [:figure :apply?]
                                              (not apply?)))})
     ($ Label
        {:id "figure-ini-head"
         :htmlFor "figure-ini"}
        "Figure")
     (d/div {:class "empty-grid-cell"})
     (when apply? ;; ? better use display: none
       ($ FuncOptsGroup
          {:class (css :gap-4 :mb-4
                       {:justify-content "flex-start"
                        :align-items "stretch"}
                       ["& > *:last-child"
                        :border-col :pt-4
                        {:border-top-width "1px"
                         :border-top-style "dashed"}])
           :dir :column
           :aria-labelledby "figure-ini-head"}
          ($ RadioGroup
             {:class (css "IniFigureType"
                          :gap-6
                          {:display "flex"
                           :flex-direction "row"})
              :value (name fig-type)
              :disabled (not apply?)
              :aria-label "CA ini figure type"
              :orientation "horizontal"
              :onValueChange (fn [s]
                               (set-ini (assoc-in ini
                                                  [:figure :fig-type]
                                                  (keyword s))))}
             (d/div
              {:class $options-subgrid}
              ($ RadioGroupItem
                 {:id "ini-fig-pattern"
                  :value "pattern"})
              ($ Label
                 {:htmlFor "ini-fig-pattern"}
                 "Pattern:")
              (d/div {:class "empty-grid-cell"})
              ($d Select
                  {:value pattern
                   :disabled (not= :pattern fig-type)
                   :onValueChange (fn [kw]
                                    (set-ini (assoc-in ini
                                                       [:figure :pattern]
                                                       kw)))}
                  ($ SelectTrigger
                     {:id "ca-fig-pattern"
                      :style {:width "6rem"}}
                     ($d SelectValue
                         (name pattern)))
                  ($ SelectContent
                     {:class "inner"}
                     (for [[kw ptn] (if (= :2d dim)
                                      ini-patterns-2d
                                      ini-patterns-1d)]
                       ($ SelectItem
                          {:key (name kw)
                           :value kw}
                          (name kw))))))
             (d/div
              {:class $options-subgrid}
              ($ RadioGroupItem
                 {:id "ini-fig-rand"
                  :value "random"})
              ($ Label
                 {:htmlFor "ini-fig-rand"}
                 "Random area:")
              (d/div {:class "empty-grid-cell"})
              ($ FuncOptsGroup
                 {:class (css :gap-6)
                  :dir :row}
                 ($ FuncOptsGroup
                    {:class (css :gap-3)
                     :dir :column}
                    (d/div
                      {:class (css :gap-2
                                   {:display "flex"
                                    :align-items "center"})}
                      ($ Input
                         {:class (css :w-20)
                          :id "ca-fig-rand-res-x"
                          :type "number"
                          :step "1"
                          :min "1"
                          :max (str (first res))
                          :value (first rand-res)
                          :disabled (not= :random fig-type)
                          :onChange
                          (fn [e]
                            (let [next-res-x
                                  (try (parse-long (.. e -target -value))
                                       (catch js/Error err
                                         (js/console.error err)))]
                              (set-ini
                               (assoc-in ini
                                         [:figure :rand-res]
                                         [next-res-x (second rand-res)]))))})
                      (when (= :2d dim)
                        (<>
                          (d/span "×")
                          ($ Input
                             {:class (css :w-20)
                              :id "ca-fig-rand-res-y"
                              :type "number"
                              :step "1"
                              :min "1"
                              :max (str (last res))
                              :value (second rand-res)
                              :disabled (not= :random fig-type)
                              :onChange
                              (fn [e]
                                (let [next-res-y
                                      (try (parse-long (.. e -target -value))
                                           (catch js/Error err
                                             (js/console.error err)))]
                                  (set-ini
                                   (assoc-in ini
                                             [:figure :rand-res]
                                             [(first rand-res) next-res-y]))))})))))
                 ($ CaOptsRandom
                    {:disabled? (not= :random fig-type)
                     :rand-weights rand-weights
                     :set-rand-weights
                     #(set-ini (assoc-in ini [:figure :rand-weights] %))}))))
          ($ FuncOptsGroup
             {:class (css :gap-8)
              :dir :row}
            ($ CaOptsIniFigurePosition
               {:dim dim
                :ini ini
                :set-ini set-ini})
            ($ CaOptsIniFigureInstances
               {:dim dim
                :ini ini
                :set-ini set-ini})
            ($ FuncOptsGroup
               {:class (css :gap-3)
                :dir :column}
               ;; ($ FuncOptHead "Instances:")
               ;; {:class (css :gap-x-2
               ;;              {:display "flex"
               ;;               :align-items "center"}
               ;;              ["& > *:nth-child(2)"
               ;;               :w-30])}
               ($ Label
                  {:htmlFor "ca-fig-rand-decay"}
                  "Decay:")
               (d/div
                 {:class (css :w-42 :gap-3
                           {:display "flex"})}
                 ($ Slider
                    {:id "ca-fig-rand-decay"
                     :value [rand-decay]
                     :onValueChange (fn [arr]
                                      (let [next-decay (aget arr 0)]
                                        (set-ini
                                         (assoc-in ini
                                                   [:figure :rand-decay]
                                                   next-decay))))
                     :min 0
                     :max 1
                     :step 0.01})
                 (d/div
                   {:class (css {:white-space "nowrap"})}
                   (str (Math/round (* 100 rand-decay))
                        (utils/<&> "&thinsp;") "%")))
               #_
               (d/div
                 (str (.toFixed fig-rand-decay 2)))
               #_
               (d/div (str (utils/pad (utils/<&> "&nbsp;") 3
                                      (str (Math/round (* 100 weight))))
                           (utils/<&> "&thinsp;") "%"))
               )))))))

(defnc CaOptsGeneral
  [{:keys [dim res set-res cell-size set-cell-size seed set-seed]}]
  (d/div
   {:class (css :gap-12
                {:display "flex"}
                ["& > *"
                 {:white-space "nowrap"}])}
   ($ CaOptsResolution
      {:res res
       :set-res set-res
       :dim dim})
   ($ FuncOptsGroup
      {:dir :column
       :aria-labelledby "ca-cellsize-head"}
      ($ Label {:id "ca-cellsize-head"
                :html-for "ca-cellsize"} "Cell Size:")
      ($ Input
         {:class (css :w-16)
          :id "ca-cellsize"
          :type "number"
          :step "1"
          :min "1"
          :max "10"
          :value cell-size
          :onChange
          (fn [e]
            (let [next-cell-size (try (parse-long (.. e -target -value))
                                      (catch js/Error err
                                        (js/console.error err)))]
              (set-cell-size next-cell-size)))}))
   ($ FuncOptsGroup
      {:dir :column}
      ($ Label {:id "ca-seed-head"
                :html-for "ca-seed"} "Random Seed:")
      (d/div
       {:class (css {:display "flex"})}
       ($ Input
          {:class (css :w-38
                       {:border-bottom-right-radius "0"
                        :border-top-right-radius "0"
                        :border-right "none"})
           :id "ca-seed"
           :type "number"
           :step "1"
           :min "0"
           :max "1000000000"
           :value seed
           :onChange
           (fn [e]
             (let [next-seed (try (parse-long (.. e -target -value))
                                  (catch js/Error err
                                    (js/console.error err)))]
               (set-seed next-seed)))})
       ($ Button
          {:variant :outline
           :size :icon
           :disabled false
           :title "randomly choose a random seed"
           :onClick #(set-seed (utils/gen-ca-seed))}
          ($d lucide-icons/Dices
              {:class (css :size-icon-sm)}))))))

(def Root (.-Root Collapsible))
(def Trigger (.-Trigger Collapsible))
(def Content (.-Content Collapsible))

(defnc CaConfig
  [{:keys [ca-type res cell-size seed]}]
  (let [;; TODO: replace with subscriptions
        ;; debug? true
        debug? false
        ;; dim :2d
        dim (ca-type->dim ca-type)
        set-res #(rf/dispatch [:modes/set-ca-res {:next-res %}])
        set-cell-size #(rf/dispatch [:modes/set-ca-cell-size
                                     {:next-cell-size %}])
        set-seed #(rf/dispatch [:modes/set-ca-seed {:next-seed %}])
        ini (rf/subscribe [:modes/ca-ini])
        set-ini #(rf/dispatch [:modes/set-ca-ini {:next-ca-ini %}])
        reset-ini #(rf/dispatch [:modes/reset-ca-ini])
        [open set-open] (hooks/use-state true)]
    ($ FuncOptsGroup
       {:class (css :gap-3
                    ["& > *:first-child"
                     :pt-0
                     {:border-top "none"}]
                    ["& > *"
                     :pt-3 :border-col
                     {:width "100%"
                      :border-top-width "1px"
                      :border-top-style "dashed"}])
        :dir :column}
       (d/div
        ($ CaOptsGeneral {:dim           dim
                          :res           res
                          :set-res       set-res
                          :cell-size     cell-size
                          :set-cell-size set-cell-size
                          :seed          seed
                          :set-seed      set-seed}))
       ($d Root
           {:open open
            :onOpenChange set-open}
           (d/div
            {:class $options-subgrid}
            ($d Trigger
                {:asChild true}
                ($ Button
                   {:variant :ghost
                    :size :icon-sm}
                   ($d (if open
                         radix-icons/TriangleDownIcon
                         radix-icons/TriangleRightIcon)
                       {:class (css :size-icon-sm)})))
            ($ FuncOptHead "SysIni")
            (d/div {:class "empty-grid-cell"})
            ($d Content
                ($ FuncOptsGroup
                   {:dir :column}
                   (d/div
                    {:class (css {:width "100%"})}
                    ($ CaOptsIniBackground {:dim dim
                                            :ini ini
                                            :set-ini set-ini
                                            :reset-ini reset-ini}))
                   (d/div
                    {:class (css :mt-2 :pt-3
                                 :border-col
                                 {:width "100%"
                                  :border-top-width "1px"
                                  :border-top-style "dashed"})}
                    ($ CaOptsIniFigure {:dim dim
                                        :res res
                                        :ini ini
                                        :set-ini set-ini
                                        :reset-ini reset-ini}))))))
       (when debug?
         (<>
          (d/pre (str res))
          (d/pre (str cell-size))
          (d/pre (str seed)))))))

