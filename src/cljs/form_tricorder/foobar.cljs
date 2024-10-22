(ns form-tricorder.foobar
  (:require
   [helix.core :refer [defnc fnc $ <>]]
   ;; [helix.hooks :as hooks]
   [helix.dom :as d :refer [$d]]
   ["lucide-react" :refer [ChevronRight]]
   ;; [form-tricorder.utils :refer [log]]
   [form-tricorder.components.common.button :refer [Button]]
   [form-tricorder.components.common.toggle :refer [Toggle]]
   [form-tricorder.components.common.label :refer [Label]]
   [form-tricorder.components.common.checkbox :refer [Checkbox]]
   [form-tricorder.components.common.input :refer [Input]]
   [form-tricorder.components.common.radio-group
    :refer [RadioGroup RadioGroupItem]]
   [form-tricorder.components.common.select
    :refer [Select SelectTrigger SelectValue SelectItem SelectContent
            SelectGroup SelectLabel]]
   [form-tricorder.stitches-config :refer [css]]))

(def l "inner")

(defnc ScaleTest
  [{:keys [scale n]}]
  (let [rng (rest (range (inc n)))
        test-styles (css {:display "flex"
                          :margin-bottom "10px"
                          "> *"
                          {:padding-right "6px"
                           "> *"
                           {:height "40px"
                            :margin-bottom "2px"
                            :background-color "black"}}})]
    (d/div
     {:class (test-styles)}
     (for [i rng
           :let [styles (css {:width (str "$" scale "$" i)})]]
       (d/div
        {:key (str i)}
        (d/div
         {:class (styles)})
        (str i))))))


(defnc Foobar
  [{:keys []}]
  (let []
    (d/div
      {:style
       {:background-color (str "var(--colors-" l "-bg)")
        :padding "2rem"
        :display "flex"
        :align-items "start"
        :gap "10px"
        :flex-direction "column"
        :border "1px solid red"}}
      (d/h1 "Testing Area")
      ;; ($ Button
      ;;    {}
      ;;    "Okay")

      (d/h2 "Selects")
      (d/div
        {:style {:display "flex"
                 :gap "8px"
                 :width "100%"
                 :margin "0.5rem 0"}}
        ($ Select
           ($ SelectTrigger {:style {:width 180} :layer l}
              ($ SelectValue {:placeholder "Theme"}))
           ($ SelectContent
              {:layer l}
              ($ SelectItem {:value "light" :layer l} "Light")
              ($ SelectItem {:value "dark" :layer l} "Dark")
              ($ SelectItem {:value "system" :layer l} "System")))
        ($ Select
           ($ SelectTrigger {:style {:width 200} :layer l}
              ($ SelectValue {:placeholder "Test Selection"}))
           ($ SelectContent
              {:layer l}
              (for [n (range 50)
                    :let [id (str n)]]
                ($ SelectItem
                   {:key id :value id :layer l}
                   (str "item " n)))))

        ($ Select 
           ($ SelectTrigger {:style {:width "280px"} :layer l} 
              ($ SelectValue {:placeholder "Select a timezone"}))
           ($ SelectContent
              {:layer l}
              ($ SelectGroup 
                 ($ SelectLabel "North America")
                 ($ SelectItem {:layer l :value "est"}
                    "Eastern Standard Time (EST)")
                 ($ SelectItem {:layer l :value "cst"}
                    "Central Standard Time (CST)")
                 ($ SelectItem {:layer l :value "mst"}
                    "Mountain Standard Time (MST)")
                 ($ SelectItem {:layer l :value "pst"}
                    "Pacific Standard Time (PST)")
                 ($ SelectItem {:layer l :value "akst"}
                    "Alaska Standard Time (AKST)")
                 ($ SelectItem {:layer l :value "hst"}
                    "Hawaii Standard Time (HST)"))
              ($ SelectGroup 
                 ($ SelectLabel "Europe & Africa")
                 ($ SelectItem {:layer l :value "gmt"}
                    "Greenwich Mean Time (GMT)")
                 ($ SelectItem {:layer l :value "cet"}
                    "Central European Time (CET)")
                 ($ SelectItem {:layer l :value "eet"}
                    "Eastern European Time (EET)")
                 ($ SelectItem {:layer l :value "west"}
                    "Western European Summer Time (WEST)")
                 ($ SelectItem {:layer l :value "cat"}
                    "Central Africa Time (CAT)")
                 ($ SelectItem {:layer l :value "eat"}
                    "East Africa Time (EAT)"))
              ($ SelectGroup 
                 ($ SelectLabel "Asia")
                 ($ SelectItem {:layer l :value "msk"}
                    "Moscow Time (MSK)")
                 ($ SelectItem {:layer l :value "ist"}
                    "India Standard Time (IST)")
                 ($ SelectItem {:layer l :value "cst_china"}
                    "China Standard Time (CST)")
                 ($ SelectItem {:layer l :value "jst"}
                    "Japan Standard Time (JST)")
                 ($ SelectItem {:layer l :value "kst"}
                    "Korea Standard Time (KST)")
                 ($ SelectItem {:layer l :value "ist_indonesia"} 
                    "Indonesia Central Standard Time (WITA)"))
              ($ SelectGroup 
                 ($ SelectLabel "Australia & Pacific")
                 ($ SelectItem {:layer l :value "awst"} 
                    "Australian Western Standard Time (AWST)")
                 ($ SelectItem {:layer l :value "acst"} 
                    "Australian Central Standard Time (ACST)")
                 ($ SelectItem {:layer l :value "aest"} 
                    "Australian Eastern Standard Time (AEST)")
                 ($ SelectItem {:layer l :value "nzst"}
                    "New Zealand Standard Time (NZST)")
                 ($ SelectItem {:layer l :value "fjt"}
                    "Fiji Time (FJT)"))
              ($ SelectGroup 
                 ($ SelectLabel "South America")
                 ($ SelectItem {:layer l :value "art"}
                    "Argentina Time (ART)")
                 ($ SelectItem {:layer l :value "bot"}
                    "Bolivia Time (BOT)")
                 ($ SelectItem {:layer l :value "brt"}
                    "Brasilia Time (BRT)")
                 ($ SelectItem {:layer l :value "clt"}
                    "Chile Standard Time (CLT)")))))

      (d/h2 "Inputs")
      (d/div
        {:style {:display "flex"
                 :flex-direction "column"
                 :gap "8px"
                 :max-width "400px"
                 :margin "0.5rem 0"}}
        ($ Input {:layer l})
        ($ Input {:type "email" :placeholder "Email" :layer l})
        ($ Input {:type "email" :placeholder "Email" :layer l :disabled true})
        (d/div
          {:style {:display "grid"
                   :width "100%"
                   :max-width "--space-sm"
                   :align-items "center"
                   :margin "0.5rem 0"
                   :gap ".375rem"}}
          ($ Label {:htmlFor "picture"} "Picture")
          ($ Input {:type "file" :id "picture" :layer l}))
        (d/div
          {:style {:display "flex"
                   :width "100%"
                   :max-width "--space-sm"
                   :align-items "center"
                   :margin "0.5rem 0"
                   :gap "0.5rem"}}
          ($ Input {:type "email" :placeholder "Email" :layer l})
          ($ Button {:type "submit" :layer l} "Subscribe"))
        )

      (d/h2 "Checkboxes")
      (d/div
        {:style {:display "flex"
                 :gap 20
                 :margin 10}}
        (for [disabled [false true]
              checked  [true false]
              :let [id (str disabled "-" checked)]]
          (d/div {:key id
                  :style {:display "flex"
                          :align-items "center"
                          :gap 8}}
            ($ Checkbox
               {:id id
                :defaultChecked checked
                :layer l
                :disabled disabled})
            ($ Label {:htmlFor id} "Label"))))

      (d/h2 "Radio Groups")
      (d/div
        ($ RadioGroup
           {:defaultValue "opt1"
            :style {:display "flex"
                    :gap "16px"}}
           (d/div
             {:style {:display "flex"
                      :align-items "center"
                      :gap 8
                      :margin 10}}
             ($ RadioGroupItem
                {:id "opt1"
                 :layer l
                 :value "opt1"})
             ($ Label
                {:htmlFor "opt1"}
                "Option one"))
           (d/div
             {:style {:display "flex"
                      :align-items "center"
                      :gap 8}}
             ($ RadioGroupItem
                {:id "opt2"
                 :layer l
                 :value "opt2"})
             ($ Label
                {:htmlFor "opt2"}
                "Option two"))))

      (d/h2 "Toggles")
      (let [variants ["default" "outline"]
            sizes ["default" "sm" "lg" "icon" "icon-sm"]
            icon-style (css {:width "$icon-sm"
                             :height "$icon-sm"})]
        (d/div
          {:style {:display "grid"
                   :margin 10
                   :grid-template-columns
                   (str "repeat(" (inc (count sizes)) ", min-content)")
                   :grid-auto-rows "auto"
                   :gap "8px"}}
          (for [v variants
                s (conj sizes "muted")]
            (d/div
              {:key (str v "-" s)
               :style {:height "auto"}}
              (d/pre
                {:style {:font-size "10px"
                         :margin-bottom 4}}
                v (d/br) s)
              ($ Toggle {:layer l
                         :variant v
                         :size (if (= "muted" s) "default" s)
                         :disabled (= "muted" s)
                         }
                 (if (or (= "icon" s) (= "icon-sm" s))
                   ($d ChevronRight
                     {:class (icon-style)})
                   "Toggle")
                 )))))
      
      (d/h2 "Buttons")
      (let [variants ["default" "secondary" "destructive"
                      "outline" "ghost" "link"]
            sizes ["default" "sm" "lg" "icon"]
            icon-style (css {:width "$icon-sm"
                             :height "$icon-sm"})]
        (d/div
          {:style {:display "grid"
                   :margin 10
                   :grid-template-columns
                   (str "repeat(" (inc (count sizes)) ", min-content)")
                   :grid-auto-rows "auto"
                   :gap "8px"}}
          (for [v variants
                s (conj sizes "muted")]
            (d/div
              {:key (str v "-" s)
               :style {:height "auto"}}
              (d/pre
                {:style {:font-size "10px"
                         :margin-bottom 4}}
                v (d/br) s)
              ($ Button {:layer l
                         :variant v
                         :size (if (= "muted" s) "default" s)
                         :disabled (= "muted" s)
                         }
                 (if (= "icon" s)
                   ($d ChevronRight
                     {:class (icon-style)})
                   "Button")))))))))
