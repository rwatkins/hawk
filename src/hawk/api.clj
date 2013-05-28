(ns hawk.api
  (:require [hawk.models.db :as db])
  (:use [ring.util.response :only [response]]))

(defn GET-category []
  (response (db/all-categories)))

(defn GET-account []
  (response (db/all-accounts)))

(defn GET-transaction [params]
  (if (or (empty? params)
          (nil? (:account_id params)))
    (response (db/all-transactions)))
    (response (db/transactions-for-account (Integer. (:account_id params)))))
