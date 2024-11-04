(ns form-tricorder.colortest
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   ;; [helix.hooks :as hooks]
   [clojure.math :as math]
   [clojure.edn :as edn]
   [helix.dom :as d :refer [$d]]
   ["apcach" :as ap]
   [form-tricorder.utils :refer [linear-map]]
   [form-tricorder.stitches-config :refer [css]]))

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


(comment
  (def crs
    [105 ;; bg
     100 ;; bg+
     90  ;; ui subtle 1
     85  ;; ui subtle 2
     80  ;; ui subtle 3
     70  ;; borders 1
     65  ;; borders 2
     ])

  (def crs (vec (range 105 (dec 55) -5)))

  (def contrasts ;; mirrored
    (let [f (fn [fg cr] {:cr cr :to-fg fg})]
      (into (mapv (partial f "#000") crs)
            (mapv (partial f "#fcfcfc") (reverse crs)))))

  (def scale-sand-inner
    (let [n (count contrasts)
          chroma-map (partial linear-map 0 (dec n) 0.003 0.006)
          hue-map    (partial linear-map 0 (dec n) 56.37 31.06)]
      (make-scale contrasts chroma-map hue-map)))

  (def scale-night-inner
    (let [n (count contrasts)
          chroma-map (partial linear-map 0 (dec n) 0.005 0.038)
          hue-map    (fn [_] 274.97)]
      (make-scale contrasts chroma-map hue-map)))

  (defn map-inner->outer
    [scale]
    (vec (map-indexed
          (fn [i js-obj] (let [f (fn [cr] (- cr 10))]
                          (set-contrast js-obj f)))
          scale)))

  (def scale-sand-outer (map-inner->outer scale-sand-inner))
  (def scale-night-outer (map-inner->outer scale-night-inner))
  ;;
  )


(defn set-contrast
  "Immutable wrapper for setContrast()"
  [apcach-obj val-or-fn]
  (let [clone (js/structuredClone apcach-obj)]
    (.setContrast ap clone val-or-fn)))

;; (defn contrast-to-fg [fg n lt dir] {:cr n :to-fg fg :dir dir :lt lt})
;; (defn contrast-to-bg [bg n lt dir] {:cr n :to-bg bg :dir dir :lt lt})
(defn contrast-to-fg [fg n dir] {:cr n :to-fg fg :dir dir})
(defn contrast-to-bg [bg n dir] {:cr n :to-bg bg :dir dir})

(comment
  (def crs-light
    [105 ;; bg
     96  ;; bg+
     90  ;; ui subtle 1
     85  ;; ui subtle 2
     80  ;; ui subtle 3
     70  ;; borders 1
     65  ;; borders 2
     60
     ])
  (def crs-dark
    [107 ;; bg
     103 ;; bg+
     93  ;; ui subtle 1
     88  ;; ui subtle 2
     83  ;; ui subtle 3
     73  ;; borders 1
     68  ;; borders 2
     63
     ]))

;; (def crs-light (vec (range 105 (dec 55) -5)))
;; (def crs-light (vec (range 105 (dec 55) -2.5)))
(def crs-light (vec (range 104 (dec 55) -3.5)))
;; (def crs-light (vec (range 104 (dec 51) -6)))
;; (def lts-light (vec (range 0.99 (+ 0.71 0.001) -0.026)))
(def dirs-light (mapv (fn [n] :lighter) crs-light))

;; (def crs-dark  (vec (range 105 (dec 55) -5)))
;; (def crs-dark  (vec (range 105 (dec 55) -2.5)))
(def crs-dark  (vec (range 104 (dec 55) -3.5)))
;; (def crs-dark  (vec (range 104 (dec 51) -6)))
;; (def lts-dark (vec (take 11 (range 0.2 (+ 0.71 0.001) 0.05))))
(def dirs-dark (mapv (fn [n] :darker) crs-dark))
;; (def crs-light (vec (range 8 (dec 55) +6)))
;; (def crs-dark  (vec (range 8 (dec 55) +6)))

;; (def darkest "#000")
;; (def lightest "#FFF")
(def darkest "oklch(0% 0.0 0)")
(def lightest "oklch(99% 0.0 0)")

;; (def contrasts-light-inner (mapv (partial contrast-to-bg lightest) crs-light))
;; (def contrasts-dark-inner (mapv (partial contrast-to-bg darkest) crs-dark))

(def contrasts-light-inner (mapv (partial contrast-to-fg darkest)
                                 crs-light dirs-light))
;; (def contrasts-light-inner (mapv (partial contrast-to-fg darkest)
;;                                  crs-light lts-light dirs-light))
;; (def contrasts-light-outer (mapv (partial contrast-to-fg darkest)
;;                                  (mapv (fn [cr] (- cr 9)) crs-light)))
(def contrasts-dark-inner (mapv (partial contrast-to-fg lightest)
                                crs-dark dirs-dark))
;; (def contrasts-dark-inner (mapv (partial contrast-to-fg lightest)
;;                                 crs-dark lts-dark dirs-dark))
;; (def contrasts-dark-outer (mapv (partial contrast-to-fg lightest)
;;                                 (mapv (fn [cr] (- cr 4)) crs-dark)))

(def contrasts-inner-mirrored
  (into contrasts-light-inner (reverse contrasts-dark-inner)))
;; (def contrasts-outer-mirrored
;;   (into contrasts-light-outer (reverse contrasts-dark-outer)))

(defn make-scale
  [contrasts ->chroma ->hue]
  (let [n (count contrasts)]
    (->> (for [i (range n)
               :let [{:keys [cr to-fg to-bg dir]} (contrasts i)
                     contrast
                     (cond to-fg (.crToFg ap to-fg cr "apca" (name dir))
                           to-bg (.crToBg ap to-bg cr "apca" (name dir))
                           :else (throw (ex-info "Contrast color missing!" {})))
                     chroma (if (number? ->chroma) ->chroma (->chroma i))
                     hue (if (number? ->hue) ->hue (->hue i))]]
           (.apcach ap contrast chroma hue))
         vec)))

;; (defn make-scale
;;   [lightnesses ->chroma ->hue]
;;   (let [n (count lightnesses)]
;;     (->> (for [i (range n)
;;                :let [{:keys [lt to-fg to-bg]} (lightnesses i)
;;                      lightness lt
;;                      chroma (if (number? ->chroma) ->chroma (->chroma i))
;;                      hue (if (number? ->hue) ->hue (->hue i))]]
;;            (.cssToApcach ap (str "oklch(" lt " " chroma " " hue ")")
;;                          #js {:fg to-fg}))
;;          vec)))


(def scales-sand
  (let [n (count crs-light)
        chroma-light 0.001 ; (partial linear-map 0 (dec n) 0.003 0.006)
        chroma-dark  0.004 ; (partial linear-map 0 (dec n) 0.006 0.003)
        hue-light 36.37 ; (partial linear-map 0 (dec n) 56.37 31.06)
        hue-dark  31.06 ; (partial linear-map 0 (dec n) 31.06 56.37)
        scale (make-scale contrasts-inner-mirrored
                          (partial linear-map 0 (dec n) chroma-light chroma-dark)
                          (partial linear-map 0 (dec n) hue-light hue-dark))]
    [scale (vec (reverse scale))]
    ;; [(make-scale contrasts-inner-mirrored
    ;;              (partial linear-map 0 (dec n) chroma-light chroma-dark)
    ;;              (partial linear-map 0 (dec n) hue-light hue-dark))
    ;;  (make-scale contrasts-outer-mirrored
    ;;              (partial linear-map 0 (dec n) chroma-light chroma-dark)
    ;;              (partial linear-map 0 (dec n) hue-light hue-dark))
    ;;  ]
    ;; [(make-scale contrasts-light chroma-light hue-light)
    ;;  (make-scale contrasts-dark chroma-dark hue-dark)]
    ))

(def scales-night
  (let [n (count crs-dark)
        chroma-light 0.005 ; (partial linear-map 0 (dec n) 0.005 0.038)
        chroma-dark  0.020 ; (partial linear-map 0 (dec n) 0.038 0.005)
        hue-light 274.97
        hue-dark  274.97
        scale (make-scale contrasts-inner-mirrored
                          (partial linear-map 0 (dec n) chroma-light chroma-dark)
                          (partial linear-map 0 (dec n) hue-light hue-dark))]
    [scale (vec (reverse scale))]
    ;; [(make-scale contrasts-inner-mirrored
    ;;              (partial linear-map 0 (dec n) chroma-light chroma-dark)
    ;;              (partial linear-map 0 (dec n) hue-light hue-dark))
    ;;  (make-scale contrasts-outer-mirrored
    ;;              (partial linear-map 0 (dec n) chroma-light chroma-dark)
    ;;              (partial linear-map 0 (dec n) hue-light hue-dark))
    ;;  ]
    ;; [(make-scale contrasts-light chroma-light hue-light)
    ;;  (make-scale contrasts-dark chroma-dark hue-dark)]
    ))

(comment
  (scales-sand 0)

  (def scales-sand
    (let [n (count crs)
          chroma-light 0.003        ; (partial linear-map 0 (dec n) 0.003 0.006)
          hue-light 56.37           ; (partial linear-map 0 (dec n) 56.37 31.06)
          ]
      [(make-scale contrasts-light-inner chroma-light hue-light)]))

  (def scales-night
    (let [n (count crs)
          chroma-dark  0.038        ; (partial linear-map 0 (dec n) 0.038 0.005)
          hue-dark  274.97]
      [(make-scale contrasts-dark-inner chroma-dark hue-dark)])))


(comment
  ;; => #js {:alpha 100, :chroma 0.003, :colorSpace "p3", :contrastConfig #js {:bgColor "apcach", :contrastModel "apca", :cr 105, :fgColor "#000", :searchDirection "auto"}, :hue 56.37, :lightness 0.9951171875}
  ;;
  )



(comment
  (map (fn [hex]
         (.toFixed
          (.. (.cssToApcach ap hex #js {:fg "#fff"}) -contrastConfig -cr) 2))
       ["#fffdfb"
        "#f7f5f3"
        "#e6e3e2"
        "#dedbd9"
        "#d5d2d0"
        "#c3c0be"
        "#888483"
        "#726f6d"
        "#676362"
        "#5a5655"
        "#3d3938"
        "#282423"]))

(comment
  (let [xs (range 12)
        n 11]
    (for [i xs]
      (mod (- n i) (/ (count xs) 2))))

  
  (map (partial linear-map 8 17 3 30) (range 8 18))
  (map (partial linear-map 0.0 math/PI 0 360) (range 0.0 (+ 0.1 math/PI) (/ math/PI 4)))
  (map (partial linear-map 0 360 0.0 math/PI) (range 0 (inc 360) 90))

  
  (.apcachToCss ap (.apcach ap 60 0.2 145) "hex")

  (.. (.apcach ap 60 0.004 56.37) -contrastConfig -cr)
  
  (+ 1 2)


  (def radix-grayP3
    ["color(display-p3 0.988 0.988 0.988)"
     "color(display-p3 0.975 0.975 0.975)"
     "color(display-p3 0.939 0.939 0.939)"
     "color(display-p3 0.908 0.908 0.908)"
     "color(display-p3 0.88 0.88 0.88)"
     "color(display-p3 0.849 0.849 0.849)"
     "color(display-p3 0.807 0.807 0.807)"
     "color(display-p3 0.732 0.732 0.732)"
     "color(display-p3 0.553 0.553 0.553)"
     "color(display-p3 0.512 0.512 0.512)"
     "color(display-p3 0.392 0.392 0.392)"
     "color(display-p3 0.125 0.125 0.125)"])
  ;; contrast to black fg
  ;; => (104.20587969006077
  ;;     102.22682853310555
  ;;     96.79389304179288
  ;;     92.17251490138935
  ;;     88.04471865346085
  ;;     83.52711920378661
  ;;     77.49673006614354
  ;;     66.9968984050902
  ;;     43.46011692819211
  ;;     38.40282869336828
  ;;     24.42100092308808
  ;;     0)
  ;; contrast to last scale color as fg
  ;; => (101.45085712600053
  ;;     99.47180596904529
  ;;     94.03887047773264
  ;;     89.41749233732911
  ;;     85.2896960894006
  ;;     80.77209663972639
  ;;     74.74170750208332
  ;;     64.24187584102998
  ;;     40.70509436413188
  ;;     35.64780612930805
  ;;     21.665978359027843
  ;;     0)

  (def radix-mauveP3
    ["color(display-p3 0.991 0.988 0.992)"
     "color(display-p3 0.98 0.976 0.984)"
     "color(display-p3 0.946 0.938 0.952)"
     "color(display-p3 0.915 0.906 0.925)"
     "color(display-p3 0.886 0.876 0.901)"
     "color(display-p3 0.856 0.846 0.875)"
     "color(display-p3 0.814 0.804 0.84)"
     "color(display-p3 0.735 0.728 0.777)"
     "color(display-p3 0.555 0.549 0.596)"
     "color(display-p3 0.514 0.508 0.552)"
     "color(display-p3 0.395 0.388 0.424)"
     "color(display-p3 0.128 0.122 0.147)"])
  ;; contrast to black fg
  ;; => (104.35935165068186
  ;;     102.61501662182147
  ;;     97.08751689361182
  ;;     92.40875913337315
  ;;     88.09086449365118
  ;;     83.76545506783953
  ;;     77.81419659168537
  ;;     67.22429932965052
  ;;     43.622633070559196
  ;;     38.530868963466496
  ;;     24.490489477651646
  ;;     0)
  ;; contrast to last scale color as fg
  ;; => (101.5750359017328
  ;;     99.83070087287238
  ;;     94.30320114466275
  ;;     89.62444338442408
  ;;     85.30654874470213
  ;;     80.98113931889046
  ;;     75.0298808427363
  ;;     64.43998358070144
  ;;     40.83831732161013
  ;;     35.74655321451743
  ;;     21.70617372870258
  ;;     0)

  
  (def radix-grayDarkP3
    ["color(display-p3 0.067 0.067 0.067)"
     "color(display-p3 0.098 0.098 0.098)"
     "color(display-p3 0.135 0.135 0.135)"
     "color(display-p3 0.163 0.163 0.163)"
     "color(display-p3 0.192 0.192 0.192)"
     "color(display-p3 0.228 0.228 0.228)"
     "color(display-p3 0.283 0.283 0.283)"
     "color(display-p3 0.375 0.375 0.375)"
     "color(display-p3 0.431 0.431 0.431)"
     "color(display-p3 0.484 0.484 0.484)"
     "color(display-p3 0.706 0.706 0.706)"
     "color(display-p3 0.933 0.933 0.933)"])
  ;; contrast to white fg
  ;; => (107.3730141251618
  ;;     106.66007586173167
  ;;     105.39193681066045
  ;;     104.1341124445451
  ;;     102.53516189382583
  ;;     99.94248778273206
  ;;     95.38921193978945
  ;;     86.61724519648001
  ;;     80.63172189985639
  ;;     74.54946925261366
  ;;     45.07222190141293
  ;;     8.989498003154553)
  ;; contrast to last scale color as fg
  ;; => (96.19565102954778
  ;;     95.48271276611764
  ;;     94.21457371504641
  ;;     92.95674934893104
  ;;     91.3577987982118
  ;;     88.76512468711802
  ;;     84.21184884417542
  ;;     75.43988210086599
  ;;     69.45435880424237
  ;;     63.37210615699964
  ;;     33.894858805798904
  ;;     0)

  (def radix-mauveDarkP3
    ["color(display-p3 0.07 0.067 0.074)"
     "color(display-p3 0.101 0.098 0.105)"
     "color(display-p3 0.138 0.134 0.144)"
     "color(display-p3 0.167 0.161 0.175)"
     "color(display-p3 0.196 0.189 0.206)"
     "color(display-p3 0.232 0.225 0.245)"
     "color(display-p3 0.286 0.277 0.302)"
     "color(display-p3 0.383 0.373 0.408)"
     "color(display-p3 0.434 0.428 0.467)"
     "color(display-p3 0.487 0.48 0.519)"
     "color(display-p3 0.707 0.7 0.735)"
     "color(display-p3 0.933 0.933 0.94)"])
  ;; contrast to white fg
  ;; => (107.34984856630771
  ;;     106.62391371874777
  ;;     105.36147282764942
  ;;     104.1062828017407
  ;;     102.53248341996128
  ;;     99.91999394369583
  ;;     95.54922920127058
  ;;     86.2837389458678
  ;;     80.44913851563653
  ;;     74.44825883398451
  ;;     45.29652997360208
  ;;     8.894102904670317)
  ;; contrast to last scale color as fg
  ;; => (96.26393123063609
  ;;     95.53799638307613
  ;;     94.2755554919778
  ;;     93.02036546606907
  ;;     91.44656608428966
  ;;     88.83407660802422
  ;;     84.46331186559895
  ;;     75.19782161019617
  ;;     69.36322117996491
  ;;     63.362341498312894
  ;;     34.21061263793045
  ;;     0)
  
  (for [css old-night]
    (.. (.cssToApcach ap css #js {:fg "#000"}) -contrastConfig -cr))

  (for [css old-night]
    (.. (.cssToApcach ap css #js {:fg "#fff"}) -contrastConfig -cr))

  (let [scale old-night]
    (for [css scale]
      (.. (.cssToApcach ap css #js {:fg (last scale)}) -contrastConfig -cr)))

  
  ;; light scale contrast to black fg
  (def light-contrasts-radix
    [104.3 ;;        bg
     102.4 ;; -1.90  bg dark
     96.9  ;; -5.50  ui subtle 1
     92.3  ;; -4.60  ui subtle 2
     88.06 ;; -4.24  ui subtle 3
     83.65 ;; -4.41  borders 1
     77.6  ;; -6.05  borders 2
     67.0  ;; -10.60 borders 3
     48.5  ;; -18.50 ui strong 1 (added)
     43.5  ;; -5.00  ui strong 2
     38.45 ;; -5.05  ui strong 3
     24.46 ;; -13.99 fg light
     0     ;; -24.46 fg
     ])

  ;; dark scale contrast to white fg
  (def dark-contrasts-radix
    [107    ;;       bg
     106.6  ;; -0.40 bg light
     105.37 ;; -1.23 ui subtle 1
     104.11 ;; -1.26 ui subtle 2
     102.53 ;; -1.58 ui subtle 3
     99.92  ;; -2.61 borders 1
     95.4   ;; -4.52 borders 2
     86.4   ;; -9.00 borders 3
     80.5   ;; -5.90 ui strong 1
     74.47  ;; -6.03 ui strong 2
     68.44  ;; -6.03 ui strong 3 (added)
     45.15  ;; -23.3 fg dark
     8.94   ;; -36.2 fg
     ])


  (map (comp #(/ % 2) +) light-contrasts-old dark-contrasts-old)
  ;; average contrast ratios
  ;; => (105.43 *
  ;;     100.995 *
  ;;     95.82
  ;;     87.705 *
  ;;     79.81 *
  ;;     71.045 *
  ;;     63.325
  ;;     54.775 *
  ;;     46.085 *
  ;;     38.035 *
  ;;     28.64
  ;;     19.925
  ;;     7.945
  ;;     3.96
  ;;     0)



























  (def harmony-contrasts-bg ;; mirrored
    [105 ;; 
     100 ;; -5
     90  ;; -10
     77  ;; -13
     65  ;; -12
     54  ;; -11
     ])

  (def harmony-sontrasts-fg
    [;; vs black fg
     [103.57 ;; 50
      99.03  ;; 100
      89.79  ;; 200
      77.81  ;; 300
      66.52  ;; 400
      56.20  ;; 500
      ]
     ;; vs white fg
     [70.52  ;; 600
      82.37  ;; 700
      94.49  ;; 800
      103.16 ;; 900
      107.11 ;; 950
      ]])
  
  (def light-contrasts-old
    [103.86 ;; 
     96.83  ;; -7.03
     89.83  ;; -7.00
     80.22  ;; -9.61
     70.28  ;; -9.94
     60.21  ;; -10.07
     51.45  ;; -8.76
     ;; ---
     41.51  ;; -9.94
     ;; ---
     34.37  ;; -7.14
     27.53  ;; -6.84
     19.63  ;; -7.90
     13.16  ;; -6.47
     0      ;; -13.16
     0      ;; -0.00
     0      ;; -0.00
     ])

  (def dark-contrasts-old ;; reversed
    [107    
     105.16 ;; -1.84
     101.81 ;; -3.35
     95.19  ;; -6.62
     89.34  ;; -5.85
     81.88  ;; -7.46
     75.2   ;; -6.68
     ;; ---
     68.04  ;; -7.16
     ;; ---
     57.8   ;; -10.24
     48.54  ;; -9.26
     37.65  ;; -10.89
     26.69  ;; -10.96
     15.89  ;; -10.80
     7.92   ;; -7.97
     0      ;; -7.92
     ])

  
  (let [cns dark-contrasts-old]
    (map (comp #(.toFixed % 2) -) cns (rest cns)))

     

  (def old-sand
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
  ;; contrast vs black fg
  ;; => (103.856163373454
  ;;     96.82577888296004
  ;;     89.83115669278233
  ;;     80.22071789694158
  ;;     70.28433607980567
  ;;     60.21231498293701
  ;;     51.44826303854907
  ;;     41.50845014294313
  ;;     34.3715095091259
  ;;     27.530968067531226
  ;;     19.62894684902496
  ;;     13.161094219317796
  ;;     0
  ;;     0
  ;;     0)
  ;; contrast vs white fg
  ;; => (0
  ;;     7.924745331517948
  ;;     15.890476340861358
  ;;     26.685428852540234
  ;;     37.6488467212671
  ;;     48.53520366709909
  ;;     57.800498809414776
  ;;     68.04360941304735
  ;;     75.19977371770334
  ;;     81.8787945618902
  ;;     89.33679776474689
  ;;     95.1925290448696
  ;;     101.80901311943688
  ;;     105.16494083075555
  ;;     107.00412921101494)
  ;; contrast vs last scale color
  ;; => (102.68425627166008
  ;;     95.6538717811661
  ;;     88.6592495909884
  ;;     79.04881079514765
  ;;     69.11242897801174
  ;;     59.040407881143075
  ;;     50.27635593675514
  ;;     40.3365430411492
  ;;     33.199602407331966
  ;;     26.359060965737292
  ;;     18.45703974723102
  ;;     11.989187117523855
  ;;     0
  ;;     0
  ;;     0)

  (def old-night
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
  ;; contrast vs black fg
  ;; => (103.69149408884101
  ;;     96.6310516887144
  ;;     89.69617886902542
  ;;     80.05251115622482
  ;;     70.51711513569174
  ;;     59.94860928600394
  ;;     51.6054062746346
  ;;     41.358733000117795
  ;;     34.31389382743495
  ;;     27.394854330237994
  ;;     19.721321869691035
  ;;     13.187905015873921
  ;;     0
  ;;     0
  ;;     0)
  ;; contrast vs white fg
  ;; => (0
  ;;     8.147693574345531
  ;;     16.043315509348336
  ;;     26.87274573702262
  ;;     37.39445480785577
  ;;     48.81692647434473
  ;;     57.636205071785774
  ;;     68.1955308163033
  ;;     75.25679953572205
  ;;     82.00972603555829
  ;;     89.251415505733
  ;;     95.16879323881572
  ;;     101.80011119918042
  ;;     105.0688966181137
  ;;     107.00903213939887)
  ;; contrast vs last scale color
  ;; => (102.52602409938298
  ;;     95.46558169925636
  ;;     88.53070887956738
  ;;     78.8870411667668
  ;;     69.35164514623372
  ;;     58.783139296545926
  ;;     50.4399362851766
  ;;     40.193263010659784
  ;;     33.148423837976935
  ;;     26.229384340779987
  ;;     18.55585188023302
  ;;     12.022435026415906
  ;;     0
  ;;     0
  ;;     0)


  (.apcach ap 91.8 0.008 325)

  (.cssToApcach ap
                "oklch(91.8% 0.008 325)"
                #js {:bg "#000"})
  (.cssToApcach ap
                "oklch(41.21% 0.008 325)"
                #js {:bg "#FFF"})

  (def harmony-redL
    ["oklch(98.83% 0.005 20)" ;; 50
     "oklch(96.68% 0.017 20)" ;; 100
     "oklch(92.19% 0.042 20)" ;; 200
     "oklch(86.13% 0.078 20)" ;; 300
     "oklch(80.08% 0.114 20)" ;; 400
     "oklch(74.22% 0.152 20)" ;; 500
     ])

  (def harmony-redR
    ["oklch(62.7% 0.136 20)" ;; 600
     "oklch(53.52% 0.116 20)" ;; 700
     "oklch(41.99% 0.091 20)" ;; 800
     "oklch(30.66% 0.066 20)" ;; 900
     "oklch(19.34% 0.041 20)" ;; 950
     ])

  (let [scale harmony-redL]
    (for [s scale]
      (.toFixed (.. (.cssToApcach ap s #js {:fg "#000"}) -contrastConfig -cr) 2)))
  ;; => ("103.57" "99.03" "89.79" "77.81" "66.52" "56.20")

  (let [scale harmony-redR]
    (for [s scale]
      (.toFixed (.. (.cssToApcach ap s #js {:fg "#FFF"}) -contrastConfig -cr) 2)))
  ;; => ("70.52" "82.37" "94.49" "103.16" "107.11")
  
  ;;
  )


(comment
  (def scales
    [scale-sand-inner
     scale-sand-outer
     scale-night-inner
     scale-night-outer]))

(def scales
  (vec (concat
        scales-sand
        scales-night)))

(def scale-height 40)

(defnc Colortest
  [{:keys []}]
  (d/div
    {:style {:margin-bottom (+ (* scale-height (count scales))
                               scale-height)}}
    (d/div
      {:style {:position "fixed"
               :width "100%"
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
          (when compare-scale
            (d/div
              {:style {:display "flex"}}
              (for [j (range (count compare-scale))
                    :let [hex (compare-scale j)]]
                (d/div
                  {:style {:flex 1
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
                {:style {:display "flex"
                         :align-items "end"
                         :justify-content "center"
                         :flex 1
                         ;; :width 60
                         :height scale-height
                         :font-family "\"iosevka term\""
                         :font-size "0.7rem"
                         :color (if (not= "apcach" fg-col) fg-col bg-col)
                         :background-color hex}}
                ;; (str j)
                (str (.toFixed cr 1))
                ))))))))

