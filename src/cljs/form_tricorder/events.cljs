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

(defn parse-vals-filter [s]
  (let [vals-filter (set (map (comp keyword string/upper-case str)
                              s))]
    (when (set/subset? vals-filter utils/consts-set)
      vals-filter)))

(defn parse-terms-filter [s term-count]
  (let [terms-filter (if (string/blank? s)
                       []
                       (mapv parse-vals-filter (string/split s ",")))]
    (when (and (= (count terms-filter) term-count)
               (every? set? terms-filter))
      terms-filter)))

(defn parse-interpr-filter-params
  "Parses query-params for interpretation filter.
  - example param: `interpr-filter=not;intersects;nu;n,uin,mnui`"
  [query-param-s term-count]
  ;; (println query-param-s)
  (let [[neg-op?-s op-s terms-filter-s vals-filter-s]
        (string/split query-param-s ";")
        neg-op? (case neg-op?-s
                  "not" true
                  ""    false
                  (throw (ex-info "Invalid interpretation filter value."
                                  {:neg-op? neg-op?-s})))
        op (let [op (keyword op-s)]
             (if (#{:intersects :subseteq :equal} op)
               op
               (throw (ex-info "Invalid interpretation filter value."
                               {:op op}))))
        vals-filter (if-let [vals-filter (parse-vals-filter vals-filter-s)]
                      vals-filter
                      (throw (ex-info "Invalid interpretation filter value."
                                      {:vals-filter vals-filter-s})))
        terms-filter (if-let [terms-filter
                              (parse-terms-filter terms-filter-s term-count)]
                       terms-filter
                       (throw (ex-info "Invalid interpretation filter value."
                                       {:terms-filter terms-filter-s})))]
    {:vals-filter vals-filter
     :terms-filter terms-filter
     :neg-op? neg-op?
     :op op}))

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

(defn reset-terms-filter
  [varorder]
  (vec (repeat (count varorder) utils/consts-set)))

(def default-db
  {:input {:formula ""
           :expr nil
           :varorder [""]}
   :frame {:orientation :cols
           :windows 1}
   :views [{:func-id :graphs}]
   :modes {:expr {:graph-style :basic}
           :eval {:interpr-filter {:vals-filter utils/consts-set
                                   :terms-filter [] ;; e.g. [#{:N :U} #{:I} #{}]
                                   :neg-op? false
                                   :op :intersects} ;; | :subseteq | equal
                  :results-filter utils/consts-set}
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

              orientation (if-let [ori-s (.get search-params "layout")]
                            (let [ori (keyword ori-s)]
                              (if (#{:cols :rows} ori)
                                ori
                                (throw (ex-info "Invalid frame orientation."
                                                {:orientation ori}))))
                            (get-in default-db [:frame :orientation]))

              views (if-let [views-s (.get search-params "views")]
                      (vec
                       (for [s (string/split views-s ",")
                             :let [view (keyword s)]]
                         (if (func-ids view)
                           {:func-id view}
                           (throw (ex-info "Invalid view-function id."
                                           {:view view})))))
                      (get default-db :views))

              graph-style (when-let [graph-style (keyword (.get search-params
                                                                "graph-style"))]
                            (if (#{:basic :gestalt} graph-style)
                              graph-style
                              (throw (ex-info "Invalid graph-style."
                                              {:graph-style graph-style}))))

              modes-expr (utils/merge-some
                          (get-in default-db [:modes :expr])
                          {:graph-style graph-style})

              interpr-filter
              (utils/merge-some
               (get-in default-db [:modes :eval :interpr-filter])
               (if-let [interpr-filter-s (.get search-params "interpr-filter")]
                 (parse-interpr-filter-params interpr-filter-s (count varorder))
                 {:terms-filter (reset-terms-filter varorder)}))

              results-filter
              (if-let [results-filter-s (.get search-params "results-filter")]
                (let [results-filter (parse-vals-filter results-filter-s)]
                  (if (and (set? results-filter)
                           (set/subset? results-filter utils/consts-set))
                    results-filter
                    (throw (ex-info "Invalid results filter."
                                    {:results-filter results-filter}))))
                (get-in default-db [:modes :eval :results-filter]))

              modes-eval (utils/merge-some
                          (get-in default-db [:modes :eval])
                          {:results-filter results-filter
                           :interpr-filter interpr-filter})

              appearance (if-let [app-s (.get search-params "theme")]
                           (let [app (keyword app-s)]
                             (if (#{:light :dark :system} app)
                               app
                               (throw (ex-info "Invalid theme appearance."
                                               {:appearance app}))))
                           (get-in default-db [:theme :appearance]))

              db {:input {:formula formula
                          :expr expr
                          :varorder varorder}
                  :frame {:orientation orientation
                          :windows (count views)}
                  :views views
                  :modes {:expr modes-expr
                          :eval modes-eval}
                  :theme {:appearance appearance}
                  :cache (get default-db :cache)
                  :error (get default-db :error)}]
          ;; (println (get-in db [:modes :eval]))
          {:db db})
        (catch js/Error e
          ;; in case of error, revert to blank config to prevent crash
          {:db (assoc default-db
                      :error e)}))))

(defn views->str
  [views]
  (string/join "," (map (comp name :func-id) views)))

(defn varorder->str
  [varorder]
  (string/join "," varorder))

(defn vals-filter->str
  [vals-filter]
  (string/join "" (map utils/pp-val vals-filter)))

(defn interpr-filter->str
  [{:keys [neg-op? op terms-filter vals-filter]}]
  (let [neg-op?-s (if neg-op? "not" "")
        op-s (name op)
        terms-filter-s (string/join "," (map vals-filter->str terms-filter))
        vals-filter-s (vals-filter->str vals-filter)]
    (string/join ";" [neg-op?-s op-s terms-filter-s vals-filter-s] )))

(rf/reg-event-fx
 :copy-stateful-link
 (fn [{:keys [db]} [_ {:keys [report-copy-status]}]]
   (let [views (get db :views)
         appearance (get-in db [:theme :appearance])
         orientation (get-in db [:frame :orientation])
         graph-style (get-in db [:modes :expr :graph-style])
         formula (get-in db [:input :formula])
         varorder (get-in db [:input :varorder])
         interpr-filter (get-in db [:modes :eval :interpr-filter])
         results-filter (get-in db [:modes :eval :results-filter])]
     {:fx [[:set-search-params [["views" (views->str views)]]]
           [:set-search-params [["layout" (name orientation)]]]
           [:set-search-params [["theme" (name appearance)]]]
           [:set-search-params [["graph-style" (name graph-style)]]]
           [:set-search-params [["f" formula]]]
           [:set-search-params [["vars" (varorder->str varorder)]]]
           [:set-search-params [["interpr-filter" (interpr-filter->str
                                                   interpr-filter)]]]
           [:set-search-params [["results-filter" (vals-filter->str
                                                   results-filter)]]]
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
                     (fn [{:keys [formula] :as m}]
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
                                  :varorder (vec next-varorder)))
                         m))))
         db-next (let [varorder (get-in db-next [:input :varorder])]
                   (if varorder
                     (assoc-in db-next
                               [:modes :eval :interpr-filter :terms-filter]
                               (reset-terms-filter varorder))
                     db-next))
         ;; formula-next (get-in db-next [:input :formula])
         ;; varorder-next (get-in db-next [:input :varorder])
         ]
     ;; (println db-next)
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

(rf/reg-event-fx
 :modes/set-interpr-filter
 (fn [{db :db} [_ {:keys [next-interpr-filter]}]]
   {:db (assoc-in db [:modes :eval :interpr-filter] next-interpr-filter)}))

(rf/reg-event-fx
 :modes/set-results-filter
 (fn [{db :db} [_ {:keys [next-results-filter]}]]
   {:db (assoc-in db [:modes :eval :results-filter] next-results-filter)}))

(rf/reg-event-fx
 :modes/reset-interpr-filter
 (fn [{db :db} [_ _]]
   (let [terms-filter (reset-terms-filter (get-in db [:input :varorder]))]
     {:db (assoc-in db [:modes :eval :interpr-filter]
                    (assoc (get-in default-db [:modes :eval :interpr-filter])
                           :terms-filter terms-filter))})))

(rf/reg-event-fx
 :modes/reset-results-filter
 (fn [{db :db} [_ _]]
   {:db (assoc-in db [:modes :eval :results-filter]
                  (get-in default-db [:modes :eval :results-filter]))}))


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
