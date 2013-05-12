(ns hawk.models.schema
  ;(:use korma.db)
  (:require [clojure.java.jdbc :as sql]))

(def db-spec {:classname "org.postgresql.Driver" ; must be in classpath
              :subprotocol "postgresql"
              :subname "//localhost:5432/clj_hawk"
              ; Any additional keys are passed to the driver
              ; as driver-specific properties.
              :user "riley"
              :password ""})

(defn create-account-table
  "Creates the account table"
  []
  (sql/with-connection db-spec
    (sql/create-table
      :account
      [:id :serial "primary key"]
      [:name "varchar(255)"])))

(defn drop-account-table []
  (sql/with-connection
    db-spec
    (sql/drop-table :account)))

(defn create-category-table
  "Creates the category table"
  []
  (sql/with-connection
    db-spec
    (sql/create-table
      :category
      [:id :serial "primary key"]
      [:name "varchar(255) not null"])))

(defn create-transaction-table
  "Creates the transaction table"
  []
  (sql/with-connection
    db-spec
    (sql/create-table
      :transaction
      [:id :serial "primary key"]
      [:account_id "int references account (id) on delete set null"]
      [:category_id "int references category (id) on delete set null"]
      [:amount "bigint not null default 0"]
      [:memo "text not null"]
      [:date "timestamp with time zone not null"])))

;{:id 2 :date "" :account 1 :payee 0 :category 2 :memo "Sandwich" :inflow 0 :outflow 0}

(defn create-tables
  "Creates all database tables"
  []
  (create-account-table)
  (create-category-table)
  (create-transaction-table))

(defn drop-tables
  "Drops all database tables"
  []
  (sql/with-connection
    db-spec
    (doseq [table [:transaction
                   :category
                   :account]]
      (try
        (sql/drop-table table)
        (catch Exception _)))))

(defn show-tables []
  (sql/with-connection
    db-spec
    (sql/with-query-results
      res
      ["SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name"]
      (doseq [row res]
        (println (:table_name row))))))

(defn init-db-postgres []
  (sql/with-connection db-spec
    (let [id [:id "bigserial primary key"]
          varchar "varchar(255) not null"
          nullchar "varchar(255)"
          text "text not null"
          desc [:title varchar]
          url [:url varchar]
          timestamp [:date_created "timestamp default localtimestamp"]
          tables [[:categories id desc url
                   [:num_posts "bigint default 0"]]
                  [:tags id desc url
                   [:num_posts "bigint default 0"]]
                  [:users id
                   [:username varchar]
                   [:password varchar]
                   [:salt varchar]]
                  [:posts id url timestamp
                   [:user_id "bigint default 1 references users(id) on delete set default"]
                   [:category_id "bigint default 1 references categories (id) on delete set default"]
                   [:status varchar]
                   [:type varchar]
                   [:title varchar]
                   [:parent_id "bigint references posts (id)"]
                   [:markdown text]
                   [:html text]
                   [:num_comments "bigint default 0"]]
                  [:comments id timestamp
                   [:post_id "bigint references posts (id) on delete cascade"]
                   [:status varchar]
                   [:author varchar]
                   [:email nullchar]
                   [:homepage nullchar]
                   [:ip varchar]
                   [:markdown text]
                   [:html text]]
                  [:post_tags id
                   [:post_id "bigint not null references posts (id) on delete cascade"]
                   [:tag_id "bigint not null references tags (id) on delete cascade"]]]]
      (doseq [[table & _] (reverse tables)]
        (sql/do-commands (str "drop table if exists " (name table)))
        (println "Dropped" table "(if it existed)."))
      (doseq [[table & specs] tables]
        (apply sql/create-table table specs)
        (println "Created" table)))
    ;; Add unique constraints on 'url' column
    (doseq [table [:posts :categories :tags]]
      (sql/do-commands (str "alter table " (name table) " add constraint " (name table) "_url unique (url)")))
    ;; Create indexes
    (doseq [[table & cols] [[:posts :category_id]
                            [:post_tags :post_id :tag_id]
                            [:comments :post_id]]
            col cols]
      (sql/do-commands (str "create index " (name table) "_" (name col)
                            " on " (name table) "(" (name col) ")")))
    (println "Added indices")
    (sql/insert-records :users {:username "Nobody" :password "" :salt ""})
    (sql/insert-records :categories {:title "Uncategorized" :url "uncategorized"})
    (println "Initialized" :categories)))
