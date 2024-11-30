(ns sports-center.routes.auth
  (:require [sports-center.auth.core :as auth]
            [sports-center.db.users :as users]
            [sports-center.validation :as val]
            [ring.util.response :as response]
            [malli.core :as m]
            [taoensso.timbre :as log]))

(def LoginSchema
  [:map
   [:email [:string {:min 5, :max 255}]]
   [:password [:string {:min 8, :max 100}]]])

(def RegisterSchema
  [:map
   [:email [:string {:min 5, :max 255}]]
   [:password [:string {:min 8, :max 100}]]
   [:name [:string {:min 2, :max 100}]]
   [:role [:enum "athlete" "coach" "mentor" "parent" "admin"]]])

(defn login-handler
  [{:keys [body-params]}]
  (try
    (if (m/validate LoginSchema body-params)
      (let [{:keys [email password]} body-params
            user (users/get-user-by-email email)]
        (if (and user (auth/verify-password password (:password user)))
          (let [token (auth/create-token user)]
            (-> (response/response {:token token})
                (response/status 200)))
          (-> (response/response {:error "Invalid credentials"})
              (response/status 401))))
      (-> (response/response {:error "Invalid input"})
          (response/status 400)))
    (catch Exception e
      (log/error e "Login failed")
      (-> (response/response {:error "Internal server error"})
          (response/status 500)))))

(defn register-handler
  [{:keys [body-params]}]
  (try
    (if (m/validate RegisterSchema body-params)
      (let [{:keys [email]} body-params]
        (if (users/get-user-by-email email)
          (-> (response/response {:error "Email already registered"})
              (response/status 409))
          (let [user (users/create-user! body-params)
                token (auth/create-token user)]
            (-> (response/response {:token token})
                (response/status 201)))))
      (-> (response/response {:error "Invalid input"})
          (response/status 400)))
    (catch Exception e
      (log/error e "Registration failed")
      (-> (response/response {:error "Internal server error"})
          (response/status 500)))))

(defn logout-handler
  [{:keys [identity]}]
  (try
    (auth/invalidate-token! (:token identity))
    (-> (response/response {:message "Logged out successfully"})
        (response/status 200))
    (catch Exception e
      (log/error e "Logout failed")
      (-> (response/response {:error "Internal server error"})
          (response/status 500)))))

(def routes
  [["/login" {:post login-handler
             :middleware [val/validate-json]}]
   ["/register" {:post register-handler
                :middleware [val/validate-json]}]
   ["/logout" {:post logout-handler
              :middleware [auth/wrap-authentication]}]])
