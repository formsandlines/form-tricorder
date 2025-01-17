(ns form-tricorder.components.common.prose
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [clojure.string :as str]
   [form-tricorder.utils :refer [let+ unite]]
   ["react" :as react]))

(def r) ;; hotfix for linting error in let+

(defnc H1
  [props]
  (let+ [{:keys [children class]
          :rest r} props]
    (d/h1
      {:class (unite class
                     (css :text-2xl :weight-normal
                          :mb-6 :mt-8
                          ["&:first-child" :mt-0]
                          ["&:last-child" :mb-0]))
       & r}
      children)))

(defnc H2
  [props]
  (let+ [{:keys [children class]
          :rest r} props]
    (d/h2
      {:class (unite class
                     (css :text-lg :weight-medium
                          :mb-3 :mt-5
                          ["&:first-child" :mt-0]
                          ["&:last-child" :mb-0]))
       & r}
      children)))

(defnc H3
  [props]
  (let+ [{:keys [children class]
          :rest r} props]
    (d/h3
      {:class (unite class
                     (css :weight-medium
                          :mb-2 :mt-4
                          {:color "var(--col-n26)"}
                          ["&:first-child" :mt-0]
                          ["&:last-child" :mb-0]))
       & r}
      children)))

(def $text-styles (css :weight-normal :line-h-snug))
(def $link-styles (css {:color "var(--col-m21)"
                        :text-decoration "underline"}
                       ["&:hover"
                        {:color "var(--col-m30)"}]))

(defnc P
  [props]
  (let+ [{:keys [children class]
          :rest r} props]
    (d/p
      {:class (unite class
                     $text-styles
                     (css :mb-1
                          ["&:last-child"
                           :mb-0]
                          ))
       & r}
      children)))

(defnc A
  [props]
  (let+ [{:keys [children class]
          :rest r} props]
    (d/a
     {:class (unite class $link-styles)
      :target "_blank"
      & r}
      children)))

(defnc Note
  [props]
  (let+ [{:keys [children class]
          :rest r} props]
    (d/p
      {:class (unite class
                     $text-styles
                     (css
                       :mb-2 :px-4 :py-2 :rounded-sm
                       {:background-color "var(--col-n4)"}
                       ["&:last-child"
                        :mb-0]))
       & r}
      (d/em
        "Note: ")
      children)))

(defnc Ul
  [props]
  (let+ [{:keys [children class]
          :rest r} props]
    (d/ul
      {:class (unite class
                     $text-styles
                     (css
                       :mb-2
                       {:list-style "none"
                        :position "relative"
                        :padding-left "1.2em"}
                       ["&:last-child"
                        :mb-0]
                       ["& li::before"
                        {:content "\"\\2014\""
                         :position "absolute"
                         :left "0"}]
                       ;; {:list-style-type "\"— \"" ;; em-dash + en-space
                       ;;  :list-style-position "outside"
                       ;;  :padding-left "1.6em"}
                       ["& > li ~ li"
                        {:margin-top "0.1em"}]))
       & r}
      children)))

(defnc Dl
  [props]
  (let+ [{:keys [children class dt-left?]
          :rest r} props]
    (d/dl
      {:class (unite class
                     $text-styles
                     (css
                       :mb-2
                       {:display "grid"
                        :column-gap "0.2rem"
                        :grid-template-columns "auto 1fr"}
                       ["&:last-child"
                        :mb-0])
                     (if dt-left?
                       (css ["& > dt"
                             {:text-align "left"}])
                       (css ["& > dt"
                             {:text-align "right"}])))
       & r}
      children)))

(defnc Formula
  [{:keys [children class]}]
  (d/code
   {:class (unite class
                  (css :m-0 :rounded-sm
                       {:background-color "var(--col-m5)"
                        :padding "0.2em 0.4em"
                        :color "var(--col-m29)"
                        :font-size "85%"
                        :white-space "nowrap"}))}
   children))
