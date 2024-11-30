(ns sports-center.websocket.handlers
  (:require [sports-center.db.core :as db]
            [sports-center.websocket.core :as ws]
            [sports-center.metrics.core :as metrics]
            [taoensso.timbre :as log]
            [monger.collection :as mc]
            [cheshire.core :as json]))

;; Session Management
(defn handle-session-join
  [{:keys [websocket-server session-id user-id role]}]
  (let [session-key (str "session:" session-id)
        user-data {:user-id user-id
                  :role role
                  :joined-at (System/currentTimeMillis)}]
    ;; Store session data in Redis
    (db/redis-command [:hset session-key user-id (json/generate-string user-data)])
    ;; Broadcast join event to session participants
    (ws/broadcast-to-session! websocket-server session-id
                            :session/user-joined
                            {:user-id user-id
                             :role role})))

(defn handle-session-leave
  [{:keys [websocket-server session-id user-id]}]
  (let [session-key (str "session:" session-id)]
    ;; Remove user from Redis session
    (db/redis-command [:hdel session-key user-id])
    ;; Broadcast leave event
    (ws/broadcast-to-session! websocket-server session-id
                            :session/user-left
                            {:user-id user-id})))

;; Performance Metrics
(defn handle-performance-update
  [{:keys [websocket-server user-id session-id metrics timestamp]}]
  (let [metric-data {:user_id user-id
                     :session_id session-id
                     :metrics metrics
                     :timestamp timestamp}]
    ;; Store in InfluxDB for time-series analysis
    (metrics/store-metrics! metric-data)
    ;; Store in MongoDB for detailed analysis
    (mc/insert (db/get-mongodb) "performance_metrics" metric-data)
    ;; Broadcast to relevant subscribers
    (ws/broadcast-to-session! websocket-server session-id
                            :performance/update
                            metric-data)))

;; Chat System
(defn handle-chat-message
  [{:keys [websocket-server session-id user-id message]}]
  (let [msg-data {:session_id session-id
                  :user_id user-id
                  :message message
                  :timestamp (System/currentTimeMillis)}]
    ;; Store message in MongoDB
    (mc/insert (db/get-mongodb) "chat_messages" msg-data)
    ;; Broadcast to session participants
    (ws/broadcast-to-session! websocket-server session-id
                            :chat/message
                            msg-data)))

;; Real-time Analytics
(defn handle-analytics-event
  [{:keys [websocket-server event-type data user-id]}]
  (let [event-data {:type event-type
                    :user_id user-id
                    :data data
                    :timestamp (System/currentTimeMillis)}]
    ;; Store analytics event
    (mc/insert (db/get-mongodb) "analytics_events" event-data)
    ;; Process for real-time metrics
    (metrics/process-analytics-event! event-data)))

;; Presence Management
(defn handle-presence-update
  [{:keys [websocket-server user-id status]}]
  (let [presence-key "user:presence"
        presence-data {:status status
                      :last_updated (System/currentTimeMillis)}]
    ;; Update Redis presence
    (db/redis-command [:hset presence-key user-id (json/generate-string presence-data)])
    ;; Broadcast to relevant subscribers
    (ws/broadcast! websocket-server
                  :presence/update
                  {:user-id user-id
                   :status status})))

;; Error Handling
(defn handle-error
  [{:keys [websocket-server user-id error-type error-data]}]
  (let [error-event {:user_id user-id
                     :type error-type
                     :data error-data
                     :timestamp (System/currentTimeMillis)}]
    ;; Log error
    (log/error "WebSocket error:" error-event)
    ;; Store error event
    (mc/insert (db/get-mongodb) "error_events" error-event)
    ;; Notify user
    (ws/send! websocket-server user-id
              :error/notification
              {:type error-type
               :message (:message error-data)})))
