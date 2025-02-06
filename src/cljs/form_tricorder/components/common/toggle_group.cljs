(ns form-tricorder.components.common.toggle-group
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+ unite]]
   [form-tricorder.components.common.toggle :refer [$$styles]]
   ["react" :as react]
   ["@radix-ui/react-toggle-group" :as ToggleGroupPrimitive]))

(def r) ;; hotfix for linting error in let+

(def Root (.-Root ToggleGroupPrimitive))
(def Item (.-Item ToggleGroupPrimitive))

(def ToggleGroupContext
  (react/createContext {:size :default
                        :variant :default}))

(def $group-base
  (css ["&[data-orientation=horizontal]"
        {:flex-direction "row"}]
       ["&[data-orientation=vertical]"
        {:flex-direction "column"}]))

(def $$group-variants
  {:variant {:spaced
             (css :gap-1
                  {:display "flex"
                   :align-items "center"
                   :justify-content "center"})

             :joined
             (css :gap-0
                  {:display "flex"
                   :width "fit-content"
                   :align-items "stretch"
                   :justify-content "center"}
                  ["& > *:focus-visible"
                   :ring-inset]
                  ["&[data-orientation=horizontal] > *"
                   :rounded-none :border-r-0]
                  ["&[data-orientation=horizontal] > *:first-child"
                   :rounded-l-sm]
                  ["&[data-orientation=horizontal] > *:last-child"
                   :rounded-r-sm :border-r]
                  ["&[data-orientation=vertical] > *"
                   :rounded-none :border-b-0]
                  ["&[data-orientation=vertical] > *:first-child"
                   :rounded-t-sm]
                  ["&[data-orientation=vertical] > *:last-child"
                   :rounded-b-sm :border-b])

             :value-filter/vmap
             (css :size-14
                  {:position "relative"
                   :margin "0.5rem"
                   :transform "rotate(45deg)"}
                  ["&::before, &::after"
                   {:content "\"\""
                    :position "absolute"
                    :top "50%"
                    :left "50%"
                    :width "100%"
                    :height "1px"
                    :background-color "var(--col-border-col-input)"
                    :z-index "10"}]
                  ["&::after"
                   {:transform "translate(-50%, -50%)"}]
                  ["&::before"
                   {:transform "translate(-50%, -50%) rotate(90deg)"}]
                  ["& > *"
                   {:position "absolute"}]
                  ["& > *:nth-child(3)" ;; north / i
                   :border-r-0 :border-b-0
                   {:top "0"
                    :left "0"}]
                  ["& > *:nth-child(4)" ;; east / m
                   :border-l-0 :border-b-0
                   {:top "0"
                    :right "0"}]
                  ["& > *:nth-child(2)" ;; south / u
                   :border-l-0 :border-t-0
                   {:bottom "0"
                    :right "0"}]
                  ["& > *:nth-child(1)" ;; west / n
                   :border-r-0 :border-t-0
                   {:bottom "0"
                    :left "0"}]
                  ["& > * > *"
                   {:transform "rotate(-45deg)"}])}})

(defn $$group-styles
  [group-variant]
  (unite $group-base
         (get-in $$group-variants [:variant (or group-variant :spaced)])))

(defnc ToggleGroup
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className group-variant variant size children]
          :rest r} props]
    ($d Root
      {:class (unite ($$group-styles group-variant)
                     className class)
       :orientation "horizontal"
       :ref ref
       & r}
      (provider
       {:context ToggleGroupContext
        :value (if (= group-variant :value-filter/vmap)
                 {:variant :outline
                  :size :value-filter/vmap}
                 {:variant variant
                  :size size})}
       children))))

(defnc ToggleGroupItem
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className variant size children]
          :rest r} props
         context (react/useContext ToggleGroupContext)]
    ($d Item
      {:class (unite ($$styles (or (:variant context) variant)
                               (or (:size context) size))
                     className class)
       :ref ref
       & r}
      children)))
