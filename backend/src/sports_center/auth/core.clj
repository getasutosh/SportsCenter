(ns sports-center.auth.core
  (:require [buddy.sign.jwt :as jwt]
            [buddy.hashers :as hashers]
            [sports-center.config :as config]
            [sports-center.db.redis :as redis]
            [taoensso.timbre :as log]
            [clojure.string :as str])
  (:import (java.time Instant)))

(def token-expiry-seconds (* 24 60 60)) ; 24 hours

(defn hash-password [password]
  (hashers/derive password {:alg :bcrypt+sha512}))

(defn verify-password [input hashed]
  (hashers/verify input hashed))

(defn create-token [user]
  (let [claims {:user-id (:id user)
                :email (:email user)
                :role (:role user)
                :exp (+ (.getEpochSecond (Instant/now)) token-expiry-seconds)}
        token (jwt/sign claims (config/get-env :jwt-secret))]
    (redis/set-token! (:id user) token token-expiry-seconds)
    token))

(defn validate-token [token]
  (try
    (let [claims (jwt/unsign token (config/get-env :jwt-secret))]
      (if (redis/token-valid? (:user-id claims) token)
        claims
        nil))
    (catch Exception e
      (log/debug e "Token validation failed")
      nil)))

(defn invalidate-token! [token]
  (when-let [claims (jwt/unsign token (config/get-env :jwt-secret))]
    (redis/invalidate-token! (:user-id claims) token)))

(defn wrap-authentication [handler]
  (fn [request]
    (let [auth-header (get-in request [:headers "authorization"])
          token (when auth-header
                 (str/replace auth-header #"^Bearer " ""))]
      (if-let [claims (and token (validate-token token))]
        (handler (assoc request :identity (assoc claims :token token)))
        {:status 401
         :body {:error "Unauthorized"}}))))

(defn wrap-authorization [handler allowed-roles]
  (fn [request]
    (let [user-role (get-in request [:identity :role])]
      (if (contains? (set allowed-roles) user-role)
        (handler request)
        {:status 403
         :body {:error "Forbidden"}}))))
