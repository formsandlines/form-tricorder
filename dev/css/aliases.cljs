(ns css.aliases
  (:require
   [clojure.string :as str]
   [css.common :as c]
   [css.colors :as colors]
   [css.styles :as styles]))


;; taken from shadow-css
;; Note: some of my names intentionally deviate from tailwind
(def alias-groups
  {:color
   {"bg" [:background-color]
    "fg" [:color]
    "border-col" [:border-color]
    "outline" [:outline-color]
    "stroke" [:stroke]
    "fill" [:fill]
    ;; "divide"
    ;; (fn [color]
    ;;   [["& > * + *" {:border-color color}]])
    }

   :space
   {;; padding
    "p" [:padding]
    "px" [:padding-left :padding-right]
    "py" [:padding-top :padding-bottom]
    "pt" [:padding-top]
    "pb" [:padding-bottom]
    "pl" [:padding-left]
    "pr" [:padding-right]
    ;; ["space-x" "& > * + *"] [:padding-left :padding-right]
    ;; ["space-y" "& > * + *"] [:padding-top :padding-bottom]

    ;; margin
    "m" [:margin]
    "mx" [:margin-left :margin-right]
    "my" [:margin-top :margin-bottom]
    "mt" [:margin-top]
    "mb" [:margin-bottom]
    "ml" [:margin-left]
    "mr" [:margin-right]

    ;; positioning
    "top" [:top]
    "right" [:right]
    "bottom" [:bottom]
    "left" [:left]
    ;; "-top" [:top]
    ;; "-right" [:right]
    ;; "-bottom" [:bottom]
    ;; "-left" [:left]
    "inset-x" [:left :right]
    "inset-y" [:top :bottom]
    "inset" [:top :right :bottom :left]
    ;; "-inset-x" [:left :right]
    ;; "-inset-y" [:top :bottom]
    ;; "-inset" [:top :right :bottom :left]

    ;; gutters (grid, flexbox)
    "gap" [:gap]
    "gap-x" [:column-gap]
    "gap-y" [:row-gap]}

   :sizes
   {"w" [:width]
    "max-w" [:max-width]
    "min-w" [:min-width]
    "h" [:height]
    "max-h" [:max-height]
    "min-h" [:min-height]
    "size" [:width :height]
    "flex-basis" [:flex-basis]}

   :border-width
   {"border" [:border-width]
    "border-x" [:border-left-width :border-right-width]
    "border-y" [:border-top-width :border-bottom-width]
    "border-t" [:border-top-width]
    "border-b" [:border-bottom-width]
    "border-l" [:border-left-width]
    "border-r" [:border-right-width]}

   :radius
   {"rounded" [:border-radius]
    "rounded-t" [:border-top-right-radius :border-top-left-radius]
    "rounded-b" [:border-bottom-right-radius :border-bottom-left-radius]
    "rounded-l" [:border-top-left-radius :border-bottom-left-radius]
    "rounded-r" [:border-top-right-radius :border-bottom-right-radius]
    "rounded-tl" [:border-top-left-radius]
    "rounded-tr" [:border-top-right-radius]
    "rounded-bl" [:border-bottom-left-radius]
    "rounded-br" [:border-bottom-right-radius]}

   ;; singleton groups
   :font         {"font" [:font-family]}
   :font-size    {"font-size" [:font-size]}
   :line-height  {"line-h" [:line-height]}
   :font-weight  {"weight" [:font-weight]}})


(def make-alias-k
  (fn [prefix style-k]
    (let [suffix (if (= style-k :_)
                   ""
                   (str "-" (name style-k)))]
      (keyword (str prefix suffix)))))

(def style-aliases
  (into {}
        (reduce-kv
         (fn [aliases group-k style-props]
           (reduce-kv
            (fn [aliases style-k [css-var _]]
              (reduce-kv
               (fn [aliases prefix props]
                 (let [alias-k (make-alias-k prefix style-k)]
                   (conj aliases
                         [alias-k
                          (reduce #(assoc %1 %2 (c/css-eval-var css-var))
                                  {} props)])))
               aliases
               (alias-groups group-k)))
            aliases
            style-props))
         []
         (merge
          styles/output
          (colors/output :light)))))

(def color-aliases
  (into {}
        (reduce-kv
         (fn [aliases style-k [css-var _ css-prop]]
           (if css-prop
             (conj aliases
                   [style-k
                    {css-prop (c/css-eval-var css-var)}])
             aliases))
         []
         (colors/output :semantic))))

;; (comment
;;   (reduce-kv
;;    (fn [aliases prop alias]
;;      (let [alias-k (make-alias-k prefix style-k)]
;;        (conj aliases
;;              [])))
;;    aliases
;;    css-prop-map))

;; :text → font-size + line-height
(def text-aliases
  (into
   {}
   (map
    (fn [[fs-k lh-k]]
      (let [[fs-prop _] ((:font-size styles/output) fs-k)
            [lh-prop _] ((:line-height styles/output) lh-k)]
        [(make-alias-k "text" fs-k)
         {:font-size (c/css-eval-var fs-prop)
          :line-height (c/css-eval-var lh-prop)}]))
    [[:xs :4]
     [:sm :5]
     [:base :6]
     [:lg :7]
     [:xl :7]
     [:2xl :8]
     [:3xl :9]
     [:4xl :10]
     [:5xl :none]
     [:6xl :none]
     [:7xl :none]
     [:8xl :none]
     [:9xl :none]])))


;; functions for complex CSS
(defn make-ring
  "Creates a ring around a UI component for keyboard navigation."
  [{:keys [w col offset-w offset-col inset?]
    :or {w 2
         col "var(--col-ring)"
         offset-w 2
         offset-col "var(--col-bg)"
         inset? false}}]
  (let [inset (when inset? "inset ")
        spread (str (+ w offset-w) "px")]
    {:--ring-shadow (str inset "0 0 0 " spread " " col)
     :--ring-offset-shadow
     (if (and offset-w (> offset-w 0))
       (let [offset-spread (str offset-w "px")]
         (str inset "0 0 0 " offset-spread " " offset-col))
       "0 0 #0000")
     :box-shadow "var(--ring-offset-shadow), var(--ring-shadow), 0 0 #0000"})) ;; var(---shadow, 0 0 #0000)

(defn make-checkerboard
  "Creates a background with checkerboard pattern to demonstrate transparency."
  [{:keys [col size]
    :or {col "var(--col-n12)"
         size "60px"}}]
  {:--opc1 "26%"
   :--opc2 "75%"
   :--col col
   :--size size
   :background-color "var(--col-n14)"
   ;; Checkerboard pattern to mock transparency
   ;; Source: https://gist.github.com/dfrankland/f6fed3e3ccc42e3de482b324126f9542?permalink_comment_id=5160713#gistcomment-5160713
   :background-image "
linear-gradient(45deg, var(--col) var(--opc1), transparent var(--opc1)),
linear-gradient(135deg, var(--col) var(--opc1), transparent var(--opc1)),
linear-gradient(45deg, transparent var(--opc2), var(--col) var(--opc2)),
linear-gradient(135deg, transparent var(--opc2), var(--col) var(--opc2))"
   :background-size "var(--size) var(--size)"
   :background-position "0 0,
calc(var(--size) / 2) 0,
calc(var(--size) / 2) calc(-1 * (var(--size) / 2)),
0px calc(var(--size) / 2)"})

(defn make-outline
  [{:keys [col w offset style]
    :or {w "2px"
         offset "2px"
         style "solid"}}]
  {:outline (str/join " " [col style w])
   :outline-offset offset})


(def special-aliases
  {:overlay-bg
   {:background-color "color-mix(in srgb, var(--col-fg) 60%, transparent)"
    ;; :backdrop-filter "blur(3px)"
    :position "fixed"
    :z-index "50"
    :inset "0"
    :animation "overlay-show 150ms cubic-bezier(0.16, 1, 0.3, 1)"}

   :overlay-content
   {:position "fixed"
    :z-index "50"
    :top "50%"
    :left "50%"
    :transform "translate(-50%, -50%)"
    :box-shadow "0 3px 5px 0px color-mix(in srgb, var(--col-fg) 30%, transparent)"
    :animation "content-show 150ms cubic-bezier(0.16, 1, 0.3, 1)"}

   :checkerboard (make-checkerboard {})

   ;; ? too specific
   :transition-colors
   {:--duration "150ms"
    :transition-property
    "color, background-color, border-color, text-decoration-color"
    :transition-timing-function "cubic-bezier(0.4, 0, 0.2, 1)"
    :transition-duration "var(--duration)"}

   :ring (make-ring {})

   :outline-none (make-outline {:col "transparent"})

   ;; ! no default for `--clamp-n`
   :line-clamp
   {:overflow "hidden"
    :display "-webkit-box"
    :-webkit-box-orient "vertical"
    :-webkit-line-clamp "var(--clamp-n)"}

   :line-clamp-none
   {:overflow "visible"
    :display "block"
    :-webkit-box-orient "horizontal"
    :-webkit-line-clamp "none"}

   })

(def output
  (merge style-aliases
         color-aliases
         text-aliases
         special-aliases))

