(ns sports-center.logging
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [clojure.java.io :as io]
            [sports-center.config :as config]))

(def log-file "logs/sports-center.log")

(defn- ensure-log-dir! []
  (let [log-dir (io/file "logs")]
    (when-not (.exists log-dir)
      (.mkdirs log-dir))))

(defn- custom-output-fn
  [{:keys [level timestamp hostname ns msg_ ?err] :as data}]
  (format "[%s] [%s] [%s] - %s%s"
          timestamp
          (name level)
          (or ns "?")
          (force msg_)
          (if-let [err ?err]
            (str "\n" (timbre/stacktrace err))
            "")))

(defn init-logging! []
  (ensure-log-dir!)
  (timbre/merge-config!
   {:level (keyword (or (config/get-env :log-level) :info))
    :output-fn custom-output-fn
    :appenders
    {:println (appenders/println-appender {:stream :auto})
     :spit (appenders/spit-appender {:fname log-file})}}))

(defn with-logging
  "Middleware that logs request and response details"
  [handler]
  (fn [request]
    (let [start-time (System/currentTimeMillis)
          response (handler request)
          end-time (System/currentTimeMillis)
          elapsed (- end-time start-time)]
      (timbre/info
       (format "Request: %s %s -> Response: %d [%dms]"
               (-> request :request-method name .toUpperCase)
               (:uri request)
               (:status response)
               elapsed))
      response)))
