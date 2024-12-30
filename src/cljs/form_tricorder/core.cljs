(ns form-tricorder.core
  {:shadow.css/include ["form_tricorder/core.css"]}
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.events :as events]
   [form-tricorder.subs :as subs]
   [form-tricorder.effects :as effects]
   [form-tricorder.functions :as func]
   ;; [form-tricorder.utils :refer [log]]
   [form-tricorder.stitches-config :as st]
   [form-tricorder.foobar :refer [Foobar]]
   [form-tricorder.colortest :refer [Colortest]]
   [form-tricorder.components.common.alert
    :refer [Alert AlertDescription AlertTitle]]
   [form-tricorder.components.header :refer [Header]]
   [form-tricorder.components.error-boundary :refer [ErrorBoundary]]
   [form-tricorder.components.formula-input :refer [FormulaInput]]
   [form-tricorder.components.function-menu :refer [FunctionMenu]]
   [form-tricorder.components.output-area :refer [OutputArea]]
   [form-tricorder.components.options-drawer :refer [OptionsDrawer]]
   ["react" :refer [StrictMode]]
   ["react-dom/client" :as rdom]))


(defnc ErrorDisplay
  []
  (let [error (rf/subscribe [:error/get])]
    (when error
      ($ Alert
         {:variant :destructive
          :class (css :mt-2)}
         ($ AlertTitle "Error!")
         ($ AlertDescription
            (d/pre
             {:class (css :max-h-32
                          {:overflow "scroll"})}
             (d/code
              {:class (css :font-mono :font-size-xs :line-h-none
                           {:text-wrap "auto"})}
              (pr-str error))))))))

(defnc App
  []
  (let [appearance (rf/subscribe [:theme/appearance])]
    (hooks/use-effect
      [appearance]
      (do
        (let [root-el (.. js/document -documentElement)]
          (.setAttribute root-el "data-theme" (name appearance))
          (aset (.-style root-el) "color-scheme" (name appearance)))
        (if (= appearance :dark)
          (do (.add js/document.body.classList st/dark-theme)
              (.remove js/document.body.classList st/light-theme))
          (do (.add js/document.body.classList st/light-theme)
              (.remove js/document.body.classList st/dark-theme)))))
    ($ ErrorBoundary
       (let [$item-styles (css ["&:last-child"
                                {:flex "1"}])]
         (d/div
          {:class (css "App" "outer"
                       :p-3 :gap-2 :fg :bg
                       {:display "flex"
                        :height "100vh"
                        :flex-direction "column"})
           :style {:color-scheme (name appearance)}}
          (d/div
           {:class $item-styles}
           ($ Header))
          (d/div
           {:class $item-styles}
           ($ FormulaInput
              {:apply-input #(rf/dispatch [:input/changed-formula
                                           {:next-formula %1
                                            :set-search-params? %2}])})
           ($ ErrorDisplay))
          (d/div
           {:class (css :bottom-0 :left-0
                        {:position "absolute"
                         :z-index "10"})}
           ($ OptionsDrawer))
          (d/div
           {:class $item-styles}
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
           {:class (str $item-styles " "
                        (css "inner"
                             {:overflow-y "auto"}))}
           ;; ($ Colortest)
           ;; ($ Foobar)
           ($ OutputArea)))))))


(defonce root
  (rdom/createRoot (js/document.getElementById "root")))

(defn ^:export init! []
  (rf/dispatch-sync [:initialize-db])
  (.render root ($ StrictMode ($ App))))

