(ns css.colors
  (:require
   ["apcach" :as ap]
   [css.common :as c]
   [clojure.string :as str]
   [clojure.pprint :as pprint]))


;; Color Scales

(defn contrast-to-fg [fg n] {:cr n :to-fg fg})
(defn contrast-to-bg [bg n] {:cr n :to-bg bg})

(def crs (vec (range 104 (dec 55) -3.5)))

(def darkest "oklch(0% 0.0 0)")
(def lightest "oklch(98.7% 0.0 0)")
(def contrasts-light (mapv (partial contrast-to-fg darkest) crs))
(def contrasts-dark (mapv (partial contrast-to-fg lightest) crs))

(def contrasts-mirrored (into contrasts-light (reverse contrasts-dark)))

(defn make-scale-apcach
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

(def scale-sand-apcach
  (let [n (count crs)]
    (make-scale-apcach contrasts-mirrored
                       (partial c/linear-map 0 (dec n) 0.006 0.008) ;; chroma
                       (partial c/linear-map 0 (dec n) 36.37 31.06) ;; hue
                       )))

(def scale-night-apcach
  (let [n (count crs)]
    (make-scale-apcach contrasts-mirrored
                       (partial c/linear-map 0 (dec n) 0.005  0.020)  ;; chroma
                       (partial c/linear-map 0 (dec n) 274.97 274.97) ;; hue
                       )))

(def scale-coral-apcach
  (let [n (count crs)]
    (make-scale-apcach contrasts-mirrored
                       (partial c/linear-map 0 (dec n) 0.047  0.11) ;; chroma
                       (partial c/linear-map 0 (dec n) 14 13) ;; hue
                       ;; (partial c/linear-map 0 (dec n) 0.18  0.176)  ;; chroma
                       ;; (partial c/linear-map 0 (dec n) 13.15 13.15) ;; hue
                       )))

;; expr mode
(def scale-seafoam-apcach
  (let [n (count crs)]
    (make-scale-apcach contrasts-mirrored
                       (partial c/linear-map 0 (dec n) 0.0185  0.0351)  ;; chroma
                       (partial c/linear-map 0 (dec n) 160.83 188.46) ;; hue
                       )))

;; eval mode
(def scale-lavender-apcach
  (let [n (count crs)]
    (make-scale-apcach contrasts-mirrored
                       (partial c/linear-map 0 (dec n) 0.0209  0.0446)  ;; chroma
                       (partial c/linear-map 0 (dec n) 285.79 280.68) ;; hue
                       )))

;; emul mode
(def scale-mauve-apcach
  (let [n (count crs)]
    (make-scale-apcach contrasts-mirrored
                       (partial c/linear-map 0 (dec n) 0.0180 0.0398)  ;; chroma
                       (partial c/linear-map 0 (dec n) 360 350) ;; hue
                       )))

(def cs-range (range 0 (inc 31)))

(defn make-scale
  [scale-apcach prefix]
  (let [scale-suffix #(str "-" %)
        n-min 0
        n-max (inc (count scale-apcach))]
    (into {}
          (concat [[(keyword (str n-min))
                    [(c/css-var prefix (scale-suffix n-min)) "#ffffff"]]]
                  (map (fn [i v]
                         (let [suffix (scale-suffix i)]
                           [(keyword (subs suffix 1))
                            [(c/css-var prefix suffix)
                             (.apcachToCss ap v "hex")]]))
                       (range (inc n-min) n-max)
                       scale-apcach)
                  [[(keyword (str n-max))
                    [(c/css-var prefix (scale-suffix n-max)) "#000000"]]]))))

(def scales
  {:sand (make-scale scale-sand-apcach "sand")
   :night (make-scale scale-night-apcach "night")
   :coral (make-scale scale-coral-apcach "coral")
   :seafoam (make-scale scale-seafoam-apcach "seafoam")
   :lavender (make-scale scale-lavender-apcach "lavender")
   :mauve (make-scale scale-mauve-apcach "mauve")})

;; Light- and darkmode scales
(defn derive-scale
  ([from-scale prefix] (derive-scale from-scale prefix false))
  ([from-scale prefix reverse?]
   (map
    (fn [[scale-k [css-prop css-var]]]
      (let [n (let [n (parse-long (name scale-k))]
                (if reverse?
                  (- (last cs-range) n)
                  n))
            k (keyword (str prefix n))]
        [k [(c/css-var (str "col-" prefix) (str n))
            ;; css-var ;; <- if custom properties in light-dark() not supported
            (c/css-eval-var css-prop)
            ]]))
    from-scale)))

(def light-mode-scales
  (into {}
        (concat
         (derive-scale (:sand scales) "n")
         (derive-scale (:night scales) "m")
         (derive-scale (:coral scales) "e")
         (derive-scale (:seafoam scales) "fx")
         (derive-scale (:lavender scales) "fv")
         (derive-scale (:mauve scales) "fe"))))

(def dark-mode-scales
  (into {}
        (concat
         (derive-scale (:night scales) "n" true)
         (derive-scale (:sand scales) "m" true)
         (derive-scale (:coral scales) "e" true)
         (derive-scale (:seafoam scales) "fx" true)
         (derive-scale (:lavender scales) "fv" true)
         (derive-scale (:mauve scales) "fe" true))))

(def light-mode-additions
  {:ring ["--col-ring" "#B8C4FF"]})

(def dark-mode-additions
  {:ring ["--col-ring" "#A19E9C"]})

(def light-mode-colors
  (merge light-mode-scales
         light-mode-additions))

(def dark-mode-colors
  (merge dark-mode-scales
         dark-mode-additions))

(def semantic-colors
  (let [from-scale (fn [scale i]
                     (-> scale name (str i)
                         keyword light-mode-scales first c/css-eval-var))]
    (into {}
          (map
           (fn [[k [v1 v2] css-prop]] ;; [scale i]
             (let [vals (if (integer? v2)
                          {:inner
                           (from-scale v1 v2)
                           :outer
                           (from-scale v1 (min (last cs-range) (+ v2 2)))}
                          {:inner v1 :outer v2})]
               [k [(c/css-var "col-" (name k))
                   vals
                   (or css-prop nil)]]))

           ;; Syntax explanation:
           ;; [ <name> <scale> <scale-index> <css-prop-map> ]
           ;; css-prop-map: { "css-prop-name" <color-alias> }
           ;; color-alias: "foo" | nil (→ same as <name>)
           (let [CSS-BG :background-color
                 CSS-FG :color
                 CSS-BORDER :border-color]
             [[:bg [:n 1] CSS-BG]
              [:fg [:n 28] CSS-FG]
              [:bg-primary [:m 21] CSS-BG]
              [:fg-primary [:m 0] CSS-FG]
              [:bg-secondary [:n 5] CSS-BG]
              [:fg-secondary [:n 27] CSS-FG]
              [:bg-destructive [:e 23] CSS-BG]
              [:fg-destructive [:e 0] CSS-FG]
              [:bg-accent [:n 5] CSS-BG]
              [:fg-accent [:n 28] CSS-FG]
              [:bg-muted [:n 8] CSS-BG]
              [:fg-muted [:n 14] CSS-FG]
              [:bg-popover [:n 1] CSS-BG]
              [:fg-popover [:n 28] CSS-FG]
              [:bg-card [:n 1] CSS-BG]
              [:fg-card [:n 28] CSS-FG]
              [:border-col-input [:n 11] CSS-BORDER]
              [:border-col [:n 11] CSS-BORDER]
              [:tab-expr [:fx 4]]
              [:tab-eval [:fv 4]]
              [:tab-emul [:fe 4]]
              [:tab-expr-hover [:fx 6]]
              [:tab-eval-hover [:fv 6]]
              [:tab-emul-hover [:fe 6]]
              [:icon-expr [:fx 26]]
              [:icon-eval [:fv 26]]
              [:icon-emul [:fe 26]]
              [:fmenu-expr [:fx 5]]
              [:fmenu-eval [:fv 5]]
              [:fmenu-emul [:fe 5]]
              [:fmenu-expr-shadow [:fx 7]]
              [:fmenu-eval-shadow [:fv 7]]
              [:fmenu-emul-shadow [:fe 7]]
              [:fmenu-expr-shadow-hover [:fx 9]]
              [:fmenu-eval-shadow-hover [:fv 9]]
              [:fmenu-emul-shadow-hover [:fe 9]]])))))


;; Output

(def output
  (merge scales
         {:light light-mode-colors
          :dark dark-mode-colors}
         {:semantic semantic-colors}))

(def light-dark-colors
  (merge-with (fn [[k1 v1] [k2 v2]]
                (assert (= k1 k2))
                [k1 {:light v1 :dark v2}])
              light-mode-colors
              dark-mode-colors))

(def css-output
  (let [opts {:syntax "<color>" :inherits true}
        cs-props (str/join "\n" (map (fn [[_ cs]]
                                       (c/scale->css-properties cs opts))
                                     scales))
        cs-light-dark (c/scale->css-vars light-dark-colors 1)
        ;; cs-light1 (c/scale->css-vars light-mode-colors 1)
        ;; cs-light-props (str/join "\n" (map (fn [[k cs]]
        ;;                                      (scale->css-properties cs opts))
        ;;                                    light-mode-colors))
        ;; cs-dark1 (c/scale->css-vars dark-mode-colors 1)
        ;; cs-dark2 (c/scale->css-vars dark-mode-colors 2)
        semantic1-inner (c/scale->css-vars semantic-colors 1 :inner)
        semantic1-outer (c/scale->css-vars semantic-colors 1 :outer)
        ;; semantic-props (scale->css-properties semantic-colors opts)
        ]
    (str
     ;; All custom properties
     cs-props "\n\n"
     ;; cs-light-props "\n\n"
     ;; semantic-props "\n\n"

     ;; User-controlled dark mode overrides
     ;; "@media (prefers-color-scheme: dark) {\n"
     ;; c/css-indent ":root {\n"
     ;; cs-dark2 "\n"
     ;; "\n" c/css-indent "}\n"
     ;; "}\n\n"

     ;; App-controlled light mode overrides
     ":root {\n"
     ;; cs-light1 "\n"
     cs-light-dark "\n"
     semantic1-inner "\n"
     "}\n\n"

     ;; App-controlled dark mode overrides
     ;; ":root[data-theme=\"dark\"] {\n"
     ;; ;; cs-dark1 "\n"
     ;; "}\n\n"

     ;; Semantic colors shift with layers
     ".outer {\n"
     semantic1-outer "\n"
     "}\n\n"
     ".inner {\n"
     semantic1-inner "\n"
     "}\n\n")))
