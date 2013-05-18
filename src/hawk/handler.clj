(ns hawk.handler
  (:use compojure.core korma.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.java.jdbc :as sql]
            [korma.db]
            [hawk.views :as views]))

(defroutes app-routes
  (GET "/" [] (views/index-page))
  (GET "/account" [] (views/accounts-page))
  (POST "/account" [& data] (views/new-account data))
  (GET "/account/:id" [id] (views/transactions-page id))
  (POST "/category" [& data] (views/new-category data))
  (POST "/transaction" [& data] (views/new-transaction data))

  (route/files "/static" {:root (str (System/getProperty "user.dir") "/resources")})

  (route/not-found "Not Found"))

(defn wrap-print-req [app]
  (fn [req]
    (println (assoc {} :form-params (:form-params req)
                       :query-params (:query-params req)
                       :request-method (:request-method req)))
    (app req)))

(def app
  (handler/site app-routes))
