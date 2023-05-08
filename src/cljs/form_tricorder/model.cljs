(ns form-tricorder.model)

(def visu-items
  [{:id    "hooks"
    :label "Hooks notation"
    :color {:base "teal"}}
   {:id    "graphs"
    :label "Graph notation"
    :color {:base "green"}}
   {:id    "depthtree"
    :label "Depth tree"
    :color {:base "olive"}}])

(def calc-items
  [{:id    "vtable"
    :label "Value table"
    :color {:base "cyan"}}
   {:id    "vmap"
    :label "vmap"
    :color {:base "blue"}}])

(def emul-items
  [{:id    "selfi"
    :label "SelFi"
    :color {:base "orange"}}
   {:id    "mindform"
    :label "mindFORM"
    :color {:base "red"}}
   {:id    "lifeform"
    :label "lifeFORM"
    :color {:base "violet"}}])

(def misc-items
  [{:id    "edn"
    :label "EDN"
    :color {:base "grey"}}
   {:id    "json"
    :label "JSON"
    :color {:base "grey"}}])

(def modes
  [{:id    "visu"
    :label "Visualize"
    :color {:base  "$teal8"
            :hover "$teal9"}
    :items visu-items}
   {:id    "calc"
    :label "Calculate"
    :color {:base  "$violet8"
            :hover "$violet9"}
    :items calc-items}
   #_{:id    "emul"
      :label "Emulate"
      :color {:base  "$crimson8"
              :hover "$crimson9"}
      :items emul-items}
   {:id    "misc"
    :label "…"
    :color {:base  "gray"
            :hover "darkgrey"}
    :items misc-items}])

(def modes-map
  (update-vals (group-by :id modes) first))
