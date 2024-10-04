(ns form-tricorder.core
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ;; [formform.calc :as calc]
   ;; [formform.expr :as expr]
   ;; [formform.io :as io]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.events :as events]
   [form-tricorder.subs :as subs]
   [form-tricorder.effects :as effects]
   [form-tricorder.functions :as func]
   [form-tricorder.utils :refer [log style> global-css> css>
                                 dark-theme light-theme]]
   [form-tricorder.components.header :refer [Header]]
   [form-tricorder.components.error-boundary :refer [ErrorBoundary]]
   [form-tricorder.components.formula-input :refer [FormulaInput]]
   [form-tricorder.components.function-menu :refer [FunctionMenu]]
   [form-tricorder.components.output-area :refer [OutputArea]]
   ["react" :refer [StrictMode]]
   ["react-dom/client" :as rdom]))


(def global-styles
  (global-css>
   {"body"
    {:font-family "$base"
     :font-weight "$normal"
     :font-size "$base"
     :line-height "$lineHeights$base"
     :background-color "$colors$outer_bg"
     "a:hover"
     {:text-decoration "underline"}}}))

;; (def body-styles
;;   (css> {}))

(def styles
  (css> {:display "flex"
         :height "100vh"
         :flex-direction "column"
         :padding "$3" ; "0.6rem"
         :gap "$2" ; "0.4rem"
         :color "$colors$outer_fg"
         "& a"
         {:color "$colors$outer_m100"
          "&:hover"
          {
           ; :color "$colors$outer_m200"
           }}}))

(def item-styles
  (css> {"&:last-child"
         {:flex "1"
          }}))

(defnc ScaleTest
  [{:keys [scale n]}]
  (let [rng (rest (range (inc n)))
        test-styles (css> {:display "flex"
                           :margin-bottom "10px"
                           "> *"
                           {:padding-right "6px"
                            "> *"
                            {:height "40px"
                             :margin-bottom "2px"
                             :background-color "black"}}})]
    (d/div
     {:class (test-styles)}
     (for [i rng
           :let [styles (css> {:width (str "$" scale "$" i)})]]
       (d/div
        {:key (str i)}
        (d/div
         {:class (styles)})
        (str i))))))

(defnc ErrorDisplay
  []
  (let [error (rf/subscribe [:error/get])]
    (when error
      (d/div
        {:style {:font-family "courier, monospace"
                 :background "#FFAAAA"
                 :color "black"
                 :padding "1em"}}
        (d/pre
          (d/code (pr-str error)))))))

(defnc App
  []
  (let [formula (rf/subscribe [:input/formula])
        appearance (rf/subscribe [:theme/appearance])]
    (hooks/use-effect
      :once
      (js/console.log {:a 1 :b "two" "c" :three 'd [4]})
      (global-styles)
      ;; (.add js/document.body.classList body-styles)
      )
    (hooks/use-effect
      [appearance]
      (if (= appearance :dark)
        (do (.add js/document.body.classList dark-theme)
            (.remove js/document.body.classList light-theme))
        (do (.add js/document.body.classList light-theme)
            (.remove js/document.body.classList dark-theme))))
    ($ ErrorBoundary
       (d/div
         {:class (str "App " (styles))}
         (d/div
           {:class (item-styles)}
           ($ Header))
         (d/div
           {:class (item-styles)}
           ($ FormulaInput
              {:current-formula formula
               :apply-input #(rf/dispatch [:input/changed-formula
                                           {:next-formula %}])})
           ($ ErrorDisplay)
)
         (d/div
           {:class (item-styles)}
           ($ FunctionMenu
              {:handle-click
               (fn [func-id alt-view?]
                 (let [view-index (if alt-view? 1 0)]
                   (do
                     (when alt-view? (rf/dispatch [:views/split]))
                     (rf/dispatch [:views/set-func-id
                                   {:next-id    func-id
                                    :view-index view-index}]))))}))
         (d/div
           {:class (item-styles)
            :style {:overflow-y "auto"}}
                                        ; ($ ScaleTest {:scale "space" :n 11})
                                        ; ($ ScaleTest {:scale "sizes" :n 3})
                                        ; ($ ScaleTest {:scale "borderWidths" :n 3})
           ($ OutputArea))))))


(defonce root
  (rdom/createRoot (js/document.getElementById "root")))

(defn ^:export init! []
  (rf/dispatch-sync [:initialize-db])
  (.render root ($ StrictMode ($ App))))

