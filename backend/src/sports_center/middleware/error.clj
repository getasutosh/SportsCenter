(ns sports-center.middleware.error
  (:require [ring.util.response :as response]
            [clojure.tools.logging :as log]
            [clojure.spec.alpha :as s])
  (:import (java.sql SQLException)
           (clojure.lang ExceptionInfo)))

(defn- error->response [^Exception e]
  (condp instance? e
    SQLException
    (do
      (log/error e "Database error")
      (response/internal-server-error
       {:error "Database error occurred"
        :message "An unexpected database error occurred"}))

    ExceptionInfo
    (let [data (ex-data e)]
      (if (= :validation (:type data))
        (response/bad-request
         {:error "Validation error"
          :details (:problems data)})
        (do
          (log/error e "Application error" data)
          (response/internal-server-error
           {:error "Internal server error"
            :message (.getMessage e)}))))

    (do
      (log/error e "Unexpected error")
      (response/internal-server-error
       {:error "Internal server error"
        :message "An unexpected error occurred"}))))

(defn wrap-error-handling
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (error->response e)))))
