(ns form-tricorder.core
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
   [form-tricorder.components.header :refer [Header]]
   [form-tricorder.components.error-boundary :refer [ErrorBoundary]]
   [form-tricorder.components.formula-input :refer [FormulaInput]]
   [form-tricorder.components.function-menu :refer [FunctionMenu]]
   [form-tricorder.components.output-area :refer [OutputArea]]
   [form-tricorder.components.options-drawer :refer [OptionsDrawer]]
   ["react" :refer [StrictMode]]
   ["react-dom/client" :as rdom]))


(def global-styles
  (st/global-css
   {"body"
    {:font-family "$base"
     :font-weight "$normal"
     ;; :font-size "$base"
     :line-height "$base"
     :color "$outer-fg"
     :background-color "$outer-bg"
     "a:hover"
     {:text-decoration "underline"}}}))

(def styles
  (st/css {:display "flex"
        :height "100vh"
        :flex-direction "column"
        :padding "$3" ; "0.6rem"
        :gap "$2" ; "0.4rem"
        :color "$colors$outer-fg"
        ;; "& a"
        ;; {:color "inherit" ;; outer_m100
        ;;  "&:hover"
        ;;  {
        ;;   ;; :color "$colors$outer-link-hover" ;; outer_m200
        ;;   }}
        }))

(def item-styles
  (st/css {"&:last-child"
        {:flex "1"
         }}))

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
  (let [appearance (rf/subscribe [:theme/appearance])]
    (hooks/use-effect
      :once
      (global-styles))
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
       (d/div
        {:class (str "App " (styles))
         :style {:color-scheme (name appearance)}}
         (d/div
           {:class (item-styles)}
           ($ Header))
         (d/div
           {:class (item-styles)}
           ($ FormulaInput
              {:apply-input #(rf/dispatch [:input/changed-formula
                                           {:next-formula %1
                                            :set-search-params? %2}])})
           ($ ErrorDisplay))
         (d/div
           {:style {:position "absolute"
                    :z-index "10"
                    :bottom 0
                    :left 0}}
           ($ OptionsDrawer))
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
           ;; ($ Colortest)
           ;; ($ Foobar)
           ($ OutputArea))))))


(defonce root
  (rdom/createRoot (js/document.getElementById "root")))

(defn ^:export init! []
  (rf/dispatch-sync [:initialize-db])
  (.render root ($ StrictMode ($ App))))

