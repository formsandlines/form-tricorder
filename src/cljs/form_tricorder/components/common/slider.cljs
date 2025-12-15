(ns form-tricorder.components.common.slider
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]
   ["@radix-ui/react-slider" :as SliderPrimitive]))

(def r) ;; hotfix for linting error in let+

(def SliderRoot (.-Root SliderPrimitive))
(def SliderTrack (.-Track SliderPrimitive))
(def SliderRange (.-Range SliderPrimitive))
(def SliderThumb (.-Thumb SliderPrimitive))

(defnc Slider
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className default-value value min max]
          :or {min 0 max 100}
          :rest r} props
         _values (hooks/use-memo
                  [value default-value min max]
                  (cond
                    (sequential? value) value
                    (sequential? default-value) default-value
                    :else [min max])
                  ;; (cond
                  ;;   (.. js/Array (isArray value)) (js->clj value)
                  ;;   (.. js/Array (isArray default-value)) (js->clj default-value)
                  ;;   :else [min max])
                  )]
    ($d SliderRoot
      {:class (unite className class
                     (css {:width "100%"
                           :display "flex"
                           :position "relative"
                           :align-items "center"
                           :user-select "none"
                           :touch-action "none"}
                          ["&[data-disabled]"
                           {:opacity "0.5"}]
                          ["&[data-orientation=vertical]"
                           :w-auto :min-h-44
                           {:height "100%"
                            :flex-direction "column"}]))
       "data-slot" "slider"
       :default-value (clj->js default-value)
       :value (clj->js value)
       :min min
       :max max
       :ref ref
       & r}
      ($d SliderTrack
        {:class (unite className class
                       (css :bg-muted :rounded-full
                            {:position "relative"
                             :flex-grow 1
                             :overflow "hidden"}
                            ["&[data-orientation=horizontal]"
                             :h-1-5
                             {:width "100%"}]
                            ["&[data-orientation=vertical]"
                             :w-1-5
                             {:height "100%"}]))
         "data-slot" "slider-track"}
        ($d SliderRange
            {:class (unite className class
                           (css
                            :bg-primary
                            {:position "absolute"}
                            ["&[data-orientation=horizontal]"
                             {:height "100%"}]
                            ["&[data-orientation=vertical]"
                             {:width "100%"}]))
             "data-slot" "slider-range"}))
      (for [i (range (count _values))]
        ($d SliderThumb
          {:class (unite className class
                         (css :size-4 :rounded-full :border :bg
                              :border-primary :ring :shadow-sm
                              {:display "block"
                               :flex-shrink 0
                               :cursor "pointer"}
                              ;; missing:
                              ;; transition-[color,box-shadow]
                              ["&:hover"
                               :bg-accent :ring] ;; ring-4
                              ["&:focus-visible"
                               :ring :outline-none] ;; ring-4
                              ["&:disabled"
                               {:pointer-events "none"
                                :opacity "0.5"}]))
           "data-slot" "slider-thumb"
           :key (str i)})))))

