(ns form-tricorder.events
  (:require
   [refx.alpha :as refx]
   [formform.expr :as expr]
   [formform.io :as io]
   [form-tricorder.utils :as utils]))


(defn make-view [func-id]
  {:func-id func-id})

;; ---- Event handler -------------------------------------------

(refx/reg-event-db
 :initialize-db
 (fn [_ _]
   (let [fml  "(a :M) {@ a (b), {..@ :M, x}, :U} b"
         expr (io/read-expr fml)
         varorder (expr/find-vars expr {:ordered? true})]
     {:input {:formula fml
              :expr expr
              :varorder varorder}
      :views [(make-view :vtable)]
      :split-orientation :cols  ;; :cols | :rows
      :modes {:calc-config nil}})))

(refx/reg-event-db
 :changed-formula
 (fn [db [_ {:keys [next-formula]}]]
   (-> db
       (update :input
               (fn [{:keys [formula expr] :as m}]
                 (if-not (= formula next-formula)
                   (let [next-expr (io/read-expr next-formula)
                         next-varorder
                         (let [vars (expr/find-vars next-expr {:ordered? true})
                               current-varorder (get-in db [:input :varorder])]
                           (if (= (sort current-varorder) vars)
                             current-varorder
                             vars))]
                     (assoc m
                            :formula  next-formula
                            :expr     next-expr
                            :varorder next-varorder))
                   m))))))

(refx/reg-event-db
 :changed-varorder
 (fn [db [_ {:keys [next-varorder]}]]
   (update db :input
           #(if-not (= (:varorder %) next-varorder)
              (assoc % :varorder next-varorder)
              %))))


(refx/reg-event-db
 :views/swap
 (fn [{:keys [views] :as db} _]
   (let [[a b] views]
     (if (nil? b)
       db
       (assoc db :views [b a])))))

(refx/reg-event-db
 :views/set-split-orientation
 (fn [db [_ {:keys [next-orientation]}]]
   {:pre [(#{:rows :cols} next-orientation)]}
   (assoc db :split-orientation next-orientation)))

(refx/reg-event-db
 :views/split
 (fn [{:keys [views] :as db} [_ _]]
   (when (< (count views) 2)
     (update db :views #(conj % (last %))))))

(refx/reg-event-db
 :views/set-func-id
 (fn [db [_ {:keys [next-id view-index]}]]
   (assoc-in db [:views view-index :func-id]
             (if (keyword? next-id) next-id (keyword next-id)))))

(refx/reg-event-db
 :views/remove
 (fn [db [_ {:keys [view-index]}]]
   (update db :views utils/dissocv view-index)))


(refx/reg-event-db
 :update-cache
 (fn [db [_ {:keys [update-fn]}]]
   (update db :cache update-fn)))



