(ns hawk.models.db
  (:use compojure.core
        korma.core)
  (:require [clojure.java.jdbc :as sql]
            [clj-time.coerce]
            [clj-time.core]
            [clj-time.format]
            [korma.db]
            [hawk.models.schema :as schema]))

(korma.db/defdb prod schema/db-spec)


;; Account

(defentity account)

(defn all-accounts []
  (select account))

(defn create-account! [{account-name :name}]
  (insert account
          (values [{:name account-name}])))


;; Category

(defentity category)

(defn all-categories []
  (select category))

(defn create-category! [{category-name :name}]
  (insert category
          (values [{:name category-name}])))


;; Transaction

(defn -full-date [date-str]
  (let [fmt (clj-time.format/formatter "yyyy-MM-dd")]
    (clj-time.format/parse fmt date-str)))

(defn -format-date [{date :date :as v}]
  "Expects :date to be in the form of 'yyy-MM-dd', and converts that to
  java.sql.Timestamp."
  (let [d (clj-time.coerce/to-long (-full-date date))]
    (assoc v :date (java.sql.Timestamp. d))))

(defentity
  transaction
  (belongs-to account)
  (belongs-to category)
  (prepare -format-date))

(defn all-transactions []
  (select transaction
          (with category
                (fields [:name :category_name]))))

(defn create-transaction!
  [{:keys [account_id amount category_id date memo]}]
  (insert transaction
          (values [{:account_id account_id
                    :amount amount
                    :category_id category_id
                    :date date
                    :memo memo}])))


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
