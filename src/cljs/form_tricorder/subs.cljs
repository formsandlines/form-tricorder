(ns form-tricorder.subs
  (:require
   [form-tricorder.utils :as utils]
   [formform.calc :as calc]
   [formform.expr :as expr]
   [formform.io :as io]
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

(rf/reg-sub
 :input/->dna
 :<- [:input/->expr-data]
 (fn [[expr varorder] _]
   (when (= :not-found expr)
     (report-error (ex-info "Expression data missing!" {})))
   (when (nil? varorder)
     (report-error (ex-info "Unknown variable ordering!" {})))
   (let [formDNA (try (if (expr/formDNA? expr)
                        expr
                        (expr/eval->expr-all {:varorder varorder} expr {}))
                      (catch js/Error e
                        (report-error e)))]
     ;; (println "computing value")
     (try (expr/op-get formDNA :dna)
          (catch js/Error e
            (report-error e))))))

(rf/reg-sub
 :input/->dna-view
 :<- [:input/->dna]
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


(rf/reg-sub
 :input/->vmap
 :<- [:input/->dna]
 (fn [dna _]
   (when-not dna
     (report-error (ex-info "formDNA data missing!" {})))
   (try (calc/dna->vmap dna)
        (catch js/Error e
          (report-error e)))))

(rf/reg-sub
 :input/->vmap-psps
 :<- [:input/->dna]
 (fn [dna _]
   ;; (println "computing vmap")
   (when-not dna
     (report-error (ex-info "formDNA data missing!" {})))
   (try (calc/vmap-perspectives (calc/dna-perspectives dna))
        (catch js/Error e
          (report-error e)))))


(rf/reg-sub
 :input/->selfi-rules-fn
 :<- [:input/->dna]
 (fn [dna _]
   ;; (println "computing ca rules function")
   (cond
     (nil? dna) (report-error (ex-info "Invalid formDNA" {}))
     :else (try (partial calc/dna-get dna)
                (catch js/Error e
                  (report-error e))))))

(rf/reg-sub
 :input/->selfi-umwelt
 :<- [:input/varorder]
 (fn [varorder _]
   ;; (println "computing ca umwelt")
   (cond
     (nil? varorder) (report-error (ex-info "Invalid variable ordering" {}))
     :else (condp = (count varorder)
             0 nil ;; constantly returns the FORM value
             1 :e
             2 :lr
             3 :ler
             4 :-lr+
             5 :-ler+
             (report-error (ex-info "Invalid variable count" {}))))))


(comment
  (expr/eval-all [:fdna [] [:M]] {})
  (expr/=>* [:fdna [] [:M]] {})

  )
