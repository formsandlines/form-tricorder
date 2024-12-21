(ns css.styles
  (:require
   [css.common :as c]
   [clojure.string :as str]
   [clojure.math :as math]))

;; dimensions

(def dim-indices (range 0 100.5 0.5))



(defn num-key [n] (str/replace (str n) "." "-"))

(defn make-dim-scale
  "Generates a dimension scale with a `step` value added to the next number, which is interpreted as `from-unit`. Returns a map with `from-unit` as key and a tuple of a `prefix`'d CSS custom property name and the scale value converted to `to-unit`.
  - units can be `:px` or `:rem`"
  [step from-unit to-unit prefix]
  (let [kvs (for [n-from dim-indices
                  :let [n-to (case from-unit
                               :px (let [px (math/floor (* n-from step))]
                                     (case to-unit
                                       :px px
                                       :rem (c/px->rem px 8)))
                               :rem (let [rem (* n-from step)]
                                      (case to-unit
                                        :px (c/rem->px rem)
                                        :rem rem)))
                        
                        n-key (keyword (num-key n-from))
                        n-val (str n-to (name to-unit))]]
              [n-key [(c/css-var prefix (name n-key)) n-val]])]
    (into {} kvs)))

(def dim-props
  {:space (make-dim-scale 3 :px :px "sp-")
   :sizes (merge (make-dim-scale 3 :px :rem "sz-")
                 {:icon-sm ["--sz-icon-sm" "1rem"]})})

(def font-props
  (c/with-props
    {:font
     {:sans "\"IBM Plex Sans\", arial, sans-serif"
      :mono "\"IBM Plex Mono\", courier, monospace"}}))

(def fontsize-props
  (merge
   (c/with-props
     {:font-size
      {;; double-stranded modular scale
       ;; ratio: 1:√3 (1.732), bases: 16, 14
       ;; https://www.modularscale.com/?16,14&px&1.732
       :1 "0.875rem" ;; 14px  sm
       :2 "1rem"     ;; 16px  base
       :3 "1.516rem" ;; 24.248px  ~2xl
       :4 "1.732rem" ;; 27.712px  ~2-3xl
       :5 "2.625rem" ;; 41.998px  ~4-5xl
       :6 "3rem"     ;; 47.997px  ~5xl
       :7 "4.546rem" ;; 72.74px   ~7xl
       :8 "5.196rem" ;; 83.131px  ~7-8xl

       :xs   "0.75rem" ;; 12px     OK :small -> :xs
       :sm   "0.875rem" ;; 14px  1  OK :base -> :sm
       :base "1rem"     ;; 16px  2  OK :medium -> :base
       :lg   "1.125rem" ;; 18px
       :xl   "1.25rem"  ;; 20px
       :2xl  "1.5rem"   ;; 24px  ~3
       :3xl  "1.875rem" ;; 30px  ~4
       :4xl  "2.25rem"  ;; 36px  ~4-5
       :5xl  "3rem"     ;; 48px  ~6
       :6xl  "3.75rem"  ;; 60px
       :7xl  "4.5rem"   ;; 72px  ~7
       :8xl  "6rem"     ;; 96px  ~8
       :9xl  "8rem"     ;; 128px
       }}
     "fs")))

(def leading-props
  (c/with-props
    {:line-height
     {:3  ".75rem"
      :4  "1rem"
      :5  "1.25rem"
      :6  "1.5rem"
      :7  "1.75rem"
      :8  "2rem"
      :9  "2.25rem"
      :10 "2.5rem"
      :loose   "2"
      :none    "1"
      :normal  "1.5"
      :relaxed "1.625"
      :snug    "1.375"
      :tight   "1.25"}}
    "lh"))

(def weight-props
  (c/with-props
    {:font-weight
     {:thin       "100"
      :extralight "200"
      :light      "200" ;; was 300
      :normal     "400"
      :medium     "600" ;; was 500
      :semibold   "800" ;; was 600
      :bold       "800" ;; was 700
      :extrabold  "900" ;; was 800
      :black      "900"}}
    "weight"))

(def border-props
  (c/with-props
    {:border-width
     {:0 "0px"
      :_ "1px"
      :2 "2px"
      :3 "3px"
      :4 "4px"
      :8 "8px"}}
    "border"))

(def radius-props
  (c/with-props
    {:radius
     {:sm   "0.125rem" ;; 2px
      :_    "0.25rem"  ;; 4px
      :md   "0.375rem" ;; 6px
      :lg   "0.5rem"   ;; 8px
      :full "9999px"
      :none "0px"}}
    "rad"))

;; (def shadow-props
;;   (c/with-props
;;     {:shadow
;;      {:sm "0 1px 2px 0 rgb(0 0 0 / 0.05)"
;;       :base "0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)"
;;       :md "0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)"
;;       :lg "0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)"
;;       :xl "0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)"
;;       :2xl "0 25px 50px -12px rgb(0 0 0 / 0.25)"
;;       :inner "inset 0 2px 4px 0 rgb(0 0 0 / 0.05)"
;;       :none "0 0 #0000"}}))

;; Output

(def output
  (merge dim-props
         font-props
         fontsize-props
         leading-props
         weight-props
         border-props
         radius-props
         ;; shadow-props
         ))

(def css-output
  (let [dim-defs
        (str/join "\n" (map (fn [[_ cs]]
                              (c/scale->css-properties
                               cs {:syntax "<length>" :inherits true}))
                            dim-props))

        fontfamily-defs
        (str/join "\n" (map (fn [[_ cs]]
                              (c/scale->css-properties
                               cs {:syntax "<string>" :inherits true}))
                            font-props))

        fontsize-defs
        (str/join "\n" (map (fn [[_ cs]]
                              (c/scale->css-properties
                               cs {:syntax "<length>" :inherits true}))
                            fontsize-props))
        
        leading-defs
        (str/join "\n" (map (fn [[_ cs]]
                              (c/scale->css-properties
                               cs {:syntax "<length> | <number>"
                                   :inherits true}))
                            leading-props))
        
        weight-defs
        (str/join "\n" (map (fn [[_ cs]]
                              (c/scale->css-properties
                               cs {:syntax "<number>" :inherits true}))
                            weight-props))

        border-defs
        (str/join "\n" (map (fn [[_ cs]]
                              (c/scale->css-properties
                               cs {:syntax "<length>" :inherits true}))
                            border-props))

        radius-defs
        (str/join "\n" (map (fn [[_ cs]]
                              (c/scale->css-properties
                               cs {:syntax "<length>" :inherits true}))
                            radius-props))

        all-props output
        prop-vars
        (str/join "\n\n" (map (fn [[_ cs]] (c/scale->css-vars cs 1))
                              all-props))]
    (str
     ;; All custom properties
     dim-defs "\n\n"
     fontfamily-defs "\n\n"
     fontsize-defs "\n\n"
     leading-defs "\n\n"
     weight-defs "\n\n"
     border-defs "\n\n"
     radius-defs "\n\n"

     ;; ? necessary
     ":root {\n"
     prop-vars "\n"
     "}\n\n")))

