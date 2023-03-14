(ns form-tricorder.utils)

(defn clj->js* 
  "Recursively calls `clj->js` on given map and each submap in the tree."
  [m]
  (cond (map? m)        (clj->js (update-vals m clj->js*))
        (sequential? m) (map clj->js* m)
        :else m))

(defn assocp
  "Calls `assoc` on props map of given component."
  [cp & kvs]
  (update cp 1 (fn [props] (apply assoc props kvs))))

(defn pow-nat [x n] (apply * (repeat n x)))

(defn geom-seq [k r] (map (fn [n] (* k (pow-nat r n))) (range)))
