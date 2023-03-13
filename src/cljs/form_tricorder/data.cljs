(ns form-tricorder.data)

(def visu-items
  [{:id    "hooks"
    :label "Hooks notation"
    :color {:base  "teal"}}
   {:id    "graphs"
    :label "Graph notation"
    :color {:base  "green"}}
   {:id    "depthtree"
    :label "Depth tree"
    :color {:base  "olive"}}])

(def calc-items
  [{:id    "vtable"
    :label "Value table"
    :color {:base  "cyan"}}
   {:id    "vmap"
    :label "vmap"
    :color {:base  "blue"}}])

(def emul-items
  [{:id    "selfi"
    :label "SelFi"
    :color {:base  "orange"}}
   {:id    "mindform"
    :label "mindFORM"
    :color {:base  "red"}}
   {:id    "lifeform"
    :label "lifeFORM"
    :color {:base  "violet"}}])

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
   {:id    "emul"
    :label "Emulate"
    :color {:base  "$crimson8"
            :hover "$crimson9"}
    :items emul-items}])

(def modes-map
  (update-vals (group-by :id modes) first))
