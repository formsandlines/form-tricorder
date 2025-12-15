(ns form-tricorder.components.function-opts
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.utils :as utils :refer [let+ unite]]
   [form-tricorder.components.common.label :refer [Label]]
   ["@radix-ui/react-icons" :as radix-icons]
   ;; ["lucide-react" :as lucide-icons]
   ))

(def r) ;; hotfix for linting error in let+

;; common styles

(def $$toggle-const-styles
  {:n (css ["&[data-state=on]"
            {:background-color "var(--col-const-n)"}]
           ["&[data-state=on]:hover"
            {:background-color "var(--col-const-n-hover)"}])
   :u (css ["&[data-state=on]"
            {:background-color "var(--col-const-u)"}]
           ["&[data-state=on]:hover"
            {:background-color "var(--col-const-u-hover)"}])
   :i (css ["&[data-state=on]"
            {:background-color "var(--col-const-i)"}]
           ["&[data-state=on]:hover"
            {:background-color "var(--col-const-i-hover)"}])
   :m (css ["&[data-state=on]"
            {:background-color "var(--col-const-m)"}]
           ["&[data-state=on]:hover"
            {:background-color "var(--col-const-m-hover)"}])})

(def $$radio-const-styles
  {:n (css ["&[data-state=checked]"
            {:background-color "var(--col-const-n)"}]
           ["&[data-state=checked]:hover"
            {:background-color "var(--col-const-n-hover)"}])
   :u (css ["&[data-state=checked]"
            {:background-color "var(--col-const-u)"}]
           ["&[data-state=checked]:hover"
            {:background-color "var(--col-const-u-hover)"}])
   :i (css ["&[data-state=checked]"
            {:background-color "var(--col-const-i)"}]
           ["&[data-state=checked]:hover"
            {:background-color "var(--col-const-i-hover)"}])
   :m (css ["&[data-state=checked]"
            {:background-color "var(--col-const-m)"}]
           ["&[data-state=checked]:hover"
            {:background-color "var(--col-const-m-hover)"}])})

;; (def $$slider-const-styles
;;   {:n (css ["& span[data-slot=slider-thumb]"
;;             {:background-color "var(--col-const-n)"}]
;;            ["& span[data-slot=slider-thumb]:hover"
;;             {:background-color "var(--col-const-n-hover)"}])
;;    :u (css ["& span[data-slot=slider-thumb]"
;;             {:background-color "var(--col-const-u)"}]
;;            ["& span[data-slot=slider-thumb]:hover"
;;             {:background-color "var(--col-const-u-hover)"}])
;;    :i (css ["& span[data-slot=slider-thumb]"
;;             {:background-color "var(--col-const-i)"}]
;;            ["& span[data-slot=slider-thumb]:hover"
;;             {:background-color "var(--col-const-i-hover)"}])
;;    :m (css ["& span[data-slot=slider-thumb]"
;;             {:background-color "var(--col-const-m)"}]
;;            ["& span[data-slot=slider-thumb]:hover"
;;             {:background-color "var(--col-const-m-hover)"}])})

(def $$slider-const-styles
  {:n (css ["& span[data-slot=slider-range]"
            {:background-color "var(--col-const-n)"}]
           ["& span[data-slot=slider-range]:hover"
            {:background-color "var(--col-const-n-hover)"}])
   :u (css ["& span[data-slot=slider-range]"
            {:background-color "var(--col-const-u)"}]
           ["& span[data-slot=slider-range]:hover"
            {:background-color "var(--col-const-u-hover)"}])
   :i (css ["& span[data-slot=slider-range]"
            {:background-color "var(--col-const-i)"}]
           ["& span[data-slot=slider-range]:hover"
            {:background-color "var(--col-const-i-hover)"}])
   :m (css ["& span[data-slot=slider-range]"
            {:background-color "var(--col-const-m)"}]
           ["& span[data-slot=slider-range]:hover"
            {:background-color "var(--col-const-m-hover)"}])})

;; common components

(defnc FuncOpts
  [{:keys [children]}]
  (d/div
   {:class (css ;; "outer"
            :border-col
            :p-2 :rounded
            :gap-2
            {:border "1px dashed"
             ;; :position "relative"
             :display "flex"
             :width "fit-content"})}
   children
   (d/label
    {:class (css :fg-muted)}
    ($ radix-icons/MixerHorizontalIcon))))

(defnc FuncOptsGroup
  [props]
  (let+ [{:keys [class className dir children] :rest r} props]
    (d/div
     {:class (unite className class
                    (css :gap-2 {:display "flex"
                                 :align-items "start"})
                    (case dir
                      :row (css {:flex-direction "row"})
                      :column (css {:flex-direction "column"})
                      (throw (ex-info "invalid flex direction" {}))))
      :role "group"
      & r}
     children)))

(defnc FuncOptHead
  [props]
  (let+ [{:keys [class className children] :rest r} props]
    (d/div
     {:class (unite className class
                    (css :font-size-sm :weight-normal :line-h-none))
      & r}
     children)))

;; (defnc FuncOptLabel
;;   [props]
;;   (let+ [{:keys [children] :rest r} props]
;;     ($ Label
;;        {:class (css :font-size-sm)
;;         & r}
;;        children)))
