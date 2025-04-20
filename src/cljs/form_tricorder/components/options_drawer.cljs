(ns form-tricorder.components.options-drawer
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.utils :as utils :refer [pp-var]]
   ;; [form-tricorder.components.varorder-select :refer [VarorderSelect]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.select
    :refer [Select SelectTrigger SelectValue SelectItem SelectContent
            SelectGroup SelectLabel]]
   ["@radix-ui/react-icons" :as icons]
   ["@radix-ui/react-collapsible" :as Collapsible]))


(def Root (.-Root Collapsible))
(def Trigger (.-Trigger Collapsible))
(def Content (.-Content Collapsible))


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
            }
           ($d SelectValue
             {:placeholder "Select varorder…"}
             (display-varorder current-varorder)))
        ($ SelectContent
           {:class "outer"}
           (for [varorder varorder-perms
                 :let [label (display-varorder varorder)]]
             ($ SelectItem
                {:key label
                 :value varorder}
                label)))))))

(defnc OptionsDrawer
  [{:keys []}]
  (let [[open set-open] (hooks/use-state true)]
    ($d Root
        {:class (css
                 :gap-3 :h-14 :p-3 :bg :rounded
                 {:display "flex"
                  :align-items "center"})
         :open open
         :onOpenChange set-open}
        ($d Content
            {:class (css
                     ;; {:overflow "hidden"}
                     ["&[data-state=open]"
                      {:animation "collapsible-slide-out 100ms ease-in"}]
                     ["&[data-state=closed]"
                      {:animation "collapsible-slide-in 100ms ease-out"}]
                     ["& > div"
                      :gap-2
                      {:display "flex"
                       :align-items "center"}])}
            (d/div
             ($ VarorderSel)
             ($ Label {:htmlFor "varorder-select"} "Interpretation order")))
        ($d Trigger
            {:class (css ["&:focus-visible"
                          :outline-none :ring])
             :asChild true}
            (d/button
             ($d (if open icons/ChevronLeftIcon icons/ChevronRightIcon)
                 {:class (css :size-icon-sm)}))))))
