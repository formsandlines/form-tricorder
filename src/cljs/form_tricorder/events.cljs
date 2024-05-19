(ns form-tricorder.events
  (:require
   [clojure.string :as string]
   [clojure.set :as set]
   [refx.alpha :as refx]
   ["react-router-dom" :refer (useSearchParams)]
   [formform.expr :as expr]
   [formform.io :as io]
   [form-tricorder.utils :as utils]))


;; Conventions for clarity:
;; - qualify keys with by the app-db entry the event is involved with

(refx/reg-event-db
 :initialize-db
 (fn [_ _]
   ;; (let [[search-params set-search-params] (useSearchParams)
   ;;       [formula
   ;;        varorder
   ;;        orientation
   ;;        windows
   ;;        views
   ;;        theme] (mapv (partial (.get search-params))
   ;;                     ["f" "var" "orient" "win" "views" "theme"])
   ;;       formula (if formula formula "")
   ;;       expr (io/read-expr formula)
   ;;       views (mapv #(into {} [:func-id (keyword %)])
   ;;                   (if views
   ;;                     (string/split views ",")
   ;;                     ["graphs"]))]
   ;;   {:input {:formula formula
   ;;            :expr expr
   ;;            :varorder varorder}
   ;;    :frame {:orientation (if (#{"cols" "rows"} orientation)
   ;;                           (keyword orientation) :cols)
   ;;            :windows (case windows
   ;;                       "2" 2
   ;;                       ("1" nil) 1
   ;;                       (throw (ex-info "Incorrect number of windows."
   ;;                                       {:windows windows})))}
   ;;    :views views
   ;;    :modes {:calc-config nil}
   ;;    :theme {:appearance :light}  ;; :dark | :light
   ;;    }
   ;;   )
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
      :cache {:selfi-evolution {:deps #{:expr :varorder}
                                :val nil}}
      })
   ))


(refx/reg-event-fx
 :input/changed-formula
 (fn [{:keys [db]} [_ {:keys [next-formula]}]]
   (let [next-db
         (-> db
             (update :input
                     ;; wrap in interceptor:
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
                         m))))]
     {:db next-db
      :fx [[:dispatch [:cache/invalidate
                       {:has-deps #{:formula :expr :varorder}}]]]})))

(refx/reg-event-fx
 :input/changed-varorder
 (fn [{:keys [db]} [_ {:keys [next-varorder]}]]
   (let [next-db (update db :input
                         #(if-not (= (:varorder %) next-varorder)
                            (assoc % :varorder next-varorder)
                            %))]
     {:db next-db
      :fx [[:dispatch [:cache/invalidate
                       {:has-deps #{:varorder}}]]]})))


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
 :cache/update
 (fn [db [_ {:keys [key update-fn]}]]
   (js/console.log (str "Caching: " key))
   (update-in db [:cache key :val] update-fn)))

(refx/reg-event-db
 :cache/invalidate
 (fn [db [_ {:keys [has-deps]}]]
   (js/console.log (str "Invalidating: " has-deps))
   (update db :cache
           (fn [cache]
             (update-vals cache
                          (fn [{:keys [deps] :as m}]
                            (if (empty? (set/intersection deps has-deps))
                              m
                              (assoc m :val nil))))))))

