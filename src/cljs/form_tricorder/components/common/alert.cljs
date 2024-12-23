(ns form-tricorder.components.common.alert
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]))

(def r) ;; hotfix for linting error in let+


(def $base
  (css :rounded-lg :p-4 :border
       {:width "100%"
        :position "relative"}
       ["& > svg"
        :left-4 :top-4 :fg
        {:position "absolute"}]
       ["& > svg ~ *"
        :pl-7]
       ["& > svg + div"
        {:transform "translateY(-3px)"}]))

(def $$variants
  {:variant {:primary
             (css :bg :fg)
             :destructive
             (css ;; :bg-destructive :fg-destructive
              {:background-color "var(--col-e4)"
               :color "var(--col-e24)"
               ;; :border-color "var(--col-bg-destructive)"
               }
              [":root[data-theme=\"dark\"] &"
               {:background-color "var(--col-e1)"
                :color "var(--col-e21)"}]
              ["& > svg"
               :fg-destructive])}})

(defn $$styles
  [variant]
  (unite $base
         (get-in $$variants [:variant (or variant :primary)])))

(defnc Alert
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className variant]
          :rest r} props]
    (d/div
     {:class (unite className class
                    ($$styles variant))
      :ref ref
      :role "alert"
      & r})))

(defnc AlertTitle
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    (d/h5
     {:class (unite className class
                    (css :mb-1 :weight-medium :line-h-none))
      :ref ref
      & r})))

(defnc AlertDescription
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className]
          :rest r} props]
    (d/div
     {:class (unite className class
                    (css :text-sm
                         ["& _p" ;; ???
                          :line-h-relaxed]))
      :ref ref
      & r})))



