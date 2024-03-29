(ns hawk.views
  (:require [clj-time.coerce]
            [clj-time.format]
            [ring.util.response]
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

(defn include-js [& paths]
  (for [path paths]
    [:script {:src path :type "text/javascript"}]))


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
             (into [:select {:name "category_id"} [:option {:value ""} "----"]]
                   (map #(identity [:option {:value (:id %)} (:name %)])
                        (db/all-categories)))
             [:input {:name "memo" :type "text"}]
             [:input {:name "amount" :type "text"}]
             [:input {:name "account_id" :type "hidden" :value account-id}]
             [:button {:type "submit"} "Add transaction"]]))


;; Lists

(defn account-li [{:keys [id name]}]
  (identity [:li [:a {:href (str "/account/" id)} name]]))

(defn accounts-ul [accounts]
  (into [:ul] (map account-li accounts)))

(defn -display-date [sql-date]
  (let [fmt (clj-time.format/formatter "yyyy-MM-dd")]
    (clj-time.format/unparse fmt (clj-time.coerce/from-sql-date sql-date))))

(defn transactions-ul [transactions]
  (into [:ul]
        (map #(identity
                [:li (str (-display-date (:date %)) ", "
                          "[" (:category_name %) "] "
                          (:memo %)
                          ", $" (/ (:amount %) 100))])
             transactions)))

(defn categories-ul [categories]
  (into [:ul] (map #(identity [:li (:name %)]) categories)))


;; Pages

(defn base-page [context-map]
  (h/html
    [:html {:ng-app "hawk"}
     [:head
      [:title (let [title (:title context-map)]
                (if (nil? title) "Hawk" title))]
      (include-css "/static/css/base.css")]
     (into [:body [:h1 [:a {:href "/"} "Hawk"]]
                  (into [:div {:ng-view ""}]
                        (:body context-map))]
           (include-js "/static/lib/jquery-1.9.1.min.js"
                       "/static/lib/angular.js"
                       "/static/js/app.js"
                       "/static/js/controllers.js"))]))

(defn index-page []
  (base-page
    {:body [[:p
             [:a {:href "#/account"} "Accounts"]
             [:br] "Click here to see all of your accounts."]]}))

(defn accounts-page []
  (base-page
    {:title "Hawk - Accounts"
     :body [[:h2 "Accounts"]
            [:ul {:ng-controller "AccountListCtrl"}
             [:li {:ng-repeat "account in accounts"}
              "<a href=\"#/account/{{account.id}}\">{{account.name}}</a>"]]
            (-account-form)
            ;[:h2 "Categories"]
            ;[:div {:ng-controller "CategoryListCtrl"}
            ; [:div "Search" [:input {:type "text" :ng-model "query"}]]
            ; [:ul
            ;  [:li {:ng-repeat "category in categories | filter:query"}
            ;   "{{category.name}}"]]]
            ;(-category-form)
            ]}))

(defn new-account [{:keys [name]}]
  (if (not= (count name) 0)
    (do
      (db/create-account! {:name name})
      "Created!")
    "Fail"))

(defn new-category [{category-name :name}]
  (if (not= (count category-name) 0)
    (do
      (db/create-category! {:name category-name})
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
  (when-not (or (nil? s) (empty? s))
    (Integer. (re-find #"\d+" s))))

(defn validator-for-key [key]
  (condp = key
    :account_id (fn [value]
                  (when (or (nil? value) (zero? value))
                    "'account' field is required"))
    :amount #(println %)
    :category_id nil
    :date (fn [value]
            (when (empty? value) "'date' field is required"))
    :memo nil))

(defn validate-transaction [t]
  (loop [errors {} all-keys (keys t) k (first all-keys)]
    (let [new-error (when-let [validate-fn (validator-for-key k)]
                      (validate-fn (k t)))
          errors (if (nil? new-error) errors (assoc errors k new-error))]
      (if (next all-keys)
        (recur errors (next all-keys) (first (next all-keys)))
        (when-not (empty? errors) errors)))))

(defn save-transaction [tr-map]
  (let [amt (or (parse-int (:amount tr-map)) 0)
        data (assoc tr-map
                    :amount amt
                    :category_id (parse-int (:category_id tr-map))
                    :account_id (parse-int (:account_id tr-map)))
        errors (validate-transaction data)]
    (if (nil? errors)
      (db/create-transaction! data)
      {:error (str errors)})))

(defn new-transaction [data]
  (let [res (save-transaction data)]
    (if-let [error (:error res)]
      error
      (ring.util.response/redirect (str "/account/" (:account_id data))))))
