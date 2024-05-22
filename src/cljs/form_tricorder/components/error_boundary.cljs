(ns form-tricorder.components.error-boundary
  (:require
   ;; [form-tricorder.re-frame-adapter :as rf]
   [helix.core :refer [defnc fnc $ <> provider defcomponent]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]))

(defcomponent ErrorBoundary
  ;; To avoid externs inference warnings, we annotate `this` with ^js whenever
  ;; accessing a method or field of the object.
  (constructor [^js this]
               (set! (.-state this) #js {:hasError false}))

  (componentDidCatch [^js this error _info]
                     (.setState this #js {:data error}))

  ^:static (getDerivedStateFromError [_this _error]
                                     #js {:hasError true})

  (render [^js this props ^js state]
          (if (.-hasError state)
            (d/div
             "test"
             (d/pre
              (d/code
               (pr-str (.-data state)))))
            (:children props))))

