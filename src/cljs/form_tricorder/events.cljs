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

(rf/reg-event-error-handler
 (fn [original-error re-frame-error]
   (println "Original Error:")
   (println original-error)
   (println "Re-frame Error:")
   (println (ex-data re-frame-error))
   (rf/dispatch [:error/set {:error original-error}])))

(def clear-errors
  (rf/->interceptor
   :id :clear-errors
   :after (fn [context]
            (assoc-in context [:effects :dispatch] [:error/clear]))))

;; (rf/reg-global-interceptor clear-errors)

(defn balanced?
  [s]
  (let [step (fn [stack c]
               (cond
                 (= c \() (conj stack c)
                 (= c \)) (if (empty? stack)
                            (reduced false)
                            (pop stack))
                 :else stack))]
    (empty? (reduce step [] s))))

(defn pre-parse-formula
  [formula]
  (when-not (balanced? formula)
    (throw (ex-info "Unbalanced expression!" {}))))

(defn parse-formula
  [formula]
  (pre-parse-formula formula)
  (let [result (io/read-expr formula)]
    (if (insta/failure? result)
      (throw (ex-info "Formula parse error."
                      {:formula formula
                       :error (insta/get-failure result)}))
      result)))

(defn conform-varorder
  [varorder expr]
  (let [vars (-> expr (expr/find-vars {}) utils/sort-varorder)]
    (cond
      (nil? varorder) vars
      ;; ? use sets to compare
      (= (utils/sort-varorder varorder) vars) varorder
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

(def default-db
  {:input {:formula ""
           :expr nil
           :varorder [""]}
   :frame {:orientation :cols
           :windows 1}
   :views [{:func-id :graphs}]
   :modes {:expr {:graph-style :basic}
           :eval nil
           :emul nil}
   :theme {:appearance :system}
   :cache {:selfi-evolution {:deps #{:expr :varorder}
                             :val nil}}
   :error nil})

(rf/reg-event-fx
 :initialize-db
 [fetch-search-params]
 (fn [{:keys [search-params]} _]
   ;; (println search-params)
   (try (let [formula (or (.get search-params "f")
                          (get-in default-db [:input :formula]))
              expr (parse-formula formula)
              varorder (let [s (.get search-params "vars")]
                         (conform-varorder
                          (when (seq s) (string/split s ",")) expr))
              orientation (if-let [ori (keyword (.get search-params "layout"))]
                            (if (#{:cols :rows} ori)
                              ori
                              (throw (ex-info "Invalid frame orientation."
                                              {:orientation ori})))
                            (get-in default-db [:frame :orientation]))
              views (if-let [views (.get search-params "views")]
                      (vec
                       (for [s (string/split views ",")
                             :let [view (keyword s)]]
                         (if (func-ids view)
                           {:func-id view}
                           (throw (ex-info "Invalid view-function id."
                                           {:view view})))))
                      (get default-db :views))
              modes (if-let [graph-style (keyword
                                          (.get search-params "graph-style"))]
                      (if (#{:basic :gestalt} graph-style)
                        {:expr {:graph-style graph-style}}
                        (throw (ex-info "Invalid graph-style."
                                        {:graph-style graph-style}))))
              appearance (if-let [app (keyword (.get search-params "theme"))]
                           (if (#{:light :dark :system} app)
                             app
                             (throw (ex-info "Invalid theme appearance."
                                             {:appearance app})))
                           (get-in default-db [:theme :appearance]))

              db {:input {:formula formula
                          :expr expr
                          :varorder varorder}
                  :frame {:orientation orientation
                          :windows (count views)}
                  :views views
                  :modes (merge (get default-db :modes) modes)
                  :theme {:appearance appearance}
                  :cache (get default-db :cache)
                  :error (get default-db :error)}]
          {:db db})
        (catch js/Error e
          ;; in case of error, revert to blank config to prevent crash
          {:db (assoc default-db
                      :error (ex-message e))}))))

(defn views->str
  [views]
  (string/join "," (map (comp name :func-id) views)))

(defn varorder->str
  [varorder]
  (string/join "," varorder))

(rf/reg-event-fx
 :copy-stateful-link
 (fn [{:keys [db]} [_ {:keys [report-copy-status]}]]
   (let [views (get db :views)
         appearance (get-in db [:theme :appearance])
         orientation (get-in db [:frame :orientation])
         graph-style (get-in db [:modes :expr :graph-style])
         formula (get-in db [:input :formula])
         varorder (get-in db [:input :varorder])]
     {:fx [[:set-search-params [["views" (views->str views)]]]
           [:set-search-params [["layout" (name orientation)]]]
           [:set-search-params [["theme" (name appearance)]]]
           [:set-search-params [["graph-style" (name graph-style)]]]
           [:set-search-params [["f" formula]]]
           [:set-search-params [["vars" (varorder->str varorder)]]]
           [:copy-url report-copy-status]]})))

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

(rf/reg-event-fx
 :input/changed-formula
 [clear-errors]
 (fn [{:keys [db]} [_ {:keys [next-formula set-search-params?]}]]
   (let [db-next
         (-> db
             (update :input
                     ;; ? wrap in interceptor:
                     (fn [{:keys [formula expr] :as m}]
                       (if-not (= formula next-formula)
                         (let [next-expr (parse-formula next-formula)
                               next-varorder
                               (let [vars (-> next-expr
                                              (expr/find-vars {})
                                              utils/sort-varorder)
                                     current-varorder (get-in
                                                       db [:input :varorder])]
                                 (if (= (utils/sort-varorder current-varorder)
                                        vars)
                                   current-varorder
                                   vars))]
                           (assoc m
                                  :formula  next-formula
                                  :expr     next-expr
                                  :varorder next-varorder))
                         m))))
         ;; formula-next (get-in db-next [:input :formula])
         ;; varorder-next (get-in db-next [:input :varorder])
         ]
     {:db db-next
      :fx (into [[:dispatch [:cache/invalidate
                             {:has-deps #{:formula :expr :varorder}}]]]
                ;; (when set-search-params?
                ;;   [[:set-search-params [["f" formula-next]]]
                ;;    [:set-search-params [["vars"
                ;;                          (varorder->str varorder-next)]]]])
                )})))

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
           ;; [:set-search-params [["vars" (varorder->str next-varorder)]]]
           ]})))

(rf/reg-event-fx
 :frame/set-orientation
 (fn [{:keys [db]} [_ {:keys [next-orientation]}]]
   {:pre [(#{:rows :cols} next-orientation)]}
   {:db (assoc-in db [:frame :orientation] next-orientation)
    ;; :fx [[:set-search-params [["layout" (name next-orientation)]]]]
    }))


(rf/reg-event-fx
 :views/swap
 (fn [{{:keys [views] :as db} :db} _]
   (let [[a b] views
         views-next [b a]]
     {:db (if (nil? b)
            db
            (assoc db :views views-next))
      ;; :fx [[:set-search-params [["views" (views->str views-next)]]]]
      })))

(rf/reg-event-fx
 :views/split
 (fn [{{:keys [views frame] :as db} :db} _]
   {:pre [(= (count views) (:windows frame))]}
   (when (< (count views) 2)
     (let [views-next (conj views (last views))]
       {:db (-> db
                (update-in [:frame :windows] inc)
                (assoc :views views-next))
        ;; :fx [[:set-search-params [["views" (views->str views-next)]]]]
        }))))

(rf/reg-event-fx
 :views/set-func-id
 (fn [{{:keys [views] :as db} :db} [_ {:keys [next-id view-index]}]]
   (let [views-next (assoc-in views [view-index :func-id]
                              (if (keyword? next-id)
                                next-id
                                (keyword next-id)))]
     {:db (assoc db :views views-next)
      ;; :fx [[:set-search-params [["views" (views->str views-next)]]]]
      })))

(rf/reg-event-fx
 :views/remove
 (fn [{{:keys [views frame] :as db} :db} [_ {:keys [view-index]}]]
   {:pre [(= (count views) (:windows frame))]}
   (let [views-next (utils/dissocv views view-index)]
     {:db (-> db
              (update-in [:frame :windows] dec)
              (assoc :views views-next))
      ;; :fx [[:set-search-params [["views" (views->str views-next)]]]]
      })))


(rf/reg-event-fx
 :theme/set-appearance
 (fn [{db :db} [_ {:keys [next-appearance]}]]
   {:db (assoc-in db [:theme :appearance] next-appearance)
    ;; :fx [[:set-search-params [["theme" (name next-appearance)]]]]
    }))

(rf/reg-event-fx
 :modes/set-graph-style
 (fn [{db :db} [_ {:keys [next-graph-style]}]]
   {:db (assoc-in db [:modes :expr :graph-style] next-graph-style)}))


;; NOTE: a proposed feature of re-frame called “flows” might make the need
;; for manual caching obsolete, see https://day8.github.io/re-frame/Flows/

;; ? where is this used or throw away
(rf/reg-event-db
 :cache/update
 (fn [db [_ {:keys [key update-fn]}]]
   ;; (js/console.log (str "Caching: " key))
   (update-in db [:cache key :val] update-fn)))

(rf/reg-event-db
 :cache/invalidate
 (fn [db [_ {:keys [has-deps]}]]
   ;; (js/console.log (str "Invalidating: " has-deps))
   (update db :cache
           (fn [cache]
             (update-vals cache
                          (fn [{:keys [deps] :as m}]
                            (if (empty? (set/intersection deps has-deps))
                              m
                              (assoc m :val nil))))))))


(rf/reg-event-db
 :error/set
 (fn [db [_ {:keys [error]}]]
   ;; (js/console.log "Setting error…")
   (assoc db :error error)))

(rf/reg-event-db
 :error/clear
 (fn [db _]
   ;; (js/console.log "Clearing error…")
   (assoc db :error nil)))
