(ns sports-center.db.connection
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [hikari-cp.core :as hikari]
            [sports-center.config :as config]
            [taoensso.timbre :as log])
  (:import (com.zaxxer.hikari HikariDataSource)))

(def datasource-options
  {:auto-commit        true
   :read-only         false
   :connection-timeout 30000
   :validation-timeout 5000
   :idle-timeout      600000
   :max-lifetime      1800000
   :minimum-idle      10
   :maximum-pool-size 10
   :pool-name         "db-pool"
   :adapter           "postgresql"
   :username          (config/get-env :postgres-user)
   :password          (config/get-env :postgres-password)
   :database-name     (config/get-env :postgres-db)
   :server-name       (config/get-env :postgres-host)
   :port-number       (config/get-env :postgres-port)
   :register-mbeans   false})

(defonce datasource (atom nil))

(defn init-pool! []
  (when (nil? @datasource)
    (log/info "Initializing database connection pool")
    (reset! datasource (hikari/make-datasource datasource-options))))

(defn close-pool! []
  (when @datasource
    (log/info "Closing database connection pool")
    (hikari/close-datasource @datasource)
    (reset! datasource nil)))

(defn get-connection []
  (when (nil? @datasource)
    (init-pool!))
  (jdbc/get-connection @datasource))

(defmacro with-db-transaction [binding & body]
  `(jdbc/with-transaction [~(first binding) (get-connection) {:isolation :serializable}]
     ~@body))
