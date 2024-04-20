(defproject jtk-dvlp/cljs-workers "1.2.0-beta"
  :description
  "A clojurescript lib for performing async tasks via web workers"

  :url
  "https://github.com/jtkDvlp/cljs-workers"

  :license
  {:name "MIT"}

  :min-lein-version
  "2.5.3"

  :source-paths
  ["src"]

  :resource-paths
  ["target/main" "target/worker"]

  :plugins
  [[lein-ancient "0.7.0"]]

  :profiles
  {:provided
   {:dependencies
    [[org.clojure/clojure "1.10.0"]
     [org.clojure/clojurescript "1.10.773"]
     [org.clojure/core.async "1.6.681"]]}

   :dev
   {:dependencies
    [[com.bhauman/figwheel-main "0.2.12"]]}

   :repl
   {:dependencies
    [[cider/piggieback "0.5.0"]]

    :repl-options
    {:nrepl-middleware
     [cider.piggieback/wrap-cljs-repl]}}}

  :aliases
  {"build-worker"
   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "worker"]})
