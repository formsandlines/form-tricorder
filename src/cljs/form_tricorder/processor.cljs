(ns form-tricorder.processor
  (:require
   [form-tricorder.functions :as func]
   [helix.core :refer [$ <> fnc]]
   [helix.dom :as d :refer [$d]]
   [formform.calc :as calc]
   [formform.expr :as expr]
   [formform.io :as io]))


(defmulti process-visu (fn [func-id _] func-id))
(defmulti process-calc (fn [func-id _] func-id))
(defmulti process-emul (fn [func-id _] func-id))

(defn make-Exn [s]
  (fnc [] (d/div (str s))))

(defmethod process-visu :default [func-id _]
  (make-Exn (ex-info "Invalid `visu` function." {:func-id func-id})))

#_(defmethod process-visu :hooks
  [_ expr]
  expr)


(defmethod process-calc :default [func-id _]
  (make-Exn (ex-info "Invalid `calc` function." {:func-id func-id})))

(defmethod process-calc :vtable [func-id expr]
  (func/gen-component func-id (expr/eval-all expr)))

(defmethod process-calc :vmap [func-id expr]
  (let [vmap (-> expr
                 (expr/=>*)
                 (expr/op-get :dna)
                 (calc/dna->vdict {})
                 (calc/vdict->vmap))]
    (func/gen-component func-id vmap)))


(defmethod process-emul :default [func-id _]
  (make-Exn (ex-info "Invalid `emul` function." {:func-id func-id})))


(defn process
  [input {:keys [mode-id func-id]}]
  (let [[mode-id func-id] [(keyword mode-id) (keyword func-id)]
        expr (io/read-expr input)
        result (case mode-id
                 :visu (process-visu func-id expr)
                 :calc (process-calc func-id expr)
                 :emul (process-emul func-id expr)
                 (throw (ex-info "Invalid mode." {:mode-id mode-id})))]
    result))
