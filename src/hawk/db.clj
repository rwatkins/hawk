(ns hawk.db
  (:use korma.db))

(def db-spec {:classname "org.postgresql.Driver" ; must be in classpath
              :subprotocol "postgresql"
              :subname (str "//localhost:5432/clj_hawk")
              ; Any additional keys are passed to the driver
              ; as driver-specific properties.
              :user "riley"
              :password ""})

(defdb prod db-spec)
