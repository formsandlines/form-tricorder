(ns form-tricorder.components.export-dialog
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ["react" :as react]
   [shadow.css :refer (css)]
   ;; [garden.color :as gc]
   [form-tricorder.components.common.button :refer [Button]]
   ["@radix-ui/react-dialog" :as Dialog]
   [form-tricorder.utils :refer [unite]]))

(def r) ;; hotfix for linting error in let+

(def Root (.-Root Dialog))
(def Trigger (.-Trigger Dialog))
(def Portal (.-Portal Dialog))
(def Overlay (.-Overlay Dialog))
(def Content (.-Content Dialog))
(def Title (.-Title Dialog))
(def Description (.-Description Dialog))
(def Close (.-Close Dialog))


(defnc ExportTrigger
  [{:keys []}]
  ($d Trigger
    {:as-child true}
    ($ Button
       {:variant :outline
        :size :sm}
       "Export")))

(defnc ExportTitle
  [{:keys [title]}]
  ($d Title
    {:class
     (css :text-lg :mb-4)}
    (or title "Export figure…")))

(defnc ExportPreview
  [{:keys [children class]}]
  (d/div
    {:class
     (unite (css "ExportPreview"
                 :px-4
                 :py-4
                 :rounded-sm
                 :checkerboard
                 {:max-height "400px"
                  :overflow "auto"
                  :display "flex"
                  :align-items "center"
                  :white-space "nowrap"
                  :flex-wrap "nowrap"})
            class)}
    (d/div
      {:class (css {:margin-left "auto"
                    :margin-right "auto"
                    :margin-top "auto"
                    :margin-bottom "auto"})}
      children)))


(defnc ExportItem
  [{:keys [title children class]}]
  (d/div
    {:class (unite "ExportItem" class)}
    (d/h3
      {:class (css :font-size-sm :fg-muted :mb-4)}
      title)
    children))

(defnc ExportGroup
  [{:keys [orientation children class] :or {orientation :vertical}}]
  (d/div
    {:class (unite "ExportGroup" class)
     :data-orientation (name orientation)}
    (d/div
      {:class (str (css "ExportGroup-items"
                        :gap-4
                        {:display "flex"}
                        ["& > *" :border-col])
                   " " (case orientation
                         :horizontal
                         (css
                           {:flex-direction "row"}
                           ["& > *"
                            :border-l :pl-4
                            {:flex "1"}]
                           ["& > *:first-child"
                            :pl-0
                            {:border-left "none"}])
                         :vertical
                         (css
                           {:flex-direction "column"}
                           ["& > *"
                            :border-t :pt-2]
                           ["& > *:first-child"
                            :pt-0
                            {:border-top "none"}])))}
      children)))

(defnc ExportOptions
  [{:keys [children class]}]
  (d/div
    {:class (unite (css "ExportOptions"
                        :mt-6)
                   class)}
    children))

(defnc ExportDialog
  [{:keys [title children on-export class]}]
  ($d Root
    {:class (or class "")
     ;; :default-open true
     }
    ($ ExportTrigger)
    ($d Portal
      ($d Overlay
        {:class (css "inner" :overlay-bg)})
      ($d Content
        {:class (css "outer"
                     :bg :fg :p-8-5 :rounded-md
                     :overlay-content
                     {:width "90vw"})}
        ($ ExportTitle {:title title})
        ;; ($d Description
        ;;   "Set some options.")
        children
        (d/div
          {:class (css "ModalActions"
                       :gap-4 :mt-4
                       {:display "flex"
                        :justify-content "end"})}
          ($d Close
            {:as-child true}
            ($ Button
               {:variant :outline}
               "Cancel"))
          ($d Close
            {:as-child true
             :on-click on-export}
            ($ Button
               "Download")))))))
