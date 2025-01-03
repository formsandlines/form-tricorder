(ns form-tricorder.components.common.button
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as str]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]
   ["@radix-ui/react-slot" :refer [Slot]]))

(def r) ;; hotfix for linting error in let+

(def $base
  (css
   :text-sm :weight-normal :rounded-sm :transition-colors
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
             (css :border :border-col-input :bg
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

   :size {:sm (css :h-9 :px-3 :py-3)
          :md (css :h-10 :px-4 :py-3)
          :lg (css :h-11 :px-6 :py-3)
          :icon (css :size-10 :p-0)
          :icon-sm (css :size-6 :p-0)}})

(defn $$styles
  [variant size]
  (unite $base
         (get-in $$variants [:variant (or variant :primary)])
         (get-in $$variants [:size (or size :md)])))

(defnc Button
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className variant size asChild children]
          :or {asChild false}
          :rest r} props]
    ($d (if asChild Slot "button")
        {:class (unite className class
                       ($$styles variant size))
         :ref ref
         & r}
        children)))
