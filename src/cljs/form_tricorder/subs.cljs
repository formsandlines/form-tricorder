(ns form-tricorder.subs
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [form-tricorder.utils :as utils]
   [formform.calc :as calc]
   [formform.expr :as expr]
   [formform.emul :as emul]
   [formform.io :as io]
   [form-tricorder.events :refer [default-db reset-terms-filter]]
   [re-frame.core :as rf]))


;; Conventions for clarity:
;; - qualify keys with by the app-db entry the sub depends on
;; - prepend `->` to sub key when the sub computes derived data
;; - use `report-error` instead of throwing the exception

(defn report-error
  [err]
  (rf/dispatch [:error/set {:error err}]))

(rf/reg-sub
 :views/->view
 (fn [db [_ index]]
   ;; {:pre [(< index (count (:views db)))]}
   ;; fails silently (sometimes views update doesn’t catch on after remove)
   (get (:views db) index)))


(rf/reg-sub
 :frame/orientation
 (fn [db _]
   (get-in db [:frame :orientation])))

(rf/reg-sub
 :frame/windows
 (fn [db _]
   (get-in db [:frame :windows])))


(rf/reg-sub
 :theme/appearance
 (fn [db _]
   (get-in db [:theme :appearance])))

(rf/reg-sub
 :modes/graph-style
 (fn [db _]
   (get-in db [:modes :expr :graph-style])))

(rf/reg-sub
 :modes/interpr-filter
 (fn [db _]
   (get-in db [:modes :eval :interpr-filter])))

(rf/reg-sub
 :modes/results-filter
 (fn [db _]
   (get-in db [:modes :eval :results-filter])))


(rf/reg-sub
 :cache/retrieve
 (fn [db [_ key]]
   (get-in db [:cache key :val])))


(rf/reg-sub
 :error/get
 (fn [db _]
   (get db :error)))


(rf/reg-sub
 :input/formula
 (fn [db _]
   (get-in db [:input :formula] :not-found)))

(rf/reg-sub
 :input/expr
 (fn [db _]
   (get-in db [:input :expr] :not-found)))

(rf/reg-sub
 :input/varorder
 (fn [db _]
   (get-in db [:input :varorder] nil)))

(rf/reg-sub
 :input/->expr-data
 (fn [db _]
   (let [input (:input db)]
     [(get input :expr :not-found)
      (get input :varorder nil)])))

(rf/reg-sub
 :input/->expr-json ;; legacy format → remove later
 :<- [:input/expr]
 (fn [expr _]
   (clj->js (try (io/uniform-expr {:legacy? true} expr)
                 (catch js/Error e
                   (report-error e))))))

(rf/reg-sub
 :input/->sorted-varorder
 :<- [:input/->expr-data]
 (fn [[_ varorder] _]
   ;; (println "sorting: " varorder)
   (utils/sort-varorder varorder)))

(rf/reg-sub
 :input/->varorder-permutations
 :<- [:input/->sorted-varorder]
 (fn [sorted-varorder _]
   ;; (println "computing permutations")
   (let [permutations (try (expr/permute-vars sorted-varorder)
                           (catch js/Error e
                             (report-error e)))]
     permutations)))


;; ? maybe replace with :dna sub
(rf/reg-sub
 :input/->value
 :<- [:input/->expr-data]
 (fn [[expr varorder] _]
   (when (= :not-found expr)
     (report-error (ex-info "Expression data missing!" {})))
   (when (nil? varorder)
     (report-error (ex-info "Unknown variable ordering!" {})))
   (let [value (try (expr/eval-all {:varorder varorder} expr {})
                    (catch js/Error e
                      (report-error e)))]
     ;; (println "computing value")
     (:results value))))

;; ! replace :input/->value with this
(rf/reg-sub
 :input/->results
 :<- [:input/->expr-data]
 (fn [[expr varorder] _]
   (when (= :not-found expr)
     (report-error (ex-info "Expression data missing!" {})))
   (when (nil? varorder)
     (report-error (ex-info "Unknown variable ordering!" {})))
   (let [value (try (expr/eval-all {:varorder varorder} expr {})
                    (catch js/Error e
                      (report-error e)))]
     (:results value))))

(defn make-interpr-filter
  [{:keys [vals-filter terms-filter neg-op? op] :as interpr-filter}]
  (if (and interpr-filter (seq terms-filter))
    (fn [interpr]
      (let [interpr (set (mapv #(%1 %2) terms-filter interpr))]
        (if (contains? interpr nil)
          false
          (case [neg-op? op]
            ;; OR
            [false :intersects] (not= #{}
                                      (set/intersection vals-filter interpr))
            ;; NOT OR
            [true :intersects] (= #{} (set/intersection vals-filter interpr))
            ;; AND
            [false :subseteq] (set/subset? vals-filter interpr)
            ;; NOT AND
            [true :subseteq] (not (set/subset? vals-filter interpr))
            ;; EQUAL
            [false :equal] (= vals-filter interpr)
            ;; NOT EQUAL
            [true :equal] (not= vals-filter interpr)
            (throw (ex-info "Invalid filter operation!"
                            {:filter interpr-filter}))))))
    (constantly true)))

(rf/reg-sub
 :input/->filtered-results
 :<- [:input/->results]
 :<- [:modes/interpr-filter]
 :<- [:modes/results-filter]
 (fn [[results interpr-filter results-filter] _]
   (when (= :not-found results)
     (report-error (ex-info "Results data missing!" {:results results})))
   (when (not= (count (ffirst results))
               (count (:terms-filter interpr-filter)))
     (report-error (ex-info "Results inconsistent with terms-filter data!"
                            {:results results
                             :terms-filter (:terms-filter interpr-filter)})))
   (if (or interpr-filter results-filter)
     (let [interpr-filter (make-interpr-filter interpr-filter)
           filtered-results
           (try (for [[interpr result] results]
                  (if (and (results-filter result)
                           (interpr-filter interpr))
                    [interpr result]
                    [interpr :_]))
                (catch js/Error e
                  (report-error e)))]
       ;; (println filtered-results)
       (vec filtered-results))
     results)))

(rf/reg-sub
 :modes/->is-filtered?
 :<- [:modes/interpr-filter]
 :<- [:modes/results-filter]
 (fn [[interpr-filter results-filter] [_ varorder]]
   (let [db-eval (get-in default-db [:modes :eval])
         def-interpr-filter (assoc (db-eval :interpr-filter)
                                   :terms-filter (reset-terms-filter varorder))
         def-results-filter (db-eval :results-filter)]
     (or (not= results-filter def-results-filter)
         (not= interpr-filter def-interpr-filter)))))

;; ? needed
(rf/reg-sub
 :input/->vspace
 :<- [:input/->results]
 (fn [results _]
   (when (= :not-found results)
     (report-error (ex-info "Results data missing!" {})))
   (let [vspc (try (vec (reverse (map first results)))
                   (catch js/Error e
                     (report-error e)))]
     vspc)))


(def dna-sub
  (fn [results _]
    (when (= :not-found results)
      (report-error (ex-info "Results data missing!" {})))
    (let [dna (try (vec (reverse (map second results)))
                   (catch js/Error e
                     (report-error e)))]
      dna)))

(rf/reg-sub
 :input/->dna
 :<- [:input/->results]
 dna-sub)

(rf/reg-sub
 :input/->filtered-dna
 :<- [:input/->filtered-results]
 dna-sub)


(def dna-view-sub
  (fn [dna [_ type]]
    (when-not dna ;; TODO: improve error handling
      (report-error (ex-info "formDNA data missing!" {})))
    (let [dna-view (try (case type
                          :nmui (calc/dna->digits calc/nmui-code dna)
                          :nuim (calc/dna->digits calc/nuim-code dna)
                          (->> dna
                               reverse ;; !TEMP
                               (mapv name)))
                        (catch js/Error e
                          (report-error e)))]
      dna-view)))

(comment
  (calc/dna->digits calc/nuim-code [:N :U :_ :M])
  ,)

(rf/reg-sub
 :input/->dna-view
 :<- [:input/->dna]
 dna-view-sub)

(rf/reg-sub
 :input/->filtered-dna-view
 :<- [:input/->filtered-dna]
 dna-view-sub)


(def vmap-sub
  (fn [dna _]
    (when-not dna
      (report-error (ex-info "formDNA data missing!" {})))
    (try (calc/dna->vmap dna)
         (catch js/Error e
           (report-error e)))))

(rf/reg-sub
 :input/->vmap
 :<- [:input/->dna]
 vmap-sub)

(rf/reg-sub
 :input/->filtered-vmap
 :<- [:input/->filtered-dna]
 vmap-sub)

(def vmap-psps-sub
  (fn [dna _]
    ;; (println "computing vmap")
    (when-not dna
      (report-error (ex-info "formDNA data missing!" {})))
    (try (calc/vmap-perspectives (calc/dna-perspectives dna))
         (catch js/Error e
           (report-error e)))))

(rf/reg-sub
 :input/->vmap-psps
 :<- [:input/->dna]
 vmap-psps-sub)

(rf/reg-sub
 :input/->filtered-vmap-psps
 :<- [:input/->filtered-dna]
 vmap-psps-sub)

(defn make-automaton
  [type-k args & res]
  (try (let [ca-spec (apply emul/specify-ca
                            (apply emul/make-species type-k args)
                            {}
                            res)]
         ca-spec
         ;; (emul/create-ca ca-spec 0)
         #_
         {:id (hash ca-spec)
          :ca (emul/create-ca ca-spec 0)})
       (catch js/Error e
         (report-error e))))

(rf/reg-sub
 :input/->ca-selfi
 :<- [:input/->dna]
 (fn [dna [_ ini-data res-w]]
   (cond
     (nil? dna) (report-error (ex-info "Invalid formDNA" {}))
     :else (let [ca (make-automaton :selfi [dna (apply emul/make-ini ini-data)]
                                    res-w)]
             ;; (println "rf ca-spec hash: " (hash ca))
             ca))))

(rf/reg-sub
 :input/->ca-mindform
 :<- [:input/->dna]
 (fn [dna [_ ini-data res-w res-h]]
   (cond
     (nil? dna) (report-error (ex-info "Invalid formDNA" {}))
     :else (make-automaton :mindform [dna (apply emul/make-ini ini-data)]
                           res-w res-h))))

(rf/reg-sub
 :input/->ca-lifeform
 :<- [:input/->dna]
 (fn [dna [_ res-w res-h]]
   (let [dim (calc/dna-dimension dna)]
     (cond
       (nil? dna) (report-error (ex-info "Invalid formDNA" {}))
       (not (== dim 3))
       (report-error (ex-info "lifeFORMs require FORMs with exactly 3 variables (must result in formDNA of dimension 3)"
                              {:dimension dim}))
       :else (make-automaton :lifeform [dna] res-w res-h)))))


(comment
  (= (->> (expr/eval-all '[[[a] b] c])
          :results
          (mapv second))
     (reverse (last (expr/=>* '[[[a] b] c]))))

  (= (->> (expr/eval-all '[[[a] b] c])
          :results
          (mapv second)
          reverse)
     (last (expr/=>* '[[[a] b] c])))

  (= (->> (reverse (last (expr/=>* '[[a] b])))
          (map vector (calc/vspace 2)))
     (:results (expr/eval-all '[[a] b])))

  (->> (calc/filter-dna (last (expr/=>* '[[a] b]))
                        [:_ :U])
       reverse)

  
  (expr/eval-all ':N)

  (require '[clojure.set :as s])

  (let [fdna (expr/make :fdna [:N :U :I :M])]
    (= fdna (expr/eval->expr-all fdna {})))
  
  (let [dna (last (expr/=>* '[[[a] b] c]))
        vspace (vec (calc/vspace (calc/dna-dimension dna)))]
    (for [i (range (count dna))
          :let [c  (dna i)
                vp (vec (vspace i))]
          :when
          (not= #{} (s/intersection #{:U :I} (set vp))) ;; OR
          ;; (= #{} (s/intersection #{:U :I} (set vp))) ;; NOT OR
          ;; (s/subset? #{:U :I} (set vp))  ;; AND
          ;; (not (s/subset? #{:U :I} (set vp)))  ;; NOT AND
          ;; (= #{:U :I} (set vp))  ;; EQ
          ;; (not= #{:U :I} (set vp))  ;; NOT EQ
          
          ;; (every? #{:U :I} vp)  ;; OR
          ;; (= (count (set vp)) (count (s/union #{:I :U} (set vp)))) ;; AND
          ;; (every? #{:I} vp)  ;; symmetric
          ;; (= (vp 1) :U)  ;; value filter
          ]
      [vp c]))

  [[[:N :N] :N]
   [[:N :U] :N]
   [[:N :I] :N]
   [[:N :M] :N]
   [[:U :N] :U]
   [[:U :U] :N]
   [[:U :I] :U]
   [[:U :M] :N]
   [[:I :N] :I]
   [[:I :U] :I]
   [[:I :I] :N]
   [[:I :M] :N]
   [[:M :N] :M]
   [[:M :U] :I]
   [[:M :I] :U]
   [[:M :M] :N]]

  (merge-with
   #(or %2 %1)
   {:a nil :b 2 :c 3}
   {:a 1 :b nil :c 0})
  
  (into {} (filter (comp some? val) {:a 1 :b nil :c 3}))
  (into {}
        (filter second               ; or (filter (comp not nil? second))
                [[:key1 :x]
                 [:key2 nil]
                 [:key3 :y]]))

  ,)

(comment

  (utils/merge-some
   {:a nil :b 2 :c 3}
   {}
   {:a 1 :b nil :c 0}
   {:b 9 :c -1})

  ,)

