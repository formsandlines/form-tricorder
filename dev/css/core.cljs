;; Note: This namespace is supposed to be evaluated with NBB.
;;       It writes its data into files of two types:
;;       - the .edn files will be processed by shadow-css to generate aliases
;;       - the .css files will be included as-is by shadow-css in compilation
(ns css.core
  (:require
   ["fs" :as fs]
   [clojure.pprint :as pprint]
   [css.colors :as colors]
   [css.styles :as styles]
   [css.aliases :as aliases]))


(.writeFileSync fs "dev/css/colors.edn"
                (with-out-str (pprint/pprint colors/output)))

(.writeFileSync fs "dev/css/styles.edn"
                (with-out-str (pprint/pprint styles/output)))

(.writeFileSync fs "dev/css/aliases.edn"
                (with-out-str (pprint/pprint aliases/output)))

(.writeFileSync fs "public/css/specs.css"
                (str colors/css-output
                     "\n\n"
                     styles/css-output))

;; (.writeFileSync fs "public/css/styles.css"
;;                 styles/css-output)

