(ns form-tricorder.processor
  (:require
   [formform.calc :as calc]
   [formform.expr :as expr]
   [formform.io :as io]))


(defmulti process-visu (fn [_ func-id] func-id))
(defmulti process-calc (fn [_ func-id] func-id))
(defmulti process-emul (fn [_ func-id] func-id))


(defmethod process-visu :default [_ func-id]
  (ex-info "Invalid `visu` function." {:func-id func-id}))

(defmethod process-visu :hooks
  [expr _]
  expr)


(defmethod process-calc :default [_ func-id]
  (ex-info "Invalid `calc` function." {:func-id func-id}))

(defmethod process-calc :vtable [expr _]
  (expr/eval-all expr))


(defmethod process-emul :default [_ func-id]
  (ex-info "Invalid `emul` function." {:func-id func-id}))


(defn process
  [input {:keys [mode-id func-id]}]
  (let [[mode-id func-id] [(keyword mode-id) (keyword func-id)]
        expr (io/read-expr input)
        result (case mode-id
                 :visu (process-visu expr func-id)
                 :calc (process-calc expr func-id)
                 :emul (process-emul expr func-id)
                 (throw (ex-info "Invalid mode." {:mode-id mode-id})))]
    (str result)))
