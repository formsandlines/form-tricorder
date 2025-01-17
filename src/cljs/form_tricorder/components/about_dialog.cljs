(ns form-tricorder.components.about-dialog
  (:require
   [helix.core :refer [defnc fnc $ <> provider]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ["react" :as react]
   [shadow.css :refer (css)]
   ;; [garden.color :as gc]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.prose :as p]
   ["@radix-ui/react-dialog" :as Dialog]
   [form-tricorder.utils :refer [unite]]))

(def Root (.-Root Dialog))
(def Trigger (.-Trigger Dialog))
(def Portal (.-Portal Dialog))
(def Overlay (.-Overlay Dialog))
(def Content (.-Content Dialog))
(def Title (.-Title Dialog))
(def Description (.-Description Dialog))
(def Close (.-Close Dialog))

(defnc AboutDialog
  [{:keys [children]}]
  ($d Root
      {:class "AboutDialog"
       ;; :default-open true
       }
      ($d Trigger
          {:asChild true}
          children)
      ($d Portal
          ($d Overlay
              {:class (css "inner" :overlay-bg)})
          ($d Content
              {:class (css "outer"
                           :bg :fg :p-12 :rounded-md
                           :overlay-content
                           {:width "80vw"
                            :max-width "600px"})}
              ($d Title
                  {:asChild true}
                  ;; {:class (css :text-lg :mb-4)}
                  ($ p/H2 "About")) 
              ($d Description
                  {:asChild true}
                  ($ p/P "FORM tricorder is a “swiss army knife” for evaluation/calculation, representation/visualization and emulation of 4-valued logical FORMs."))
              (d/hr
               {:class (css :mt-6 :mb-4 :border-col)})
              ($ p/Ul
                 {:class (css :text-sm)}
                 (d/li "Concept and development 2018–2025 by (myself) " ($ p/A {:href "https://formsandlines.eu"} "Peter Hofmann") ".")
                 (d/li "Built upon the FORM logic library " ($ p/A {:href "https://formform.dev"} "formform") ", my passion-project for many years.")
                 (d/li "Algorithms and theory based on demonstrations in the book " ($ p/A {:href "https://uformiform.info"} "uFORM iFORM") " by Ralf Peyn (2017), the " ($ p/A {:href "https://en.wikipedia.org/wiki/Laws_of_Form"} "Laws of Form") " by George Spencer-Brown (1969–2015), as well as my own humble research and ideas."))
              (d/div
               {:class (css "ModalActions"
                            :gap-4 :mt-4
                            {:display "flex"
                             :justify-content "end"})}
               ($d Close
                   {:as-child true}
                   ($ Button
                      {:variant :secondary}
                      "Close")))))))
