(ns sports-center.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [sports-center.core :as core]
            [clojure.data.json :as json]))

(deftest app-test
  (testing "Health check endpoint"
    (let [response (core/app (mock/request :get "/api/health"))]
      (is (= 200 (:status response)))
      (is (= {:status "ok"} (json/read-str (:body response) :key-fn keyword))))))
