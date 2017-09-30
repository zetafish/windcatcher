(ns windcatcher.core
  (:gen-class)
  (:require [mount.core :as mount]
            [compojure.core :refer [GET POST defroutes]]
            [windcatcher.dal :as dal]
            [windcatcher.simulation :as sim]
            [clojure.string :as str]
            [taoensso.timbre :as log]
            [org.httpkit.server :as web]
            [clojure.core.async :as a]))

(mount/defstate shutdown-ch
  :start (a/chan))

(defroutes routes
  (POST "/shutdown" []
    (a/put! shutdown-ch :token)
    {:status 204})
  (POST "/simulation/start" []
    (sim/start-simulation)
    {:status 204})
  (GET "/users/:id" [n]
    (dal/get-user n)
    {:status 204}))

(defn start-server
  [host port]
  (log/infof "Starting web server on %s:%s" host port)
  (web/run-server routes {:ip host :port port}))

(defn stop-server
  [server]
  (log/infof "Stopping web server")
  (server))

(mount/defstate server
  :start (start-server "0.0.0.0" 5000)
  :stop (stop-server server))

(defn add-shutdown-hook
  [hook]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. hook)))

(defn -main [& args]
  (mount/start)
  (add-shutdown-hook mount/stop)
  (a/<!! shutdown-ch))
