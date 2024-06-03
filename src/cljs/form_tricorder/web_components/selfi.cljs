(ns form-tricorder.web-components.selfi
  (:require
   [clojure.math]
   [garden.core :refer [css]]
   [garden.selectors :as s]
   ;; ["/stitches.config" :refer (getCssText)]
   [formform.calc :as calc]
   [form-tricorder.utils :as utils :refer [pp-val pp-var css>]]
   [zero.core :as z]
   [zero.config :as zc]
   [zero.component]))


(def KRGB {:N "#000000"
           :U "#FF0000"
           :I "#00FF00"
           :M "#0000FF"})

(defn sys-ini
  [ini-ptn res]
  (condp ini-ptn =
    :random (vec (repeatedly res calc/rand-const))
    (throw (ex-info "Unknown ini pattern" {}))))

(defn sys-next
  [gen-prev rules umwelt]
  (let [p 0
        q (dec (count gen-prev))]
    (into []
          (for [i (range (count gen-prev))]
            (let [L (if (> i p) (- i 1) q)
                  E i
                  R (if (< i q) (+ i 1) p)]
              (rules
               (mapv gen-prev
                     (condp = umwelt
                       :e   [E]
                       :lr  [L R]
                       :ler [L E R]
                       (throw (ex-info "Invalid cell neighbourhood" {}))))))))))

(defn emulate
  [rules umwelt num evolution]
  (let [prev-gen (last evolution)
        next-gen (sys-next prev-gen rules umwelt)]
    (if (<= num 1)
      evolution
      (recur rules umwelt (dec num) (conj evolution next-gen)))))

(defn draw [context evolution cw ch cell-size]
  (.clearRect context 0 0 cw ch)
  (aset context "fillStyle" "black")
  (.fillRect context 0 0 cw ch)
  (doseq [[i gen] (map-indexed vector evolution)
          [j val] (map-indexed vector gen)
          :let [x (* j cell-size)
                y (* i cell-size)]]
    (aset context "fillStyle" (KRGB val))
    (.fillRect context x y cell-size cell-size)))


(def styles
  (css [[:host
         {}]]))

;; (js/console.log styles)

(def stylesheet
  (doto (js/CSSStyleSheet.)
    (.replaceSync styles)))

(defn view
  [{:keys [res vislimit iniptn rules umwelt cellsize]}]
  [:root>
   {;; ::z/css stylesheet
    ::z/on {
            ;; :connect
            ;; (fn [e]
            ;;   (let [evolution (emulate rules umwelt vis-limit
            ;;                            [(sys-ini ini-ptn res)])])
            ;;   nil)
            
            :render
            (fn [e]
              (let [evolution (emulate rules umwelt vislimit
                                       [(sys-ini (keyword iniptn) res)])
                    shadow (.. e -target)
                    canvas (.querySelector shadow "canvas")
                    cw (.-width canvas)
                    ch (.-height canvas)
                    context (.getContext canvas "2d")]
                (draw context evolution cw ch cellsize)))}
    }
   [:canvas {:width  (* res cellsize)
             :height (* vislimit cellsize)}]])

(zc/reg-components
 :ff/selfi
 {:props {:rules    :field
          :umwelt   :field
          :res      :default
          :iniptn   :default
          :vislimit :default
          :cellsize :default}
  :view view})

