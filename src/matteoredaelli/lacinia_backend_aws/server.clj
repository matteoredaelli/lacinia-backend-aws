(ns  matteoredaelli.lacinia-backend-aws.server
  (:require [com.stuartsierra.component :as component]
            [com.walmartlabs.lacinia.pedestal2 :as lp2]
            [io.pedestal.http :as http]))

(defrecord Server [schema-provider server port]

  component/Lifecycle
  (start [this]
    (assoc this :aws-server (-> schema-provider
                                :aws-schema
                                (lp2/default-service nil)
                                http/create-server
                                http/start)))

  (stop [this]
    (http/stop server)
    (assoc this :aws-server nil)))

(defn new-server
  []
  {:aws-server (component/using (map->Server {:port 8888})
                            [:schema-provider])})
