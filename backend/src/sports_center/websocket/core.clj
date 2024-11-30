(ns sports-center.websocket.core
  (:require [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
            [sports-center.auth.core :as auth]
            [sports-center.db.core :as db]
            [taoensso.timbre :as log]
            [clojure.core.async :as async :refer [<! >! go-loop]]
            [com.stuartsierra.component :as component]))

(defrecord WebSocketServer [ch-recv send-fn connected-uids router stop-router!]
  component/Lifecycle
  
  (start [this]
    (if ch-recv
      this
      (let [{:keys [ch-recv send-fn connected-uids]}
            (sente/make-channel-socket! (get-sch-adapter)
                                      {:user-id-fn (fn [ring-req]
                                                   (get-in ring-req [:session :uid]))
                                       :csrf-token-fn nil
                                       :wrap-recv-evs? false})
            router (start-router! ch-recv)]
        (log/info "WebSocket server started")
        (assoc this
               :ch-recv ch-recv
               :send-fn send-fn
               :connected-uids connected-uids
               :router router
               :stop-router! (fn [] (router))))))
  
  (stop [this]
    (when stop-router!
      (stop-router!)
      (log/info "WebSocket server stopped"))
    (assoc this :ch-recv nil :send-fn nil :connected-uids nil :router nil :stop-router! nil)))

(defn new-websocket-server []
  (map->WebSocketServer {}))

;; Event handling
(defmulti handle-event :id)

(defmethod handle-event :default [{:keys [id] :as ev-msg}]
  (log/debug "Unhandled event:" id))

;; Client presence tracking
(defmethod handle-event :chsk/uidport-open [{:keys [uid client-id]}]
  (log/debug "New client connected:" uid client-id))

(defmethod handle-event :chsk/uidport-close [{:keys [uid client-id]}]
  (log/debug "Client disconnected:" uid client-id))

(defmethod handle-event :chsk/ws-ping [_]
  nil) ; No-op, just to keep connection alive

;; Session management
(defmethod handle-event :session/join
  [{:keys [uid client-id ?data]}]
  (let [{:keys [session-id role]} ?data]
    (log/debug "User" uid "joined session" session-id "as" role)))

(defmethod handle-event :session/leave
  [{:keys [uid client-id ?data]}]
  (let [{:keys [session-id]} ?data]
    (log/debug "User" uid "left session" session-id)))

;; Real-time updates
(defmethod handle-event :performance/update
  [{:keys [uid ?data]}]
  (let [{:keys [metrics timestamp]} ?data]
    (log/debug "Performance update from" uid ":" metrics)))

;; Chat functionality
(defmethod handle-event :chat/message
  [{:keys [uid ?data]}]
  (let [{:keys [session-id message]} ?data]
    (log/debug "Chat message in session" session-id "from" uid ":" message)))

;; Router
(defn- start-router! [ch-recv]
  (go-loop []
    (when-let [{:keys [id ?data] :as evt} (<! ch-recv)]
      (handle-event evt)
      (recur))))

;; Broadcasting helpers
(defn broadcast!
  "Send a message to all connected clients"
  [websocket-server event data]
  (let [uids (:any @(:connected-uids websocket-server))
        send-fn (:send-fn websocket-server)]
    (doseq [uid uids]
      (send-fn uid [event data]))))

(defn broadcast-to-session!
  "Send a message to all clients in a specific session"
  [websocket-server session-id event data]
  (let [session-users (get-session-users session-id)
        send-fn (:send-fn websocket-server)]
    (doseq [uid session-users]
      (send-fn uid [event data]))))

;; Ring handlers
(defn ring-handlers [websocket-server]
  {:ws-handshake-fn (fn [ring-req]
                      ((get-in websocket-server [:ch-recv :ajax-get-or-ws-handshake-fn])
                       ring-req))
   :ajax-post-fn (fn [ring-req]
                   ((get-in websocket-server [:ch-recv :ajax-post-fn])
                    ring-req))})
