(ns form-tricorder.colortest
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   ;; [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ["apcach" :as ap]
   [form-tricorder.utils :refer [linear-map]]))

(def sand-old
  ["#fdfbfa"
   "#f2efed"
   "#e6e3e1"
   "#d6d2cf"
   "#c4c0be"
   "#b2adab"
   "#a19c9b"
   "#8d8887"
   "#7e7978"
   "#6f6a69"
   "#5c5856"
   "#4c4846"
   "#373332"
   "#272322"
   "#181514"])

(def night-old
  ["#fafbff"
   "#edeff7"
   "#e0e3ef"
   "#cfd2df"
   "#bdc1ce"
   "#aaadbb"
   "#999dac"
   "#858899"
   "#76798c"
   "#666a7d"
   "#55586b"
   "#44485b"
   "#2e3347"
   "#202337"
   "#131328"])


(def contrast-max 108)
(def chroma-max 0.37)
(def hue-max 360)

(defn set-contrast
  "Immutable wrapper for setContrast()"
  [apcach-obj val-or-fn]
  (let [clone (js/structuredClone apcach-obj)]
    (.setContrast ap clone val-or-fn)))


(defn contrast-to-fg [fg n] {:cr n :to-fg fg})
(defn contrast-to-bg [bg n] {:cr n :to-bg bg})

(def crs-light (vec (range 104 (dec 55) -3.5)))
(def crs-dark  (vec (range 104 (dec 55) -3.5)))

(def darkest "oklch(0% 0.0 0)")
(def lightest "oklch(98.7% 0.0 0)")
(def contrasts-light (mapv (partial contrast-to-fg darkest) crs-light))
(def contrasts-dark (mapv (partial contrast-to-fg lightest) crs-dark))

(def contrasts-mirrored (into contrasts-light (reverse contrasts-dark)))

(defn make-scale
  [contrasts ->chroma ->hue]
  (let [n (count contrasts)]
    (->> (for [i (range n)
               :let [{:keys [cr to-fg to-bg]} (contrasts i)
                     contrast
                     (cond to-fg (.crToFg ap to-fg cr)
                           to-bg (.crToBg ap to-bg cr)
                           :else (throw (ex-info "Contrast color missing!" {})))
                     chroma (if (number? ->chroma) ->chroma (->chroma i))
                     hue (if (number? ->hue) ->hue (->hue i))]]
           (.apcach ap contrast chroma hue))
         vec)))

(def scales-sand
  (let [n (count crs-light)
        scale (make-scale contrasts-mirrored
                          (partial linear-map 0 (dec n) 0.004 0.005) ;; chroma
                          (partial linear-map 0 (dec n) 36.37 31.06) ;; hue
                          )]
    [scale (vec (reverse scale))]))

(def scales-night
  (let [n (count crs-dark)
        scale (make-scale contrasts-mirrored
                          (partial linear-map 0 (dec n) 0.005  0.020)  ;; chroma
                          (partial linear-map 0 (dec n) 274.97 274.97) ;; hue
                          )]
    [scale (vec (reverse scale))]))


(def scales (vec (concat scales-sand scales-night)))
(def scale-height 40)

(defnc Colortest
  [{:keys []}]
  (d/div
    {:style {:margin-bottom (+ (* scale-height (count scales))
                               scale-height)}}
    (d/div
      {:style {:position "fixed"
               :width "100%"
               :left 0
               :z-index 999
               :display "flex"
               :flex-direction "column"}}
      (for [i (range (count scales))
            :let [scale (scales i)
                  compare-scale (case i
                                  0 sand-old
                                  2 night-old
                                  nil)]]
        (<>
          {:key (str i)}
          (when compare-scale
            (d/div
              {:style {:display "flex"}}
              (for [j (range (count compare-scale))
                    :let [hex (compare-scale j)]]
                (d/div
                  {:key (str i "-" j)
                   :style {:flex 1
                           :height (/ scale-height 2)
                           :background-color hex}}))))
          (d/div
            {:style {:display "flex"}}
            (for [j (range (count (scales i)))
                  :let [hex (.apcachToCss ap (scale j) "oklch")
                        cr  (.. (scale j) -contrastConfig -cr)
                        fg-col (.. (scale j) -contrastConfig -fgColor)
                        bg-col (.. (scale j) -contrastConfig -bgColor)]]
              (d/div
                {:key (str i "x" j)
                 :style {:display "flex"
                         :align-items "end"
                         :justify-content "center"
                         :flex 1
                         ;; :width 60
                         :height scale-height
                         :font-family "\"iosevka term\""
                         :font-size "0.7rem"
                         :line-height "1.0em"
                         :color (if (not= "apcach" fg-col) fg-col bg-col)
                         :background-color hex}}
                (str j)
                (d/br)
                (str (.toFixed cr 1))))))))))

