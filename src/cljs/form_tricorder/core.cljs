(ns form-tricorder.core
  {:shadow.css/include ["form_tricorder/core.css"
                        "form_tricorder/keyframes.css"]}
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
   [form-tricorder.utils :refer [unite]]
   ;; [form-tricorder.test.comptest :refer [Foobar]]
   ;; [form-tricorder.test.colortest :refer [Colortest]]
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
  (let [error (rf/subscribe [:error/get])
        error-msg (ex-message error)
        error-data (ex-data error)]
    (when error
      ($ Alert
         {:variant :destructive
          :class (css :mt-2)}
         ($ AlertTitle "Error!")
         ($ AlertDescription
            (d/pre
             {:class (css :max-h-32
                          {:overflow "auto"})}
             (d/code
              {:class (css :font-mono :font-size-xs :line-h-none
                           {:text-wrap "auto"})}
               (str error-msg " \nData: " error-data))))))))

(defnc App
  []
  (let [appearance (rf/subscribe [:theme/appearance])]
    (hooks/use-effect
     :once
     (.. js/window
         (matchMedia "(prefers-color-scheme: dark)")
         (addEventListener
          "change"
          #(rf/dispatch [:theme/set-system-color-scheme
                         {:next-system-color-scheme
                          (if (.-matches %) "dark" "light")}]))))
    (hooks/use-effect
     [appearance]
     (let [root-el (.. js/document -documentElement)]
       (aset (.-style root-el) "color-scheme" (if (= :system appearance)
                                                "light dark"
                                                (name appearance)))))
    ($ ErrorBoundary
       (let [$item-styles (css ["&:last-child"
                                {:flex "1"}])]
         (d/div
          {:class (css "App" "outer"
                       :p-3 :gap-2 :fg :bg
                       {:display "flex"
                        :height "100vh"
                        :flex-direction "column"})
           ;; :style {:color-scheme (name appearance)}
           }
          (d/div
           {:class $item-styles}
           ($ Header))
          (d/div
           {:class (css "inner" :gap-2
                        {:display "flex"
                         :flex-direction "column"
                         :max-width "100%"}
                        [:media-min-lg
                         {:flex-direction "row"}])}
           (d/div
            {:class (css :min-h-10 :rounded-sm
                         [:media-min-lg
                          {:flex "4 1 auto"
                           :min-width "10rem"
                           :height "100%"}])}
            ($ FormulaInput
               {:apply-input #(rf/dispatch [:input/changed-formula
                                            {:next-formula %1
                                             :set-search-params? %2}])})
            ($ ErrorDisplay))
           (d/div
            {:class (css [:media-min-lg
                          {:flex "1 0 28rem"
                           :height "100%"}])}
            ($ FunctionMenu)))
          (d/div
           {:class (css :bottom-0 :left-0
                        {:position "absolute"
                         :z-index "10"})}
           ($ OptionsDrawer))
          (d/div
           {:class (str $item-styles " "
                        (css "inner"
                             {:height "100%"
                              :overflow "hidden"}))}
           ;; ($ Colortest)
           ;; ($ Foobar)
           ($ OutputArea)))))))


(defonce root
  (rdom/createRoot (js/document.getElementById "root")))

(defn ^:export init []
  (rf/dispatch-sync [:initialize-db])
  ;; (.render root ($ App))
  (.render root ($ StrictMode ($ App))))

