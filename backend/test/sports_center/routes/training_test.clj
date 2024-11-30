(ns sports-center.routes.training-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [sports-center.core :refer [app]]
            [sports-center.test-helpers :as helpers]
            [cheshire.core :as json]
            [java-time :as t]))

(use-fixtures :each helpers/with-clean-db)

(def test-session
  {:team_id (:id helpers/test-team)
   :title "Test Training Session"
   :description "Test session description"
   :start_time (str (t/instant))
   :end_time (str (t/plus (t/instant) (t/hours 2)))
   :location "Test Location"})

(deftest test-session-creation
  (testing "Create training session with valid data"
    (helpers/create-test-team!)
    (let [token (helpers/login-test-user)
          response (app (-> (mock/request :post "/api/v1/training/sessions")
                          (mock/json-body test-session)
                          (mock/header "Authorization" (str "Bearer " token))))
          body (helpers/parse-body response)]
      (is (= 201 (:status response)))
      (is (contains? body :id))
      (is (= (:title test-session) (:title body)))))

  (testing "Create session with invalid data"
    (helpers/create-test-team!)
    (let [token (helpers/login-test-user)
          invalid-session (assoc test-session :start_time "invalid-date")
          response (app (-> (mock/request :post "/api/v1/training/sessions")
                          (mock/json-body invalid-session)
                          (mock/header "Authorization" (str "Bearer " token))))
          body (helpers/parse-body response)]
      (is (= 400 (:status response)))
      (is (contains? body :error)))))

(deftest test-session-retrieval
  (testing "Get team sessions"
    (helpers/create-test-team!)
    (let [token (helpers/login-test-user)
          response (app (-> (mock/request :get (str "/api/v1/training/team/" 
                                                   (:id helpers/test-team) 
                                                   "/sessions"))
                          (mock/header "Authorization" (str "Bearer " token))))
          body (helpers/parse-body response)]
      (is (= 200 (:status response)))
      (is (vector? body)))))

(deftest test-performance-metrics
  (testing "Record performance metric"
    (helpers/create-test-team!)
    (let [token (helpers/login-test-user)
          metric {:session_id (:id test-session)
                 :athlete_id (:id helpers/test-user)
                 :metric_type "speed"
                 :metric_value {:speed 15.5 :unit "mph"}}
          response (app (-> (mock/request :post "/api/v1/training/metrics")
                          (mock/json-body metric)
                          (mock/header "Authorization" (str "Bearer " token))))
          body (helpers/parse-body response)]
      (is (= 201 (:status response)))
      (is (contains? body :id)))))
