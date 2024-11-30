(ns sports-center.db.core
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [monger.core :as mg]
            [monger.collection :as mc]
            [taoensso.carmine :as car]
            [clojurewerkz.elastisch.rest :as esr]
            [neo4j-clj.core :as neo4j]
            [influxdb-client.core :as influx]
            [sports-center.config :as config]
            [taoensso.timbre :as log]
            [com.stuartsierra.component :as component])
  (:import (com.zaxxer.hikari HikariDataSource)))

;; Database connection specs and pools
(defonce ^:private system (atom nil))

(defrecord PostgresDB [datasource]
  component/Lifecycle
  (start [this]
    (if datasource
      this
      (let [db-spec {:jdbcUrl (config/get-env :postgres-url)
                     :username (config/get-env :postgres-user)
                     :password (config/get-env :postgres-password)}]
        (log/info "Starting PostgreSQL connection pool")
        (assoc this :datasource (connection/->pool HikariDataSource db-spec)))))
  
  (stop [this]
    (when datasource
      (log/info "Stopping PostgreSQL connection pool")
      (.close datasource))
    (assoc this :datasource nil)))

(defrecord MongoDB [conn db]
  component/Lifecycle
  (start [this]
    (if conn
      this
      (let [{:keys [conn db]} (mg/connect-via-uri (config/get-env :mongodb-uri))]
        (log/info "Connecting to MongoDB")
        (assoc this :conn conn :db db))))
  
  (stop [this]
    (when conn
      (log/info "Disconnecting from MongoDB")
      (mg/disconnect conn))
    (assoc this :conn nil :db nil)))

(defrecord RedisDB [pool spec]
  component/Lifecycle
  (start [this]
    (if pool
      this
      (let [spec {:pool {} :spec {:uri (config/get-env :redis-url)}}]
        (log/info "Connecting to Redis")
        (assoc this :pool (car/wcar spec) :spec spec))))
  
  (stop [this]
    (when pool
      (log/info "Disconnecting from Redis"))
    (assoc this :pool nil :spec nil)))

(defrecord ElasticsearchDB [conn]
  component/Lifecycle
  (start [this]
    (if conn
      this
      (let [conn (esr/connect (config/get-env :elasticsearch-url))]
        (log/info "Connecting to Elasticsearch")
        (assoc this :conn conn))))
  
  (stop [this]
    (when conn
      (log/info "Disconnecting from Elasticsearch"))
    (assoc this :conn nil)))

(defrecord Neo4jDB [driver]
  component/Lifecycle
  (start [this]
    (if driver
      this
      (let [driver (neo4j/connect (config/get-env :neo4j-uri)
                                 (config/get-env :neo4j-user)
                                 (config/get-env :neo4j-password))]
        (log/info "Connecting to Neo4j")
        (assoc this :driver driver))))
  
  (stop [this]
    (when driver
      (log/info "Disconnecting from Neo4j")
      (.close driver))
    (assoc this :driver nil)))

(defrecord InfluxDB [client]
  component/Lifecycle
  (start [this]
    (if client
      this
      (let [client (influx/create-client {:url (config/get-env :influxdb-url)
                                         :token (config/get-env :influxdb-token)
                                         :org (config/get-env :influxdb-org)})]
        (log/info "Connecting to InfluxDB")
        (assoc this :client client))))
  
  (stop [this]
    (when client
      (log/info "Disconnecting from InfluxDB")
      (.close client))
    (assoc this :client nil)))

(defn new-system []
  (component/system-map
   :postgres (->PostgresDB nil)
   :mongodb (->MongoDB nil nil)
   :redis (->RedisDB nil nil)
   :elasticsearch (->ElasticsearchDB nil)
   :neo4j (->Neo4jDB nil)
   :influxdb (->InfluxDB nil)))

(defn start! []
  (reset! system (component/start (new-system))))

(defn stop! []
  (when @system
    (component/stop @system)
    (reset! system nil)))

;; Database access helpers
(defn get-postgres []
  (get-in @system [:postgres :datasource]))

(defn get-mongodb []
  (get-in @system [:mongodb :db]))

(defn get-redis-conn []
  (get-in @system [:redis :spec]))

(defn get-elasticsearch []
  (get-in @system [:elasticsearch :conn]))

(defn get-neo4j []
  (get-in @system [:neo4j :driver]))

(defn get-influxdb []
  (get-in @system [:influxdb :client]))

;; Database operations wrapper
(defmacro with-transaction [binding & body]
  `(jdbc/with-transaction [~(first binding) (get-postgres) {}]
     ~@body))

;; Team operations
(defn create-team! [team]
  (with-transaction [tx (get-in @system [:postgres :datasource])]
    (jdbc/execute-one! tx
      ["INSERT INTO teams (name, description, sport, creator_id)
        VALUES (?, ?, ?, ?)
        RETURNING *"
       (:name team)
       (:description team)
       (:sport team)
       (:creator_id team)])))

(defn get-team [id]
  (jdbc/execute-one! (get-in @system [:postgres :datasource])
    ["SELECT * FROM teams WHERE id = ?" id]))

(defn update-team! [id team]
  (jdbc/execute-one! (get-in @system [:postgres :datasource])
    ["UPDATE teams 
      SET name = ?, description = ?, sport = ?
      WHERE id = ?"
     (:name team)
     (:description team)
     (:sport team)
     id]))

(defn delete-team! [id]
  (jdbc/execute-one! (get-in @system [:postgres :datasource])
    ["DELETE FROM teams WHERE id = ?" id]))

(defn get-team-members [team-id]
  (jdbc/execute! (get-in @system [:postgres :datasource])
    ["SELECT u.*, tm.role
      FROM team_members tm
      JOIN users u ON tm.user_id = u.id
      WHERE tm.team_id = ?"
     team-id]))

;; Training session operations
(defn create-training-session! [session]
  (with-transaction [tx (get-in @system [:postgres :datasource])]
    (jdbc/execute-one! tx
      ["INSERT INTO training_sessions 
        (team_id, title, description, start_time, end_time, location, created_by)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        RETURNING *"
       (:team_id session)
       (:title session)
       (:description session)
       (:start_time session)
       (:end_time session)
       (:location session)
       (:created_by session)])))

(defn get-team-sessions [team-id]
  (jdbc/execute! (get-in @system [:postgres :datasource])
    ["SELECT * FROM training_sessions 
      WHERE team_id = ? 
      ORDER BY start_time DESC"
     team-id]))

(defn record-performance-metric! [metric]
  (jdbc/execute-one! (get-in @system [:postgres :datasource])
    ["INSERT INTO performance_metrics 
      (session_id, athlete_id, metric_type, metric_value, recorded_by)
      VALUES (?, ?, ?, ?::jsonb, ?)
      RETURNING *"
     (:session_id metric)
     (:athlete_id metric)
     (:metric_type metric)
     (:metric_value metric)
     (:recorded_by metric)]))

(defn init! []
  (start!))
