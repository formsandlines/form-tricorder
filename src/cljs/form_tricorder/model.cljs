(ns form-tricorder.model)

(def visu-items
  [{:id    :hooks
    :label "Hooks notation"
    :color {:base "teal"}}
   {:id    :graphs
    :label "Graph notation"
    :color {:base "green"}}
   {:id    :depthtree
    :label "Depth tree"
    :color {:base "olive"}}])

(def calc-items
  [{:id    :vtable
    :label "Value table"
    :color {:base "cyan"}}
   {:id    :vmap
    :label "vmap"
    :color {:base "blue"}}])

(def emul-items
  [{:id    :selfi
    :label "SelFi"
    :color {:base "orange"}}
   {:id    :mindform
    :label "mindFORM"
    :color {:base "red"}}
   {:id    :lifeform
    :label "lifeFORM"
    :color {:base "violet"}}])

(def misc-items
  [{:id    :edn
    :label "EDN"
    :color {:base "grey"}}
   {:id    :json
    :label "JSON"
    :color {:base "grey"}}])

(def modes
  [{:id    :visu
    :label "visualize"
    :color {:base  "$fmenu_visu"
            :hover ""}
    :items visu-items}
   {:id    :calc
    :label "calculate"
    :color {:base  "$fmenu_calc"
            :hover ""}
    :items calc-items}
   #_{:id    :emul
      :label "emulate"
      :color {:base  "$fmenu_emul"
              :hover ""}
      :items emul-items}
   {:id    :misc
    :label "…"
    :color {:base  "$fmenu_emul"
            :hover ""}
    :items misc-items}])


(def modes-map
  (update-vals (group-by :id modes) first))

(def func->mode
  (into {}
        (for [{:keys [id items]} modes
              {func-id :id} items]
          [func-id id])))


(comment
  (modes-map :calc))
