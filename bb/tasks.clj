(ns tasks
  (:require [zprint.core :as zp]
            [babashka.tasks :as tasks]))

(defn css-repl
  []
  (tasks/run 'css-defs)
  ;; pretty-print edn files, sorting map keys
  (let [path "dev/css/"
        files ["colors.edn" "styles.edn" "aliases.edn"]
        ;; for some keys I want an explicit ordering sequence
        ;; especially since :1,:10,:2 should be :1 :2 … :10
        key-order (concat
                   (for [prefix ["" "n" "m" "e" "fx" "fv" "fe"]
                         n (range 32)]
                     (keyword (str prefix n)))
                   [:sand :night :coral :seafoam :lavender :mauve
                    :light :dark
                    :semantic])]
    (doseq [fname files]
      (zp/zprint-file (str path fname) fname
                      (str path fname)
                      {:map {:sort? true
                             :key-order key-order}}))))
