(ns sports-center.middleware.validation
  (:require [clojure.spec.alpha :as s]
            [ring.util.response :as response]
            [taoensso.timbre :as log]))

(defn validate-request
  "Validates request data against a spec.
   Returns nil if valid, error map if invalid."
  [data spec]
  (when-not (s/valid? spec data)
    (let [problems (s/explain-data spec data)]
      {:type :validation-error
       :problems (::s/problems problems)})))

(defn wrap-validation
  "Middleware for validating request data against specs.
   Specs should be provided in a map with keys matching request keys:
   {:body-params :spec/body
    :query-params :spec/query}"
  [handler specs]
  (fn [request]
    (let [validations (for [[req-key spec] specs
                           :let [data (get request req-key)
                                 validation-result (validate-request data spec)]
                           :when validation-result]
                       [req-key validation-result])]
      (if (seq validations)
        (do
          (log/warn "Validation failed:" validations)
          (-> (response/response
               {:error "Validation failed"
                :details (into {} validations)})
              (response/status 400)))
        (handler request)))))

;; Common specs
(s/def :user/id uuid?)
(s/def :user/email (s/and string? #(re-matches #"^[^@]+@[^@]+\.[^@]+$" %)))
(s/def :user/password (s/and string? #(>= (count %) 8)))
(s/def :user/name (s/and string? #(>= (count %) 2)))

(s/def :team/id uuid?)
(s/def :team/name (s/and string? #(>= (count %) 2)))
(s/def :team/description string?)
(s/def :team/sport string?)

(s/def :session/id uuid?)
(s/def :session/title (s/and string? #(>= (count %) 2)))
(s/def :session/description string?)
(s/def :session/start-time inst?)
(s/def :session/end-time inst?)
(s/def :session/location string?)

;; Request specs
(s/def :auth/login-request
  (s/keys :req-un [:user/email :user/password]))

(s/def :team/create-request
  (s/keys :req-un [:team/name :team/sport]
          :opt-un [:team/description]))

(s/def :session/create-request
  (s/keys :req-un [:team/id :session/title 
                   :session/start-time :session/end-time]
          :opt-un [:session/description :session/location])))
