(ns form-tricorder.components.common.input
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]))

(def r) ;; hotfix for linting error in let+

(def $styles
  (css
    :h-10 :rounded-md :border :px-3 :py-2 :text-sm
    :border-col-input :bg
    {:display "flex"
     :width "100%"}
    ["&[type=file]" :py-0]
    ["&[type=file]::-webkit-file-upload-button, &[type=file]::file-selector-button"
     :text-sm :weight-medium
     {:color "currentcolor"
      :border-width "0px"
      :background-color "transparent"
      :height "100%"
      :display "inline-flex"
      :align-items "center"
      :cursor "pointer"}]
    ["&:focus-visible" :outline-none :ring]
    ["&:disabled"
     {:cursor "not-allowed"
      :opacity "0.5"}]
    ["&::placeholder" :fg-muted]))

(defnc Input
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className type]
          :rest r} props]
    (d/input
      {:class (unite className class $styles)
       :type type
       :ref ref
       & r})))

