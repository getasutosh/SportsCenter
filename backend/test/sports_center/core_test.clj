(ns sports-center.core-test
  (:require [clojure.test :as t]
            [sports-center.core :as core]
            [ring.mock.request :as mock]
            [clojure.data.json :as json]))

(defn parse-json-body [response]
  (when-let [body (:body response)]
    (json/read-str body :key-fn keyword)))

(t/deftest health-check-test
  (t/testing "Health check endpoint"
    (let [response (core/app (mock/request :get "/health"))
          body (parse-json-body response)]
      (t/is (= 200 (:status response)))
      (t/is (= "healthy" (:status body)))
      (t/is (string? (:timestamp body))))))
