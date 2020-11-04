(ns matteoredaelli.lacinia-backend-aws.schema
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.walmartlabs.lacinia.parser.schema :as parser]
    [com.walmartlabs.lacinia.util :as util]
    [com.stuartsierra.component :as component]
    [matteoredaelli.lacinia-backend-aws.backend :as backend]))

(defn field-customfield
  [backend]
  (fn [context _args value]
    "dummy test field"))

(defn query-aws-ec2
  [backend]
  (fn [context args value]
    (let [
          {:keys [profile filters]} args]
      (clojure.pprint/pprint args)
      (backend/ec2-describe-instances backend profile filters))))

(defn query-aws-rds
  [backend]
  (fn [context args value]
    (let [
          {:keys [profile filters]} args]
      (backend/rds-describe-db-instances backend profile filters))))

(defn resolver-map
  [component]
  (let [backend (:aws-backend component)]
    {:Query {:aws_ec2 (query-aws-ec2 backend)
             :aws_rds (query-aws-rds backend)}
     ;; :Ec2Instances {:customfield (field-customfield backend)}
     }
    ))

(defn resolver-map-old
  [component]
  (let [backend (:aws-backend component)]
    { :Query {:aws_ec2 (query-aws-ec2 backend)}}
    ))

(defn get-schema-old
  [component]
  (parser/parse-schema (slurp (io/resource "aws-schema.graphql"))
                       {:resolvers (resolver-map component)}
                       ))

(defn get-schema
  [component]
  (-> "aws-schema.graphql"
      io/resource
      slurp
      (parser/parse-schema {:resolvers (resolver-map component)})))

(defn load-schema
  [component]
  (-> (get-schema component)
      ;;(util/attach-resolvers (resolver-map component))
      schema/compile))


(defrecord SchemaProvider [schema]

  component/Lifecycle

  (start [this]
    (assoc this :aws-schema (load-schema this)))

  (stop [this]
    (assoc this :aws-schema nil)))

(defn new-schema-provider
  []
  {:schema-provider (-> {}
                        map->SchemaProvider
                        (component/using [:aws-backend]))})
