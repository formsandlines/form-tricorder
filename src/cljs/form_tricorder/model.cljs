(ns form-tricorder.model
  (:require
   [form-tricorder.icons :as icons]))

(def expr-items
  [{:id    :hooks
    :label "Hooks notation"}
   {:id    :graphs
    :label "Graph notation"}
   {:id    :depthtree
    :label "Depth tree"}
   {:id    :edn
    :label "EDN data format"}
   {:id    :json
    :label "JSON data format"}])

(def eval-items
  [{:id    :vtable
    :label "Value table"}
   {:id    :vmap
    :label "vmap"}
   ;; {:id    :vcube
   ;;  :label "vcube"}
   ;; {:id    :vgraph
   ;;  :label "vgraph (hypercube)"}
   {:id    :fdna
    :label "formDNA"}])

(def emul-items
  [{:id    :selfi
    :label "SelFi"}
   {:id    :mindform
    :label "mindFORM"}
   ;; {:id    :lifeform
   ;;  :label "lifeFORM"}
   ])

(def modes
  [{:id    :expr
    :label "expression"
    :color {:base  "$outer-fmenu-expr"
            :hover ""}
    :items expr-items}
   {:id    :eval
    :label "evaluation"
    :color {:base  "$outer-fmenu-eval"
            :hover ""}
    :items eval-items}
   {:id    :emul
    :label "emulation"
    :color {:base  "$outer-fmenu-emul"
            :hover ""}
    :items emul-items}])


(def modes-map
  (update-vals (group-by :id modes) first))

(def func-ids (set (mapcat (comp (partial map :id) :items) (vals modes-map))))

(def func->mode
  (into {}
        (for [{:keys [id items]} modes
              {func-id :id} items]
          [func-id id])))


(comment
  (prn func->mode)
  (modes-map :eval))
