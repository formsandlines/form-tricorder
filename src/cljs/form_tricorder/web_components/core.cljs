(ns form-tricorder.web-components.core
  (:require
   ;; [zero.core :as z]
   [zero.config :as zc]
   [clojure.edn :as edn]
   [form-tricorder.web-components.vmap]
   [form-tricorder.web-components.vtable]
   [form-tricorder.web-components.fgraph]
   [form-tricorder.web-components.selfi]))

(defn attr-reader [attr-str-value _ _]
  (edn/read-string attr-str-value))

(zc/reg-attr-readers :ff/* attr-reader)
