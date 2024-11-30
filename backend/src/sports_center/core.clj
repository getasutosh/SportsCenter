(ns sports-center.core
  (:require [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.coercion :as coercion]
            [muuntaja.core :as m]
            [ring.adapter.jetty :as jetty]
            [sports-center.routes.auth :as auth]
            [sports-center.routes.users :as users]
            [sports-center.routes.athletes :as athletes]
            [sports-center.routes.coaches :as coaches]
            [sports-center.routes.mentors :as mentors]
            [sports-center.routes.teams :as teams]
            [sports-center.routes.training :as training]
            [sports-center.middleware.auth :as auth-middleware]
            [sports-center.middleware.error :as error-middleware]
            [sports-center.middleware.rate-limit :as rate-limit]
            [sports-center.middleware.validation :as validation]
            [sports-center.db.connection :as db-conn]
            [sports-center.config :as config]
            [sports-center.metrics.core :as metrics]
            [taoensso.timbre :as log])
  (:gen-class))

(def app-routes
  [["/api/v1"
    ["/auth" auth/routes]
    ["/users" users/routes]
    ["/athletes" athletes/routes]
    ["/coaches" coaches/routes]
    ["/mentors" mentors/routes]
    ["/teams" teams/routes]
    ["/training" training/routes]]])

(def app
  (ring/ring-handler
   (ring/router
    app-routes
    {:data {:muuntaja m/instance
            :middleware [parameters/parameters-middleware
                        muuntaja/format-middleware
                        (rate-limit/wrap-rate-limit 
                          {:limit 100 :window 3600})
                        error-middleware/wrap-error-handling
                        auth-middleware/wrap-auth
                        (validation/wrap-validation {})
                        coercion/coerce-exceptions-middleware]}})
   (ring/create-default-handler 
     {:not-found (constantly 
                   (response/not-found 
                     {:error "Not found"}))})))

(defn start-server []
  (try
    (db-conn/init-pool!)
    (metrics/init!)
    (let [port (or (config/get-env :port) 3000)
          server (jetty/run-jetty #'app {:port port :join? false})]
      (log/info "Server started on port" port)
      server)
    (catch Exception e
      (log/error e "Failed to start server")
      (System/exit 1))))

(defn stop-server [server]
  (try
    (when server
      (.stop server))
    (db-conn/close-pool!)
    (metrics/shutdown!)
    (catch Exception e
      (log/error e "Error stopping server"))))

(defn -main [& args]
  (start-server))
