(ns form-tricorder.utils)

(defn clj->js* [m]
  (cond (map? m)        (clj->js (update-vals m clj->js*))
        (sequential? m) (map clj->js* m)
        :else m))
