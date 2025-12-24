;; SPDX-FileCopyrightText:
;;   2015 Simon Howe <footless@gmail.com>
;;   2025 Peter Hofmann <peter.hofmann@formsandlines.eu>
;;
;; SPDX-License-Identifier: EPL-1.0

;: This file is a heavily modified/adapted version of the original source:
;; https://github.com/foot/slider2d/blob/master/src/cljs/slider2d/core.cljs

(ns form-tricorder.components.common.slider2d
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   ;; [clojure.string :as str]
   [form-tricorder.utils :as utils :refer [let+ unite]]
   ["react" :as react]
   [goog.events :as events])
  (:import [goog.events EventType]))

(def r) ;; hotfix for linting error in let+

(defn drag-move-fn [on-drag]
  (fn [e]
    (on-drag (.-clientX e) (.-clientY e))))

(defn drag-end-fn [drag-move drag-end on-end]
  (fn [e]
    (events/unlisten js/window EventType.MOUSEMOVE drag-move)
    (events/unlisten js/window EventType.MOUSEUP @drag-end)
    (on-end (.-clientX e) (.-clientY e))))

(defn dragging
  ([on-drag] (dragging on-drag (fn []) (fn [_ _])))
  ([on-drag on-start] (dragging on-drag on-start (fn [])))
  ([on-drag on-start on-end]
   (let [drag-move (drag-move-fn on-drag)
         drag-end-atom (atom nil)
         drag-end (drag-end-fn drag-move drag-end-atom on-end)]
     (on-start)
     (reset! drag-end-atom drag-end)
     (events/listen js/window EventType.MOUSEMOVE drag-move)
     (events/listen js/window EventType.MOUSEUP drag-end))))

(defn clamp [a v b]
  (min (max a v) b))

(defn scale [[d1 d2] [r1 r2]]
  (let [dd (- d2 d1)
        dr (- r2 r1)]
    (fn [x] (+ r1 (* dr (/ (- x d1) dd))))))

(defn both-scales [d r]
  [(scale d r) (scale r d)])


;; View

(defn snap-to
  [x target rad]
  (if (and (<= x (+ target rad))
           (>= x (- target rad)))
    target
    x))

(defnc Slider2D
  [props]
  (let+ [{:keys [className class
                 w h min max xmin xmax ymin ymax value
                 on-value-change on-value-commit]
          :or {min 0 max 1 value 1
               on-value-change (fn [_ _])
               on-value-commit (fn [_ _])}
          :rest r} props
         ref (hooks/use-ref nil)
         pw 12 
         ph 12 
         ew (- w pw)
         eh (- h ph)
         [x->value x->pixels] (both-scales [0 ew] [xmin xmax])
         [y->value y->pixels] (both-scales [0 eh] [ymin ymax]) 
         make-move-handler (fn [value-fn]
                             (fn [x y]
                               (let [bcr (.getBoundingClientRect @ref)
                                     w (.-width bcr)
                                     h (.-height bcr)
                                     x (-> (- x (.-left bcr))
                                           (snap-to (/ w 2) 6)
                                           (snap-to (/ w 4) 4)
                                           (snap-to (* 3 (/ w 4)) 4))
                                     y (-> (- y (.-top bcr))
                                           (snap-to (/ h 2) 6)
                                           (snap-to (/ h 4) 4)
                                           (snap-to (* 3 (/ h 4)) 4))
                                     x (- x (* 0.5 pw))
                                     y (- y (* 0.5 ph))]
                                 (value-fn
                                  (x->value (clamp 0 x ew))
                                  (y->value (clamp 0 y eh))))))
         ;; take-point (make-move-handler on-drag-start)
         move-point (make-move-handler on-value-change)
         drop-point (make-move-handler on-value-commit)
         on-mouse-down (fn [e]
                         ;; called on mouse down
                         (move-point (.-clientX e) (.-clientY e)
                                     on-value-change)
                         ;; called (repeatedly) while dragging
                         (dragging move-point (fn []) drop-point))]
    (d/div
     {:class (unite className class "plot" "noselect"
                    (css :border :border-col-input :bg
                         {:position "relative"
                          :touch-action "none"
                          :user-select "none"
                          :cursor "pointer"}))
      :on-mouse-down on-mouse-down
      :style {:width w
              :height h}
      :ref ref
      & r}
     (d/svg ; grid lines
      {:width w
       :height h}
      (d/g
       {:class (css "slider2d-gridlines-half"
                    {:stroke "var(--col-bg-muted)"})}
       (d/line
        {:style {:stroke-dasharray (str (/ h 13))}
         :x1 "50%"
         :x2 "50%"
         :y1 "0%"
         :y2 "100%"})
       (d/line
        {:style {:stroke-dasharray (str (/ w 13))}
         :x1 "0%"
         :x2 "100%"
         :y1 "50%"
         :y2 "50%"}))
      (d/g
       {:class (css "slider2d-gridlines-quarter"
                    {:stroke "var(--col-bg-muted)"})
        :style {:stroke-dasharray "1 6"}}
       (d/line
        {:x1 "25%"
         :x2 "25%"
         :y1 "0%"
         :y2 "100%"})
       (d/line
        {:x1 "0%"
         :x2 "100%"
         :y1 "25%"
         :y2 "25%"})
       (d/line
        {:x1 "75%"
         :x2 "75%"
         :y1 "0%"
         :y2 "100%"})
       (d/line
        {:x1 "0%"
         :x2 "100%"
         :y1 "75%"
         :y2 "75%"})))
     (d/span
      {:class (unite "point"
                     (css :border :bg :border-primary
                          :shadow-sm :rounded-full
                          {:position "absolute"}
                          ["&:hover"
                           :bg-accent :ring]
                          ["&:focus-visible"
                           :ring :outline-none]
                          ["&:disabled"
                           {:pointer-events "none"
                            :opacity "0.5"}]))
       :role "slider"
       :aria-valuemin (str xmin ymin)
       :aria-valuemax (str xmax ymax)
       :style {:width pw
               :height ph
               :left (x->pixels (:xvalue props))
               :top (y->pixels (:yvalue props))}}))))
