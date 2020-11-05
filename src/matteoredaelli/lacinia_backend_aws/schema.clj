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

(defn query-aws
  [backend service op]
  (fn [context args value]
    (let [
          {:keys [profile filters]} args]
      (clojure.pprint/pprint args)
      (backend/aws-invoke backend profile service op filters))))

(defn resolver-map
  [component]
  (let [backend (:aws-backend component)]
    {:Query {:aws_ec2 (query-aws backend :ec2 :DescribeInstances)
             :aws_rds (query-aws backend :rds :DescribeDBInstances)}
     ;; :Ec2Instances {:customfield (field-customfield backend)}
     }
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
