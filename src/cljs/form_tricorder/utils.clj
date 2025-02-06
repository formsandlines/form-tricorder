(ns form-tricorder.utils)

(defmacro merge-some
  "Merges all maps in order as in `merge` but falls back to non-nil values."
  [& ms]
  (let [symbs (vec (repeatedly (count ms) gensym))]
    `(merge-with (fn ~symbs (or ~@(rseq symbs))) ;; bind ltr, merge rtl
                 ~@ms)))

(defmacro let+
  "Like `let`, but with the ability to bind a rest symbol to `:rest` when destructuring maps (e.g. `{:keys […] :rest r}`), which refers to the remaining values instead of the whole map. This symbol can then be used in the body like a spread operator (e.g. `{… & r})`."
  [bindings & body]
  (let [binds (partition-all 2 bindings)
        _     (assert (every? #(= 2 (count %)) binds)
                      "binding form must be even yo!")]
    `(let [~@(->> (for [[l r :as b] binds]
                    (if (and (map? l)
                             (l :rest)
                             (or (l :keys) (l :syms) (l :strs)))
                      (let [ks        (l :keys)
                            strs      (l :strs)
                            syms      (l :syms)
                            remaining (l :rest)
                            selected  (concat (map keyword ks)
                                              (map name strs)
                                              (for [s syms]
                                                `(quote ~s)))
                            symb      (gensym "the-map")]
                        [symb r
                         (dissoc l :rest)  symb
                         remaining        `(dissoc ~symb ~@selected)])
                      b))
                  (reduce (fn [acc xs] (apply conj acc xs)) []))]
       ~@body)))

