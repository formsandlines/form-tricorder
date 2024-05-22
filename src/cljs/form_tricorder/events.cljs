(ns form-tricorder.events
  (:require
   [clojure.string :as string]
   [clojure.set :as set]
   [instaparse.core :as insta]   
   [re-frame.core :as rf]
   [formform.expr :as expr]
   [formform.io :as io]
   [form-tricorder.effects]
   [form-tricorder.model :refer [func-ids]]
   [form-tricorder.utils :as utils]))


;; Conventions for clarity:
;; - qualify keys with by the app-db entry the event is involved with

;; (rf/reg-event-error-handler)

(defn parse-formula
  [formula]
  (let [result (io/read-expr formula)]
    (if (insta/failure? result)
      (throw (ex-info "Formula parse error."
                      {:formula formula
                       :error (insta/get-failure result)}))
      result)))

(defn conform-varorder
  [varorder expr]
  (let [vars (expr/find-vars expr {:ordered? true})]
    (cond
      (nil? varorder) vars
      (= (sort varorder) vars) varorder
      :else (throw (ex-info "Varorder inconsistent with expression variables."
                            {:varorder varorder
                             :expr-vars vars})))))

(def fetch-search-params
  (rf/->interceptor
   :id     :fetch-search-params
   :before (fn [context]
             (let [s (.. js/window -location -search)
                   search-params ^js (new js/URLSearchParams s)]
               (assoc-in context [:coeffects :search-params]
                         search-params)))))

(rf/reg-event-fx
 :initialize-db
 [fetch-search-params]
 (fn [{:keys [search-params]} _]
   (println search-params)
   (let [formula (or (.get search-params "f") "")
         expr (parse-formula formula)
         varorder (let [s (.get search-params "vars")]
                    (conform-varorder
                     (when s (string/split s ",")) expr))
         orientation (if-let [ori (keyword (.get search-params "layout"))]
                       (if (#{:cols :rows} ori)
                         ori
                         (throw (ex-info "Invalid frame orientation."
                                         {:orientation ori})))
                       :cols)
         views (if-let [views (.get search-params "views")]
                 (vec
                  (for [s (string/split views ",")
                        :let [view (keyword s)]]
                    (if (func-ids view)
                      {:func-id view}
                      (throw (ex-info "Invalid view-function id."
                                      {:view view})))))
                 [{:func-id :vmap}])
         appearance (if-let [app (keyword (.get search-params "theme"))]
                      (if (#{:light :dark} app)
                        app
                        (throw (ex-info "Invalid theme appearance."
                                        {:appearance app})))
                      :light)

         db {:input {:formula formula
                     :expr expr
                     :varorder varorder}
             :frame {:orientation orientation
                     :windows (count views)}
             :views views
             :modes {:calc-config nil}
             :theme {:appearance appearance}
             :cache {:selfi-evolution {:deps #{:expr :varorder}
                                       :val nil}}}]
     ;; (println db)
     {:db db})))


;; (def update-expr
;;   (rf/->interceptor
;;    :id     :update-expr
;;    :before nil
;;    :after  (fn [context]
;;              (let [input (get-in context [:effects :db :input])
;;                    expr  (assoc input :expr (io/read-expr (input :formula)))
;;                    varorder (let [varorder (input :varorder)]
;;                               (if varorder
;;                                 verorder
;;                                 ))]
;;                (assoc-in context [:effects :db :input] input)))))


(defn views->str
  [views]
  (string/join "," (map (comp name :func-id) views)))

(defn varorder->str
  [varorder]
  (string/join "," varorder))


(rf/reg-event-fx
 :input/changed-formula
 (fn [{:keys [db]} [_ {:keys [next-formula]}]]
   (let [db-next
         (-> db
             (update :input
                     ;; ? wrap in interceptor:
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
                         m))))
         formula-next (get-in db-next [:input :formula])
         varorder-next (get-in db-next [:input :varorder])]
     {:db db-next
      :fx [[:dispatch [:cache/invalidate
                       {:has-deps #{:formula :expr :varorder}}]]
           [:set-search-params [["f" formula-next]]]
           [:set-search-params [["vars" (varorder->str varorder-next)]]]]})))

(rf/reg-event-fx
 :input/changed-varorder
 (fn [{:keys [db]} [_ {:keys [next-varorder]}]]
   (let [next-db (update db :input
                         #(if-not (= (:varorder %) next-varorder)
                            (assoc % :varorder next-varorder)
                            %))]
     {:db next-db
      :fx [[:dispatch [:cache/invalidate
                       {:has-deps #{:varorder}}]]
           [:set-search-params [["vars" (varorder->str next-varorder)]]]]})))

(rf/reg-event-fx
 :frame/set-orientation
 (fn [{:keys [db]} [_ {:keys [next-orientation]}]]
   {:pre [(#{:rows :cols} next-orientation)]}
   {:db (assoc-in db [:frame :orientation] next-orientation)
    :fx [[:set-search-params [["layout" (name next-orientation)]]]]}))


(rf/reg-event-fx
 :views/swap
 (fn [{{:keys [views] :as db} :db} _]
   (let [[a b] views
         views-next [b a]]
     {:db (if (nil? b)
            db
            (assoc db :views views-next))
      :fx [[:set-search-params [["views" (views->str views-next)]]]]})))

(rf/reg-event-fx
 :views/split
 (fn [{{:keys [views frame] :as db} :db} _]
   {:pre [(= (count views) (:windows frame))]}
   (when (< (count views) 2)
     (let [views-next (conj views (last views))]
       {:db (-> db
                (update-in [:frame :windows] inc)
                (assoc :views views-next))
        :fx [[:set-search-params [["views" (views->str views-next)]]]]}))))

(rf/reg-event-fx
 :views/set-func-id
 (fn [{{:keys [views] :as db} :db} [_ {:keys [next-id view-index]}]]
   (let [views-next (assoc-in views [view-index :func-id]
                              (if (keyword? next-id)
                                next-id
                                (keyword next-id)))]
     {:db (assoc db :views views-next)
      :fx [[:set-search-params [["views" (views->str views-next)]]]]})))

(rf/reg-event-fx
 :views/remove
 (fn [{{:keys [views frame] :as db} :db} [_ {:keys [view-index]}]]
   {:pre [(= (count views) (:windows frame))]}
   (let [views-next (utils/dissocv views view-index)]
     {:db (-> db
              (update-in [:frame :windows] dec)
              (assoc :views views-next))
      :fx [[:set-search-params [["views" (views->str views-next)]]]]})))


(rf/reg-event-fx
 :theme/set-appearance
 (fn [{db :db} [_ {:keys [next-appearance]}]]
   {:db (assoc-in db [:theme :appearance] next-appearance)
    :fx [[:set-search-params [["theme" (name next-appearance)]]]]}))


;; NOTE: a proposed feature of re-frame called “flows” might make the need
;; for manual caching obsolete, see https://day8.github.io/re-frame/Flows/

;; ? where is this used or throw away
(rf/reg-event-db
 :cache/update
 (fn [db [_ {:keys [key update-fn]}]]
   (js/console.log (str "Caching: " key))
   (update-in db [:cache key :val] update-fn)))

(rf/reg-event-db
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

