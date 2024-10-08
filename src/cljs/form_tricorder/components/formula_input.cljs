(ns form-tricorder.components.formula-input
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.re-frame-adapter :as rf]
   ;; [form-tricorder.re-frame-adapter :as rf]
   ;; [form-tricorder.icons :refer [InputHelpIcon]]
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
   ["@radix-ui/react-icons" :refer [QuestionMarkCircledIcon]]))

(def styles
  (css {:display "flex"
        :backgroundColor "$colors$inner_bg"
        :borderRadius "$3"
        }))

(def input-styles
  (css {:fontFamily "$mono"
        ;; :color "$colors$inner_fg"
        :width "100%"
        ;; :backgroundColor "transparent"
        ;; :appearance "none"
        ;; :border-width "0"
        ;; :padding "$3" ; "0.6rem"
        ;; :flex "1 1 auto"
        }))

(def button-wrapper-styles
  (css {:alignItems "center"}))

(def button-styles
  (css {:height "100%"
        :width "$icon-input" ; "1.9rem"
        :outline "none"
        :border "none"
        :cursor "pointer"
        ;; :borderRadius "$round"
        :background "none"
        "& svg"
        {:width "100%"
         :height "100%"
         :stroke "$inner_n200"
         "&:hover"
         {:stroke "$inner_n100"}}}))

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
     (d/input
      {:type "checkbox"
       :checked submit-mode
       :on-change (fn [_]
                    ;; (js/console.log "Submit mode: " submit-mode)
                    (set-submit-mode (not submit-mode)))})
     (d/div
      {:class (input-styles)
       :ref editor})
     (d/div
      {:class (button-wrapper-styles)}
      (d/button
       {:class (button-styles)
        :on-click (fn [_]
                    ;; (js/console.log "Clicked help button")
                    (when submit-mode (apply-input code)))}
       ($d QuestionMarkCircledIcon))))))
