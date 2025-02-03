(ns form-tricorder.components.formula-input
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   ;; [form-tricorder.re-frame-adapter :as rf]
   ;; [form-tricorder.icons :refer [InputHelpIcon]]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
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
                {:backgroundColor "#7a0000"}})))


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
  [{:keys [apply-input]}]
  (let [editor (hooks/use-ref nil)
        current-formula (rf/subscribe [:input/formula])
        appearance (rf/subscribe [:theme/appearance])
        [code set-code] (hooks/use-state (or current-formula ""))
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
                          (apply-input input false)))))]
    (hooks/use-effect
      :once
      (let [submit-mode-cmd (fn [^js v]
                              (if (.. v -state (field submit-mode-active))
                                (let [input (.. v -state -doc toString)]
                                  (apply-input input true)
                                  true)
                                ;; ? just update search params
                                (let [input (.. v -state -doc toString)]
                                  (apply-input input true)
                                  true) ;; false
                                ))
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
      {:class (css "FormulaInput"
                   {:display "flex"
                    :align-items "stretch"
                    :justify-content "space-between"})
      :on-blur (fn [_] (when-not submit-mode
                        ;; ? just set search params
                        ;; (js/console.log (str "code: " code))
                        (apply-input code true)))}
      (d/div
       {:class (css
                :font-mono :bg :rounded-sm-l
                {:overflow "auto"
                 :flex "1"
                 ;; :backgroundColor "transparent"
                 ;; :appearance "none"
                 ;; :border-width "0"
                 ;; :padding "$3" ; "0.6rem"
                 })
        :ref editor})
      (d/div
       {:class (css :bg :p-1 :rounded-sm-r
                    {:display "flex"
                     :align-items "center"})}
        ($ Toggle
           {:variant :formula-input/submit-mode
            :size :formula-input/submit-mode
            :pressed (not submit-mode)
            :on-pressed-change
            (fn [_]
              ;; (js/console.log "Submit mode: " submit-mode)
              (set-submit-mode (not submit-mode)))}
           ($d (if submit-mode LinkBreak1Icon LinkNone1Icon))))
      (d/div
       {:class (css "outer"
                    {:height "inherit"
                     :transition "all 0.2s ease-out 0.2s"})
        :style {:width (if submit-mode "var(--sz-9-5)" "0")
                :margin-left (if submit-mode "var(--sp-1)" "0")
                :visibility (if submit-mode "visible" "hidden")}}
        ($ Button
           {:style {:height "100%"
                    :width "100%"
                    :opacity (if submit-mode "1.0" "0.0")
                    :transition "opacity 0.1s ease-out"}
            :variant :secondary
            :size :icon
            :on-click (fn [_] (when submit-mode (apply-input code true)))}
           ($d ResetIcon))))))
