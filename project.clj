(defproject hawk "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.2"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [korma "0.3.0-RC5"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler hawk.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
