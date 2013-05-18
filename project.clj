(defproject hawk "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [clj-time "0.5.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.2"]
                 [korma "0.3.0-RC5"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [ring/ring-json "0.2.0"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler hawk.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
