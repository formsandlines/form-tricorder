(ns form-tricorder.components.common.button
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+]]
   [form-tricorder.stitches-config :refer [css styled]]
   ["react" :as react]
   ["@radix-ui/react-slot" :refer [Slot]]
   ;; ["@stitches/react" :refer [css]]
   ))

(def r) ;; hotfix for linting error in let+

;; (def css> #(-> % clj->js css))

(def styles
  {:touch-action "manipulation"
   :display "inline-flex"
   :justify-content "center"
   :align-items "center"
   :white-space "nowrap"
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
     {}

     :destructive
     {}

     :outline
     {:border-width 1}

     :secondary
     {}

     :ghost
     {}

     :link
     {:text-underline-offset 4
      "&:hover"
      {:text-decoration-line "underline"}}}

    :size
    {:default {:height "$10"
               :_paddingX "$4"
               :_paddingY "$3"}

     :sm   {:height "$9"
            :border-radius "$base"
            :_paddingX "$3"
            :_paddingY "$3"}

     :lg   {:height "$11"
            :border-radius "$md"
            :_paddingX "$6"
            :_paddingY "$3"}

     :icon {:padding 0
            :height "$10"
            :width "$10"}}

    :layer
    {:outer {"&:focus-visible" {:_ringOuter []}}
     :inner {"&:focus-visible" {:_ringInner []}}}}

   :compoundVariants
   [{:variant :default
     :layer :outer
     :css {:background-color "$outer-primary"
           :color "$outer-primary-fg"
           "&:hover"
           {:background-color
            "color-mix(in srgb, $colors$outer-primary 85%, $colors$n30)"}}}
    {:variant :default
     :layer :inner
     :css {:background-color "$inner-primary"
           :color "$inner-primary-fg"
           "&:hover"
           {:background-color
            "color-mix(in srgb, $colors$inner-primary 85%, $colors$n30)"}}}

    {:variant :secondary
     :layer :outer
     :css {:background-color "$outer-secondary"
           :color "$outer-secondary-fg"
           "&:hover"
           {:background-color
            "color-mix(in srgb, $colors$outer-secondary 95%, $colors$n30)"}}}
    {:variant :secondary
     :layer :inner
     :css {:background-color "$inner-secondary"
           :color "$inner-secondary-fg"
           "&:hover"
           {:background-color
            "color-mix(in srgb, $colors$inner-secondary 95%, $colors$n30)"}}}
    
    {:variant :destructive
     :layer :outer
     :css {:background-color "$outer-destructive"
           :color "$outer-destructive-fg"
           "&:hover"
           {:background-color
            "color-mix(in srgb, $colors$outer-destructive 85%, $colors$n30)"}}}
    {:variant :destructive
     :layer :inner
     :css {:background-color "$inner-destructive"
           :color "$inner-destructive-fg"
           "&:hover"
           {:background-color
            "color-mix(in srgb, $colors$inner-destructive 85%, $colors$n30)"}}}

    {:variant :outline
     :layer :outer
     :css {:border-color "$outer-input"
           :background-color "$outer-bg"
           "&:hover"
           {:background-color "$outer-accent"
            :color "$outer-accent-fg"}}}
    {:variant :outline
     :layer :inner
     :css {:border-color "$inner-input"
           :background-color "$inner-bg"
           "&:hover"
           {:background-color "$inner-accent"
            :color "$inner-accent-fg"}}}

    {:variant :ghost
     :layer :outer
     :css {"&:hover"
           {:background-color "$outer-accent"
            :color "$outer-accent-fg"}}}
    {:variant :ghost
     :layer :inner
     :css {"&:hover"
           {:background-color "$inner-accent"
            :color "$inner-accent-fg"}}}

    {:variant :link
     :layer :outer
     :css {:color "$outer-primary"}}
    {:variant :link
     :layer :inner
     :css {:color "$inner-primary"}}]
   
   :defaultVariants
   {:variant "default"
    :layer :outer
    :size "default"}})

(def dButton (styled "button" styles))
(def Slot (styled Slot styles))

(defnc Button
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className variant size layer asChild children]
          :or {asChild false}
          :rest r} props]
    ($d (if asChild Slot dButton)
      {:class (string/join " " (remove nil? [className class]))
       :variant (or variant js/undefined)
       :size (or size js/undefined)
       :layer (or layer js/undefined)
       :ref ref
       & r}
      children)))
