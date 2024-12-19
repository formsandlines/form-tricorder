(ns css.colors
  (:require
   ["apcach" :as ap]
   [css.common :as c]
   [clojure.string :as str]))


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
                       (partial c/linear-map 0 (dec n) 0.18  0.176)  ;; chroma
                       (partial c/linear-map 0 (dec n) 13.15 13.15) ;; hue
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

(defn make-scale
  [scale-apcach prefix]
  (let [key-comparator (c/make-key-comparator #(parse-long (name %)))
        scale-suffix #(str "-" %)
        n-min 0
        n-max (inc (count scale-apcach))]
    (into (sorted-map-by key-comparator)
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
  [from-scale prefix]
  (map-indexed
   (fn [i [css-prop _]]
     (let [k (keyword (str prefix i))]
       [k [(c/css-var (str "col-" prefix) (str i))
           (c/css-eval-var css-prop)]]))
   (vals from-scale)))


(def key-comparator
  (fn [a b]
    (let [[s1 s2] (map #(re-find #"[a-z]+" (name %)) [a b])
          [n1 n2] (map #(parse-long (re-find #"\d+" (name %))) [a b])
          result1 (compare s1 s2)]
      (case result1
        0 (compare n1 n2)
        result1))))

(def light-mode-scales
  (into (sorted-map-by key-comparator)
        (concat
         (derive-scale (:sand scales) "n")
         (derive-scale (:night scales) "m")
         (derive-scale (:coral scales) "e")
         (derive-scale (:seafoam scales) "fx")
         (derive-scale (:lavender scales) "fv")
         (derive-scale (:mauve scales) "fe"))))

(def dark-mode-scales
  (into (sorted-map-by key-comparator)
        (concat
         (derive-scale (reverse (:night scales)) "n")
         (derive-scale (reverse (:sand scales)) "m")
         (derive-scale (reverse (:coral scales)) "e")
         (derive-scale (reverse (:seafoam scales)) "fx")
         (derive-scale (reverse (:lavender scales)) "fv")
         (derive-scale (reverse (:mauve scales)) "fe"))))


(def semantic-colors
  (let [cs-range (range 0 (inc 31))]
    (into (sorted-map)
          (map
           (fn [[k scale i css-prop]]
             [k [(c/css-var "col-" (name k))
                 {:inner
                  (-> scale (str i)
                      keyword light-mode-scales first c/css-eval-var)
                  :outer
                  (-> scale (str (min (last cs-range) (+ i 2)))
                      keyword light-mode-scales first c/css-eval-var)}
                 (or css-prop nil)]])

           ;; Syntax explanation:
           ;; [ <name> <scale> <scale-index> <css-prop-map> ]
           ;; css-prop-map: { "css-prop-name" <color-alias> }
           ;; color-alias: "foo" | nil (→ same as <name>)
           (let [CSS-BG :background-color
                 CSS-FG :color]
             [[:bg :n 1 CSS-BG]
              [:fg :n 28 CSS-FG]
              [:bg-primary :m 21 CSS-BG]
              [:fg-primary :m 0 CSS-FG]
              [:bg-secondary :n 5 CSS-BG]
              [:fg-secondary :n 27 CSS-FG]
              [:bg-destructive :e 23 CSS-BG]
              [:fg-destructive :e 0 CSS-FG]
              [:bg-accent :n 5 CSS-BG]
              [:fg-accent :n 28 CSS-FG]
              [:bg-input :n 11 CSS-BG]
              [:fg-input :n 27 CSS-FG]
              [:bg-muted :n 8 CSS-BG]
              [:fg-muted :n 14 CSS-FG]
              [:bg-popover :n 1 CSS-BG]
              [:fg-popover :n 28 CSS-FG]
              [:bg-card :n 1 CSS-BG]
              [:fg-card :n 28 CSS-FG]
              [:border-col :n 11 :border-color]
              ;; [:border-fg :n 20 {CSS-FG nil}]
              [:tab-expr :fx 4]
              [:tab-eval :fv 4]
              [:tab-emul :fe 4]
              [:tab-expr-hover :fx 6]
              [:tab-eval-hover :fv 6]
              [:tab-emul-hover :fe 6]
              [:icon-expr :fx 26]
              [:icon-eval :fv 26]
              [:icon-emul :fe 26]
              [:fmenu-expr :fx 5]
              [:fmenu-eval :fv 5]
              [:fmenu-emul :fe 5]
              [:fmenu-expr-shadow :fx 7]
              [:fmenu-eval-shadow :fv 7]
              [:fmenu-emul-shadow :fe 7]
              [:fmenu-expr-shadow-hover :fx 9]
              [:fmenu-eval-shadow-hover :fv 9]
              [:fmenu-emul-shadow-hover :fe 9]])))))


;; Output

(def output
  (merge scales
         {:light light-mode-scales
          :dark dark-mode-scales}
         {:semantic semantic-colors}))


(def css-output
  (let [opts {:syntax "<color>" :inherits true}
        cs-props (str/join "\n" (map (fn [[_ cs]]
                                       (c/scale->css-properties cs opts))
                                     scales))
        cs-light1 (c/scale->css-vars light-mode-scales 1)
        ;; cs-light-props (str/join "\n" (map (fn [[k cs]]
        ;;                                      (scale->css-properties cs opts))
        ;;                                    light-mode-scales))
        cs-dark1 (c/scale->css-vars dark-mode-scales 1)
        cs-dark2 (c/scale->css-vars dark-mode-scales 2)
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
     "@media (prefers-color-scheme: dark) {\n"
     c/css-indent ":root {\n"
     cs-dark2 "\n"
     "\n" c/css-indent "}\n"
     "}\n\n"

     ;; App-controlled light mode overrides
     ":root {\n"
     cs-light1 "\n"
     semantic1-inner "\n"
     "}\n\n"

     ;; App-controlled dark mode overrides
     ":root[data-theme=\"dark\"] {\n"
     cs-dark1 "\n"
     "}\n\n"

     ;; Semantic colors shift with layers
     ".outer {\n"
     semantic1-outer "\n"
     "}\n\n"
     ".inner {\n"
     semantic1-inner "\n"
     "}\n\n")))





;; Semantic colors

;; fg-outer
;; bg-outer
;; fg-outer-primary
;; bg-outer-primary

;; ? awkward → should refactor this sometime
(comment
  (def semantic-colors
    (let [CSS-BG "background-color"
          CSS-FG "color"
          cs-range (range 0 (inc 31))
          schema [ ;; Syntax explanation:
                  ;; [ <name> <scale> <scale-index> <css-prop-map> ]
                  ;; css-prop-map: { "css-prop-name" <color-alias> }
                  ;; color-alias: "foo" | nil (→ same as <name>)
                  [:bg :n 1 {CSS-BG ""}]
                  [:fg :n 28 {CSS-FG ""}]
                  [:primary :m 21 {CSS-BG nil}]
                  [:primary-fg :m 0 {CSS-FG nil}]
                  [:secondary :n 5 {CSS-BG nil}]
                  [:secondary-fg :n 27 {CSS-FG nil}]
                  [:destructive :e 23 {CSS-BG nil}]
                  [:destructive-fg :e 0 {CSS-FG nil}]
                  [:accent :n 5 {CSS-BG nil}]
                  [:accent-fg :n 28 {CSS-FG nil}]
                  [:input :n 11 {CSS-BG nil}]
                  [:input-fg :n 27 {CSS-FG nil}]
                  [:muted :n 8 {CSS-BG nil}]
                  [:muted-fg :n 14 {CSS-FG nil}]
                  [:popover :n 1 {CSS-BG nil}]
                  [:popover-fg :n 28 {CSS-FG nil}]
                  [:card :n 1 {CSS-BG nil}]
                  [:card-fg :n 28 {CSS-FG nil}]
                  [:border :n 11 {CSS-BG nil "border-color" ""}]
                  [:border-fg :n 20 {CSS-FG nil}]
                  [:tab-expr :fx 4]
                  [:tab-eval :fv 4]
                  [:tab-emul :fe 4]
                  [:tab-expr-hover :fx 6]
                  [:tab-eval-hover :fv 6]
                  [:tab-emul-hover :fe 6]
                  [:icon-expr :fx 26]
                  [:icon-eval :fv 26]
                  [:icon-emul :fe 26]
                  [:fmenu-expr :fx 5]
                  [:fmenu-eval :fv 5]
                  [:fmenu-emul :fe 5]
                  [:fmenu-expr-shadow :fx 7]
                  [:fmenu-eval-shadow :fv 7]
                  [:fmenu-emul-shadow :fe 7]
                  [:fmenu-expr-shadow-hover :fx 9]
                  [:fmenu-eval-shadow-hover :fv 9]
                  [:fmenu-emul-shadow-hover :fe 9]]
          make-color-kvs (fn [prefix f]
                           (map (fn [[k scale i css-prop-map]]
                                  [(keyword (str prefix (name k)))
                                   [(c/css-var (str "col-" prefix) (name k))
                                    (str "var("
                                         (c/css-var (str "col-" (name scale))
                                                    (f i)) ")")
                                    (or css-prop-map {})]])
                                schema))
          outer-offset (comp (partial min (last cs-range))
                             (partial + 2))
          semantic-inner (make-color-kvs "inner-" identity)
          semantic-outer (make-color-kvs "outer-" outer-offset)]
      (into (into (sorted-map) semantic-inner) semantic-outer))))
