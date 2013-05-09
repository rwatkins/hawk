(ns hawk.models.db
  (:use compojure.core
        korma.core)
  (:require [clojure.java.jdbc :as sql]
            [korma.db]
            [hawk.models.schema :as schema]))

(korma.db/defdb prod schema/db-spec)

(defentity account)

(defn all-accounts []
  (select account))

(defn create-account [{:keys [name]}]
  (insert account
          (values [{:name name}])))

(defn init-data []
  (insert account
          (values [{:name "SF Fire Checking"}
                   {:name "SF Fire Money Market"}
                   {:name "Simple"}])))
