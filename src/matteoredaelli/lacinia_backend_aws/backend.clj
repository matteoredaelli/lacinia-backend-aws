(ns matteoredaelli.lacinia-backend-aws.backend
  (:require
   [com.stuartsierra.component :as component]
   [cognitect.aws.client.api :as aws]
   [cognitect.aws.credentials :as credentials])
  )

(defn ^:private pooled-data-source
  []
  {})

(defrecord AwsBackend [ds]

  component/Lifecycle

  (start [this]
    (assoc this
           :ds (pooled-data-source)))

  (stop [this]
    (assoc this :ds nil)))

(defn new-backend
  []
  {:aws-backend (map->AwsBackend {})})

(defn parse-filter
  [filter]
  (let [kv (clojure.string/split filter #"=")
        key (nth kv 0)
        values_string (nth kv 1)
        values (clojure.string/split values_string #",")]
    {:Name key :Values values}))

(defn parse-filters
  [filters]
  (if (= filters "")
    []
    (map parse-filter (clojure.string/split filters #";"))))

(defn aws-connect
  [profile service]
  (aws/client {:api
               service
               :credentials-provider (credentials/profile-credentials-provider profile)}))

(defn aws-invoke
  [component profile service op filters-string]
  (clojure.pprint/pprint [profile service op filters-string])
  (let [service (aws-connect profile service)
        filters (parse-filters filters-string)
        resp (aws/invoke service
                         {:op op
                          :request {:Filters filters}
                          }
                         )]
    resp
  ))
