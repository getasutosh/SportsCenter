(ns sports-center.middleware.rate-limit
  (:require [sports-center.db.core :as db]
            [ring.util.response :as response]
            [taoensso.timbre :as log]
            [taoensso.carmine :as car]))

(def redis-conn {:pool {} :spec {:host (or (System/getenv "REDIS_HOST") "localhost")
                                :port (or (System/getenv "REDIS_PORT") 6379)}})

(defmacro wcar* [& body]
  `(car/wcar redis-conn ~@body))

(def default-limit 100)  ; requests
(def default-window 3600)  ; seconds (1 hour)

(defn- get-client-identifier [request]
  (or (get-in request [:headers "x-forwarded-for"])
      (:remote-addr request)))

(defn- over-limit? [identifier limit window]
  (let [current-count (or (wcar* (car/get identifier)) 0)]
    (>= (Integer/parseInt (str current-count)) limit)))

(defn- update-request-count! [identifier window]
  (wcar*
   (car/incr identifier)
   (car/expire identifier window)))

(defn wrap-rate-limit
  "Rate limiting middleware. Limits requests based on client IP.
   Options:
   :limit  - Maximum number of requests allowed within window
   :window - Time window in seconds"
  ([handler]
   (wrap-rate-limit handler {}))
  ([handler {:keys [limit window]
            :or {limit default-limit
                window default-window}}]
   (fn [request]
     (let [identifier (str "rate-limit:" (get-client-identifier request))]
       (if (over-limit? identifier limit window)
         (do
           (log/warn "Rate limit exceeded for" identifier)
           (-> (response/response {:error "Rate limit exceeded"
                                 :message "Too many requests. Please try again later."})
               (response/status 429)))
         (do
           (update-request-count! identifier window)
           (handler request))))))))
