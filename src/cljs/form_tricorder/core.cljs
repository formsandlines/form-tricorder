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

(def call-function
  (fn [func-id alt-view?]
    (let [view-index (if alt-view? 1 0)]
      (when alt-view? (rf/dispatch [:views/split]))
      (rf/dispatch [:views/set-func-id
                    {:next-id    func-id
                     :view-index view-index}]))))

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
  (let [appearance (rf/subscribe [:theme/appearance])
        [keybind-mode-value set-keybind-mode-value] (hooks/use-state nil)]
    (hooks/use-effect
      [appearance]
      (let [root-el (.. js/document -documentElement)]
        (aset (.-style root-el) "color-scheme" (if (= :system appearance)
                                                 "light dark"
                                                 (name appearance)))))
    (hooks/use-effect
      :once
      (let [handle-key-down
            (fn [e] ;; ? refactor to use model data instead of hardcoding
              (cond
                (.-ctrlKey e)
                (let [shift? (.-shiftKey e)
                      [expr-open? eval-open? emul-open?]
                      (map #(= "open"
                               (.. js/document (getElementById %)
                                   (getAttribute "data-state")))
                           ["mode-expr" "mode-eval" "mode-emul"])]
                  (cond
                    expr-open?
                    (do (case (.-key e)
                          ("h" "H") (call-function :hooks shift?)
                          ("c" "C") (call-function :graphs shift?)
                          ("t" "T") (call-function :depthtree shift?)
                          ("e" "E") (call-function :edn shift?)
                          ("j" "J") (call-function :json shift?)
                          nil)
                        (set-keybind-mode-value nil))
                    eval-open?
                    (do (case (.-key e)
                          ("t" "T") (call-function :vtable shift?)
                          ("v" "V") (call-function :vmap shift?)
                          ("d" "D") (call-function :fdna shift?)
                          nil)
                        (set-keybind-mode-value nil))
                    emul-open?
                    (do (case (.-key e)
                          ("s" "S") (call-function :selfi shift?)
                          ("m" "M") (call-function :mindform shift?)
                          nil)
                        (set-keybind-mode-value nil))
                    :else
                    (case (.-key e)
                      ("x" "X") (set-keybind-mode-value "expr")
                      ("v" "V") (set-keybind-mode-value "eval")
                      ("e" "E") (set-keybind-mode-value "emul")
                      nil)))
                (= (.-key e) "Escape")
                (set-keybind-mode-value nil)
                :else nil))]
        (.addEventListener js/window "keydown" handle-key-down)
        ;; Cleanup
        #(.removeEventListener js/window "keydown" handle-key-down)))
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
               ($ FunctionMenu
                  {:handle-select-fn call-function
                   :keybind-mode-value keybind-mode-value})))
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
  (.render root ($ StrictMode ($ App)))
  )

