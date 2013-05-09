(ns hawk.handler
  (:use compojure.core korma.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.java.jdbc :as sql]
            [korma.db]
            [hawk.views :as views]))

(defroutes app-routes
  (GET "/" [] (views/index-page))
  (GET "/accounts" [] (views/accounts-page))
  (POST "/accounts" [& data] (views/new-account data))
  (GET "/accounts/:id" [id] (views/transactions-page id))
  (POST "/transactions" [& data] (views/new-transaction data))

  (route/files "/static" {:root (str (System/getProperty "user.dir") "/resources")})

  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
