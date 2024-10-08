(ns form-tricorder.components.header
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ;; [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.stitches-config :refer [styled css]]
   [form-tricorder.components.app-toolbar :refer [AppToolbar]]))

(def styles
  (css {:display "flex"
        :min-width "max-content"
        :height "1.5rem"
        :padding "0 0.1rem"
        :justify-content "space-between"
        :column-gap "6px"
        :align-items "stretch"
        :margin "-0.2rem 0 0.2rem 0"

        "& a"
        {:text-decoration "none"}}))

(def item-styles
  (css {:flex "0 0 auto"}))

(def applink-styles
  (css {:display "block"
        :align-self "center"}))

(def toolbar-styles
  (css {}))

(defnc Header
  []
  (d/div
    {:class (str "Header " (styles))}
    (d/a
      {:class (str (item-styles) " " (applink-styles))
       :href "https://tricorder.formform.dev"}
      "FORM tricorder")
    (d/div
      {:class (str (item-styles) " " (toolbar-styles))}
      ($ AppToolbar))))
