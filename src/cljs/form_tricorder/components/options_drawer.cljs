(ns form-tricorder.components.options-drawer
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.stitches-config :refer [styled css keyframes]]
   [form-tricorder.utils :as utils :refer [pp-var]]
   ;; [form-tricorder.components.varorder-select :refer [VarorderSelect]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.select
    :refer [Select SelectTrigger SelectValue SelectItem SelectContent
            SelectGroup SelectLabel]]
   ["@radix-ui/react-icons" :refer [ChevronLeftIcon ChevronRightIcon]]
   ["@radix-ui/react-collapsible" :as Collapsible]))


(def Root
  (styled (.-Root Collapsible)
          {:display "flex"
           :align-items "center"
           :gap "$3"
           :height "$14"
           :padding "$3"
           :border-radius "$base"
           :background-color "$outer-bg"}))

(def Trigger
  (styled (.-Trigger Collapsible)
          {}))

(def slide-out
  (keyframes
   {:from {:width 0}
    :to   {:width "var(--radix-collapsible-content-width)"}}))

(def slide-in
  (keyframes
   {:from {:width "var(--radix-collapsible-content-width)"}
    :to   {:width 0}}))

(def Content
  (styled (.-Content Collapsible)
          {:overflow "hidden"
           "&[data-state=open]"
           {:animation (str slide-out " 100ms ease-in")}
           "&[data-state=closed]"
           {:animation (str slide-in " 100ms ease-out")}

           "& > div"
           {:display "flex"
            :align-items "center"
            :gap "$2"}}))


(def common-icon-styles
  {:width "$icon-sm"
   :height "$icon-sm"})

(def IconChevronLeft (styled ChevronLeftIcon common-icon-styles))
(def IconChevronRight (styled ChevronRightIcon common-icon-styles))

(defn display-varorder
  [varorder]
  (reduce #(str %1 (when (not (empty? %1)) " > ") (pp-var %2)) "" varorder))

(defnc VarorderSel
  [{:keys []}]
  (let [current-varorder (rf/subscribe [:input/varorder])
        set-varorder #(rf/dispatch
                       [:input/changed-varorder {:next-varorder %}])
        varorder-perms (rf/subscribe [:input/->varorder-permutations])]
    (when current-varorder
      ($d Select
        {:id "varorder-select"
         :value current-varorder
         :onValueChange (fn [v] (set-varorder v))}
        ($ SelectTrigger
           {;; :style {:width 200}
            :layer "outer"}
           ($d SelectValue
             {:placeholder "Select varorder…"}
             (display-varorder current-varorder)))
        ($ SelectContent
           {:layer "outer"}
           (for [varorder varorder-perms
                 :let [label (display-varorder varorder)]]
             ($ SelectItem
                {:key label
                 :value varorder
                 :layer "outer"}
                label)))))))

(defnc OptionsDrawer
  [{:keys []}]
  (let [[open set-open] (hooks/use-state true)]
    ($d Root
      {:open open
       :onOpenChange set-open}
      ($d Content
        (d/div
          ($ VarorderSel)
          ($ Label {:htmlFor "varorder-select"} "Interpretation order")))
      ($d Trigger
        {:asChild true}
        (d/button
          ($d (if open IconChevronLeft IconChevronRight)))))))
