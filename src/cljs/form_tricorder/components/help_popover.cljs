(ns form-tricorder.components.help-popover
  (:require
   [clojure.math :as math]
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   ;; [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.prose :as p]
   [form-tricorder.utils :refer [let+ unite <&> $nowrap]]
   ["@radix-ui/react-popover" :as PopoverPrimitive]
   ["@radix-ui/react-icons" :refer [Cross2Icon]]))

(def r) ;; hotfix for linting error in let+

(def Root (.-Root PopoverPrimitive))
(def Trigger (.-Trigger PopoverPrimitive))
(def Content (.-Content PopoverPrimitive))
(def Anchor (.-Anchor PopoverPrimitive))
(def Portal (.-Portal PopoverPrimitive))
(def Close (.-Close PopoverPrimitive))
(def Arrow (.-Arrow PopoverPrimitive))

(defnc HelpDl
  [{:keys [children class]}]
  (d/dl
    {:class (unite class
                   (css :mt-4
                        {:display "grid"
                         :row-gap "0.8rem"
                         :column-gap "0.6rem"
                         :grid-template-columns "1fr"}
                        ["& > dt"
                         :border-t :border-col :pt-1
                         :weight-medium]
                        ["@media (min-width: 551px)"
                         {:grid-template-columns "auto 1fr"}
                         ["& > dd"
                          :border-t :border-col :pt-1]
                         ["& > dt"
                          {:max-width "6.5rem"}]]
                        ["@media (max-width: 550px)"
                         {:gap "0"}
                         ["& > dd"
                          :mt-2]
                         ["& > dt:not(:first-child)"
                          :mt-4]]))}
    children))

(defnc HelpNavLink
  [props]
  (let+ [{:keys [target class children]
          :rest r} props]
    (d/button
      {:class (unite class
                     p/$link-styles)
       :on-click (fn [_]
                   (let [el (.querySelector js/document target)]
                     (if el
                       (.scrollIntoView el
                                        #js {:behavior "smooth"
                                             :block "start"})
                       (throw (ex-info "Help navigation id not found!"
                                       {:id target})))))
       & r}
      children)))

(defnc HelpPopover
  [{:keys [children]}]
  (let [[open? set-open?] (hooks/use-state false)]
    ($d Root
      {:open open?
       :onOpenChange #(set-open? %)
       :modal false}
      ($d Trigger
        {:asChild true}
        children)
      ($d Portal
        ($d Content
          {:align "start"
           :side "bottom"
           :sideOffset 96
           :collisionPadding 6
           :onInteractOutside #(.preventDefault %)
           :sticky "always"
           :class (css "outer"
                       :px-6 :py-6 :text-sm :shadow-md
                       ;; :bg-popover
                       :fg-popover
                       :border :border-col :rounded-md
                       {:width "70vw"
                        :min-width "260px"
                        :max-width "600px"
                        :max-height "600px"
                        :overflow "auto"
                        :z-index "11"
                        :background-color "color-mix(in srgb, var(--col-bg-popover) 96%, transparent)"})}
          ($d Close
            {:asChild true}
            ($ Button
               {:class (css :top-6 :right-6
                            {:position "absolute"
                             :outline "0.4rem solid var(--col-bg)"
                             :z-index "12"})
                :variant :secondary
                :size :icon-sm}
               ($d Cross2Icon)))
          (d/div
            ($ p/H1
               "Help")
            ($ p/P "To use the app, simply enter an expression " ($ HelpNavLink {:target "#help-notation"} "FORMula") " in the input field at the top and choose an output function in one of the three categories:")
            (d/div
              {:class (css
                        :mt-2
                        {:display "grid"
                         :column-gap "0.4rem"
                         :grid-template-columns "1fr"}
                        ["@media (min-width: 551px)"
                         {:grid-template-columns "repeat(3, 1fr)"}
                         ["& > *"
                          :pl-3
                          {:border-left-width "3px"}]]
                        ["@media (max-width: 550px)"
                         {:gap "0"}
                         ["& > *"
                          :mt-2
                          {:border-top-width "3px"}]
                         ["& > *:first-child"
                          :mt-0]])}
              (d/div
                {:class (css {:border-color "var(--col-fmenu-expr)"})}
                ($ p/H3 ($ HelpNavLink {:target "#help-expr"} "expression"))
                ($ p/P "visualize the FORM in different formats and simplify it as much as possible"))
              (d/div
                {:class (css {:border-color "var(--col-fmenu-eval)"})}
                ($ p/H3 ($ HelpNavLink {:target "#help-eval"} "evaluation"))
                ($ p/P "calculate the FORM and analyze its value structure for different interpretations"))
              (d/div
                {:class (css {:border-color "var(--col-fmenu-emul)"})}
                ($ p/H3 ($ HelpNavLink {:target "#help-emul"} "emulation"))
                ($ p/P "run the FORM in various cellular automatons to observe its system behaviour")))
            ($ p/H2
               {:id "help-notation"
                :class (css :pt-1
                            :border-col)}
               "FORMula Notation")
            ($ HelpDl
               (d/dt "Empty space")
               (d/dd
                 ($ p/P ($ p/Formula " ") (<&> "&nbsp;") "⇔ unmarked FORM"))
               (d/dt "Relation")
               (d/dd
                 ($ p/P ($ p/Formula "x y …") " (where " ($ p/Formula "x") ", "
                    ($ p/Formula "y") ", … are expressions)"))
               (d/dt "FORM")
               (d/dd
                 ($ p/P ($ p/Formula "()") ", " ($ p/Formula "(())") ", "
                    ($ p/Formula "(x y)") ", " ($ p/Formula "(x (y (…))") ", …"))
               (d/dt "Variable")
               (d/dd
                 ($ p/P ($ p/Formula "name") " (any string of characters)")
                 ($ p/Ul
                    (d/li "with subscript: " ($ p/Formula "name_sub"))
                    (d/li "with spaces: " ($ p/Formula "long name"))))
               (d/dt "Constant")
               (d/dd
                 ($ p/Dl
                    (d/dt ($ p/Formula ":N"))
                    (d/dd " → " (d/em "unmarked")
                      " ⇔" (<&> "&nbsp;") ($ p/Formula " "))
                    (d/dt ($ p/Formula ":M"))
                    (d/dd " → " (d/em "marked")
                      " ⇔ " ($ p/Formula "()"))
                    (d/dt ($ p/Formula ":U"))
                    (d/dd " → " (d/em "undetermined")
                      " ⇔ " ($ p/Formula "{..@}") " / " ($ p/Formula "{@ ,}"))
                    (d/dt ($ p/Formula ":I"))
                    (d/dd " → " (d/em "imaginary")
                      " ⇔ " ($ p/Formula "{..@.}") " / " ($ p/Formula "{@ }"))))
               (d/dt "Unclear FORM")
               (d/dd
                 ($ p/P ($ p/Formula "/unclear name/"))
                 ($ p/Ul
                    (d/li "with subscript: " ($ p/Formula "/name_sub/"))))
               (d/dt (d/span {:class $nowrap} "self-equivalent") " "
                 (d/span {:class $nowrap} "re-entry") " FORM")
               (d/dd 
                 ($ p/P ($ p/Formula "{<signature/options> a,…,z}"))
                 ($ p/Ul
                    (d/li ($ p/Formula "a,…,z") " are left-nested "
                      (d/em "expressions") ", equivalent to "
                      ($ p/Formula "(((a) …) z)")))
                 ($ p/H3 "Signature syntax (more terse):")
                 ($ p/Dl
                    (d/dt ($ p/Formula "..@") " / " ($ p/Formula "..@.") " :")
                    (d/dd (d/em "even / odd") " re-entry number")
                    (d/dt ($ p/Formula "@_") " :")
                    (d/dd "outermost FORM is " (d/em "unmarked"))
                    (d/dt ($ p/Formula "@~") " :")
                    (d/dd "interpret as "
                      (d/em "“recursive identity”") " in evaluation"))
                 ($ p/Note "when combined, " ($ p/Formula "@~")
                    " belongs together and " ($ p/Formula "_")
                    " is the last character, e. g. "
                    ($ p/Formula "{..@~._ …}"))
                 ($ p/H3 "Options syntax (more explicit):")
                 ($ p/Dl
                    (d/dt ($ p/Formula "2r") " / " ($ p/Formula "2r+1") " :")
                    (d/dd (d/em "even / odd") " re-entry number")
                    (d/dt ($ p/Formula "open") " :")
                    (d/dd "outermost FORM is " (d/em "unmarked"))
                    (d/dt ($ p/Formula "alt") " :")
                    (d/dd "interpret as "
                      (d/em "“recursive identity”") " in evaluation"))
                 ($ p/Note "each option must be followed by a pipe, e. g. "
                    ($ p/Formula "{2r+1|alt|open| …}"))
                 ($ p/P
                    {:class (css :mt-3)}
                    "Without any signature/options, the re-entry defaults to:")
                 ($ p/Ul
                    (d/li "even resolution: "
                      ($ p/Formula "{,}") " / " ($ p/Formula "{@ ,}") " ⇔ "
                      ($ p/Formula "{..@ ,}") " / " ($ p/Formula "{2r| ,}"))
                    (d/li "odd resolution: "
                      ($ p/Formula "{}") " / " ($ p/Formula "{@ }") " ⇔ "
                      ($ p/Formula "{..@. }") " / " ($ p/Formula "{2r+1| }")))))
            ($ p/H2
               {:id "help-expr"}
               "Functions: expression")
            ($ HelpDl
               (d/dt "Hook notation")
               (d/dd 
                 ($ p/P "A common representation of FORMS introduced by George Spencer-Brown in his " ($ p/A {:href "https://en.wikipedia.org/wiki/Laws_of_Form"} "Laws of Form") ". Also used throughout " ($ p/A {:href "https://uformiform.info"} "uFORM iFORM") ".")
                 ($ p/P "Spatially more efficient than " (d/em "circle notation") ", although less readable with complex FORMs."))
               (d/dt "Circle notation")
               (d/dd 
                 ($ p/P "Alternative representation of FORMs using nested circles. Has been used sparingly in " ($ p/A {:href "https://en.wikipedia.org/wiki/Laws_of_Form"} "Laws of Form") " as well as in " ($ p/A {:href "https://uformiform.info"} "uFORM iFORM") " (here extended to re-entry FORMs), mostly for illustrative or reflective purposes.")
                 ($ p/P "Less spatially efficient than " (d/em "hook notation") " and thus often not convenient for mathematical writing. Can, however, be clearer and more intuitive to read and is especially well suited for didactive and illustrative purposes."))
               (d/dt "Depth tree")
               (d/dd 
                 ($ p/P "Analytical representation of FORMs as a tree with the first space (shallowest depth) as its root. Each FORM inside a space of depth " (d/em "d" (d/sub "n")) " creates a space " (d/em "d" (d/sub "n+1")) " inside of it.")
                 ($ p/P "The visualization exposes all expressions in the same depth from different spaces, which can be helpful to untangle very complex FORMs. It may also be useful as a tool for teaching."))
               (d/dt "EDN/JSON")
               (d/dd
                 ($ p/P "Data formats that store representations used internally " ($ p/A {:href "https://formform.dev"} "formform") ", which is the library that powers the logic and computations in this app.")
                 ($ p/P "EDN is the current format used by formform and is much more compact and readable than JSON. If you are using formform yourself, it is convenient to copy expressions from this function instead of writing them yourself, but I mostly use if for myself.")
                 ($ p/P "JSON is a legacy format that is not in use anymore by the current version of formform. However, some visualizations still rely on it, so it may be used for debugging.")))
            ($ p/H2
               {:id "help-eval"}
               "Functions: evaluation")
            ($ HelpDl
               (d/dt "Value table")
               (d/dd
                 ($ p/P "Also known as a " ($ p/A {:href "https://en.wikipedia.org/wiki/Truth_table"} "truth table") " in propositional logic. Each row shows an interpretation of all variables in the input expression with a unique set of values and the evaluated result in the rightmost column. If there are no variables, there is only one result."))
               (d/dt "vmap")
               (d/dd
                 ($ p/P "Short for “variable/value-map”. Can be seen as an alternative representation to value tables that is especially useful for pattern recognition.")
                 ($ p/P "Each variable of an expression is represented as a diamond-shaped group that consists of four smaller groups representing its interpretation states in four directions:")
                 ($ p/Dl
                    (d/dt "← : ")
                    (d/dd ($ p/Formula ":N") " " (d/em "(unmarked)"))
                    (d/dt "→ : ")
                    (d/dd ($ p/Formula ":M") " " (d/em "(marked)"))
                    (d/dt "↓ : ")
                    (d/dd ($ p/Formula ":U") " " (d/em "(undetermined)"))
                    (d/dt "↑ : ")
                    (d/dd ($ p/Formula ":I") " " (d/em "(imaginary)")))
                 ($ p/P "Following each direction, this process is repeated recursively down to the smallest diamonds (the ones that are actually drawn), which represent the interpretation results. Each result is color-coded according to the color convention used in uFORM iFORM:")
                 ($ p/Dl
                    (let [square 10
                          rhomb (math/round (math/sqrt (* square square 2)))]
                      (for [[col v] [["#000000" :N]
                                     ["#4757ff" :M]
                                     ["#ff0044" :U]
                                     ["#00ff5f" :I]]]
                        (<>
                         {:key (name v)}
                         (d/dt
                          (d/svg
                           {:class (css {:display "inline-block"})
                            :width rhomb
                            :height rhomb}
                           (d/g
                            {:transform (str "translate (0," (/ rhomb 2)
                                             ") rotate(-45,0,0)")}
                            (d/rect
                             {:x 0
                              :y 0
                              :width square
                              :height square
                              :fill col})))
                          " : ")
                         (d/dd ($ p/Formula (str v)))))))
                 ($ p/P "Below the diagram you can see the order in which variables are interpreted in the vmap. A different ordering can be selected using the drop-down menu in the bottom-left corner of the app.")
                 ($ p/P "A vmap of a FORM with more than one variable is called a " (d/em "vmap perspective") ", since it can only represent one unique ordering path coherently. It is often useful to examine multiple different perspectives side by side to spot sub-patterns or symmetries. By clicking on the button above the diagram, you can render all " (d/em "vmap perspectives") " at once. ")
                 ($ p/Note "For more information about the vmap and its applications, please read my " ($ p/A {:href "https://observablehq.com/@formsandlines/recursive-mapping-of-4-valued-forms-with-vmaps"} "introductory notebook") "."))
               (d/dt "formDNA")
               (d/dd
                 ($ p/P "A code format that stores all possible interpretation results of a FORM in a single " ($ p/A {:href "https://en.wikipedia.org/wiki/Quaternary_numeral_system"} "quaternary number") ". It is essentially a compact representation an equivalence class of FORMs that all share the same value structure.")
                 ($ p/P "Each formDNA is equivalent to a corresponding " ($ p/A {:href "https://en.wikipedia.org/wiki/Wolfram_code"} "Wolfram code") " with type ‘1D’ and rulespace ‘k=4, r=1’, which can be useful to study arbitrary value structures in cellular automata such as " (d/em "SelFis") ".")
                 ($ p/Note "For more information about formDNA and its applications, please read my " ($ p/A {:href "https://observablehq.com/@formsandlines/the-dna-of-4-valued-forms"} "introductory notebook") ".")))
            ($ p/H2
               {:id "help-emul"}
               "Functions: emulation")
            ($ HelpDl
               (d/dt "SelFi") 
               (d/dd
                 ($ p/P "Introduced by Ralf Peyn in the appendix of " ($ p/A {:href "https://uformiform.info"} "uFORM iFORM") ", 2017.")
                 ($ p/P "A 1-dimensional " ($ p/A {:href "https://en.wikipedia.org/wiki/Cellular_automaton"} "cellular automaton") " with 4 colors corresponding to the 4 values in FORM logic. The next cell state is the result of evaluating the given input FORM, using the neighborhood of sorrounding cell states from the previous evolution as an interpretation for each of the variables in the FORM.")
                 ($ p/Dl
                    (let [square 10]
                      (for [[col v] [["#000000" "n (unmarked)"]
                                     ["#4757ff" "m (marked)"]
                                     ["#ff0044" "u (undetermined)"]
                                     ["#00ff5f" "i (imaginary)"]]]
                        (<>
                         {:key (name v)}
                         (d/dt
                          (d/svg
                           {:class (css {:display "inline-block"})
                            :width square
                            :height square}
                           (d/rect
                            {:x 0
                             :y 0
                             :width square
                             :height square
                             :fill col}))
                          " : ")
                         (d/dd (d/em v))))))
                 )
               (d/dt "mindFORM")
               (d/dd
                 ($ p/Note
                    {:style {:background-color "var(--col-e4)"}}
                    "work in progress")
                 ($ p/P "Introduced by Ralf Peyn via an article written by Gitta Peyn ‘" ($ p/A {:href "https://www.carl-auer.de/magazin/systemzeit/how-does-system-function-operate-5"} "How does System function/operate 5 – mindFORMs") "’, published in February 2, 2019 on " ($ p/A {:href "https://www.carl-auer.de/magazin/systemzeit/" :class (css {:letter-spacing "0.1em"})} "systemzeit") ".")
                 ($ p/P "A 2-dimensional " ($ p/A {:href "https://en.wikipedia.org/wiki/Cellular_automaton"} "cellular automaton") " with 4 colors corresponding to the 4 values in FORM logic. The next cell state is evaluated just like in SelFis, but with the neighborhood adjacent to the direction observed by the current cell. This direction is decided by its own state:")
                 ($ p/Dl
                    {:dt-left? true}
                    (d/dt (d/em "n (unmarked):"))
                    (d/dd "←")
                    (d/dt (d/em "m (marked):"))
                    (d/dd "→")
                    (d/dt (d/em "u (undetermined):"))
                    (d/dd "↓")
                    (d/dt (d/em "i (imaginary):"))
                    (d/dd "↑")))
               )))))))
