(ns form-tricorder.contexts
  (:require
    [helix.core :refer [defnc $ <> provider]]
    [helix.hooks :as hooks]
    [helix.dom :as d]
    ["react" :as react]))

(def OutputContext (react/createContext nil))
(def ViewsContext (react/createContext nil))
(def ThemeContext (react/createContext nil))

#_(defnc ViewsProvider [{:keys [children]}]
    (let [[views dispatch] ()]
      (provider
        {:context ViewsContext
         :value [{:mode "a" :func "a" :active true}
                 {:mode "c" :func "b" :active true}]}
        children)))
