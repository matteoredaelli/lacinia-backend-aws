(ns matteoredaelli.lacinia-backend-aws
  (:require [matteoredaelli.lacinia-backend-aws.system :as system]
            [com.stuartsierra.component :as component]
            )
 (:gen-class) 
  )


(defn ^:private my-system
  "Creates a new system suitable for testing, and ensures that
  the HTTP port won't conflict with a default running system."
  []
  (-> (system/new-system)
      (assoc-in [:server :port] 8888)))

(def ^:dynamic ^:private *system*)

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (binding [*system* (component/start-system (my-system))]))
