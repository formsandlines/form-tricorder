(ns form-tricorder.components.header
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.utils :refer [unite]]
   ;; [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.app-toolbar :refer [AppToolbar]]))


(defnc Header
  []
  (let [$item-styles (css {:flex "0 0 auto"})]
    (d/div
      {:class (css "Header"
                   :h-8 :gap-x-2 :font-size-sm
                   {:display "flex"
                    :min-width "max-content"
                    :padding "0 0.1rem"
                    :margin "-0.2rem 0 0.2rem 0"
                    :justify-content "space-between"
                    :align-items "stretch"}
                   ["& a"
                    {:text-decoration "none"}])}
      (d/a
        {:class (unite $item-styles
                       (css {:display "block"
                             :align-self "center"}))
         :href "https://tricorder.formform.dev"}
        "FORM tricorder")
      (d/div
        {:class $item-styles}
        ($ AppToolbar)))))
