(ns sports-center.routes.training
  (:require [sports-center.db.core :as db]
            [ring.util.response :as response]
            [sports-center.middleware.auth :as auth]
            [taoensso.timbre :as log]))

(defn create-session! [{:keys [body-params] :as req}]
  (try
    (let [user-id (get-in req [:identity :id])
          session (assoc body-params :created_by user-id)
          result (db/create-training-session! session)]
      (response/created 
        (str "/api/v1/training/" (:id result)) 
        result))
    (catch Exception e
      (log/error e "Failed to create training session")
      (response/internal-server-error 
        {:error "Failed to create training session"}))))

(defn get-team-sessions [{:keys [path-params] :as req}]
  (try
    (let [team-id (:team_id path-params)
          sessions (db/get-team-sessions team-id)]
      (response/response sessions))
    (catch Exception e
      (log/error e "Failed to get team sessions")
      (response/internal-server-error 
        {:error "Failed to get team sessions"}))))

(defn record-metric! [{:keys [body-params] :as req}]
  (try
    (let [user-id (get-in req [:identity :id])
          metric (assoc body-params :recorded_by user-id)
          result (db/record-performance-metric! metric)]
      (response/created 
        (str "/api/v1/training/metrics/" (:id result)) 
        result))
    (catch Exception e
      (log/error e "Failed to record performance metric")
      (response/internal-server-error 
        {:error "Failed to record performance metric"}))))

(def routes
  [["/training"
    {:middleware [auth/wrap-auth]}
    ["/sessions"
     {:post {:handler create-session!
             :parameters {:body {:team_id string?
                               :title string?
                               :description string?
                               :start_time string?
                               :end_time string?
                               :location string?}}}}]
    ["/team/:team_id/sessions"
     {:get {:handler get-team-sessions
            :parameters {:path {:team_id string?}}}}]
    ["/metrics"
     {:post {:handler record-metric!
             :parameters {:body {:session_id string?
                               :athlete_id string?
                               :metric_type string?
                               :metric_value map?}}}}]]])
