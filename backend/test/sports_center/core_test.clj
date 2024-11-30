(ns sports-center.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [sports-center.core :refer [app]]
            [sports-center.db.connection :as db]
            [sports-center.config :as config]
            [cheshire.core :as json]))

(defn parse-body [response]
  (-> response
      :body
      slurp
      (json/parse-string true)))

(defn with-test-db [f]
  (config/load-config! "test")
  (db/init-pool!)
  (f)
  (db/close-pool!))

(use-fixtures :once with-test-db)

(deftest test-health-check
  (testing "Health check endpoint"
    (let [response (app (mock/request :get "/api/v1/health"))
          body (parse-body response)]
      (is (= 200 (:status response)))
      (is (= "healthy" (:status body))))))

(deftest test-auth-endpoints
  (testing "Login endpoint"
    (let [response (app (-> (mock/request :post "/api/v1/auth/login")
                           (mock/json-body {:email "test@example.com"
                                          :password "password123"})))
          body (parse-body response)]
      (is (= 200 (:status response)))
      (is (contains? body :token))))

  (testing "Register endpoint"
    (let [response (app (-> (mock/request :post "/api/v1/auth/register")
                           (mock/json-body {:email "new@example.com"
                                          :password "password123"
                                          :name "Test User"})))
          body (parse-body response)]
      (is (= 201 (:status response)))
      (is (contains? body :id)))))

(deftest test-protected-endpoints
  (testing "Protected endpoint without token"
    (let [response (app (mock/request :get "/api/v1/users/profile"))]
      (is (= 401 (:status response)))))

  (testing "Protected endpoint with invalid token"
    (let [response (app (-> (mock/request :get "/api/v1/users/profile")
                           (mock/header "Authorization" "Bearer invalid-token")))]
      (is (= 401 (:status response))))))
