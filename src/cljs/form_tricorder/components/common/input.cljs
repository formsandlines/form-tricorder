(ns form-tricorder.components.common.input
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [clojure.string :as string]
   [form-tricorder.utils :refer [let+]]
   [form-tricorder.stitches-config :refer [styled]]
   ["react" :as react]))

(def r) ;; hotfix for linting error in let+


(def dInput
  (styled "input"
          {:display "flex"
           :height "$10"
           :width "100%"
           :border-radius "$md"
           :border-width "$1"
           :_paddingX "$3"
           :_paddingY "$2"
           :_text ["$sm"]

           "&[type=file]"
           {:_paddingY 0}

           "&[type=file]::-webkit-file-upload-button, &[type=file]::file-selector-button"
           {:color "currentcolor"
            :border-width "0px"
            :background-color "transparent"
            :_text ["$sm"]
            :font-weight "$medium"

            :height "100%"
            :display "inline-flex"
            :align-items "center"
            :cursor "pointer"}

           "&:focus-visible"
           {:_outlineNone []}
           "&:disabled"
           {:cursor "not-allowed"
            :opacity "0.5"}

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


(defnc Input
  [props ref]
  {:wrap [(react/forwardRef)]}
  (let+ [{:keys [class className type layer]
          :rest r} props]
    ($d dInput
      {:class
       (string/join " " (remove nil? [className class]))
       :layer (or layer js/undefined)
       :type type
       :ref ref
       & r})))

