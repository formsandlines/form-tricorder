((clojurescript-mode
  ;; You use a shadow-cljs to build the project
  ;; This answers the question "which command should be used?"
  (cider-preferred-build-tool . shadow-cljs)
  ;; This sets a default repl type & answers the question "select cljs repl type".
  (cider-default-cljs-repl . shadow)
  ;; This tells shadow cljs what to build & should match a key in shadow-cljs.edn
  ;; build map. e.g :builds {:<some-key> {...}}
  ;; pramas passed to shadow-cljs to start nrepl via cider-jack-in
  (cider-shadow-default-options . "app")))
