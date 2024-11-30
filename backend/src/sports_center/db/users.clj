(ns sports-center.db.users
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]
            [sports-center.db.core :as db]
            [sports-center.auth.core :as auth]
            [monger.collection :as mc]
            [taoensso.timbre :as log])
  (:import (java.util UUID)))

(def users-table :users)
(def user-profiles-collection "user_profiles")

;; PostgreSQL operations for core user data
(defn create-user!
  [{:keys [email password name role] :as user-data}]
  (db/with-transaction [tx]
    (let [user-id (UUID/randomUUID)
          user (sql/insert! tx users-table
                           {:id user-id
                            :email email
                            :password (auth/hash-password password)
                            :name name
                            :role role
                            :status "active"
                            :created_at (java.time.Instant/now)}
                           {:return-keys true
                            :builder-fn rs/as-unqualified-maps})]
      ;; Create corresponding MongoDB profile
      (mc/insert (db/get-mongodb) user-profiles-collection
                {:user_id (str user-id)
                 :email email
                 :name name
                 :role role
                 :preferences {}
                 :settings {}
                 :created_at (java.time.Instant/now)})
      user)))

(defn get-user-by-id
  [id]
  (sql/get-by-id (db/get-postgres) users-table id
                 {:builder-fn rs/as-unqualified-maps}))

(defn get-user-by-email
  [email]
  (first
   (sql/find-by-keys (db/get-postgres) users-table {:email email}
                     {:builder-fn rs/as-unqualified-maps})))

(defn update-user!
  [id updates]
  (db/with-transaction [tx]
    (let [updates (cond-> updates
                   (:password updates) (update :password auth/hash-password))]
      (sql/update! tx users-table
                  updates
                  {:id id}
                  {:return-keys true
                   :builder-fn rs/as-unqualified-maps}))))

(defn delete-user!
  [id]
  (db/with-transaction [tx]
    ;; Soft delete in PostgreSQL
    (sql/update! tx users-table
                {:status "deleted"
                 :deleted_at (java.time.Instant/now)}
                {:id id})
    ;; Remove profile from MongoDB
    (mc/remove (db/get-mongodb) user-profiles-collection
              {:user_id (str id)})))

;; MongoDB operations for user profiles
(defn get-user-profile
  [user-id]
  (mc/find-one-as-map (db/get-mongodb) user-profiles-collection
                      {:user_id (str user-id)}))

(defn update-user-profile!
  [user-id updates]
  (mc/update (db/get-mongodb) user-profiles-collection
             {:user_id (str user-id)}
             {:$set updates}
             {:upsert true}))

(defn update-user-preferences!
  [user-id preferences]
  (mc/update (db/get-mongodb) user-profiles-collection
             {:user_id (str user-id)}
             {:$set {:preferences preferences}}))

;; Search operations using Elasticsearch
(defn search-users
  [query {:keys [role status page size] :or {page 0 size 10}}]
  (let [es-query {:query
                  {:bool
                   {:must [{:multi_match
                           {:query query
                            :fields ["name^2" "email" "role"]}}]
                    :filter (cond-> []
                             role (conj {:term {:role role}})
                             status (conj {:term {:status status}}))}}}
        results (-> (db/get-elasticsearch)
                   (search-users-index es-query
                                     {:from (* page size)
                                      :size size}))]
    (->> results
         :hits
         :hits
         (map :_source))))
