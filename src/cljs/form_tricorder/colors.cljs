(ns form-tricorder.colors
  (:require
   ["apcach" :as ap]
   [form-tricorder.utils :refer [linear-map]]))

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
                       (partial linear-map 0 (dec n) 0.004 0.005) ;; chroma
                       (partial linear-map 0 (dec n) 36.37 31.06) ;; hue
                       )))

(def scale-night-apcach
  (let [n (count crs)]
    (make-scale-apcach contrasts-mirrored
                       (partial linear-map 0 (dec n) 0.005  0.020)  ;; chroma
                       (partial linear-map 0 (dec n) 274.97 274.97) ;; hue
                       )))

(def scale-coral-apcach
  (let [n (count crs)]
    (make-scale-apcach contrasts-mirrored
                       (partial linear-map 0 (dec n) 0.18  0.176)  ;; chroma
                       (partial linear-map 0 (dec n) 13.15 13.15) ;; hue
                       )))

(defn make-scale
  [scale-apcach]
  (vec (concat ["#ffffff"]
               (map #(.apcachToCss ap % "hex") scale-apcach)
               ["#000000"])))

(def scale-sand  (make-scale scale-sand-apcach))
(def scale-night (make-scale scale-night-apcach))
(def scale-coral (make-scale scale-coral-apcach))

(def scale-range [1 30])
(def scale-range-ext [0 31])

(defn scale->kvs
  [scale prefix]
  (zipmap (map (fn [i] (keyword (str prefix i))) (range))
          scale))

(def semantic-colors
  (let [schema [[:bg :n 1]
                [:fg :n 28]

                [:primary :m 21]
                [:primary-fg :m 0]
                [:secondary :n 5]
                [:secondary-fg :n 27]
                [:destructive :e 23]
                [:destructive-fg :e 0]
                [:accent :n 5]
                [:accent-fg :n 28]
                [:muted :n 8]
                [:muted-fg :n 14]
                [:popover :n 1]
                [:popover-fg :n 28]
                [:card :n 1]
                [:card-fg :n 28]
                [:border :n 11]
                [:border :n 11]
                ]
        make-color-kvs (fn [prefix f]
                         (map (fn [[k scale i]]
                                [(keyword (str prefix "-" (name k)))
                                 (str "var(--colors-" (name scale) (f i) ")")])
                              schema))
        outer-offset (comp (partial min (last scale-range-ext))
                           (partial + 2))
        semantic-inner (make-color-kvs "inner" identity)
        semantic-outer (make-color-kvs "outer" outer-offset)]
    (into (into {} semantic-inner) semantic-outer)))

(def colors-light
  (let [n-scale (scale->kvs scale-sand "n")
        m-scale (scale->kvs scale-night "m")
        signal-scale (scale->kvs scale-coral "e")]
    (merge n-scale m-scale signal-scale semantic-colors
           {:outer-bg "var(--colors-n4)"
            :inner-visu "#D8EBE2"
            :inner-calc "#E8E8FF"
            :inner-emul "#F5E1E8"
            :fmenu-visu "#C4D1CC"
            :fmenu-calc "#C8CAE0"
            :fmenu-emul "#DDC2CD"
            :fmenu-base "var(--colors-n8)"
            :fmenu-glow "var(--colors-n5)"
            :ring       "#B8C4FF"})))

(def colors-dark
  (let [n-scale (scale->kvs (reverse scale-night) "n")
        m-scale (scale->kvs (reverse scale-sand) "m")
        signal-scale (scale->kvs (reverse scale-coral) "e")]
    (merge n-scale m-scale signal-scale semantic-colors
           {:inner-visu "#3B524F"
            :inner-calc "#3C3E5E"
            :inner-emul "#593447"
            :fmenu-visu "#516F6C"
            :fmenu-calc "#51537E"
            :fmenu-emul "#654E6A"
            :fmenu-base "var(--colors-n8)"
            :fmenu-glow "var(--colors-n11)"
            :ring       "#A19E9C"})))

;; (def scale-sand-outer (concat (drop 2 scale-sand)
;;                               (take 2 (repeat (last scale-sand)))))

;; (def scale-night-outer (concat (drop 1 scale-sand)
;;                                (take 1 (repeat (last scale-sand)))))


(comment
  ;; Scale values in hex
  
  scale-sand
  ;; => ["#ffffff"
  ;;     "#fffbfa"
  ;;     "#f9f5f4"
  ;;     "#f3efee"
  ;;     "#ede9e8"
  ;;     "#e7e3e2"
  ;;     "#e1dddc"
  ;;     "#dbd7d6"
  ;;     "#d5d1d0"
  ;;     "#cecac9"
  ;;     "#c8c4c3"
  ;;     "#c2bebd"
  ;;     "#bbb7b6"
  ;;     "#b5b1b0"
  ;;     "#aeaaa9"
  ;;     "#a7a3a2"
  ;;     "#a09c9b"
  ;;     "#999594"
  ;;     "#938e8e"
  ;;     "#8c8787"
  ;;     "#84807f"
  ;;     "#7d7978"
  ;;     "#757170"
  ;;     "#6d6968"
  ;;     "#656060"
  ;;     "#5c5857"
  ;;     "#534e4e"
  ;;     "#484444"
  ;;     "#3e3939"
  ;;     "#302c2c"
  ;;     "#1a1616"
  ;;     "#000000"]

  scale-night
  ;; => ["#ffffff"
  ;;     "#fbfcff"
  ;;     "#f5f6fa"
  ;;     "#eff0f5"
  ;;     "#e8eaf0"
  ;;     "#e2e4eb"
  ;;     "#dcdee5"
  ;;     "#d5d7e0"
  ;;     "#cfd1da"
  ;;     "#c9cbd5"
  ;;     "#c2c5cf"
  ;;     "#bbbec9"
  ;;     "#b5b8c3"
  ;;     "#aeb1be"
  ;;     "#a7aab7"
  ;;     "#a0a4b1"
  ;;     "#999cab"
  ;;     "#9296a4"
  ;;     "#8b8f9e"
  ;;     "#848898"
  ;;     "#7d8191"
  ;;     "#75798a"
  ;;     "#6d7183"
  ;;     "#65697b"
  ;;     "#5c6173"
  ;;     "#54586a"
  ;;     "#4a4e61"
  ;;     "#404457"
  ;;     "#35394d"
  ;;     "#282c3f"
  ;;     "#121527"
  ;;     "#000000"]
  ,)  

(comment
  ;; Mapping to old scales (same for all scales)

  ;;  0 "#ffffff" ;; 
  
  ;;  1 "#fffbfa" ;; x = 0
  ;;  2 "#f9f5f4" ;;   
  ;;  3 "#f3efee" ;;   = 50
  ;;  4 "#ede9e8" ;;  
  ;;  5 "#e7e3e2" ;; x = 100
       
  ;;  6 "#e1dddc" ;;   
  ;;  7 "#dbd7d6" ;;  
  ;;  8 "#d5d1d0" ;; x = 200
  ;;  9 "#cecac9" ;;   
  ;; 10 "#c8c4c3" ;;  
  ;; 11 "#c2bebd" ;; x = 300
  ;; 12 "#bbb7b6" ;;   
       
  ;; 13 "#b5b1b0" ;;  
  ;; 14 "#aeaaa9" ;; x = 400
  ;; 15 "#a7a3a2" ;; 
       
  ;; 16 "#a09c9b" ;;   = 500
  ;; 17 "#999594" ;;  
  ;; 18 "#938e8e" ;; 
       
  ;; 19 "#8c8787" ;;   = 600
  ;; 20 "#84807f" ;;  
  ;; 21 "#7d7978" ;; x = 700
  ;; 22 "#757170" ;;  
  ;; 23 "#6d6968" ;;   = 800
  ;; 24 "#656060" ;;  
  ;; 25 "#5c5857" ;;   = 900
       
  ;; 26 "#534e4e" ;;  
  ;; 27 "#484444" ;; x = 1000
  ;; 28 "#3e3939" ;; x 
  ;; 29 "#302c2c" ;;  
  ;; 30 "#1a1616" ;;   = 1100/1150/1200
       
  ;; 31 "#000000" ;;
)



(comment
  ;; Calculations/experiments to map old scale values onto new ones

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


  (let [old-scale (mapv #(.cssToApcach ap % #js {:fg "#000000"}) night-old)
        new-scale scale-night-apcach
        get-contrast (fn [col-old col-new]
                       (Math/abs (- (.. col-old -lightness)
                                    (.. col-new -lightness))))]
    (for [i (range (count old-scale))
          :let [col-old (old-scale i)
                min-contrast (reduce
                              (fn [[min-pos min-cr] [j col]]
                                (let [cr (get-contrast col-old col)]
                                  (if (< cr min-cr)
                                    [(inc j) cr]
                                    [min-pos min-cr])))
                              [-1 10000]
                              (map vector (range) new-scale)
                              ;; (map vector (range 29 -1 -1) (reverse new-scale))
                              ,)]]
      [i min-contrast]))

  ;; legend: [index-old [index-new contrast-ratio]]

  ;; Night lightness old vs new
  ;;      0  [0 [1 0.00177001953125]]
  ;;     50  [1 [3 0.00213623046875]]
  ;;    100  [2 [5 0.001708984375]]
  ;;    200  [3 [8 0.00341796875]]
  ;;    300  [4 [11 0.008544921875]]
  ;;    400  [5 [14 0.0089111328125]]
  ;;    500  [6 [16 0.001841796875000079]]
  ;;    600  [7 [19 0.002899902343750038]]
  ;;    700  [8 [21 0.0022524414062499165]]
  ;;    800  [9 [23 0.0034882812499998916]]
  ;;    900  [10 [25 0.002193847656250081]]
  ;;   1000  [11 [27 0.014431640624999964]]
  ;;   1100  [12 [30 0.20385791015625002]]
  ;;   1150  [13 [30 0.20385791015625002]]
  ;;   1200  [14 [30 0.20385791015625002]]

  ;; Sand lightness old vs new
  ;;      0  [0 [1 4.8828125E-4]]
  ;;     50  [1 [3 9.765625E-4]]
  ;;    100  [2 [5 0.001220703125]]
  ;;    200  [3 [8 0.00390625]]
  ;;    300  [4 [11 0.00732421875]]
  ;;    400  [5 [14 0.0107421875]]
  ;;    500  [6 [16 0.001353515625000079]]
  ;;    600  [7 [19 0.002899902343750038]]
  ;;    700  [8 [21 0.0027343749999999556]]
  ;;    800  [9 [23 0.005916992187499748]]
  ;;    900  [10 [25 0.0012109374999999534]]
  ;;   1000  [11 [27 0.013943359374999964]]
  ;;   1100  [12 [30 0.20409887695312504]]
  ;;   1150  [13 [30 0.20409887695312504]]
  ;;   1200  [14 [30 0.20409887695312504]]

  ;; ---

  ;; Misguided approach to compare contrast ratios:
  ;; -> too coarse, sometimes 4 values with same contrast ratio
  ;; -> use lightness, better aligned with perception

  ;; Sand vs col-old (bg, reversed)
  ;; => ([0 [3 0]]
  ;;     [1 [5 0]]
  ;;     [2 [7 0]]
  ;;     [3 [10 0]]
  ;;     [4 [13 0]]
  ;;     [5 [16 0]]
  ;;     [6 [18 0]]
  ;;     [7 [21 0]]
  ;;     [8 [23 0]]
  ;;     [9 [25 0]]
  ;;     [10 [27 0]]
  ;;     [11 [28 0]]
  ;;     [12 [30 0]]
  ;;     [13 [30 0]]
  ;;     [14 [30 0]])

  ;; Sand vs col-old (bg)
  ;; => ([0 [1 0]]       0
  ;;     [1 [1 0]]      50
  ;;     [2 [3 0]]     100
  ;;     [3 [6 0]]     200
  ;;     [4 [9 0]]     300
  ;;     [5 [12 0]]    400
  ;;     [6 [14 0]]    500
  ;;     [7 [17 0]]    600
  ;;     [8 [19 0]]    700
  ;;     [9 [21 0]]    800
  ;;     [10 [23 0]]   900
  ;;     [11 [25 0]]  1000
  ;;     [12 [27 0]]  1100
  ;;     [13 [28 0]]  1150
  ;;     [14 [28 0]]) 1200

  
  ;; Sand vs col-new (bg) (== Night vs col-new (bg))
  ;; => ([0 [1 0]]
  ;;     [1 [1 0]]
  ;;     [2 [3 0]]
  ;;     [3 [6 0]]
  ;;     [4 [8 0]] <9
  ;;     [5 [11 0]] <12
  ;;     [6 [14 0]]
  ;;     [7 [17 0]]
  ;;     [8 [19 0]]
  ;;     [9 [21 0]]
  ;;     [10 [23 0]]
  ;;     [11 [25 0]]
  ;;     [12 [27 0]]
  ;;     [13 [28 0]]
  ;;     [14 [28 0]])

  
  ;; Night vs col-old (bg) (== Sand vs col-old (bg))
  ;; => ([0 [1 0]]
  ;;     [1 [1 0]]
  ;;     [2 [3 0]]
  ;;     [3 [6 0]]
  ;;     [4 [9 0]]
  ;;     [5 [12 0]]
  ;;     [6 [14 0]]
  ;;     [7 [17 0]]
  ;;     [8 [19 0]]
  ;;     [9 [21 0]]
  ;;     [10 [23 0]]
  ;;     [11 [25 0]]
  ;;     [12 [27 0]]
  ;;     [13 [28 0]]
  ;;     [14 [28 0]])

  ;; Night vs col-new (bg)
  ;; => ([0 [1 0]]
  ;;     [1 [1 0]]
  ;;     [2 [3 0]]
  ;;     [3 [6 0]]
  ;;     [4 [8 0]] <9
  ;;     [5 [11 0]] <12
  ;;     [6 [14 0]]
  ;;     [7 [17 0]]
  ;;     [8 [19 0]]
  ;;     [9 [21 0]]
  ;;     [10 [23 0]]
  ;;     [11 [25 0]]
  ;;     [12 [27 0]]
  ;;     [13 [28 0]]
  ;;     [14 [28 0]])

  
  ,)

