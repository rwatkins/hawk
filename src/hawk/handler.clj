(ns hawk.handler
  (:use compojure.core korma.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [hiccup.core :as h]
            [clojure.java.jdbc :as sql]
            [korma.db]
            [hawk.models.account :as account]))


;; Data

(def -categories [{:id 1 :name "Coffee"}
                  {:id 2 :name "Restaurants"}
                  {:id 3 :name "Travel"}])

(def -transactions [{:id 1
                     :date ""
                     :account 1
                     :payee 0
                     :category 1
                     :memo "Latte"
                     :inflow 0
                     :outflow 0}
                    {:id 2
                     :date ""
                     :account 1
                     :payee 0
                     :category 2
                     :memo "Sandwich"
                     :inflow 0
                     :outflow 0}
                    {:id 3
                     :date ""
                     :account 2
                     :payee 0
                     :category 3
                     :memo "Air fare"
                     :inflow 0
                     :outflow 0}
                    {:id 4
                     :date ""
                     :account 3
                     :payee 0
                     :category 0
                     :memo "Groceries"
                     :inflow 0
                     :outflow 0}
                    {:id 5
                     :date ""
                     :account 1
                     :payee 0
                     :category 3
                     :memo "Train ticket"
                     :inflow 0
                     :outflow 0}])


(def ^:dynamic *db*
  (ref {:categories -categories
        :transactions -transactions}))

;(dosync
;  (ref-set *db*
;           (let [old-table (:accounts @*db*)]
;             (assoc-in @*db*
;                       [:accounts]
;                       (conj old-table {:id 5 :name "Capital One"})))))

(defn find-by-id [collection id]
  (first (filter #(= (str id) (str (:id %))) collection)))

(defn save-transaction [data]
  (if (nil? (:id data))
    "insert"
    "update"))


;; View helpers

(defn -account-form []
  (identity [:form {:method "post" :action ""}
             [:input {:type "text" :name "name"}]
             [:button {:type "submit"} "Add"]]))

(defn include-css [path]
  [:link {:rel "stylesheet" :type "text/css" :href path}])

(defn include-js [path]
  (identity [:script {:src path :type "text/javascript"}]))

(defn has-transactions [account-id]
  (some #(= account-id (:account %)) (:transactions @*db*)))

(defn account-li [{:keys [id name]}]
  (identity
    (if (has-transactions id)
      [:li [:a {:href (str "/accounts/" id)} name]]
      [:li name])))

(defn accounts-ul [accounts]
  (reduce conj [:ul] (map account-li accounts)))

(defn transactions-ul [transactions]
  (reduce conj [:ul]
          (map #(identity
                  [:li
                   (if (> (:category %) 0)
                     (str "[" (:name (find-by-id (:categories @*db*) (:category %))) "] " (:memo %))
                     (:memo %))])
               transactions)))

(defn categories-ul [categories]
  (reduce conj [:ul] (map #(identity [:li (:name %)]) categories)))

;; Views

(defn base-page [context-map]
  (h/html
    [:html
     [:head
      [:title (let [title (:title context-map)]
                (if (nil? title) "Hawk" title))]
      (include-css "/static/css/base.css")]
     (conj (into [:body [:h1 [:a {:href "/"} "Hawk"]]]
                 (:body context-map))
           (include-js "/static/lib/jquery-1.9.1.min.js"))]))

(defn index-page []
  (base-page
    {:body [[:p
             [:a {:href "/accounts"} "Accounts"]
             [:br] "Click here to see all of your accounts."]]}))

(defn accounts-page []
  (base-page
    {:title "Hawk - Accounts"
     :body [[:h2 "Accounts"]
            (accounts-ul (account/all))
            (-account-form)
            [:h2 "Categories"]
            (categories-ul (:categories @*db*))]}))

(defn new-account [{:keys [name]}]
  (when-not (= (count name) 0)
    (do (account/create {:name name})))
  "Created!")

(defn transactions-page [account-id]
  (base-page
    {:title "Hawk - Accounts"
     :body [[:h2 [:a {:href "/accounts"} "Accounts"]]
            [:h3 (str "Transactions for "
                      (:name (find-by-id (account/all) account-id)))]
            (transactions-ul (filter #(= account-id (str (:account %)))
                                     (:transactions @*db*)))]}))

(defn new-transaction [data]
  (save-transaction data))


;; Routes

(defroutes app-routes
  (GET "/" [] (index-page))
  (GET "/accounts" [] (accounts-page))
  (POST "/accounts" [& data] (new-account data))
  (GET "/accounts/:id" [id] (transactions-page id))
  (POST "/transactions" [data] (new-transaction data))

  (route/files "/static" {:root (str (System/getProperty "user.dir") "/resources")})

  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
