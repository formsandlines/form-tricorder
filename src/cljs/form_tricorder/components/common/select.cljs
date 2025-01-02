(ns form-tricorder.components.common.select
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+ unite]]
   ["@radix-ui/react-select" :as SelectPrimitive]
   ;; ["lucide-react" :refer [Check ChevronDown ChevronUp]]
   ["@radix-ui/react-icons" :as icons]
   ["react" :as react]))

(def r) ;; hotfix for linting error in let+

(def Select (.-Root SelectPrimitive))
(def SelectGroup (.-Group SelectPrimitive))
(def SelectValue (.-Value SelectPrimitive))
(def Icon (.-Icon SelectPrimitive))
(def Trigger (.-Trigger SelectPrimitive))
(def ScrollUpButton (.-ScrollUpButton SelectPrimitive))
(def ScrollDownButton (.-ScrollDownButton SelectPrimitive))
(def Content (.-Content SelectPrimitive))
(def Portal (.-Portal SelectPrimitive))
(def Viewport (.-Viewport SelectPrimitive))
(def Separator (.-Separator SelectPrimitive))
(def ItemIndicator (.-ItemIndicator SelectPrimitive))
(def ItemText (.-ItemText SelectPrimitive))
(def Item (.-Item SelectPrimitive))
(def Label (.-Label SelectPrimitive))

(def $common-icon-styles
  (css :size-icon-sm)) ;; 4 x 4


(defnc SelectTrigger
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer children]
          :rest r} props]
    ($d Trigger
      {:class (unite className class
                     (css
                       :h-10 :px-3 :py-2 :border :rounded-md :text-sm
                       :bg :border-col-input
                       {:display "flex"
                        :width "100%"
                        :align-items "center"
                        :justify-content "space-between"}

                       ["&::placeholder"
                        :fg-muted]
                       ["&:focus-visible"
                        :outline-none :ring]
                       ["&:disabled"
                        {:cursor "not-allowed"
                         :opacity "0.5"}]
                       ["& > span"
                        :line-clamp
                        {:--clamp-n "1"}]))
       :layer (or layer js/undefined)
       :ref ref
       & r}
      children
      ($d Icon
        {:asChild true}
        ($d icons/ChevronDownIcon
          {:class (unite $common-icon-styles
                         (css {:opacity "0.5"}))})))))


(def $scroll-button-styles
  (css
   :py-1
   {:display "flex"
    :cursor "default"
    :align-items "center"
    :justify-content "center"}))

(defnc SelectScrollUpButton
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d ScrollUpButton
      {:class (unite className class $scroll-button-styles)
       :ref ref
       & r}
      ($d icons/ChevronUpIcon
        {:class $common-icon-styles}))))

(defnc SelectScrollDownButton
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d ScrollDownButton
      {:class (unite className class $scroll-button-styles)
       :ref ref
       & r}
      ($d icons/ChevronDownIcon
        {:class $common-icon-styles}))))


(defnc SelectContent
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [className class children position layer]
          :or {position "popper"}
          :rest r} props]
    ($d Portal
      ($d Content
        {:class (unite className class
                       (css
                         :border-col-input :bg-popover :fg-popover
                         :max-h-96 :min-w-43 :border :rounded-md
                         {:position "relative"
                          :z-index "50"
                          :max-height "$96" ;; 96
                          :overflow "hidden"
                          :box-shadow "$md"}

                         ;; ["&[data-state=open]"
                         ;;  { ;; animate-in
                         ;;   ;; fade-in-0
                         ;;   ;; zoom-in-95
                         ;;   }]
                         ;; ["&[data-state=closed]"
                         ;;  { ;; animate-out
                         ;;   ;; fade-out-0
                         ;;   ;; zoom-out-95
                         ;;   }]

                         ;; ["&[data-side=left]"
                         ;;  { ;; slide-in-from-right-2
                         ;;   }]
                         ;; ["&[data-side=right]"
                         ;;  { ;; slide-in-from-left-2
                         ;;   }]
                         ;; ["&[data-side=top]"
                         ;;  { ;; slide-in-from-bottom-2
                         ;;   }]
                         ;; ["&[data-side=bottom]"
                         ;;  { ;; slide-in-from-top-2
                         ;;   }]
                         )
                       ;; (when (= position "popper")
                       ;;   (css
                       ;;     ["&[data-side=left]"
                       ;;      { ;; -translate-x-1
                       ;;       }]
                       ;;     ["&[data-side=right]"
                       ;;      { ;; translate-x-1
                       ;;       }]
                       ;;     ["&[data-side=top]"
                       ;;      { ;; -translate-y-1
                       ;;       }]
                       ;;     ["&[data-side=bottom]"
                       ;;      { ;; translate-y-1
                       ;;       }]))
                       )
         :layer (or layer js/undefined)
         :position position
         :ref ref
         & r}
        ($d SelectScrollUpButton)
        ($d Viewport
          {:class (unite
                   (css :padding-1)
                   (when (= position "popper")
                     (css {:height "var(--radix-select-trigger-height)"
                           :width "100%"
                           :min-width "var(--radix-select-trigger-width)"})))}
          children)
        ($d SelectScrollDownButton)))))


(defnc SelectLabel
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    ($d Label
      {:class (unite className class
                     (css :py-1-5 :pl-8 :pr-2
                          :font-size-sm :weight-semibold))
       :ref ref
       & r})))

(defnc SelectItem
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer children]
          :rest r} props]
    ($d Item
      {:class (unite className class
                     (css :rounded-sm :py-1-5 :pl-8 :pr-2 :font-size-sm
                          :outline-none
                          {:position "relative"
                           :display "flex"
                           :width "100%"
                           :cursor "default"
                           :user-select "none"
                           :align-items "center"}
                          ["&[data-disabled]"
                           {:pointer-events "none"
                            :opacity "0.5"}]
                          ["&:focus"
                           :bg-accent :fg-accent]))
       :layer (or layer js/undefined)
       :ref ref
       & r}
      ($d "span"
        {:class (css :left-2
                     {:position "absolute"
                      :display "flex"
                      :height "0.875rem" ;; 3.5
                      :width "0.875rem" ;; 3.5
                      :align-items "center"
                      :justify-content "center"})}
        ($d ItemIndicator
          ($d icons/CheckIcon
            {:class $common-icon-styles})))
      ($ ItemText
         children))))

(defnc SelectSeparator
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className layer]
          :rest r} props]
    ($d Separator
      {:class (unite className class
                     (css :bg-muted
                          {:margin "1px -1px"
                           :height "1px"}))
         :layer (or layer js/undefined)
         :ref ref
         & r})))

