(ns form-tricorder.model)

(def visu-items
  [{:id    :hooks
    :label "Hooks notation"
    :color {:base "$inner-visu"}}
   {:id    :graphs
    :label "Graph notation"
    :color {:base "$inner-visu"}}
   {:id    :depthtree
    :label "Depth tree"
    :color {:base "$inner-visu"}}])

(def calc-items
  [{:id    :vtable
    :label "Value table"
    :color {:base "$inner-calc"}}
   {:id    :vmap
    :label "vmap"
    :color {:base "$inner-calc"}}])

(def emul-items
  [{:id    :selfi
    :label "SelFi"
    :color {:base "$inner-emul"}}
   {:id    :mindform
    :label "mindFORM"
    :color {:base "$inner-emul"}}
   {:id    :lifeform
    :label "lifeFORM"
    :color {:base "$inner-emul"}}])

(def misc-items
  [{:id    :edn
    :label "EDN"
    :color {:base "$inner-emul"}}
   {:id    :json
    :label "JSON"
    :color {:base "$inner-emul"}}])

(def modes
  [{:id    :visu
    :label "visualize"
    :color {:base  "$fmenu-visu"
            :hover ""}
    :items visu-items}
   {:id    :calc
    :label "calculate"
    :color {:base  "$fmenu-calc"
            :hover ""}
    :items calc-items}
   {:id    :emul
    :label "emulate"
    :color {:base  "$fmenu-emul"
            :hover ""}
    :items emul-items}
   {:id    :misc
    :label "…"
    :color {:base  "$fmenu-emul"
            :hover ""}
    :items misc-items}])


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
  (modes-map :calc))
