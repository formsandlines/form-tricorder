(ns css.common
  (:require
   ;; [clojure.walk :as walk]
   [clojure.string :as str]))

(defn linear-map
  "Domain->range mapping similar to Processings `map`:
  https://processing.org/reference/map_.html"
  [dmin dmax rmin rmax x]
  (+ (* (- x dmin)
        (/ (- rmax rmin)
           (- dmax dmin)))
     rmin))

;; (defn sort-map-by-keys
;;   "Sorts a map `m` by matching a vector `order` of its maps."
;;   [m order]
;;   (let [sorted-keys (sort (fn [k1 k2]
;;                             (let [index1 (.indexOf order k1)
;;                                   index2 (.indexOf order k2)]
;;                               (compare index1 index2)))
;;                           (keys m))]
;;     (apply array-map (mapcat (fn [k] [k (m k)]) sorted-keys))))

;; (defn deep-order-maps [v]
;;   (walk/postwalk (fn [x]
;;                    (cond->> x
;;                      (map? x) (into (sorted-map))))
;;                  v))

;; (comment
;;   (sort-map-by-keys
;;    {:b 2, :a 1, :d 4, :c 3}
;;    [:a :b :c :d]) ;; => {:a 1, :b 2, :c 3, :d 4}
;;   )

;; (defn transform-nested-map
;;   "Transforms the innermost values of a nested map `m` by a given function `f`, which is provided with a vector of all keys from the root up to the value and the value itself.
;;   - takes an optional `keep-order?` boolean to restore the order of the keys in the original map (note: use `array-map` to create maps that retain their order)"
;;   ([m f] (transform-nested-map m f false))
;;   ([m f keep-order?]
;;    (letfn [(collect-path [path v]
;;              (if (map? v)
;;                (let [m-transformed
;;                      (reduce-kv (fn [acc k v]
;;                                   (assoc acc k (collect-path (conj path k) v)))
;;                                 {}
;;                                 v)]
;;                  (if keep-order?
;;                    (sort-map-by-keys m-transformed (keys v))
;;                    m-transformed))
;;                (f path v)))]
;;      (collect-path [] m))))

(defn transform-nested-map
  "Transforms the innermost values of a nested map `m` by a given function `f`, which is provided with a vector of all keys from the root up to the value and the value itself.
  - takes an optional `keep-order?` boolean to restore the order of the keys in the original map (note: use `array-map` to create maps that retain their order)"
  [m f]
  (letfn [(collect-path [path v]
            (if (map? v)
              (reduce-kv (fn [acc k v]
                           (assoc acc k (collect-path (conj path k) v)))
                         {}
                         v)
              (f path v)))]
    (collect-path [] m)))

(def no-k :_) ;; empty key

(defn with-props
  "Transforms the values of a nested style map `m` to tuples of a CSS custom property name (that combines each key in the map up to the value), and the value itself. This makes writing style maps less repetitive.
  - takes an optional `prefix` string to replace the key path"
  ([m] (with-props m nil))
  ([m prefix]
   (transform-nested-map
    m
    (fn [path v]
      (let [path-s (map #(if (= no-k %) nil (name %))
                        path)]
        [(str "--" (if prefix
                     (str prefix (when-let [s (last path-s)]
                                   (str "-" s)))
                     (str/join "-" (remove nil? path-s))))
         v])))))

(defn format-number [num places]
  (.toFixed num places))

(def base-fontsize 16)

(defn px->rem
  [px precision]
  (parse-double (format-number (/ px base-fontsize) precision)))

(defn rem->px
  [rem]
  (* base-fontsize rem))

;; (defn make-key-comparator
;;   [k->n]
;;   (fn [a b] (compare (k->n a) (k->n b))))

(defn css-var
  [prefix label]
  (str "--" prefix label))

(defn css-eval-var
  [prop]
  (str "var(" prop ")"))

(def css-indent "    ")

;; (defn scale->css-vars
;;   [scale indents]
;;   (str/join "\n"
;;             (map #(str (str/join (repeat indents css-indent))
;;                        (str/join ": " (take 2 %)) ";")
;;                  (vals scale))))

#_
(defn scale->css-vars
  ([scale indents] (scale->css-vars scale indents nil))
  ([scale indents val-k]
   (str/join "\n"
             (map #(str (str/join (repeat indents css-indent))
                        (str/join ": " (if val-k
                                         [(first %) (get (second %) val-k)]
                                         (take 2 %))) ";")
                  (vals scale)))))

(defn scale->css-vars
  ([scale indents] (scale->css-vars scale indents nil))
  ([scale indents val-k]
   (str/join
    "\n"
    (map (fn [[k v]]
           (str (str/join (repeat indents css-indent))
                (str k ": " (cond (and (map? v) val-k) (get v val-k)
                                  (and (map? v)
                                       (find v :light)
                                       (find v :dark))
                                  (str "light-dark("
                                       (v :light) ", " (v :dark)
                                       ")")
                                  :else v))
                ";"))
         (vals scale)))))

(defn scale->css-properties
  [scale {:keys [syntax inherits? init] :or {inherits? true}}]
  (str/join "\n\n"
            (map (fn [[pname v _]]
                   (str "@property " pname " {\n"
                        css-indent "syntax: \"" syntax "\";\n"
                        css-indent "inherits: " (str inherits?) ";\n"
                        css-indent "initial-value: " (or init v) ";\n"
                        "}"))
                 (vals scale))))
