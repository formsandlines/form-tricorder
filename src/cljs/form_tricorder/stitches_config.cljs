(ns form-tricorder.stitches-config
  (:require
   ["@stitches/react" :refer [createStitches]]
   [form-tricorder.styles :refer [stitches-specs colors-light colors-dark]]))

(def ^js obj
  (createStitches
   (clj->js
    stitches-specs)))

(defn wrap [o]
  (fn [m] (->> m clj->js o)))

(defn wrap+el [o]
  (fn [el m] (->> m clj->js (o el))))

(def styled       (wrap+el (aget obj "styled")))
(def css          (wrap (aget obj "css")))
(def global-css   (wrap (aget obj "globalCss")))
(def keyframes    (wrap (aget obj "keyframes")))
(def get-css-text (aget obj "getCssText"))
(def theme        (aget obj "theme"))
(def create-theme (aget obj "createTheme"))
(def config       (aget obj "config"))
(def sheet        (aget obj "sheet"))

(def light-theme (create-theme "light-theme" (clj->js {:colors colors-light})))
(def dark-theme  (create-theme "dark-theme" (clj->js {:colors colors-dark})))
