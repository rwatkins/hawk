(ns hawk.handler
  (:use compojure.core korma.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [clojure.java.jdbc :as sql]
            [korma.db]
            [hawk.api :as api]
            [hawk.views :as views]
            [ring.middleware.json :as middleware]))

(defroutes app-routes
  (GET "/" [] (views/index-page))
  (context "/account" []
    (GET  "/"    [] (views/accounts-page))
    (POST "/"    [& data] (views/new-account data))
    (GET  "/:id" [id] (views/transactions-page id)))
  (POST "/category" [& data] (views/new-category data))
  (POST "/transaction" [& data] (views/new-transaction data))
  (GET "/api/account" [] (api/GET-account))
  (GET "/api/category" [] (api/GET-category))
  (GET "/api/transaction" {params :params} (api/GET-transaction params))
  (route/files "/static" {:root (str (System/getProperty "user.dir") "/resources")})
  (route/not-found "Not Found"))

(defn wrap-print-req [app]
  (fn [req]
    (println (assoc {} :form-params (:form-params req)
                       :query-params (:query-params req)
                       :request-method (:request-method req)))
    (app req)))

(def app
  (-> (handler/site app-routes)
    (middleware/wrap-json-body)
    (middleware/wrap-json-response)))
