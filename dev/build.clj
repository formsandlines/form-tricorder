(ns build
  (:require
   [shadow.css.build :as cb]
   [clojure.java.io :as io]
   [clojure.edn :as edn]))

;; To manually run the build:
;; > clj -X:build-css

;; The watch command is configured via .dir-locals in Emacs/Cider

(defn load-preflight-from-classpath
  "Replaces `shadow.css.build/load-preflight-from-classpath`"
  [build-state]
  (assoc build-state
         :preflight-src
         (slurp (io/resource "css/normalize.css"))))

(defn load-default-aliases-from-classpath
  "Replaces `shadow.css.build/load-default-aliases-from-classpath`"
  [build-state]
  (update build-state :aliases cb/merge-left
          (edn/read-string (slurp (io/resource "css/aliases.edn")))))

(defn start
  "Replaces `shadow.css.build/start`"
  ([]
   (start (cb/init)))
  ([build-state]
   (-> build-state
       (load-preflight-from-classpath)
       (load-default-aliases-from-classpath)
       ;; (cb/load-colors-from-classpath)
       (cb/load-indexes-from-classpath)
       ;; (cb/generate-color-aliases)
       ;; (cb/generate-spacing-aliases)
       )))

(defn css-release [& args]
  (let [build-state
        (-> (start)
            ;; (update :aliases merge aliases)
            (cb/index-path (io/file "src" "cljs" "form_tricorder") {})
            (cb/generate
             '{:ui
               {:entries [form-tricorder.core]}})
            (cb/minify)
            (cb/write-outputs-to (io/file "public" "css")))]

    (doseq [mod (:outputs build-state)
            {:keys [warning-type] :as warning} (:warnings mod)]
      (prn [:CSS (name warning-type) (dissoc warning :warning-type)]))))

