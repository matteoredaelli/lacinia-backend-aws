(ns matteoredaelli.lacinia-backend-aws.schema
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.walmartlabs.lacinia.parser.schema :as parser]
    [com.walmartlabs.lacinia.resolve :as resolve]
    [com.walmartlabs.lacinia.util :as util]
    [com.stuartsierra.component :as component]
    [matteoredaelli.lacinia-backend-aws.backend :as backend]))

(defn field-customfield
  [backend]
  (fn [context _args value]
    "dummy test field"))

(defn query-aws-old
  [backend service op]
  (fn [context args value]
    (let [
          {:keys [profile filters]} args]
      (clojure.pprint/pprint context)
      (backend/aws-invoke backend profile service op filters))))

(defn invoke-aws
  [backend service op]
  (fn [context args value]
    (let [
          {:keys [::profile ::filters]} context]
      (clojure.pprint/pprint value)
      (backend/aws-invoke backend ::profile service op ::filters))))

(defn query-aws
  [backend]
  (fn [context args value]
    (let [
          {:keys [profile filters]} args]
      (clojure.pprint/pprint args
      (-> {}
                 ;; https://lacinia.readthedocs.io/en/latest/resolve/context.html
          (resolve/with-context {::profile profile
                                 ::filter filter
                                 })))))

(defn resolver-map
  [component]
  (let [backend (:aws-backend component)]
    {:Query {:aws (query-aws backend)
            }
     :Aws {:EC2 (invoke-aws backend :ec2 :DescribeInstances)
           :customfield (field-customfield backend)}
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
