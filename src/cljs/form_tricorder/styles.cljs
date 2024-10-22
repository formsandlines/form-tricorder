(ns form-tricorder.styles
  (:require
   [clojure.string :as string]
   [clojure.math :as math]))

(defn num-key [n]
  (-> n str (string/replace "." "-") keyword))

(def base-fontsize 16)

(defn pp-scale-row
  [n-key s-px s-rem]
  (str "| " (name n-key)
       (string/join (repeat (- 6 (count (name n-key)) 1) " "))
       "| " s-px
       (string/join (repeat (- 6 (count s-px)) " "))
       "| " s-rem
       (string/join (repeat (- 11 (count s-rem)) " "))
       "|"))

(defn make-scale
  [step from-px?]
  (for [n    (range 0 100.5 0.5)
        :let [[px rem]
              (if from-px?
                (let [px (math/floor (* n step))]
                  [px (js/Number (.toFixed (/ px base-fontsize) 8))])
                (let [rem (* n step)]
                  [(* base-fontsize rem) rem]))
              n-key (num-key n)
              s-px  (str px "px")
              s-rem (str rem "rem")]]
    (do
      ;; (println (pp-scale-row n-key s-px s-rem))
      [n-key [s-px s-rem]])))

(def scale
  (let [kvs (make-scale 3 true)]
    {:space (map (fn [[k [space _]]] [k space]) kvs)
     :sizes (map (fn [[k [_ size]]] [k size]) kvs)}))

;; Palettes:
;; https://huetone.ardov.me/?palette=N4IgdghgtgpiBcIBmAVATgSwMYAIAKEANjAC4lwA0IAFgK4wDOCA2qJLAiAMoRgAmIKlgD2hYWibxmIAMRI%2BSAEZIIg2TABsMAMwwAjGpl8NfAExYkhrABYsABkWVZi0xD4RFhiHoCcWH55UMgAcfMHhAOyGETARPhHBhhpIGhAaPoYArFiZwZkahrbWwdYFQdoR2lWmhnrBepl61iAAugC%2BFGzQcIgAchgA5tQkaiJiEiyyKkpIlkEwdjowc7IWZgqGinxYelhOMhCHfIqBsj7n7liGeeHn0RpxwVdBGq8QEQJBmd-BGqcy1kBeX%2Bph02msUSCem00NMiXanXA3U4XBgECQwmgo1E4kk0jk2lmSAyQWMMB8CysPmMfAMQQgTz8ZTOfz%2B2mumQgnJqQQivjsoSS2nqMSyENMyUKEUySGyhgq1iQtkMplM2h8FUMdmsem88I6XQ4iAAMhAAG4wfgwNDY8Z4qbKZQrGT6fRIRJBNZ6WKbAJ7fZpI7-HyZc7%2Ba51ayKdm8iqpGOyDSldIJmSZbT5BKFcGvSGyUxIGGZOy1Ux6VVwBGGnogACyEFoFttuMmclm6OeU1MfBgMEyhk0WDc-dJekUJmZMkcEFsqbcGpchh8wQixM%2BsmC8QF%2BziGj0otjthlSWhNn21ggpkqJPzwQFutaBqRRpAAGFxERmxMpFN2ypDLMfDUv8sz2DkAEqNobgQcEpgagBvyZB8A7aikqieksmT-Iol4wNYAY6lUnYhIodh2AWSQaGRpgluUehkQ4j4tFQJDCGAjCTLRID0VxNFcdoZFqNYglUMWXFUVxEQiSAd5cT40k8Vxeg8Wo5aCe0QA

(def color-scales
  {;; Sand
   :--Sand-0 "#fdfbfa"    ;;  1 0
   :--Sand-100 "#e6e3e1"  ;;  2 100 
   :--Sand-200 "#d6d2cf"  ;;  3 200 
   :--Sand-300 "#c4c0be"  ;;  4 300 
   :--Sand-400 "#b2adab"  ;;  5 400 
   :--Sand-500 "#a19c9b"  ;;  6 500 
   :--Sand-600 "#8d8887"  ;;  7 600 
   :--Sand-700 "#7e7978"  ;;  8 700 
   :--Sand-800 "#6f6a69"  ;;  9 800 
   :--Sand-900 "#5c5856"  ;; 10 900 
   :--Sand-1000 "#4c4846" ;; 11 1000
   :--Sand-1100 "#373332" ;; 12 1100
   :--Sand-1200 "#181514" ;; 13 1200

   ;; Night
   :--Night-0 "#fafbff"    ;; 13 0
   :--Night-100 "#e0e3ef"  ;; 12 100 
   :--Night-200 "#cfd2df"  ;; 11 200 
   :--Night-300 "#bdc1ce"  ;; 10 300 
   :--Night-400 "#aaadbb"  ;;  9 400 
   :--Night-500 "#999dac"  ;;  8 500 
   :--Night-600 "#858899"  ;;  7 600 
   :--Night-700 "#76798c"  ;;  6 700 
   :--Night-800 "#666a7d"  ;;  5 800 
   :--Night-900 "#55586b"  ;;  4 900 
   :--Night-1000 "#44485b" ;;  3 1000
   :--Night-1100 "#2e3347" ;;  2 1100
   :--Night-1200 "#131328" ;;  1 1200

   ;; Coral
   :--Coral-0 "#fffafa"
   :--Coral-100 "#ffd9db"
   :--Coral-200 "#ffc0c5"
   :--Coral-300 "#ffa3ad"
   :--Coral-400 "#ff8293"
   :--Coral-500 "#f8657d"
   :--Coral-600 "#e04f6a"
   :--Coral-700 "#ce3e5b"
   :--Coral-800 "#ba2e4e"
   :--Coral-900 "#a4133c"
   :--Coral-1000 "#8b002f"
   :--Coral-1100 "#660020"
   :--Coral-1200 "#31000b"})

(def cs color-scales)

(defn make-semantic-color-scales
  [scales prefixes invert?]
  (into {}
        (for [n (range 13)
              s (range (count scales))
              :let [k (keyword (str (prefixes s) (if invert? (- 12 n) n)))
                    i (keyword (str (scales s) "-" n (when (> n 0) "00")))]]
          [k (color-scales i)])))

(def colors-light
  (merge
   (make-semantic-color-scales ["--Sand" "--Night" "--Coral"]
                               ["n" "m" "e"] false)
   {:outer-primary        (cs :--Night-800)
    :outer-primary-fg     "#FFFFFF"
    :outer-secondary      (cs :--Sand-200)
    :outer-secondary-fg   (cs :--Sand-1100)
    :outer-destructive    (cs :--Coral-900)
    :outer-destructive-fg "#FFFFFF"
    :outer-accent         (cs :--Sand-200)
    :outer-accent-fg      (cs :--Sand-1200)
    :outer-muted          (cs :--Sand-300)
    :outer-muted-fg       (cs :--Sand-500)
    :outer-popover        (cs :--Sand-100)
    :outer-popover-fg     (cs :--Sand-1200)
    :outer-card           (cs :--Sand-100)
    :outer-card-fg        (cs :--Sand-1200)
    :outer-bg             (cs :--Sand-100)
    :outer-fg             (cs :--Sand-1200)
    :outer-border         (cs :--Sand-400)
    :outer-input          (cs :--Sand-400)
    ;; :outer-ring           "#AAB8FF"

    :inner-primary        (cs :--Night-700)
    :inner-primary-fg     "#FFFFFF"
    :inner-secondary      (cs :--Sand-100)
    :inner-secondary-fg   (cs :--Sand-1000)
    :inner-destructive    (cs :--Coral-800)
    :inner-destructive-fg "#FFFFFF"
    :inner-accent         (cs :--Sand-100)
    :inner-accent-fg      (cs :--Sand-1100)
    :inner-muted          (cs :--Sand-200)
    :inner-muted-fg       (cs :--Sand-400)
    :inner-popover        (cs :--Sand-0)
    :inner-popover-fg     (cs :--Sand-1100)
    :inner-card           (cs :--Sand-0)
    :inner-card-fg        (cs :--Sand-1100)
    :inner-bg             (cs :--Sand-0)
    :inner-fg             (cs :--Sand-1100)
    :inner-border         (cs :--Sand-300)
    :inner-input          (cs :--Sand-300)
    ;; :inner-ring           "#B8C4FF"
    :inner-visu "#D8EBE2"
    :inner-calc "#E8E8FF"
    :inner-emul "#F5E1E8"

    :ring       "#B8C4FF"

    :fmenu-visu "#C4D1CC"
    :fmenu-calc "#C8CAE0"
    :fmenu-emul "#DDC2CD"
    :fmenu-base (cs :--Sand-200) ;; "#D9D7D4"
    :fmenu-glow (cs :--Sand-100) ;; "#EDEBE8"
    }))

(def colors-dark
  (merge
   (make-semantic-color-scales ["--Night" "--Sand" "--Coral"]
                               ["n" "m" "e"] true)
   {:outer-primary        (cs :--Sand-200)
    :outer-primary-fg     "#000000"
    :outer-secondary      (cs :--Night-1000)
    :outer-secondary-fg   (cs :--Night-100)
    :outer-destructive    (cs :--Coral-800)
    :outer-destructive-fg "#FFFFFF"
    :outer-accent         (cs :--Night-900)
    :outer-accent-fg      (cs :--Night-100)
    :outer-muted          (cs :--Night-900)
    :outer-muted-fg       (cs :--Night-700)
    :outer-popover        (cs :--Night-1100)
    :outer-popover-fg     (cs :--Night-0)
    :outer-card           (cs :--Night-1100)
    :outer-card-fg        (cs :--Night-0)
    :outer-bg             (cs :--Night-1100)
    :outer-fg             (cs :--Night-0)
    :outer-border         (cs :--Night-800)
    :outer-input          (cs :--Night-800)
    ;; :outer-ring           "#A19E9C"

    :inner-primary        (cs :--Sand-300)
    :inner-primary-fg     "#000000"
    :inner-secondary      (cs :--Night-1100)
    :inner-secondary-fg   (cs :--Night-200)
    :inner-destructive    (cs :--Coral-900)
    :inner-destructive-fg "#FFFFFF"
    :inner-accent         (cs :--Night-1000)
    :inner-accent-fg      (cs :--Night-200)
    :inner-muted          (cs :--Night-1000)
    :inner-muted-fg       (cs :--Night-800)
    :inner-popover        (cs :--Night-1200)
    :inner-popover-fg     (cs :--Night-100)
    :inner-card           (cs :--Night-1200)
    :inner-card-fg        (cs :--Night-100)
    :inner-bg             (cs :--Night-1200)
    :inner-fg             (cs :--Night-100)
    :inner-border         (cs :--Night-900)
    :inner-input          (cs :--Night-900)
    ;; :inner-ring           "#8F8D8B"
    :inner-visu "#3B524F"
    :inner-calc "#3C3E5E"
    :inner-emul "#593447"

    :ring       "#A19E9C"

    :fmenu-visu "#516F6C"
    :fmenu-calc "#51537E"
    :fmenu-emul "#654E6A"
    :fmenu-base (cs :--Night-900) ;; #5D627A
    :fmenu-glow (cs :--Night-600) ;; #848AA3
    }))

(def stitches-specs
  {:theme
   {:space
    (into {}
          (:space scale))

    :fontSizes
    {;; double-stranded modular scale
     ;; ratio: 1:√3 (1.732), bases: 16, 14
     ;; https://www.modularscale.com/?16,14&px&1.732
     :1 "0.875rem" ;; 14px
     :2 "1rem"     ;; 16px
     :3 "1.516rem" ;; 24.248px
     :4 "1.732rem" ;; 27.712px
     :5 "2.625rem" ;; 41.998px
     :6 "3rem"     ;; 47.997px
     :7 "4.546rem" ;; 72.74px
     :8 "5.196rem" ;; 83.131px

     :xs   "0.75rem"  ;; 12px  OK :small -> :xs
     :sm   "0.875rem" ;; 14px  OK :base -> :sm
     :base "1rem"     ;; 16px  OK :medium -> :base
     :lg   "1.125rem" ;; 18px
     :xl   "1.25rem"  ;; 20px
     :2xl  "1.5rem"   ;; 24px
     :3xl  "1.875rem" ;; 30px
     :4xl  "2.25rem"  ;; 36px
     :5xl  "3rem"     ;; 48px
     :6xl  "3.75rem"  ;; 60px
     :7xl  "4.5rem"   ;; 72px
     :8xl  "6rem"     ;; 96px
     :9xl  "8rem"     ;; 128px
     }

    :fonts
    {:base "\"IBM Plex Sans\", arial, sans-serif"
     :mono "\"IBM Plex Mono\", courier, monospace"
     ;; :base "\"Berkeley Mono\", \"IBM Plex Sans\", arial, sans-serif"
     ;; :mono "\"Berkeley Mono\", \"IBM Plex Mono\", courier, monospace"
     }

    :fontWeights
    {:thin 100
     :extralight 200
     :light 200  ;; was 300
     :normal 400
     :medium 600  ;; was 500
     :semibold 800 ;; was 600
     :bold 800 ;; was 700
     :extrabold 900 ;; was 800
     :black 900}

    :lineHeights
    {:sm "1.25em" ;; 20px | was rem
     :base "1.3em"
     :none "1"}

    :letterSpacings
    {}

    :sizes
    (into {;; old:
           :icon-toolbar "18px" ;; -> rem?
           :icon-input "30px" ;; -> rem?
           :icon-tab "36px" ;; -> rem?
           ;; new:
           :px "1px"
           :icon-xs "0.75rem" ;; 12px
           :icon-sm "1rem" ;; 16px
           :icon-lg "1.25rem" ;; 20px
           :full "100%"}
          (:sizes scale))

    :borderWidths
    {:1 "1px"
     :2 "2px"
     :3 "3px"
     :4 "4px"
     :8 "8px"}

    :borderStyles
    {}
    
    :radii
    {;; old:
     :1 "2px"
     :2 "4px"
     :3 "6px"
     :4 "10px"
     :round "50%"
     ;; new:
     :sm   "0.125rem" ;; 2px
     :base "0.25rem"  ;; 4px
     :md   "0.375rem" ;; 6px
     :lg   "0.5rem"   ;; 8px
     :full "9999px"
     }

    :shadows
    {:sm "0 1px 2px 0 rgb(0 0 0 / 0.05)"
     :base "0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)"
     :md "0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)"
     :lg "0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)"
     :xl "0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)"
     :2xl "0 25px 50px -12px rgb(0 0 0 / 0.25)"
     :inner "inset 0 2px 4px 0 rgb(0 0 0 / 0.05)"
     :none "0 0 #0000"}

    :zIndices
    {}

    :transitions
    {}}

   :media
   {:bp1 "(min-width: 480px)"}

   :utils
   {:_transition_colors
    (fn [[duration]]
      (let [t (str (or duration 150) "ms")]
        #js {:transition-property
             "color, background-color, border-color, text-decoration-color"
             :transition-timing-function "cubic-bezier(0.4, 0, 0.2, 1)"
             :transition-duration        t}))

    ;; :_shadow
    ;; (fn [[]]
    ;;   (let []
    ;;     #js {:box-shadow
    ;;          ""}))

    :_ring
    (fn [[w col offset-w offset-col inset?]]
      (let [inset  (when inset? "inset ")
            spread (str (+ w (or offset-w 0)) "px")
            col    (or col "#000")]
        #js {"$$ringShadow" (str inset "0 0 0 " spread " " col)
             "$$ringOffsetShadow"
             (if (and offset-w (> offset-w 0))
               (let [off-spread (str offset-w "px")
                     off-col    (or offset-col "$colors$outer-bg")]
                 (str inset "0 0 0 " off-spread " " off-col))
               "0 0 #0000")
             :box-shadow
             "$$ringOffsetShadow, $$ringShadow, var(---shadow, 0 0 #0000)"}))

    :_ringOuter
    (fn [_]
      #js {:_ring [2 "$colors$ring" 2 "$colors$outer-bg"]})

    :_ringInner
    (fn [_]
      #js {:_ring [2 "$colors$ring" 2 "$colors$inner-bg"]})

    :_outline
    (fn [[col w offset style]]
      (let [col    (or col "#000")
            width  (or w "2px")
            offset (or offset "2px")
            style  (or style "solid")]
        #js {:outline (str col " " style " " width)
             :outline-offset offset}))

    :_outlineNone
    (fn [_]
      #js {:_outline ["transparent"]})

    :_paddingX
    (fn [v]
      (let [s (if (string? v) v (str v "px"))]
        #js {:padding-left  s
             :padding-right s}))
    
    :_paddingY
    (fn [v]
      (let [s (if (string? v) v (str v "px"))]
        #js {:padding-top    s
             :padding-bottom s}))

    :_marginX
    (fn [v]
      (let [s (if (string? v) v (str v "px"))]
        #js {:margin-left  s
             :margin-right s}))
    
    :_marginY
    (fn [v]
      (let [s (if (string? v) v (str v "px"))]
        #js {:margin-top    s
             :margin-bottom s}))

    :_text
    (fn [[size lh]]
      (let [lh (or lh size)]
        #js {:font-size size
             :line-height lh}))

    :_lineClamp
    (fn [n]
      #js {:overflow "hidden"
           :display "-webkit-box"
           :-webkit-box-orient "vertical"
           :-webkit-line-clamp n})

    :_lineClampNone
    (fn [_]
      #js {:overflow "visible"
           :display "block"
           :-webkit-box-orient "horizontal"
           :-webkit-line-clamp "none"})
    
    :_size
    (fn [[w h]]
      (let [w (str (or w 0) (when-not (string? w) "px"))
            h (str (or h w) (when-not (string? h) "px"))]
        #js {:width  w
             :height h}))}})

