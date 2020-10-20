(ns matteoredaelli.lacinia-backend-aws.schema
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.walmartlabs.lacinia.util :as util]
    [com.stuartsierra.component :as component]
    [matteoredaelli.lacinia-backend-aws.backend :as backend]))

(defn query-aws-ec2
  [backend]
  (fn [context args value]
    (let [
          {:keys [profile filters]} args]
      (backend/ec2-describe-instances backend profile filters))))

(defn resolver-map
  [component]
  (let [backend (:backend component)]
    {
     :query/aws-ec2 (query-aws-ec2 backend)
     }
    ))

(defn get-schema
  [component]
  (-> (io/resource "schema.edn")
      slurp
      edn/read-string))

(defn load-schema
  [component]
  (-> (get-schema component)
      (util/attach-resolvers (resolver-map component))
      schema/compile))


(defrecord SchemaProvider [schema]

  component/Lifecycle

  (start [this]
    (assoc this :schema (load-schema this)))

  (stop [this]
    (assoc this :schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (-> {}
                        map->SchemaProvider
                        (component/using [:backend]))})
