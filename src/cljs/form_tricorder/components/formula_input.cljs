(ns form-tricorder.components.formula-input
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.re-frame-adapter :as rf]
   ;; [form-tricorder.re-frame-adapter :as rf]
   ;; [form-tricorder.icons :refer [InputHelpIcon]]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
   [form-tricorder.stitches-config :refer [styled css]]
   [form-tricorder.utils :refer [log]]
   ["@codemirror/state" :refer [EditorState StateField StateEffect]]
   ["@codemirror/view" :refer [EditorView keymap placeholder
                               drawSelection highlightSpecialChars]]
   ["@codemirror/commands" :refer [defaultKeymap historyKeymap history]]
   ["@codemirror/autocomplete" :refer [closeBrackets closeBracketsKeymap]]
   ["@codemirror/language" :refer [syntaxHighlighting defaultHighlightStyle
                                   bracketMatching]]
   ["@codemirror/search" :refer [highlightSelectionMatches]]
   ["@radix-ui/react-icons" :refer [LinkNone1Icon LinkBreak1Icon ResetIcon]]
   ;; ["@radix-ui/react-icons" :refer [QuestionMarkCircledIcon]]
   ))

(def styles
  (css {:display "flex"
        :align-items "stretch"
        :justify-content "space-between"
        :min-height "$10"
        }))

(def input-styles
  (css {:fontFamily "$mono"
        :background-color "$inner-bg"
        ;; :color "$inner-fg"
        :width "100%"
        :border-radius "$2"
        ;; :backgroundColor "transparent"
        ;; :appearance "none"
        ;; :border-width "0"
        ;; :padding "$3" ; "0.6rem"
        ;; :flex "1 1 auto"
        }))

(def button-wrapper-styles
  (css {
        :padding "$1-5"
        ;; :align-items "center"
        }))

(def button-styles
  (css {:height "$7"
        :width "$7" ;; "$icon-input" ; "1.9rem"
        :touch-action "manipulation"
        :display "inline-flex"
        :justify-content "center"
        :align-items "center"
        :white-space "nowrap"
        :color "$m5"

        :_transition_colors []
        "&:focus-visible"
        {:_outlineNone []}
        "&:disabled"
        {:pointer-events "none"
         :opacity "0.5"}
        "&:hover"
        {:cursor "pointer"}

        "& svg"
        {:width "100%"
         :height "100%"}

        ;; :outline "none"
        ;; :border "none"
        ;; :borderRadius "$round"
        ;; :background "none"
        ;; "& svg"
        ;; {:width "100%"
        ;;  :height "100%"
        ;;  :fill "$n3"
        ;;  "&:hover"
        ;;  {:fill "$n2"}}
        }))

(def ^js ft-theme
  (.baseTheme EditorView
              (clj->js
               {"&.cm-editor"
                {:fontSize "1rem"}
                
                "&light"
                {:color "#4A4847"}
                "&dark"
                {:color "#C5C7D1"}

                "&light .cm-content"
                {:caretColor "#9297B0"}
                "&dark .cm-content"
                {:caretColor "#878584"}
                
                "&light.cm-focused .cm-matchingBracket"
                {:backgroundColor "#ece9e8"}
                "&light.cm-focused .cm-nonmatchingBracket"
                {:backgroundColor "#ffcbcb"}
                "&dark.cm-focused .cm-matchingBracket"
                {:backgroundColor "#46495C"}
                "&dark.cm-focused .cm-nonmatchingBracket"
                {:backgroundColor "#7a0000"}
                })))


(def update-darkmode-type (.define StateEffect))
(def update-submitmode-type (.define StateEffect))

(defn define-statefield [init state-effect-type]
  (.define StateField
           #js {:create (fn [_] init)
                :update (fn [v tsx]
                          (if-let [effect (some
                                           #(when (.is % state-effect-type)
                                              %) (.-effects tsx))]
                            (.-value effect)
                            v))}))

(defnc FormulaInput
  [{:keys [current-formula apply-input]}]
  (let [editor (hooks/use-ref nil)
        appearance (rf/subscribe [:theme/appearance])
        [code set-code] (hooks/use-state "")
        [view set-view] (hooks/use-state nil)
        [submit-mode set-submit-mode] (hooks/use-state false)
        submit-mode-active (define-statefield
                             submit-mode update-submitmode-type)
        darkmode-active (define-statefield
                          (= appearance :dark) update-darkmode-type)
        on-update (fn [^js v]
                    (when (.-docChanged v)
                      (let [input (.. v -state -doc toString)
                            submit-mode (.. v -state (field submit-mode-active))]
                        (set-code input)
                        (when-not submit-mode
                          (apply-input input)))))]
    (hooks/use-effect
      :once
      (let [submit-mode-cmd (fn [^js v]
                              (if (.. v -state (field submit-mode-active))
                                (let [input (.. v -state -doc toString)]
                                  (apply-input input)
                                  true)
                                false))
            submit-keymap #js [#js {:key "Shift-Enter"
                                    :run submit-mode-cmd}]
            start-state
            (.create EditorState
                     #js {:doc current-formula
                          :extensions
                          #js [ft-theme
                               (.of keymap
                                    ^js/Array (.concat
                                               submit-keymap
                                               closeBracketsKeymap
                                               defaultKeymap
                                               historyKeymap))
                               submit-mode-active
                               darkmode-active
                               (.. EditorView -darkTheme
                                   (from darkmode-active identity))
                               (highlightSpecialChars)
                               (history)
                               ;; (drawSelection)
                               (bracketMatching)
                               (closeBrackets)
                               (highlightSelectionMatches)
                               (syntaxHighlighting
                                defaultHighlightStyle
                                #js {:fallback true})
                               (placeholder "Enter formula…")
                               ;; (.. EditorView -darkTheme
                               ;;     (of (= appearance :dark)))
                               (.. EditorView -updateListener
                                   (of on-update))]})
            view (new EditorView
                      #js {:state start-state
                           :parent (.-current editor)})]
        (set-view view)
        (fn [] (.destroy view))))
    (hooks/use-effect
      [submit-mode]
      (when view
        (let [tsx (.. view -state
                      (update (clj->js
                               {:effects
                                #js [(.. update-submitmode-type
                                         (of submit-mode))]})))]
          (.. view (dispatch tsx)))))
    (hooks/use-effect
      [appearance]
      (when view
        (let [tsx (.. view -state
                      (update (clj->js
                               {:effects
                                #js [(.. update-darkmode-type
                                         (of (= appearance :dark)))]})))]
          (.. view (dispatch tsx)))))
    (d/div
      {:class (str "FormulaInput " (styles))}
      (d/div
        {:class (input-styles)
         :ref editor})
      (d/div
        {:class ((css {:display "flex"
                       :align-items "center"
                       :background-color "$inner-bg"
                       :padding "$1"}))}
        ($ Toggle
           {:css (clj->js {:border-radius "$round"
                           :width "$8"
                           :height "$8"
                           "&:hover"
                           {:background-color "$m1"}
                           "&[data-state=on]"
                           {:background-color "$m1"}})
            :variant "default"
            :size "icon-sm"
            :layer "inner"
            :pressed (not submit-mode)
            :on-pressed-change
            (fn [_]
              ;; (js/console.log "Submit mode: " submit-mode)
              (set-submit-mode (not submit-mode)))}
           ($d (if submit-mode LinkBreak1Icon LinkNone1Icon))))
      (d/div
        {:class ((css {:height "inherit"
                       :width (if submit-mode "$9-5" 0)
                       :margin-left (if submit-mode "$1" 0)
                       :visibility (if submit-mode "visible" "hidden")
                       :transition "all 0.2s ease-out 0.2s"}))}
        ($ Button
           {:css (clj->js {:height "100%"
                           :width "100%"
                           :opacity (if submit-mode "1.0" "0.0")
                           :transition "opacity 0.1s ease-out"})
            :variant "secondary"
            :size "icon"
            :layer "outer"
            :on-click (fn [_]
                        (when submit-mode (apply-input code)))}
           ($d ResetIcon))))))
