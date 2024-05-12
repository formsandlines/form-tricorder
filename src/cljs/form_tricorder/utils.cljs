(ns form-tricorder.utils
  (:require
    [helix.hooks :as hooks]
    ["/stitches.config" :refer (styled css darkTheme lightTheme)]))

(defn splitv-at [i v]
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


(def dark-theme darkTheme)
(def light-theme lightTheme)

(defn style>
  [elem styles-map]
  (styled elem (clj->js* styles-map)))

(defn css>
  [styles-map]
  (-> styles-map clj->js* css))

(defn pp-val [v] (-> (name v) .toLowerCase))
(defn pp-var [s] (if (> (count s) 1) (str "'" s "'") s))

(comment
  
  (pow-nat 2 3)

  (take 5 (geom-seq 3 5))
  
  )
