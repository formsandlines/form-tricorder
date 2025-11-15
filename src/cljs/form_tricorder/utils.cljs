(ns form-tricorder.utils
  (:require
   [helix.hooks :as hooks]
   [clojure.string :as string]
   [goog.string :as gstring]
   [shadow.css :refer (css)]
   [formform.utils :refer [compare-names]]
   ;; [clojure.math :as math]
   )
  (:require-macros [form-tricorder.utils]))

(def $nowrap (css {:white-space "nowrap"}))

(def <&> gstring/unescapeEntities)

(defn splitv-atv [i v]
  {:pre [(vector? v)]}
  (vector (into [] (subvec v 0 i))
          (into [] (subvec v i (count v)))))

(defn dissocv [v i]
  {:pre [(vector? v)]}
  (into (into [] (subvec v 0 i))
        (subvec v (inc i))))

(defn reversev [xs]
  (vec (if (vector? xs)
         (rseq xs)
         (reverse xs))))


(defn pow-nat [x n] (apply * (repeat n x)))

(defn geom-seq [k r] (map (fn [n] (* k (pow-nat r n))) (range)))


(def log js/console.log)

(defn assocp
  "Calls `assoc` on props map of given component."
  [cp & kvs]
  (update cp 1 (fn [props] (apply assoc props kvs))))

;; ? needed
(defn clj->js* 
  "Recursively calls `clj->js` on given map and each submap in the tree."
  [m]
  (cond (map? m)        (clj->js (update-vals m clj->js*))
        (sequential? m) (map clj->js* m)
        :else m))

;; Credits to Clojurians Slack user tomc:
;; -> https://clojurians.slack.com/archives/CRRJBCX7S/p1606239745203400
(defn use-custom-compare-memoize [deps eq?]
  (let [ref (hooks/use-ref [])]
    (if (or (not @ref) (not (eq? @ref deps)))
      (reset! ref deps))
    (into-array @ref)))

; (defn use-custom-compare-effect [effect deps eq?]
;   (react/useEffect effect (use-custom-compare-memoize deps eq?)))


;; (def dark-theme darkTheme)
;; (def light-theme lightTheme)

;; (defn pp-val [v] (name v)) ;; <-- replace with this
(defn pp-val [v] (-> (name v) .toLowerCase))
(defn pp-var [s] (if (> (count s) 1) (str "'" s "'") s))

(defn sort-varorder
  [varorder]
  (let [vars (set varorder)]
    (cond
      (= #{"-" "L" "E" "R" "+"} vars) ["-" "L" "E" "R" "+"]
      (= #{"-" "L" "R" "+"} vars) ["-" "L" "R" "+"]
      (= #{"L" "E" "R"} vars) ["L" "E" "R"]
      (= #{"L" "R"} vars) ["L" "R"]
      :else (sort compare-names varorder))))

(defn merge-deep
  "Deeply merges all maps recursively nested inside the given input maps."
  [m & ms]
  (apply merge-with
         (fn [x y]
           (if (and (map? x)
                    (map? y))
             (merge-deep x y)
             y))
         m
         ms))

(defn linear-map
  "Domain->range mapping similar to Processings `map`:
  https://processing.org/reference/map_.html"
  [dmin dmax rmin rmax x]
  (+ (* (- x dmin)
        (/ (- rmax rmin)
           (- dmax dmin)))
     rmin))

(defn pad
  "Pads `0` or given `pad-char` before string."
  ([len s] (pad \0 len s))
  ([pad-char len s]
   (let [s (if (string? s) s (str s))]
     (loop [s s]
       (if (< (count s) len)
         (recur (str pad-char s))
         s)))))

(defn get-timestamp
  []
  (let [date    (js/Date.)
        year    (.getUTCFullYear date)
        month   (pad 2 (inc (.getUTCMonth date)))
        day     (pad 2 (.getUTCDate date))
        hours   (pad 2 (.getHours date))
        minutes (pad 2 (.getMinutes date))
        seconds (pad 2 (.getSeconds date))]
    (str year month day "-" hours minutes seconds)))

(defn unite
  "Convenience function to join strings (e.g. classnames) that may be nil."
  [& strings]
  (string/join " " (remove nil? strings)))

(defn copy-to-clipboard
  [text report-copy-status]
  (.then (.. js/navigator -clipboard (writeText text))
         (fn [] (report-copy-status [true text]))
         (fn [err] (report-copy-status [false err]))))

(def const->col-ui
  {:n "var(--col-const-n)"
   :u "var(--col-const-u)"
   :i "var(--col-const-i)"
   :m "var(--col-const-m)"})

(def const->col-ui-hover
  {:n "var(--col-const-n-hover)"
   :u "var(--col-const-u-hover)"
   :i "var(--col-const-i-hover)"
   :m "var(--col-const-m-hover)"})

(def const->col-contrast
  {:n "#000000"
   :u "#FF0000"
   :i "#00FF00"
   :m "#0000FF"})

(def consts [:n :u :i :m])
(def consts-set #{:n :u :i :m})

(comment
  (pad 2 (.getUTCDate (js/Date.)))
  (get-timestamp)

  (= (merge-deep
      {:a 1
       :foo "a"
       :b {:x 2
           :bar "b"
           :y {:o 3
               :baz 4}}}
      {:a 9
       :moo "m"
       :b {:x 8
           :mar "n"
           :y {:o 7
               :maz "o"}}})
     {:a 9, :foo "a", :b {:x 8, :bar "b",
                          :y {:o 7, :baz 4, :maz "o"}, :mar "n"}, :moo "m"})
  
  )
