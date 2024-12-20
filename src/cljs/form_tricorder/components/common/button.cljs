(ns form-tricorder.components.common.button
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as str]
   [form-tricorder.utils :refer [let+]]
   ["react" :as react]
   ["@radix-ui/react-slot" :refer [Slot]]))

(def r) ;; hotfix for linting error in let+

;; (def css> #(-> % clj->js css))

;; (def styles
;;   {:touch-action "manipulation"
;;    :display "inline-flex"
;;    :justify-content "center"
;;    :align-items "center"
;;    :white-space "nowrap"
;;    :border-radius "$md"
;;    :_text ["$sm"]
;;    :font-weight "$normal"
;;    :_transition_colors []
;;    "&:focus-visible"
;;    {:_outlineNone []}
;;    "&:disabled"
;;    {:pointer-events "none"
;;     :opacity "0.5"}
;;    "&:hover"
;;    {:cursor "pointer"}

;;    :variants
;;    {:variant
;;     {:default
;;      {}

;;      :destructive
;;      {}

;;      :outline
;;      {:border-width 1}

;;      :secondary
;;      {}

;;      :ghost
;;      {}

;;      :link
;;      {:text-underline-offset 4
;;       "&:hover"
;;       {:text-decoration-line "underline"}}}

;;     :size
;;     {:default {:height "$10"
;;                :_paddingX "$4"
;;                :_paddingY "$3"}

;;      :sm   {:height "$9"
;;             :border-radius "$base"
;;             :_paddingX "$3"
;;             :_paddingY "$3"}

;;      :lg   {:height "$11"
;;             :border-radius "$md"
;;             :_paddingX "$6"
;;             :_paddingY "$3"}

;;      :icon {:padding 0
;;             :height "$10"
;;             :width "$10"}}

;;     :layer
;;     {:outer {"&:focus-visible" {:_ringOuter []}}
;;      :inner {"&:focus-visible" {:_ringInner []}}}}

;;    :compoundVariants
;;    [{:variant :default
;;      :layer :outer
;;      :css {:background-color "$outer-primary"
;;            :color "$outer-primary-fg"
;;            "&:hover"
;;            {:background-color
;;             "color-mix(in srgb, $colors$outer-primary 85%, $colors$n30)"}}}
;;     {:variant :default
;;      :layer :inner
;;      :css {:background-color "$inner-primary"
;;            :color "$inner-primary-fg"
;;            "&:hover"
;;            {:background-color
;;             "color-mix(in srgb, $colors$inner-primary 85%, $colors$n30)"}}}

;;     {:variant :secondary
;;      :layer :outer
;;      :css {:background-color "$outer-secondary"
;;            :color "$outer-secondary-fg"
;;            "&:hover"
;;            {:background-color
;;             "color-mix(in srgb, $colors$outer-secondary 95%, $colors$n30)"}}}
;;     {:variant :secondary
;;      :layer :inner
;;      :css {:background-color "$inner-secondary"
;;            :color "$inner-secondary-fg"
;;            "&:hover"
;;            {:background-color
;;             "color-mix(in srgb, $colors$inner-secondary 95%, $colors$n30)"}}}
    
;;     {:variant :destructive
;;      :layer :outer
;;      :css {:background-color "$outer-destructive"
;;            :color "$outer-destructive-fg"
;;            "&:hover"
;;            {:background-color
;;             "color-mix(in srgb, $colors$outer-destructive 85%, $colors$n30)"}}}
;;     {:variant :destructive
;;      :layer :inner
;;      :css {:background-color "$inner-destructive"
;;            :color "$inner-destructive-fg"
;;            "&:hover"
;;            {:background-color
;;             "color-mix(in srgb, $colors$inner-destructive 85%, $colors$n30)"}}}

;;     {:variant :outline
;;      :layer :outer
;;      :css {:border-color "$outer-input"
;;            :background-color "$outer-bg"
;;            "&:hover"
;;            {:background-color "$outer-accent"
;;             :color "$outer-accent-fg"}}}
;;     {:variant :outline
;;      :layer :inner
;;      :css {:border-color "$inner-input"
;;            :background-color "$inner-bg"
;;            "&:hover"
;;            {:background-color "$inner-accent"
;;             :color "$inner-accent-fg"}}}

;;     {:variant :ghost
;;      :layer :outer
;;      :css {"&:hover"
;;            {:background-color "$outer-accent"
;;             :color "$outer-accent-fg"}}}
;;     {:variant :ghost
;;      :layer :inner
;;      :css {"&:hover"
;;            {:background-color "$inner-accent"
;;             :color "$inner-accent-fg"}}}

;;     {:variant :link
;;      :layer :outer
;;      :css {:color "$outer-primary"}}
;;     {:variant :link
;;      :layer :inner
;;      :css {:color "$inner-primary"}}]
   
;;    :defaultVariants
;;    {:variant "default"
;;     :layer :outer
;;     :size "default"}})

;; (def dButton (st/styled "button" styles))
;; (def Slot (st/styled Slot styles))


(def $base
  (css
   :text-sm :weight-normal :rounded-md :transition-colors
   {:touch-action "manipulation"
    :display "inline-flex"
    :justify-content "center"
    :align-items "center"
    :white-space "nowrap"}
   ["&:focus-visible" :ring :outline-none]
   ["&:disabled"
    {:pointer-events "none"
     :opacity "0.5"}]
   ["&:hover"
    {:cursor "pointer"}]))

(def $$variants
  {:variant {:primary
             (css :bg-primary :fg-primary
                  ["&:hover"
                   {:background-color "color-mix(in srgb, var(--col-bg-primary) 85%, var(--col-n30))"}])
             :destructive
             (css :bg-destructive :fg-destructive
                  ["&:hover"
                   {:background-color "color-mix(in srgb, var(--col-bg-destructive) 85%, var(--col-n30))"}])
             :outline
             (css :border :bg
                  {:border-color "var(--col-fg-input)"}
                  ["&:hover"
                   :bg-accent :fg-accent])
             :secondary
             (css :bg-secondary :fg-secondary
                  ["&:hover"
                   {:background-color "color-mix(in srgb, var(--col-bg-secondary) 95%, var(--col-n30))"}])
             :ghost
             (css ["&:hover"
                   :bg-accent :fg-accent])
             :link
             (css :fg-primary
                  {:text-underline-offset "4px"}
                  ["&:hover"
                   {:text-decoration-line "underline"}])}

   :size {:sm (css :h-9 :px-3 :py-3 :rounded)
          :md (css :h-10 :px-4 :py-3)
          :lg (css :h-11 :px-6 :py-3 :rounded-md)
          :icon (css :size-10 :p-0)}})

(defn $$styles
  [variant size]
  (str/join " "
            [$base
             (get-in $$variants [:variant (or variant :primary)])
             (get-in $$variants [:size (or size :md)])]))

(defnc Button
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className variant size asChild children]
          :or {asChild false}
          :rest r} props]
    ($d (if asChild Slot "button")
      {:class (str/join " " (remove nil? [className class
                                          ($$styles variant size)]))
       :ref ref
       & r}
      children)))
