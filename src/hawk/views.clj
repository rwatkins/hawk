(ns hawk.views
  (:require [hiccup.core :as h]
            [hawk.models.db :as db]))

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
  (some #(= account-id (:account %)) (db/all-transactions)))

(defn account-li [{:keys [id name]}]
  (identity [:li [:a {:href (str "/accounts/" id)} name]]))

(defn accounts-ul [accounts]
  (reduce conj [:ul] (map account-li accounts)))

(defn transactions-ul [transactions]
  (reduce conj [:ul]
          (map #(identity
                  [:li
                   (if (> (:category_id %) 0)
                     (str "[" (:name (find-by-id (db/all-categories) (:category_id %))) "] " (:memo %))
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
            (categories-ul (db/all-categories))
            (-category-form)]}))

(defn new-account [{account-name :name}]
  (if (not= (count account-name) 0)
    (do
      (db/create-account {:name account-name})
      "Created!")
    "Fail"))

(defn new-category [{category-name :name}]
  (if (not= (count category-name) 0)
    (do (db/create-category {:name category-name})
      "Created!")
    "Fail"))

(defn transactions-page [account-id]
  (let [title "Hawk - Transactions"
        body [[:h2 [:a {:href "/accounts"} "Accounts"]]
              [:h3 (str "Transactions for " (:name (find-by-id (db/all-accounts) account-id)))]
              (transactions-ul (filter #(= account-id (str (:account_id %))) (db/all-transactions)))]]
    (base-page {:title title :body body})))

(defn save-transaction [data]
  (if (nil? (:id data))
    "insert"
    "update"))

(defn new-transaction [data]
  (save-transaction data))
