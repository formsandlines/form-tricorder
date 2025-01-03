(ns form-tricorder.components.common.toggle
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]
   ["@radix-ui/react-toggle" :as TogglePrimitive]))

(def r) ;; hotfix for linting error in let+

(def Root (.-Root TogglePrimitive))

(def $base
  (css
   :text-sm :weight-normal :transition-colors
   {:touch-action "manipulation"
    :display "inline-flex"
    :justify-content "center"
    :align-items "center"}
   ["&:focus-visible"
    :outline-none :ring]
   ["&:disabled"
    {:pointer-events "none"
     :opacity "0.5"}]
   ["&:hover"
    {:cursor "pointer"}]))

(def $$variants
  {:variant {:primary
             (css
              {:background-color "transparent"}
              ["&:hover"
               :bg-accent :fg-accent]
              ["&[data-state=on]"
               :bg-accent :fg])
             :outline
             (css
              :border :border-col-input
              {:background-color "transparent"}
              ["&:hover"
               :bg-accent :fg-accent]
              ["&[data-state=on]"
               :bg-accent :fg-accent])

             :formula-input/submit-mode
             (css
              {:background-color "transparent"}
              ["&:hover"
               {:background-color "var(--col-m5)"}]
              ["&[data-state=on]"
               {:background-color "var(--col-m5)"}])}

   :size {:sm (css :h-9 :px-2-5 :rounded-sm)
          :md (css :h-10 :px-3 :rounded-sm)
          :lg (css :h-11 :px-5 :rounded-sm)
          :icon (css :size-10 :p-0 :rounded-sm)
          :icon-sm (css :size-9 :p-0 :rounded-sm)

          :formula-input/submit-mode
          (css :size-8 :p-0 :rounded-full)}})

(defn $$styles
  [variant size]
  (unite $base
         (get-in $$variants [:variant (or variant :primary)])
         (get-in $$variants [:size (or size :md)])))

(defnc Toggle
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className variant size]
          :rest r} props]
    ($d Root
        {:class (unite ($$styles variant size)
                       className class)
         :ref ref
         & r})))

