(ns form-tricorder.components.common.select
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+]]
   [form-tricorder.stitches-config :as st]
   ["@radix-ui/react-select" :as SelectPrimitive]
   ;; ["lucide-react" :refer [Check ChevronDown ChevronUp]]
   ["@radix-ui/react-icons" :refer [CheckIcon ChevronDownIcon ChevronUpIcon]]
   ["react" :as react]))

(def r) ;; hotfix for linting error in let+


(def Select
  (.-Root SelectPrimitive))

(def SelectGroup
  (.-Group SelectPrimitive))

(def SelectValue
  (.-Value SelectPrimitive))


(def Icon
  (.-Icon SelectPrimitive))

(def Trigger
  (st/styled (.-Trigger SelectPrimitive)
          {:display "flex"
           :height "$10"
           :width "100%"
           :align-items "center"
           :justify-content "space-between"
           :border-radius "$md"
           :border-width 1
           :_paddingX "$3"
           :_paddingY "$2"
           :_text ["$sm"]

           "&:focus-visible"
           {:_outlineNone []}
           "&:disabled"
           {:cursor "not-allowed"
            :opacity "0.5"}
           "& > span"
           {:_lineClamp 1}

           :variants
           {:layer
            {:outer
             {:border-color "$outer-input"
              :background-color "$outer-bg"

              "&::placeholder"
              {:color "$outer-muted-fg"}
              "&:focus-visible"
              {:_ringOuter []}}

             :inner
             {:border-color "$inner-input"
              :background-color "$inner-bg"

              "&::placeholder"
              {:color "$inner-muted-fg"}
              "&:focus-visible"
              {:_ringInner []}}}}

           :defaultVariants
           {:layer :outer}}))

(def common-icon-styles
  {:width "$icon-sm" ;; 4 x 4
   :height "$icon-sm"})

(def IconChevronUp (st/styled ChevronUpIcon common-icon-styles))
(def IconChevronDown (st/styled ChevronDownIcon common-icon-styles))


(defnc SelectTrigger
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer children]
          :rest r} props]
    ($d Trigger
        {:class (string/join " " (remove nil? [className class]))
         :layer (or layer js/undefined)
         :ref ref
         & r}
        children
        ($d Icon
            {:asChild true}
            ($d IconChevronDown
              {:css (clj->js {:opacity "0.5"})})))))


(def scroll-button-styles
  {:display "flex"
   :cursor "default"
   :align-items "center"
   :justify-content "center"
   :_paddingY "$1"})

(def ScrollUpButton
  (st/styled (.-ScrollUpButton SelectPrimitive)
          scroll-button-styles))

(defnc SelectScrollUpButton
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d ScrollUpButton
        {:class (string/join " " (remove nil? [className class]))
         :ref ref
         & r}
        ($d IconChevronUp))))

(def ScrollDownButton
  (st/styled (.-ScrollDownButton SelectPrimitive)
          scroll-button-styles))

(defnc SelectScrollDownButton
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d ScrollDownButton
        {:class (string/join " " (remove nil? [className class]))
         :ref ref
         & r}
        ($d IconChevronDown))))


(def Portal
  (.-Portal SelectPrimitive))

(def Content
  (st/styled (.-Content SelectPrimitive)
          {:position "relative"
           :z-index "50"
           :max-height "$96" ;; 96
           :min-width "8rem"
           :overflow "hidden"
           :border-radius "$md"
           :border-width 1
           :box-shadow "$md"

           "&[data-state=open]"
           {;; animate-in
            ;; fade-in-0
            ;; zoom-in-95
            }
           "&[data-state=closed]"
           {;; animate-out
            ;; fade-out-0
            ;; zoom-out-95
            }

           "&[data-side=left]"
           {;; slide-in-from-right-2
            }
           "&[data-side=right]"
           {;; slide-in-from-left-2
            }
           "&[data-side=top]"
           {;; slide-in-from-bottom-2
            }
           "&[data-side=bottom]"
           {;; slide-in-from-top-2
            }

           :variants
           {:layer
            {:outer
             {:border-color "$outer-input"
              :background-color "$outer-popover"
              :color "$outer-popover-fg"}

             :inner
             {:border-color "$inner-input"
              :background-color "$inner-popover"
              :color "$inner-popover-fg"}}}

           :defaultVariants
           {:layer :outer}}))

(def Viewport
  (st/styled (.-Viewport SelectPrimitive)
          {:padding "$1"}))

(defnc SelectContent
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [className class children position layer]
          :or {position "popper"}
          :rest r} props]
    ($d Portal
      ($d Content
        {:class (string/join " " (remove nil? [className class]))
         :css (if (= position "popper")
                (clj->js {"&[data-side=left]"
                          { ;; -translate-x-1
                           }
                          "&[data-side=right]"
                          { ;; translate-x-1
                           }
                          "&[data-side=top]"
                          { ;; -translate-y-1
                           }
                          "&[data-side=bottom]"
                          { ;; translate-y-1
                           }})
                js/undefined)
         :layer (or layer js/undefined)
         :position position
         :ref ref
         & r}
        ($d SelectScrollUpButton)
        ($d Viewport
          {:css (if (= position "popper")
                  (clj->js {:height "var(--radix-select-trigger-height)"
                            :width "100%"
                            :min-width "var(--radix-select-trigger-width)"})
                  js/undefined)}
          children)
        ($d SelectScrollDownButton)))))


(def Label
  (st/styled (.-Label SelectPrimitive)
          {:_paddingY "$1-5"
           :padding-left "$8"
           :padding-right "$2"
           :font-size "$sm"
           :font-weight "$semibold"}))

(defnc SelectLabel
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d Label
        {:class (string/join " " (remove nil? [className class]))
         :ref ref
         & r})))


(def Item
  (st/styled (.-Item SelectPrimitive)
          {:position "relative"
           :display "flex"
           :width "100%"
           :cursor "default"
           :user-select "none"
           :align-items "center"
           :border-radius "$sm"
           :_paddingY "$1-5"
           :padding-left "$8"
           :padding-right "$2"
           :font-size "$sm"
           :_outlineNone []

           "&[data-disabled]"
           {:pointer-events "none"
            :opacity "0.5"}

           :variants
           {:layer
            {:outer
             {"&:focus"
              {:background-color "$outer-accent"
               :color "$outer-accent-fg"}}

             :inner
             {"&:focus"
              {:background-color "$inner-accent"
               :color "$inner-accent-fg"}}}}

           :defaultVariants
           {:layer :outer}}))

(def ItemIndicator
  (.-ItemIndicator SelectPrimitive))

(def ItemText
  (.-ItemText SelectPrimitive))

(def dSpan
  (st/styled "span"
          {:position "absolute"
           :left "$2"
           :display "flex"
           :height "0.875rem" ;; 3.5
           :width "0.875rem" ;; 3.5
           :align-items "center"
           :justify-content "center"}))


(def IconCheck (st/styled CheckIcon common-icon-styles))

(defnc SelectItem
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer children]
          :rest r} props]
    ($d Item
      {:class (string/join " " (remove nil? [className class]))
       :layer (or layer js/undefined)
       :ref ref
       & r}
      ($d dSpan
        ($d ItemIndicator
          ($d IconCheck)))
      ($ ItemText
         children))))


(def Separator
  (st/styled (.-Separator SelectPrimitive)
          {:_marginX -1
           :_marginY 1
           :height "$px"
           :background-color "$inner-muted"}))

(defnc SelectSeparator
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer]
          :rest r} props]
    ($d Separator
        {:class (string/join " " (remove nil? [className class]))
         :layer (or layer js/undefined)
         :ref ref
         & r})))

