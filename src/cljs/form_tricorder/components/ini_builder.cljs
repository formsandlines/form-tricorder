(ns form-tricorder.components.ini-builder
  (:require
   [clojure.set :as set]
   [clojure.string :as string]
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
   [form-tricorder.components.common.toggle-group
    :refer [ToggleGroup ToggleGroupItem]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   [form-tricorder.utils :as utils :refer [let+ unite]]
   ;; ["@radix-ui/react-icons" :as radix-icons]
   ["lucide-react" :as lucide-icons]))

(def r) ;; hotfix for linting error in let+

(defnc IniBuilder
  [{:keys []}]
  (let [bg-ini (rf/subscribe [:modes/background-ini])
        ptn-ini (rf/subscribe [:modes/pattern-ini])
        set-bg-ini #(rf/dispatch [:modes/background-ini
                                  {:next-bg-ini bg-ini}])]))
