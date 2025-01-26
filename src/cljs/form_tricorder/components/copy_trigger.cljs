(ns form-tricorder.components.copy-trigger
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   [shadow.css :refer (css)]
   [form-tricorder.re-frame-adapter :as rf]
   [form-tricorder.utils :as utils :refer [let+ unite]]
   [form-tricorder.components.common.tooltip :as tooltip]))

(defnc CopyTrigger
  [{:keys [text-to-copy tooltip-msg copy-handler children]
    :or {text-to-copy nil
         copy-handler utils/copy-to-clipboard}}]
  (let [[show-tooltip-copied-link?
         set-show-tooltip-copied-link?] (hooks/use-state false)
        report-copy-status (fn [[success? err]]
                             (if success?
                               (set-show-tooltip-copied-link? success?)
                               (rf/dispatch [:error/set
                                             {:error (.toString err)}])))
        handle-copy-link (fn [_] (copy-handler text-to-copy
                                              report-copy-status))]
    (hooks/use-effect
     [show-tooltip-copied-link?]
     (when show-tooltip-copied-link?
       (let [timeout-id (js/setTimeout
                         (fn [] (set-show-tooltip-copied-link? false))
                         800)]
         (fn [] (js/clearTimeout timeout-id)))))
    ($d tooltip/Provider
        {:disableHoverableContent true}
        ($d tooltip/Root
            {:open show-tooltip-copied-link?}
            ($d tooltip/Trigger
                {:asChild true
                 :onClick handle-copy-link}
                children)
            ($ tooltip/Content
               {:class "outer"}
               (d/p (or tooltip-msg "Copied!")))))))
