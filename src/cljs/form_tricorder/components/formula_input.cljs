(ns form-tricorder.components.formula-input
  (:require
   [refx.alpha :as refx]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ; [form-tricorder.icons :refer [InputHelpIcon]]
   [form-tricorder.utils :refer [log style> css>]]
   ["@radix-ui/react-icons" :refer [QuestionMarkCircledIcon]]))

(def styles
  (css> {:display "flex"
         :backgroundColor "$colors$inner_bg"
         :borderRadius "$3"
         }))

(def input-styles
  (css> {:fontFamily "monospace"
         :color "$colors$inner_fg"
         :backgroundColor "transparent"
         :appearance "none"
         :width "100%"
         :border-width "0"
         :padding "0.6rem"
         :flex "1 1 auto"
         }))

(def button-wrapper-styles
  (css> {:alignItems "center"}))

(def button-styles
  (css> {:height "100%"
         :width "1.9rem"
         :outline "none"
         :border "none"
         :cursor "pointer"
         ; :borderRadius "$round"
         :background "none"
         "& svg"
         {:width "100%"
          :height "100%"
          :stroke "$inner_n200"
          "&:hover"
          {:stroke "$inner_n100"}}}))

(defnc FormulaInput
  [{:keys [apply-input]}]
  (let [[input set-input] (hooks/use-state "")]
    (d/div
     {:class (str "FormulaInput " (styles))}
     (d/input
      {:class (input-styles)
       :value input
       :placeholder "((a) b)"
       :on-change (fn [e] (do (.preventDefault e)
                              (set-input (.. e -target -value))))
       :on-key-press (fn [e] (when (= "Enter" (.-key e))
                               (apply-input input)))})
     (d/div
       {:class (button-wrapper-styles)}
       (d/button
         {:class (button-styles)
          :on-click (fn [e] (apply-input input))}
         ($d QuestionMarkCircledIcon))))))
