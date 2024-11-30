(ns sports-center.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]))

(def ^:private config-file "config.edn")

(defn load-config
  ([] (load-config :default))
  ([profile]
   (aero/read-config (io/resource config-file) {:profile profile})))

(def ^:private env-config (atom nil))

(defn init!
  ([] (init! :default))
  ([profile]
   (reset! env-config (load-config profile))))

(defn get-env
  ([key] (get-env key nil))
  ([key default]
   (or (get @env-config key)
       (System/getenv (-> key name (.replace "-" "_") .toUpperCase))
       default)))

;; Initialize config with default profile
(init!)
