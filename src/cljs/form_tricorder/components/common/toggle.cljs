(ns form-tricorder.components.common.toggle
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+]]
   [form-tricorder.stitches-config :refer [css styled]]
   ["react" :as react]
   ["@radix-ui/react-toggle" :as TogglePrimitive]))

(def r) ;; hotfix for linting error in let+


(def Root
  (styled (.-Root TogglePrimitive)
          {:touch-action "manipulation"
           :display "inline-flex"
           :justify-content "center"
           :align-items "center"
           :border-radius "$md"
           :_text ["$sm"]
           :font-weight "$normal"
           :_transition_colors []
           
           "&:focus-visible"
           {:_outlineNone []}
           "&:disabled"
           {:pointer-events "none"
            :opacity "0.5"}
           "&:hover"
           {:cursor "pointer"}

           :variants
           {:variant
            {:default
             {:background-color "transparent"}
             :outline
             {:border-width 1
              :background-color "transparent"}}

            :size
            {:default
             {:height "$10"
              :_paddingX "$3"}
             :sm
             {:height "$9"
              :_paddingX "$2-5"}
             :lg
             {:height "$11"
              :_paddingX "$5"}
             :icon
             {:padding "0"
              :height "$10"
              :width "$10"}
             :icon-sm
             {:padding "0"
              :height "$9"
              :width "$9"}}}

           :compoundVariants
           [{:variant :default
             :layer :outer
             :css {"&:hover"
                   {:background-color "$outer-accent"
                    :color "$outer-accent-fg"}
                   "&:focus-visible"
                   {:_ringOuter []}
                   "&[data-state=on]"
                   {:background-color "$outer-accent"
                    :color "$outer-fg"}}}
            {:variant :default
             :layer :inner
             :css {"&:hover"
                   {:background-color "$inner-accent"
                    :color "$inner-accent-fg"}
                   "&:focus-visible"
                   {:_ringInner []}
                   "&[data-state=on]"
                   {:background-color "$inner-accent"
                    :color "$inner-fg"}}}

            {:variant :outline
             :layer :outer
             :css {:border-color "$outer-input"
                   "&:hover"
                   {:background-color "$outer-accent"
                    :color "$outer-accent-fg"}
                   "&:focus-visible"
                   {:_ringOuter []}
                   "&[data-state=on]"
                   {:background-color "$outer-accent"
                    :color "$outer-fg"}}}
            {:variant :outline
             :layer :inner
             :css {:border-color "$inner-input"
                   "&:hover"
                   {:background-color "$inner-accent"
                    :color "$inner-accent-fg"}
                   "&:focus-visible"
                   {:_ringInner []}
                   "&[data-state=on]"
                   {:background-color "$inner-accent"
                    :color "$inner-fg"}}}]

           :defaultVariants
           {:variant "default"
            :size "default"
            :layer :outer}}))

(defnc Toggle
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer variant size]
          :rest r} props]
    ($d Root
      {:class (string/join " " (remove nil? [className class]))
       :variant (or variant js/undefined)
       :size (or size js/undefined)
       :layer (or layer js/undefined)
       :ref ref
       & r})))

