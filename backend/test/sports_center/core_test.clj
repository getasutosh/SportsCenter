(ns sports-center.core-test
  (:require [clojure.test :as t]
            [sports-center.core :as core]
            [ring.mock.request :as mock]
            [clojure.data.json :as json]))

(t/deftest health-check-test
  (t/testing "Health check endpoint"
    (let [response (core/app (mock/request :get "/health"))]
      (t/is (= 200 (:status response)))
      (let [body (json/read-str (:body response) :key-fn keyword)]
        (t/is (= "healthy" (:status body)))
        (t/is (string? (:timestamp body)))))))
