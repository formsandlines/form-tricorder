(ns form-tricorder.components.common.tooltip
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]
   ["@radix-ui/react-tooltip" :as TooltipPrimitive]))



(def r) ;; hotfix for linting error in let+

(def Provider (.-Provider TooltipPrimitive))
(def Root (.-Root TooltipPrimitive))
(def Trigger (.-Trigger TooltipPrimitive))

;; "z-50 overflow-hidden rounded-md border bg-popover px-3 py-1.5 text-sm text-popover-foreground shadow-md animate-in fade-in-0 zoom-in-95 data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2",


(def $styles
  (css :rounded-md :border :px-3 :py-1 :text-sm
       :bg-primary :fg-primary
       ;; :bg-popover :fg-popover
       :shadow-md
       {:z-index "50"
        :overflow "hidden"
        ;; :background-color "var(--col-m30)"
        ;; :color "var(--col-m0)"
        :border-color "var(--col-n0)"
        ;; animate-in
        ;; fade-in-0
        ;; zoom-in-95
        }
       #_#_#_#_#_
       ["&[data-state=closed]"
        ;; animate-out
        ;; fade-out-0
        ;; zoom-out-95
        ]
       ["&[data-side=bottom]"
        ;; slide-in-from-top-2
        ]
       ["&[data-side=left]"
        ;; slide-in-from-right-2
        ]
       ["&[data-side=right]"
        ;; slide-in-from-left-2
        ]
       ["&[data-side=top]"
        ;; slide-in-from-bottom-2
        ]
       )
  )

(defnc Content
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className side-offset]
          :rest r} props]
    ($d (.-Content TooltipPrimitive)
        {:class (unite $styles className class)
         :sideOffset (or side-offset 4)
         :ref ref
         & r})))
