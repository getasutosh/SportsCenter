(ns sports-center.core
  (:require [reitit.ring :as ring]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.data.json :as json]))

(defn health-handler [_request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body {:status "healthy"
          :timestamp (str (java.time.Instant/now))}})

(def app
  (-> (ring/ring-handler
       (ring/router
        [["/health" {:get health-handler}]]))
      (wrap-json-response)
      (wrap-json-body {:keywords? true})))

(defn start-server [port]
  (run-jetty app {:port port :join? false}))

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    (println (str "Server starting on port " port))
    (start-server port)))
