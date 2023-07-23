(ns form-tricorder.events
  (:require
    [refx.alpha :as refx]
    [formform.expr :as expr]
    [formform.io :as io]))


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
      :views [{:func-id :vtable}
              {:func-id :vmap}]
      :view-orientation "Horizontal"
      :view-split? true
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
     (assoc db :views [b a]))))

(refx/reg-event-db
 :views/change-split
 (fn [db [_ {:keys [split?]}]]
   {:pre [(boolean? split?)]}
   (-> db
       (assoc :view-split? split?))))

(refx/reg-event-db
 :views/change-orientation
 (fn [db [_ {:keys [next-orientation]}]]
   {:pre [(#{"Horizontal" "Vertical"} next-orientation)]}
   (-> db
       (assoc :view-orientation next-orientation))))


(refx/reg-event-db
 :set-func-id
 (fn [db [_ {:keys [next-id view-id]}]]
   (println next-id " " view-id)
   (assoc-in db [:views view-id :func-id]
             (if (keyword? next-id) next-id (keyword next-id)))))

(refx/reg-event-db
 :update-cache
 (fn [db [_ {:keys [update-fn]}]]
   (update db :cache update-fn)))

