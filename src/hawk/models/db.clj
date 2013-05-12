(ns hawk.models.db
  (:use compojure.core
        korma.core)
  (:require [clojure.java.jdbc :as sql]
            [korma.db]
            [hawk.models.schema :as schema]))

(korma.db/defdb prod schema/db-spec)


;; Account

(defentity account)

(defn all-accounts []
  (select account))

(defn create-account [{account-name :name}]
  (insert account
          (values [{:name account-name}])))


;; Category

(defentity category)

(defn all-categories []
  (select category))

(defn create-category [{category-name :name}]
  (insert category
          (values [{:name category-name}])))


;; Miscellaneous

(defn init-data []
  (insert account
          (values [{:name "SF Fire Checking"}
                   {:name "SF Fire Money Market"}
                   {:name "Simple"}]))
  (insert category
          (values [{:name "Coffee"}
                   {:name "Restaurants"}
                   {:name "Travel"}])))
