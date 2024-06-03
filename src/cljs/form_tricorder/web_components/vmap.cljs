(ns form-tricorder.web-components.vmap
  (:require
   [clojure.math]
   [formform.calc :as calc]
   [form-tricorder.utils :as utils]
   [zero.core :as z]
   [zero.config :as zc]
   [zero.component]))

(defn gen-margins
  [{:keys [gap-bounds gap-growth]
    :or {gap-bounds [0.1 ##Inf] gap-growth 0.3}}
   dim]
  (let [growth-fn (fn [part-dim]
                    (min (max (* part-dim gap-growth)
                              (first gap-bounds))
                         (second gap-bounds)))]
    (mapv growth-fn (range 0 dim))))

(def KRGB {:N "#000000"
           :U "#FF0000"
           :I "#00FF00"
           :M "#0000FF"})

(def const->coords {:N [0 0]
                    :U [0 1]
                    :I [1 0]
                    :M [1 1]
                    :_ [0 0]})

(defn view
  [{:keys [vmap scale-to-fit? cellsize padding margins stroke-width
           colors bg-color stroke-color label]
    :or {scale-to-fit? false padding 0 stroke-width 0.5
         colors KRGB bg-color nil stroke-color nil label nil}}]
  (let [dim       (calc/vmap-dimension vmap) ;; ? remove dependency
        vmap      (if (> dim 0) vmap {:_ vmap})
        cellsize  (if (nil? cellsize)
                    12
                    cellsize)
        margins   (reverse (take dim (if (nil? margins)
                                       (utils/geom-seq 0.1 2.0)
                                       margins)))
        sum-gaps  (fn [margins] (apply + (map (fn [n i]
                                               (* n (utils/pow-nat 2 i)))
                                             margins (range))))
        vmap-size (+ (* (utils/pow-nat 2 dim) cellsize)
                     (* (sum-gaps margins) cellsize))
        vmap-diag (clojure.math/sqrt (* (* vmap-size vmap-size) 2))
        full-size (+ vmap-diag padding)]

    [:svg {:width  (if scale-to-fit? "100%" full-size)
           :height (if scale-to-fit? "100%" full-size)
           :view-box (let [shift (+ (/ padding 2))]
                       (str "-" shift " -" shift
                            " " full-size " " full-size))}
     (when (some? bg-color)
       [:rect {:x (str "-" (/ padding 2))
               :y (str "-" (/ padding 2))
               :width  full-size
               :height full-size
               :fill   bg-color}])
     [:g {:transform
          (str "translate(" 0 "," (/ vmap-diag 2) ")"
               "scale(" cellsize ") "
               "rotate(" -45 ") ")}
      ((fn f [vmap dim margins]
         (for [[k v] vmap]
           [:g {::z/key (str "dim" dim "_" (name k))
                :transform
                (let [coords (const->coords k)
                      gaps   (if (> (count margins) 1)
                               (sum-gaps (rest margins))
                               0)
                      shift  (+ (utils/pow-nat 2 dim)
                                (first margins) ; (if (empty? margins) 0 …)
                                gaps)
                      x (* (coords 0) shift)
                      y (* (coords 1) shift)]
                  (str "translate(" x "," y ")"))}
            (if (> dim 0)
              (f v (dec dim) (rest margins))
              [:rect
               (merge {:width  1
                       :height 1
                       :fill (colors v)}
                      (if (nil? stroke-color)
                        {}
                        {:stroke-width (/ stroke-width cellsize)
                         :stroke stroke-color}))])]))
       vmap (dec dim) margins)]

     ;; ! TODO
     (when (some? label)
       [:g {:transform (str "translate(" 0 "," vmap-diag ")")}
        [:text {}
         label]])]))

(zc/reg-components
 :ff/vmap
 {:props {:vmap          :field
          ;; :scale-to-fit? :default
          ;; :cellsize      :default
          ;; :padding       :default
          ;; :margins       :default
          ;; :stroke-width  :default
          ;; :colors        :default
          ;; :bg-color      :default
          ;; :stroke-color  :default
          ;; :label         :default
          }
  :view view})
