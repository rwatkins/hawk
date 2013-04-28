(ns hawk.models.account
  (:use compojure.core korma.core)
  (:require [clojure.java.jdbc :as sql]
            [korma.db]
            [hawk.db :as db]))

(defentity account)

(defn init-data []
  (insert account
          (values [{:name "SF Fire Checking"}
                   {:name "SF Fire Money Market"}
                   {:name "Simple"}])))

(defn -drop-table []
  (sql/drop-table :account))

(defn reset-table []
  (sql/with-connection db/db-spec
    (try
      (-drop-table)
      (catch Exception e
        (println (.getNextException e))))
    (sql/create-table :account
                      [:id :serial "primary key"]
                      [:name "varchar(255)"])))

(defn bootstrap []
  (reset-table)
  (init-data))

(defn all []
  (select account))

(defn create [{:keys [name]}]
  (insert account
          (values [{:name name}])))
