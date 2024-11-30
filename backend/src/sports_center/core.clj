(ns sports-center.core
  (:require [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [sports-center.routes :as routes]))

(defn health-handler [_]
  {:status 200
   :body {:status "healthy"
          :timestamp (java.time.Instant/now)}})

(def app
  (-> (ring/ring-handler
       (ring/router
        [["/api"
          ["/health" {:get health-handler}]
          routes/auth-routes
          routes/user-routes]]))
      (wrap-json-response)
      (wrap-json-body {:keywords? true})))

(defn start-server [port]
  (jetty/run-jetty app {:port port :join? false}))

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println (str "Server starting on port " port))
    (start-server port)))
