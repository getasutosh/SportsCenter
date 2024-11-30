(ns sports-center.metrics.core
  (:require [sports-center.db.core :as db]
            [taoensso.timbre :as log]
            [influxdb-client.core :as influx]
            [monger.collection :as mc]
            [clojure.core.async :as async :refer [>! <! go-loop chan]]
            [cheshire.core :as json]))

;; Metrics buffer channel
(def metrics-channel (chan 1000))

;; InfluxDB measurement names
(def ^:private measurements
  {:performance "performance_metrics"
   :session "session_metrics"
   :system "system_metrics"
   :analytics "analytics_metrics"})

(defn start-metrics-processor! []
  (go-loop []
    (when-let [metric (<! metrics-channel)]
      (try
        (process-metric! metric)
        (catch Exception e
          (log/error e "Failed to process metric:" metric)))
      (recur))))

(defn store-metrics!
  "Store metrics data in InfluxDB"
  [{:keys [type data timestamp] :as metric}]
  (let [measurement (get measurements type)
        point (-> {:measurement measurement
                  :tags (select-keys data [:user_id :session_id :event_type])
                  :fields (dissoc data :user_id :session_id :event_type)
                  :timestamp timestamp})]
    (try
      (influx/write-point (db/get-influxdb) point)
      (catch Exception e
        (log/error e "Failed to store metrics in InfluxDB:" metric)))))

(defn process-analytics-event!
  "Process and store analytics events"
  [{:keys [type user_id data timestamp] :as event}]
  (let [processed-data (assoc data
                             :event_type type
                             :user_id user_id
                             :timestamp timestamp)]
    ;; Store in MongoDB for detailed analysis
    (mc/insert (db/get-mongodb) "analytics_events" processed-data)
    ;; Store time-series data in InfluxDB
    (store-metrics! {:type :analytics
                    :data processed-data
                    :timestamp timestamp})))

;; Real-time aggregation
(defn aggregate-session-metrics
  "Aggregate metrics for a specific session"
  [session-id time-range]
  (let [query (str "SELECT mean(value) FROM performance_metrics "
                   "WHERE session_id = $session_id "
                   "AND time > now() - $time_range "
                   "GROUP BY metric_type, time(1m)")
        params {:session_id session-id
                :time_range time-range}]
    (influx/query (db/get-influxdb) query params)))

;; Performance analysis
(defn analyze-performance-trends
  "Analyze performance trends for a user"
  [user-id metric-type time-range]
  (let [query (str "SELECT mean(value) FROM performance_metrics "
                   "WHERE user_id = $user_id "
                   "AND metric_type = $metric_type "
                   "AND time > now() - $time_range "
                   "GROUP BY time(1d)")
        params {:user_id user-id
                :metric_type metric-type
                :time_range time-range}]
    (influx/query (db/get-influxdb) query params)))

;; System monitoring
(defn record-system-metrics!
  "Record system-level metrics"
  []
  (let [timestamp (System/currentTimeMillis)
        metrics {:heap-memory (Runtime/getRuntime)
                 :thread-count (Thread/activeCount)
                 :system-load (-> (java.lang.management.ManagementFactory/getOperatingSystemMXBean)
                                .getSystemLoadAverage)}]
    (store-metrics! {:type :system
                    :data metrics
                    :timestamp timestamp})))

;; Alerting
(defn check-performance-alerts!
  "Check for performance anomalies and trigger alerts"
  [session-id threshold]
  (let [query (str "SELECT last(value) FROM performance_metrics "
                   "WHERE session_id = $session_id "
                   "AND time > now() - 5m "
                   "HAVING value > $threshold")
        params {:session_id session-id
                :threshold threshold}]
    (when-let [anomalies (influx/query (db/get-influxdb) query params)]
      (doseq [anomaly anomalies]
        (trigger-alert! anomaly)))))

(defn- trigger-alert!
  "Trigger an alert for anomalous metrics"
  [anomaly]
  (let [alert-data {:type :performance_alert
                    :data anomaly
                    :timestamp (System/currentTimeMillis)}]
    (mc/insert (db/get-mongodb) "alerts" alert-data)
    ;; TODO: Implement notification system
    (log/warn "Performance alert triggered:" alert-data)))

;; Initialize metrics system
(defn init!
  []
  (start-metrics-processor!)
  ;; Start system metrics recording
  (async/go-loop []
    (record-system-metrics!)
    (Thread/sleep 60000) ; Record every minute
    (recur)))
