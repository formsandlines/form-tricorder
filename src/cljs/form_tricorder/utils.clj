(ns form-tricorder.utils)

(defmacro let+ [bindings & body]
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

