(ns sports-center.routes.teams
  (:require [sports-center.db.core :as db]
            [ring.util.response :as response]
            [sports-center.middleware.auth :as auth]
            [taoensso.timbre :as log]))

(defn create-team! [{:keys [body-params] :as req}]
  (try
    (let [user-id (get-in req [:identity :id])
          team (assoc body-params :creator_id user-id)
          result (db/create-team! team)]
      (response/created (str "/api/v1/teams/" (:id result)) result))
    (catch Exception e
      (log/error e "Failed to create team")
      (response/internal-server-error {:error "Failed to create team"}))))

(defn get-team [req]
  (try
    (let [team-id (get-in req [:path-params :id])
          team (db/get-team team-id)]
      (if team
        (response/response team)
        (response/not-found {:error "Team not found"})))
    (catch Exception e
      (log/error e "Failed to get team")
      (response/internal-server-error {:error "Failed to get team"}))))

(defn update-team! [{:keys [body-params path-params] :as req}]
  (try
    (let [team-id (:id path-params)
          user-id (get-in req [:identity :id])
          team (db/get-team team-id)]
      (if (= user-id (:creator_id team))
        (do
          (db/update-team! team-id body-params)
          (response/response {:status "success"}))
        (response/forbidden {:error "Not authorized to update team"})))
    (catch Exception e
      (log/error e "Failed to update team")
      (response/internal-server-error {:error "Failed to update team"}))))

(defn delete-team! [{:keys [path-params] :as req}]
  (try
    (let [team-id (:id path-params)
          user-id (get-in req [:identity :id])
          team (db/get-team team-id)]
      (if (= user-id (:creator_id team))
        (do
          (db/delete-team! team-id)
          (response/response {:status "success"}))
        (response/forbidden {:error "Not authorized to delete team"})))
    (catch Exception e
      (log/error e "Failed to delete team")
      (response/internal-server-error {:error "Failed to delete team"}))))

(def routes
  [["/teams"
    {:middleware [auth/wrap-auth]}
    [""
     {:post {:handler create-team!
             :parameters {:body {:name string?
                               :description string?
                               :sport string?}}}
      :get {:handler (fn [_] 
                      (response/response (db/get-all-teams)))}}]
    ["/:id"
     {:get {:handler get-team
            :parameters {:path {:id string?}}}
      :put {:handler update-team!
            :parameters {:path {:id string?}
                        :body {:name string?
                              :description string?
                              :sport string?}}}
      :delete {:handler delete-team!
               :parameters {:path {:id string?}}}}]]])
