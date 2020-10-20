(ns matteoredaelli.lacinia-backend-aws.system
  (:require
   [com.stuartsierra.component :as component]
   [matteoredaelli.lacinia-backend-aws.schema :as schema]
   [matteoredaelli.lacinia-backend-aws.server :as server]
   [matteoredaelli.lacinia-backend-aws.backend :as backend]
))

(defn new-system
  []
  (merge (component/system-map)
         (server/new-server)
         (schema/new-schema-provider)
         (backend/new-backend)))
