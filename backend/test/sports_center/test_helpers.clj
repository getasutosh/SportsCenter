(ns sports-center.test-helpers
  (:require [sports-center.db.connection :as db]
            [sports-center.auth.core :as auth]
            [cheshire.core :as json]))

(def test-user
  {:id #uuid "550e8400-e29b-41d4-a716-446655440000"
   :email "test@example.com"
   :password "password123"
   :name "Test User"})

(def test-team
  {:id #uuid "660e8400-e29b-41d4-a716-446655440000"
   :name "Test Team"
   :sport "Football"
   :description "Test team description"})

(defn create-test-user! []
  (db/with-db-transaction [tx (db/get-connection)]
    (jdbc/execute-one! tx
      ["INSERT INTO users (id, email, password_hash, name)
        VALUES (?, ?, ?, ?)"
       (:id test-user)
       (:email test-user)
       (auth/hash-password (:password test-user))
       (:name test-user)])))

(defn create-test-team! []
  (db/with-db-transaction [tx (db/get-connection)]
    (jdbc/execute-one! tx
      ["INSERT INTO teams (id, name, sport, description, creator_id)
        VALUES (?, ?, ?, ?, ?)"
       (:id test-team)
       (:name test-team)
       (:sport test-team)
       (:description test-team)
       (:id test-user)])))

(defn clean-db! []
  (db/with-db-transaction [tx (db/get-connection)]
    (jdbc/execute! tx ["DELETE FROM team_members"])
    (jdbc/execute! tx ["DELETE FROM teams"])
    (jdbc/execute! tx ["DELETE FROM users"])))

(defn with-clean-db [f]
  (clean-db!)
  (f)
  (clean-db!))

(defn auth-header [token]
  {"Authorization" (str "Bearer " token)})

(defn login-test-user []
  (auth/create-token test-user))
