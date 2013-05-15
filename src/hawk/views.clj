(ns hawk.views
  (:require [clj-time.coerce]
            [clj-time.format]
            [hiccup.core :as h]
            [hawk.models.db :as db]))

;; Misc

(defn find-by-id [collection id]
  (first (filter #(= (str id) (str (:id %))) collection)))

(defn has-transactions [account-id]
  (some #(= account-id (:account %)) (db/all-transactions)))

(defn include-css [& paths]
  (for [path paths]
    [:link {:rel "stylesheet" :type "text/css" :href path}]))

(defn include-js [path]
  (identity [:script {:src path :type "text/javascript"}]))


;; Forms

(defn -account-form []
  (identity [:form {:method "post" :action "/account"}
             [:input {:type "text" :name "name"}]
             [:button {:type "submit"} "Add"]]))

(defn -category-form []
  (identity [:form {:method "post" :action "/category"}
             [:input {:type "text" :name "name"}]
             [:button {:type "submit"} "Add"]]))

(defn -transaction-form [account-id]
  (identity [:form {:method "post" :action "/transaction"}
             [:label "Date" [:input {:name "date"}]]
             (reduce conj
                     [:select {:name "category_id"}
                      [:option {:value ""} "----"]]
                     (for [c (db/all-categories)]
                       [:option {:value (:id c)} (:name c)]))
             [:input {:name "memo" :type "text"}]
             [:input {:name "amount" :type "text"}]
             [:input {:name "account_id" :type "hidden" :value account-id}]
             [:button {:type "submit"} "Add transaction"]]))


;; Lists

(defn account-li [{:keys [id name]}]
  (identity [:li [:a {:href (str "/account/" id)} name]]))

(defn accounts-ul [accounts]
  (reduce conj [:ul] (map account-li accounts)))

(defn -display-date [sql-date]
  (let [fmt (clj-time.format/formatter "yyyy-MM-dd")]
    (clj-time.format/unparse fmt (clj-time.coerce/from-sql-date sql-date))))

(defn transactions-ul [transactions]
  (reduce
    conj [:ul]
    (map #(identity
            [:li (str "[" (:category_name %) "] "
                      (-display-date (:date %)) ", "
                      (:memo %)
                      ", $" (/ (:amount %) 100))])
         transactions)))

(defn categories-ul [categories]
  (reduce conj [:ul] (map #(identity [:li (:name %)]) categories)))


;; Pages

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
             [:a {:href "/account"} "Accounts"]
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

(defn new-account [{:keys [name]}]
  (if (not= (count name) 0)
    (do
      (db/create-account {:name name})
      "Created!")
    "Fail"))

(defn new-category [{category-name :name}]
  (if (not= (count category-name) 0)
    (do (db/create-category {:name category-name})
      "Created!")
    "Fail"))

(defn transactions-page [account-id]
  (let [title "Hawk - Transactions"
        body [[:h2 [:a {:href "/account"} "Accounts"]]
              [:h3 (str "Transactions for " (:name (find-by-id (db/all-accounts) account-id)))]
              (-transaction-form account-id)
              (transactions-ul (filter #(= account-id (str (:account_id %))) (db/all-transactions)))]]
    (base-page {:title title :body body})))

(defn parse-int [s]
  (Integer. (re-find #"\d+" s)))

(defn save-transaction [tr-map]
  (let [amt (parse-int (:amount tr-map))
        data (assoc tr-map
                    :amount amt
                    :category_id (parse-int (:category_id tr-map))
                    :account_id (parse-int (:account_id tr-map)))
        result (db/create-transaction! data)]
    (str result)))

(defn new-transaction [data]
  (save-transaction data))
