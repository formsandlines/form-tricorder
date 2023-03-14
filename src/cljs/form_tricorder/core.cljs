(ns form-tricorder.core
  (:require
   [form-tricorder.processor :refer [process]]
   [form-tricorder.contexts :refer [OutputContext]]
   [form-tricorder.components.app-toolbar :refer [AppToolbar]]
   [form-tricorder.components.input-area :refer [InputArea]]
   [form-tricorder.components.function-menubar :refer [FunctionMenubar]]
   [form-tricorder.components.output-area :refer [OutputArea]]
   [helix.core :refer [defnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   ["react-dom/client" :as rdom]
   ["/stitches.config" :refer (css)]))


(defnc App
  []
  (let [[input set-input] (hooks/use-state "((a)b)")
        [views set-views] (hooks/use-state
                           [{:mode-id "calc", :func-id "vtable",
                             :active true}
                            {:mode-id "visu", :func-id "hooks",
                             :active true}])
        process-view (partial process input)
        style (-> {:backgroundColor "darkgray"}
                  clj->js
                  css)]
    (d/div
     {:class (style)}
     ($ AppToolbar
        {:views     views
         :set-views set-views})
     ($ InputArea
        {:submit-handler set-input})
     ($ FunctionMenubar
        {:views     views
         :set-views set-views})
     (provider
      {:context OutputContext
       :value   process-view}
      ($ OutputArea
         {:views     views
          :set-views set-views})))))


(defonce root
  (rdom/createRoot (js/document.getElementById "app")))

(defn ^:export init! []
  ; (refx/dispatch-sync [:initialize-db])
  (.render root ($ App)))



