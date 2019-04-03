(ns degas-server.core
  (:require
   [degas-server.handler :refer [ws broadcast-and-run! stop! clear-users! run-update-async]]
   [org.httpkit.server   :refer [run-server]]
   [environ.core :refer [env]])
  (:gen-class))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& port]
  (let [port (Integer. (or port (env :port) 5000))]
    (reset! server (run-server ws {:port port}))
    (run-update-async)
    (println "Server started on port" port)))


(defn restart-server []
  (stop-server)
  (-main))

;; (restart-server)
;; (broadcast-and-run!)
;; (stop!)
;; (clear-users!)
