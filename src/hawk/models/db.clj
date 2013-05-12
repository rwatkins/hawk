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

;; Category

(defentity category)

(defn all-categories []
  (select category))

(defn create-category [{category-name :name}]
  (insert category
          (values [{:name category-name}])))


(defn init-data []
  (insert account
          (values [{:name "SF Fire Checking"}
                   {:name "SF Fire Money Market"}
                   {:name "Simple"}]))
  (insert category
          (values [{:name "Coffee"}
                   {:name "Restaurants"}
                   {:name "Travel"}])))
