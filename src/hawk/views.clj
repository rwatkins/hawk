(ns hawk.views
  (:require [hiccup.core :as h]
            [hawk.models.db :as db]))

(def -transactions [{:id 1 :date "" :account 1 :payee 0 :category 1 :memo "Latte" :inflow 0 :outflow 0}
                    {:id 2 :date "" :account 1 :payee 0 :category 2 :memo "Sandwich" :inflow 0 :outflow 0}
                    {:id 3 :date "" :account 2 :payee 0 :category 3 :memo "Air fare" :inflow 0 :outflow 0}
                    {:id 4 :date "" :account 3 :payee 0 :category 0 :memo "Groceries" :inflow 0 :outflow 0}
                    {:id 5 :date "" :account 1 :payee 0 :category 3 :memo "Train ticket" :inflow 0 :outflow 0}])

(defn find-by-id [collection id]
  (first (filter #(= (str id) (str (:id %))) collection)))

(defn -account-form []
  (identity [:form {:method "post" :action ""}
             [:input {:type "text" :name "name"}]
             [:button {:type "submit"} "Add"]]))

(defn -category-form []
  (identity [:form {:method "post" :action "/categories"}
             [:input {:type "text" :name "name"}]
             [:button {:type "submit"} "Add"]]))

(defn include-css [& paths]
  (for [path paths]
    [:link {:rel "stylesheet" :type "text/css" :href path}]))

(defn include-js [path]
  (identity [:script {:src path :type "text/javascript"}]))

(defn has-transactions [account-id]
  (some #(= account-id (:account %)) -transactions))

(defn account-li [{:keys [id name]}]
  (identity [:li [:a {:href (str "/accounts/" id)} name]]))

(defn accounts-ul [accounts]
  (reduce conj [:ul] (map account-li accounts)))

(defn transactions-ul [transactions]
  (reduce conj [:ul]
          (map #(identity
                  [:li
                   (if (> (:category %) 0)
                     (str "[" (:name (find-by-id (db/all-categories) (:category %))) "] " (:memo %))
                     (:memo %))])
               transactions)))

(defn categories-ul [categories]
  (reduce conj [:ul] (map #(identity [:li (:name %)]) categories)))

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
            (accounts-ul (db/all-accounts))
            (-account-form)
            [:h2 "Categories"]
            (categories-ul (db/all-categories))]}))

(defn new-account [{:keys [account-name]}]
  (when-not (= (count account-name) 0)
    (do (db/create-account {:name account-name})))
  "Created!")

(defn transactions-page [account-id]
  (base-page
    {:title "Hawk - Accounts"
     :body [[:h2 [:a {:href "/accounts"} "Accounts"]]
            [:h3 (str "Transactions for "
                      (:name (find-by-id (db/all-accounts) account-id)))]
            (transactions-ul (filter #(= account-id (str (:account %)))
                                     -transactions))]}))

(defn save-transaction [data]
  (if (nil? (:id data))
    "insert"
    "update"))

(defn new-transaction [data]
  (save-transaction data))
