(ns sports-center.routes.teams-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [sports-center.core :refer [app]]
            [sports-center.test-helpers :as helpers]
            [cheshire.core :as json]))

(use-fixtures :each helpers/with-clean-db)

(deftest test-team-creation
  (testing "Create team with valid data"
    (let [token (helpers/login-test-user)
          response (app (-> (mock/request :post "/api/v1/teams")
                          (mock/json-body {:name "New Team"
                                         :sport "Basketball"
                                         :description "Test team"})
                          (mock/header "Authorization" (str "Bearer " token))))
          body (helpers/parse-body response)]
      (is (= 201 (:status response)))
      (is (contains? body :id))
      (is (= "New Team" (:name body)))))

  (testing "Create team with invalid data"
    (let [token (helpers/login-test-user)
          response (app (-> (mock/request :post "/api/v1/teams")
                          (mock/json-body {:name ""  ; Invalid: empty name
                                         :sport "Basketball"})
                          (mock/header "Authorization" (str "Bearer " token))))
          body (helpers/parse-body response)]
      (is (= 400 (:status response)))
      (is (contains? body :error)))))

(deftest test-team-retrieval
  (testing "Get team by ID"
    (helpers/create-test-team!)
    (let [token (helpers/login-test-user)
          response (app (-> (mock/request :get (str "/api/v1/teams/" (:id helpers/test-team)))
                          (mock/header "Authorization" (str "Bearer " token))))
          body (helpers/parse-body response)]
      (is (= 200 (:status response)))
      (is (= (:name helpers/test-team) (:name body)))))

  (testing "Get non-existent team"
    (let [token (helpers/login-test-user)
          response (app (-> (mock/request :get "/api/v1/teams/00000000-0000-0000-0000-000000000000")
                          (mock/header "Authorization" (str "Bearer " token))))
          body (helpers/parse-body response)]
      (is (= 404 (:status response)))
      (is (contains? body :error)))))

(deftest test-team-update
  (testing "Update team with valid data"
    (helpers/create-test-team!)
    (let [token (helpers/login-test-user)
          response (app (-> (mock/request :put (str "/api/v1/teams/" (:id helpers/test-team)))
                          (mock/json-body {:name "Updated Team Name"
                                         :sport "Basketball"
                                         :description "Updated description"})
                          (mock/header "Authorization" (str "Bearer " token))))
          body (helpers/parse-body response)]
      (is (= 200 (:status response)))
      (is (= "success" (:status body))))))
