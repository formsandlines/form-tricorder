(ns form-tricorder.events
  (:require
   [refx.alpha :as refx]
   [formform.expr :as expr]
   [formform.io :as io]
   [form-tricorder.utils :as utils]))


;; ---- Event handler -------------------------------------------

(refx/reg-event-db
 :initialize-db
 (fn [_ _]
   (let [fml  "{L,E,R}{R,E,L}{L,R,E}" ; "(a :M) {@ a (b), {..@ :M, x}, :U} b"
         expr (io/read-expr fml)
         varorder ["L" "E" "R"] ; (expr/find-vars expr {:ordered? true})
         ]
     {:input {:formula fml
              :expr expr
              :varorder varorder}
      :frame {:orientation :cols  ;; :cols | :rows
              :windows 2} ;; 1 or 2
      :views [{:func-id :selfi} {:func-id :vtable}] ;; 1 or 2 of functions
      :modes {:calc-config nil}
      :theme {:appearance :light}  ;; :dark | :light
      })))


(refx/reg-event-db
 :input/changed-formula
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
 :input/changed-varorder
 (fn [db [_ {:keys [next-varorder]}]]
   (update db :input
           #(if-not (= (:varorder %) next-varorder)
              (assoc % :varorder next-varorder)
              %))))


(refx/reg-event-db
 :frame/set-orientation
 (fn [db [_ {:keys [next-orientation]}]]
   {:pre [(#{:rows :cols} next-orientation)]}
   (assoc-in db [:frame :orientation] next-orientation)))


(refx/reg-event-db
 :views/swap
 (fn [{:keys [views] :as db} _]
   (let [[a b] views]
     (if (nil? b)
       db
       (assoc db :views [b a])))))

(refx/reg-event-db
 :views/split
 (fn [{:keys [views frame] :as db} [_ _]]
   {:pre [(= (count views) (:windows frame))]}
   (when (< (count views) 2)
     (-> db
         (update-in [:frame :windows] inc)
         (update :views #(conj % (last %)))))))

(refx/reg-event-db
 :views/set-func-id
 (fn [db [_ {:keys [next-id view-index]}]]
   (assoc-in db [:views view-index :func-id]
             (if (keyword? next-id) next-id (keyword next-id)))))

(refx/reg-event-db
 :views/remove
 (fn [{:keys [views frame] :as db} [_ {:keys [view-index]}]]
   {:pre [(= (count views) (:windows frame))]}
   (-> db
       (update-in [:frame :windows] dec)
       (update :views utils/dissocv view-index))))


(refx/reg-event-db
 :theme/set-appearance
 (fn [db [_ {:keys [next-appearance]}]]
   (assoc-in db [:theme :appearance] next-appearance)))


;; ? where is this used or throw away
(refx/reg-event-db
 :update-cache
 (fn [db [_ {:keys [update-fn]}]]
   (update db :cache update-fn)))

