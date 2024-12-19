(ns form-tricorder.icons
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.dom :as d :refer [$d]]
   [form-tricorder.stitches-config :as st]))

;; RegEx to convert paths to Helix components:
;; ^.+\sd=(".+?")\stransform=(".+?").+$
;; (d/path\n{:d $1\n:transform $2})


(defnc PerspectivesExpandIcon
  [{:keys [classes style] :or {style {}}}]
  (d/svg
    {:class (str "icon" (when classes (str " " classes)))
     :style (merge {:stroke "currentcolor"
                    :stroke-width "1.2px"
                    :stroke-linecap "round"
                    :width 16
                    :height 16}
                   style)
     :viewBox "0 0 16 16"
     :xmlns "http://www.w3.org/2000/svg"}
    (d/g
      {:transform "translate(8,8)"}
      (d/line
        {:x1 -6.8 :x2 6.8
         :y1 0 :y2 0
         :transform "rotate(30)"})
      (d/line
        {:x1 -6.8 :x2 6.8
         :y1 0 :y2 0
         :transform "rotate(-30)"})
      (d/line
        {:x1 -6.8 :x2 6.8
         :y1 0 :y2 0
         :transform "rotate(90)"}))))

(defnc PerspectivesCollapseIcon
  [{:keys [classes style] :or {style {}}}]
  (d/svg
    {:class (str "icon" (when classes (str " " classes)))
     :style (merge {:stroke "currentcolor"
                    :stroke-width "1.2px"
                    :stroke-linecap "round"
                    :width 16
                    :height 16}
                   style)
     :viewBox "0 0 16 16"
     :xmlns "http://www.w3.org/2000/svg"}
    (d/g
      {:transform "translate(8,8)"}
      (d/line
        {:x1 -6.8 :x2 6.8
         :y1 0 :y2 0}))))


(defn ficon-params
  [mode-id]
  {:size 24})

(def FunctionIcon
  (st/styled "svg"
          { ;; :opacity "0.7"
           "& .ficon-area" {:fill "currentcolor"
                            :fill-opacity "0.5"}
           "& .ficon-line" {:fill "none"
                            :stroke "currentcolor"
                            ;; :stroke-opacity "0.8"
                            :stroke-width "2px"}
           :variants
           {:mode
            {:expr {:color "$inner-icon-expr"}
             :eval {:color "$inner-icon-eval"}
             :emul {:color "$inner-icon-emul"}}}
           }))

(defmulti function-icon (fn [func-id] func-id))

(defmethod function-icon :default
  [_]
  nil)


(defmethod function-icon :lifeform
  [_]
  (let [{:keys [size]} (ficon-params :emul)]
    ($ FunctionIcon
       {:xmlns "http://www.w3.org/2000/svg",
        :viewBox (str "0 0 " size " " size),
        :mode "emul"
        :width (str size "px"),
        :height (str size "px")}
       (d/rect
         {:class "ficon-area"
          :x "9", 
          :width "6", 
          :height "6", 
          :rx "1", 
          :ry "1", 
          :y "16"})
       (d/rect
         {:class "ficon-area"
          :x "9", 
          :width "6", 
          :height "6", 
          :rx "1", 
          :ry "1", 
          :y "2"})
       (d/rect
         {:class "ficon-area"
          :x "16", 
          :width "6", 
          :height "6", 
          :rx "1", 
          :ry "1", 
          :y "9"})
       (d/rect
         {:class "ficon-area"
          :x "2", 
          :width "6", 
          :height "6", 
          :rx "1", 
          :ry "1", 
          :y "9"})
       (d/ellipse
         {:class "ficon-line"
          :cx "12", 
          :cy "12", 
          :rx "2", 
          :ry "2"}))))

(defmethod function-icon :mindform
  [_]
  (let [{:keys [size]} (ficon-params :emul)]
    ($ FunctionIcon
      {:xmlns "http://www.w3.org/2000/svg",
       :viewBox (str "0 0 " size " " size),
       :mode "emul"
       :width (str size "px"),
       :height (str size "px")}
      (d/rect
        {:class "ficon-area"
	:x "16", 
         :width "6", 
         :height "6", 
         :rx "1", 
         :ry "1", 
         :y "9"})
      (d/rect
        {:class "ficon-area"
	:x "16", 
         :width "6", 
         :height "6", 
         :rx "1", 
         :ry "1", 
         :y "16"})
      (d/rect
        {:class "ficon-area"
	:x "16", 
         :width "6", 
         :height "6", 
         :rx "1", 
         :ry "1", 
         :y "2"})
      (d/path
        {:class "ficon-line"
         :style {:stroke-linecap "round"},
         :d "M 13 18 L 3 12 L 13 6"})
      (d/ellipse
        {:class "ficon-area"
         :style {:fill-opacity "0.9"},
         :cx "11.5", 
         :cy "12", 
         :rx "1.5", 
         :ry "3.999"}))))

(defmethod function-icon :selfi
  [_]
  (let [{:keys [size]} (ficon-params :emul)]
    ($ FunctionIcon
       {:xmlns "http://www.w3.org/2000/svg",
        :viewBox (str "0 0 " size " " size),
        :mode "emul"
        :width (str size "px"),
        :height (str size "px")}
       (d/rect
         {:class "ficon-area"
          :x "2", 
          :width "6", 
          :height "6", 
          :rx "1", 
          :ry "1", 
          :y "2"})
       (d/rect
         {:class "ficon-area"
          :x "9", 
          :width "6", 
          :height "6", 
          :rx "1", 
          :ry "1", 
          :y "2"})
       (d/rect
         {:class "ficon-area"
          :x "16", 
          :width "6", 
          :height "6", 
          :rx "1", 
          :ry "1", 
          :y "2"})
       (d/rect
         {:class "ficon-area"
          :x "9", 
          :width "6", 
          :height "6", 
          :rx "1", 
          :ry "1", 
          :y "9"})
       (d/g
         {:class "ficon-line"
          :style {:stroke-width "1.8px"
                  :stroke-linecap "round"}}
         (d/path
           {:d "M 18.001 13.999 L 18.001 18.999"})
         (d/path
           {:style {:transform-origin "19.501px 19.499px"},
            :d "M 21.001 17.999 L 18.001 21"})
         (d/path
           {:style {:transform-origin "16.5px 19.499px"},
            :d "M 15 17.999 L 18 21"})))))

(defmethod function-icon :vmap
  [_]
  (let [{:keys [size]} (ficon-params :eval)]
    ($ FunctionIcon
      {:xmlns "http://www.w3.org/2000/svg",
       :viewBox (str "0 0 " size " " size),
       :mode "eval"
       :width (str size "px"),
       :height (str size "px")}
      (d/rect
        {:class "ficon-area"
         :style {:transform-origin "8.485px 2.828px"},
         :transform "matrix(0.707107, 0.707107, -0.707107, 0.707107, 3.513735, 5.17035)",
         :x "6.363", 
         :y "0.707", 
         :width "4.242", 
         :height "4.242"})
      (d/rect
        {:class "ficon-area"
         :style {:transform-origin "8.485px 2.828px"},
         :transform "matrix(0.707107, 0.707107, -0.707107, 0.707107, 3.513075, 13.169616)",
         :x "6.364", 
         :y "0.707", 
         :width "4.243", 
         :height "4.243"})
      (d/rect
        {:class "ficon-area"
         :style {:transform-origin "8.486px 2.828px"},
         :transform "matrix(0.707107, 0.707107, -0.707107, 0.707107, 7.512611, 9.169589)",
         :x "6.364", 
         :y "0.707", 
         :width "4.243", 
         :height "4.243"})
      (d/rect
        {:class "ficon-area"
         :style {:transform-origin "8.485px 2.828px"},
         :transform "matrix(0.707107, 0.707107, -0.707107, 0.707107, -0.485884, 9.169976)",
         :x "6.363", 
         :y "0.707", 
         :width "4.242", 
         :height "4.242"})
      (d/rect
        {:class "ficon-line"
         :style {:stroke-width "1.8px"
                 :transform-origin "8.046px 7.873px"} ,
         :transform "matrix(0.707107, -0.707107, 0.707107, 0.707107, 3.954706, 4.127)",
         :x "0.266",
         :y "0.093",
         :width "15.559", 
         :height "15.559", 
         :rx "0.497", 
         :ry "0.497"}))))

(defmethod function-icon :vtable
  [_]
  (let [{:keys [size]} (ficon-params :eval)]
    ($ FunctionIcon
      {:xmlns "http://www.w3.org/2000/svg",
       :viewBox (str "0 0 " size " " size),
       :mode "eval"
       :width (str size "px"),
       :height (str size "px")}
      (d/path
        {:d "M 3 3 L 21 3",
         :fill "none",
         :class "ficon-line"
         :style {:stroke-linecap "round"}})
      (d/path
        {:d "M 12 3 L 12 21",
         :class "ficon-line"
         :style {:stroke-linecap "round"}})
      (d/path
        {:d "M 16 7 H 21 V 19 A 1 1 0 0 1 20 20 H 16 V 7 Z",
         :class "ficon-area"})
      (d/path
        {:d "M 3 7 H 8 V 20 H 4 A 1 1 0 0 1 3 19 V 7 Z",
         :class "ficon-area"}))))

(defmethod function-icon :depthtree
  [_]
  (let [{:keys [size]} (ficon-params :expr)]
    ($ FunctionIcon
      {:xmlns "http://www.w3.org/2000/svg",
       :viewBox (str "0 0 " size " " size),
       :mode "expr"
       :width (str size "px"),
       :height (str size "px")}
      (d/rect
        {:class "ficon-area"
         :x "19",
         :width "5",
         :height "22",
         :rx "1",
         :ry "1",
         :y "1"})
      (d/path
        {:d "M 2 12 C 4.8 12 7.789 11.937 9.933 9 C 12.078 6.063 13.2 5 16 5",
         :class "ficon-line"
         :style {:stroke-linecap "round"}})
      (d/path
        {:d "M 2 12 C 4.8 12 7.789 12.063 9.933 15 C 12.078 17.937 13.2 19 16 19",
         :class "ficon-line"
         :style {:stroke-linecap "round"}}))))

(defmethod function-icon :graphs
  [_]
  (let [{:keys [size]} (ficon-params :expr)]
    ($ FunctionIcon
      {:xmlns "http://www.w3.org/2000/svg",
       :viewBox (str "0 0 " size " " size),
       :mode "expr"
       :width (str size "px"),
       :height (str size "px")}
      (d/ellipse
        {:class "ficon-line"
         :style {:stroke-width "1.8px"}
         :cx "12",
         :cy "12",
         :rx "11",
         :ry "11"})
      (d/ellipse
        {:class "ficon-area"
         :cx "12", 
         :cy "12", 
         :rx "7", 
         :ry "7"}))))

(defmethod function-icon :hooks
  [_]
  (let [{:keys [size]} (ficon-params :expr)]
    ($ FunctionIcon
      {:xmlns "http://www.w3.org/2000/svg",
       :viewBox (str "0 0 " size " " size),
       :mode "expr"
       :width (str size "px"),
       :height (str size "px")}
      (d/path
        {:d "M 21 21 L 21 3 L 3 3",
         :class "ficon-line"
         :style {:stroke-linecap "round"
                 :stroke-linejoin "round"
                 :transform-origin "12px 12px"}})
      (d/rect
        {:class "ficon-area"
         :x "4",
         :y "7",
         :width "13",
         :height "13"}))))



(defnc SunIcon
  [{:keys [classes style] :as props}]
  (d/svg
    {:class (str "icon" (or (str " " classes) ""))
     :style (or style {})
     :viewBox "0 0 19.641 19.661"
     :xmlns "http://www.w3.org/2000/svg"}
    (d/g
      {:transform "translate(-2.5 -2.5)"}
      (d/path
        {:d "M30.407,25.3a5.106,5.106,0,1,0,5.106,5.106A5.117,5.117,0,0,0,30.407,25.3Z"
         :transform "translate(-18.087 -18.087)"})
      (d/path
        {:d "M47.462,5.808a.653.653,0,0,0,.661-.661V3.161a.661.661,0,1,0-1.323,0V5.167A.648.648,0,0,0,47.462,5.808Z"
         :transform "translate(-35.142 0)"})
      (d/path
        {:d "M17.01,18.021a.649.649,0,0,0,.909,0,.675.675,0,0,0,0-.93l-1.406-1.406a.658.658,0,0,0-.93.93Z"
         :transform "translate(-10.231 -10.311)"})
      (d/path
        {:d "M5.808,47.462a.653.653,0,0,0-.661-.661H3.161a.661.661,0,1,0,0,1.323H5.167A.648.648,0,0,0,5.808,47.462Z"
         :transform "translate(0 -35.142)"})
      (d/path
        {:d "M17.091,71.527l-1.406,1.406a.675.675,0,0,0,0,.93.649.649,0,0,0,.909,0L18,72.458a.675.675,0,0,0,0-.93A.613.613,0,0,0,17.091,71.527Z"
         :transform "translate(-10.311 -54.596)"})
      (d/path
        {:d "M47.462,81.5a.653.653,0,0,0-.661.661v2.005a.661.661,0,1,0,1.323,0V82.161A.653.653,0,0,0,47.462,81.5Z"
         :transform "translate(-35.142 -62.667)"})
      (d/path
        {:d "M72.518,71.588a.658.658,0,0,0-.93.93l1.406,1.406a.649.649,0,0,0,.909,0,.675.675,0,0,0,0-.93Z"
         :transform "translate(-54.656 -54.656)"})
      (d/path
        {:d "M84.069,46.8H82.064a.661.661,0,1,0,0,1.323h2.005a.661.661,0,0,0,0-1.323Z"
         :transform "translate(-62.59 -35.142)"})
      (d/path
        {:d "M71.961,18.308a.642.642,0,0,0,.455-.186l1.406-1.406a.658.658,0,0,0-.93-.93l-1.406,1.406a.675.675,0,0,0,0,.93A.73.73,0,0,0,71.961,18.308Z"
         :transform "translate(-54.576 -10.392)"}))))

(defnc MoonIcon
  [{:keys [classes style] :as props}]
  (d/svg
    {:class (str "icon" (or (str " " classes) ""))
     :style (or style {})
     :viewBox "0 0 15.888 15.917"
     :xmlns "http://www.w3.org/2000/svg"}
    (d/path
      {:d "M13.028,7.343a.432.432,0,0,0-.2.029A8.274,8.274,0,0,0,7.343,15.1,8.155,8.155,0,0,0,23.2,17.779a.432.432,0,0,0,.029-.2.585.585,0,0,0-.583-.583.5.5,0,0,0-.2.029,7.649,7.649,0,0,1-2.3.408,6.952,6.952,0,0,1-6.588-9.3.432.432,0,0,0,.029-.2.559.559,0,0,0-.554-.583Z"
       :transform "translate(-7.343 -7.343)"})))

(defnc SwapIcon
  [{:keys [classes style] :as props}]
  (d/svg
    {:class (str "icon" (or (str " " classes) ""))
     :style (or style {})
     :viewBox "0 0 17.883 18.063"
     :xmlns "http://www.w3.org/2000/svg"}
    (d/path
      {:d "M41.157,27.384a.8.8,0,0,0-.034-.112c-.007-.02-.01-.041-.018-.06a.834.834,0,0,0-.063-.116.4.4,0,0,0-.022-.041.9.9,0,0,0-.115-.139l-4.181-4.182a.921.921,0,0,0-1.3,1.3l2.61,2.61H24.409a.921.921,0,0,0,0,1.841H38.033L35.421,31.1a.921.921,0,0,0,1.3,1.3l4.182-4.182a.967.967,0,0,0,.115-.139c.008-.013.014-.027.022-.041a1,1,0,0,0,.063-.116c.008-.02.012-.041.018-.06a1.021,1.021,0,0,0,.034-.112.893.893,0,0,0,0-.366Zm-1.1,7.12H26.436l2.612-2.612a.921.921,0,0,0-1.3-1.3l-4.182,4.183a.966.966,0,0,0-.115.139c-.008.013-.013.026-.021.039a.794.794,0,0,0-.063.119c-.008.02-.012.039-.018.059a1.045,1.045,0,0,0-.034.114.942.942,0,0,0-.018.182.932.932,0,0,0,.018.182,1.139,1.139,0,0,0,.034.114c.007.02.01.039.018.059a1.033,1.033,0,0,0,.063.119c.008.013.013.026.021.039a.9.9,0,0,0,.115.139l4.182,4.182a.921.921,0,1,0,1.3-1.3l-2.61-2.61H40.061a.921.921,0,1,0,0-1.841Z"
       :transform "translate(-23.293 -22.464)"})))

(defnc ViewVerticalIcon
  [{:keys [classes style] :as props}]
  (d/svg
    {:class (str "icon" (or (str " " classes) ""))
     :style (or style {})
     :viewBox "0 0 18 18"
     :xmlns "http://www.w3.org/2000/svg"}
    (d/rect
      {:width "8"
       :height "18"
       :rx "2"})
    (d/rect
      {:width "8"
       :height "18"
       :rx "2"
       :transform "translate(10)"})))

(defnc ViewHorizontalIcon
  [{:keys [classes style] :as props}]
  (d/svg
    {:class (str "icon" (or (str " " classes) ""))
     :style (or style {})
     :viewBox "0 0 18 18"
     :xmlns "http://www.w3.org/2000/svg"}
    (d/rect
      {:width "8"
       :height "18"
       :rx "2"
       :transform "translate(18) rotate(90)"})
    (d/rect
      {:width "8"
       :height "18"
       :rx "2"
       :transform "translate(18 10) rotate(90)"})))

                                        ; (defnc InputHelpIcon
                                        ;   [props]
                                        ;   (d/svg
                                        ;     {:width "18"
                                        ;      :height "18"
                                        ;      :viewBox "0 0 18 18"
                                        ;      :xmlns "http://www.w3.org/2000/svg"}
                                        ;     (d/g
                                        ;       (d/circle
                                        ;         {:cx "9"
                                        ;          :cy "9"
                                        ;          :r "9"})
                                        ;       (d/text
                                        ;         {:x "9"
                                        ;          :y "9"}
                                        ;         "?"))))

(defnc SourceIcon
  [{:keys [classes style] :as props}]
  ;; (c) GitHub
  (d/svg
    {:class (str "icon" (or (str " " classes) ""))
     :style (or style {})
     :viewBox "0 0 98 96"
     :xmlns "http://www.w3.org/2000/svg"}
    (d/path
      {:fill-rule "evenodd"
       :clip-rule "evenodd"
       :d "M48.854 0C21.839 0 0 22 0 49.217c0 21.756 13.993 40.172 33.405 46.69 2.427.49 3.316-1.059 3.316-2.362 0-1.141-.08-5.052-.08-9.127-13.59 2.934-16.42-5.867-16.42-5.867-2.184-5.704-5.42-7.17-5.42-7.17-4.448-3.015.324-3.015.324-3.015 4.934.326 7.523 5.052 7.523 5.052 4.367 7.496 11.404 5.378 14.235 4.074.404-3.178 1.699-5.378 3.074-6.6-10.839-1.141-22.243-5.378-22.243-24.283 0-5.378 1.94-9.778 5.014-13.2-.485-1.222-2.184-6.275.486-13.038 0 0 4.125-1.304 13.426 5.052a46.97 46.97 0 0 1 12.214-1.63c4.125 0 8.33.571 12.213 1.63 9.302-6.356 13.427-5.052 13.427-5.052 2.67 6.763.97 11.816.485 13.038 3.155 3.422 5.015 7.822 5.015 13.2 0 18.905-11.404 23.06-22.324 24.283 1.78 1.548 3.316 4.481 3.316 9.126 0 6.6-.08 11.897-.08 13.526 0 1.304.89 2.853 3.316 2.364 19.412-6.52 33.405-24.935 33.405-46.691C97.707 22 75.788 0 48.854 0z"
       })))

