(ns sports-center.core-test
  (:require [clojure.test :as t]
            [sports-center.core :as core]
            [ring.mock.request :as mock]))

(t/deftest app-test
  (t/testing "Health check endpoint"
    (let [response (core/app (mock/request :get "/api/health"))]
      (t/is (= 200 (:status response)))
      (t/is (= {:status "ok"} (json/read-str (:body response) :key-fn keyword))))))

(t/deftest health-check-test
  (t/testing "Health check endpoint"
    (let [response (core/app (mock/request :get "/health"))]
      (t/is (= 200 (:status response)))
      (t/is (= "healthy" (get-in response [:body :status])))
      (t/is (string? (get-in response [:body :timestamp]))))))
