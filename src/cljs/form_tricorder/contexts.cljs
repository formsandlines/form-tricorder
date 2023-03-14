(ns form-tricorder.contexts
  (:require
    [helix.core :refer [defnc $ <> provider]]
    [helix.hooks :as hooks]
    [helix.dom :as d]
    ["react" :as react]))

(def OutputContext (react/createContext nil))
(def ThemeContext (react/createContext nil))


