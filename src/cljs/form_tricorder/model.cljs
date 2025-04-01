(ns form-tricorder.model)

(def expr-items
  [{:id    :hooks
    :keybind "⌃h"
    :label "Hooks notation"}
   {:id    :graphs
    :keybind "⌃c"
    :label "Circle notation"}
   {:id    :depthtree
    :keybind "⌃t"
    :label "Depth tree"}
   {:id    :edn
    :keybind "⌃e"
    :label "EDN data format"}
   {:id    :json
    :keybind "⌃j"
    :label "JSON data format"}])

(def eval-items
  [{:id    :vtable
    :keybind "⌃t"
    :label "Value table"}
   {:id    :vmap
    :keybind "⌃v"
    :label "vmap"}
   ;; {:id    :vcube
   ;;  :label "vcube"}
   ;; {:id    :vgraph
   ;;  :label "vgraph (hypercube)"}
   {:id    :fdna
    :keybind "⌃d"
    :label "formDNA"}])

(def emul-items
  [{:id    :selfi
    :keybind "⌃s"
    :label "SelFi"}
   {:id    :mindform
    :keybind "⌃m"
    :label "mindFORM"}
   {:id    :lifeform
    :keybind "⌃l"
    :label "lifeFORM"}
   ])

(def modes
  [{:id    :expr
    :label "expression"
    :keybind "⌃x"
    :color {:base  "$outer-fmenu-expr"
            :hover ""}
    :items expr-items}
   {:id    :eval
    :label "evaluation"
    :keybind "⌃v"
    :color {:base  "$outer-fmenu-eval"
            :hover ""}
    :items eval-items}
   {:id    :emul
    :label "emulation"
    :keybind "⌃e"
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
