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


(def color-scales
  {:s1  "#FCFCFA"
   :s2  "#E6E3E1"
   :s3  "#D5D2CF"
   :s4  "#C4C0BE"
   :s5  "#B3AEAD"
   :s6  "#A19C9B"
   :s7  "#918C8C"
   :s8  "#797576"
   :s9  "#656363"
   :s10 "#514F4F"
   :s11 "#3E3D3C"
   :s12 "#2A2A28"
   :s13 "#1B1B13"

   :n1  "#14151F"
   :n2  "#2E3347"
   :n3  "#424659"
   :n4  "#55586B"
   :n5  "#666A7D"
   :n6  "#76798C"
   :n7  "#8A8D9E"
   :n8  "#9A9EAD"
   :n9  "#AEB1BF"
   :n10 "#BEC2CF"
   :n11 "#CED1DE"
   :n12 "#E1E4F0"
   :n13 "#F8F9FD"})

(def colors-ui
  {:primary        "#76798C"
   :primary-fg     "#fff"
   :secondary      "#E6E3E1"
   :secondary-fg   "#3E3D3C"
   :destructive    "#BE5F5F"
   :destructive-fg "#fff"
   :accent         "#E1E4F0"
   :accent-fg      "#000"
   :muted          "#D5D2CF"
   :muted-fg       "#FFFFFF"
   :popover        "white"
   :popover-fg     "black"
   :card           "white"
   :card-fg        "black"
   :fg             "black"
   :bg             "#FCFCFA"
   :border         "#ddd"
   :input          "#bab3a9"
   :ring           "rgb(59 130 246 / 0.5)"})

(def cs color-scales)

(def colors-base
  (merge colors-ui color-scales))

(def colors-light
  (into colors-base
        {:outer_bg (cs :s2)
         :outer_fg (cs :s12) ;; "#333231"
         :outer_n100 (cs :s7) ;; "#969493"
         :outer_n200 (cs :s4) ;; "#C2BEBE"
         :outer_m100 (cs :n2) ;; "#2F3347"
         :outer_m200 (cs :n6) ;; "#7A7E91"
         :outer_hl "#9297B0"
         :outer_contrast "#FFFBEB"
         :fmenu_visu "#C4D1CC"
         :fmenu_calc "#C8CAE0"
         :fmenu_emul "#DDC2CD"
         :fmenu_base "#D9D7D4"
         :fmenu_glow "#EDEBE8"
         :inner_bg (cs :s1)
         :inner_fg (cs :s11) ;; "#4A4847"
         :inner_n100 (cs :s6) ;; "#ADABAA"
         :inner_n200 (cs :s3) ;; "#D9D4D4"
         :inner_m100 (cs :n3) ;; "#3E445E"
         :inner_m200 (cs :n7) ;; "#8D92A8"
         :inner_hl "#FFFBEB"
         :inner_contrast "#FFFBEB"
         :inner_visu "#D8EBE2"
         :inner_calc "#E8E8FF"
         :inner_emul "#F5E1E8"}))

(def colors-dark
  (into colors-base
        {:outer_bg (cs :n2) ;; "#2F3347"
         :outer_fg (cs :n12)
         :outer_n100 (cs :n7) ;; "#848AA3"
         :outer_n200 (cs :n4) ;; "#5D627A"
         :outer_m100 (cs :s2) ;; "#E6E3E1"
         :outer_m200 (cs :s6) ;; "#A5A3A2"
         :outer_hl "#B6B9DB"
         :outer_contrast "#B6B9DB"
         :fmenu_visu "#516F6C"
         :fmenu_calc "#51537E"
         :fmenu_emul "#654E6A"
         :fmenu_base "$outer_n200"
         :fmenu_glow "$outer_n100"
         :inner_bg (cs :n1)  ;; "#14151F"
         :inner_fg (cs :n11) ;; "#C5C7D1"
         :inner_n100 (cs :n6) ;; "#6B7085"
         :inner_n200 (cs :n3) ;; "#46495C"
         :inner_m100 (cs :s3) ;; "#C7C5C3"
         :inner_m200 (cs :s7) ;; "#878584"
         :inner_hl "#000"      ;; "#9D9FBD"
         :inner_contrast "#9D9FBD"
         :inner_visu "#3B524F"
         :inner_calc "#3C3E5E"
         :inner_emul "#593447"}))


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
                     off-col    (or offset-col "$colors$bg")]
                 (str inset "0 0 0 " off-spread " " off-col))
               "0 0 #0000")
             :box-shadow
             "$$ringOffsetShadow, $$ringShadow, var(---shadow, 0 0 #0000)"}))

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

