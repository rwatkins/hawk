(ns hawk.api
  (:require [hawk.models.db :as db])
  (:use [ring.util.response :only [response]]))

(defn GET-category []
  (response (db/all-categories)))

(defn GET-account []
  (response (db/all-accounts)))
