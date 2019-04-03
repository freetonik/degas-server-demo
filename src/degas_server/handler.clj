(ns degas-server.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [clojure.core.async :refer [>! <! go chan timeout]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [degas-server.tsm :refer :all]
            [org.httpkit.server :refer [with-channel on-receive on-close send!]]))

;; Helpers
(defn sort-by-value-desc [x]
  (into (sorted-map-by (fn [key1 key2]
                     (compare [(get x key2) key2]
                              [(get x key1) key1]))) x))

;; State
(def running? (atom false))
(def update-queue (chan 10))
(def clients (atom {}))
(def users (atom {})) ;; {:name "STR", :best-ind [...], :fitness INT}

(defn broadcast-message [message]
  (doseq [client @clients]
    (send! (key client) (pr-str message) false)))

(defn get-ratings [usersdata]
  (apply merge (map (fn [[k v]] {(:name v) (:fitness v)}) usersdata)))

;; Communcation
(defn ws
  "Clients communication handler."
  [req]
  (with-channel req con
    (swap! clients assoc con true)
    (println "[+]" con)

    (on-receive con (fn [message]
                      (let [m (clojure.edn/read-string message)]

                        (if (:name m)
                          (do
                            (swap! users
                                   assoc
                                   con {:name (:name m)
                                        :best []
                                        :fitness nil})
                            (broadcast-message {:update (get-ratings @users)})))

                        (if (:best m)
                          (swap! users
                                 assoc
                                 con {:name (:name (@users con))
                                      :best (:best m)
                                      :fitness (fitness-tsm (:best m))}))

                        (if (:admin-start m)
                          (do
                            (println "ADMIN START")
                            (broadcast-and-run!)))

                        (if (:admin-stop m)
                          (stop!)))))

    (on-close con (fn [status]
                    (swap! clients dissoc con)
                    (swap! users dissoc con)
                    (println "[-]" con " => " status)))))

(defn run-update-async []
  (go (while @running?
        (>! update-queue 1)
        (<! (timeout 500))))

  (go (while true
        (let [item (<! update-queue)]
          (broadcast-message {:update (get-ratings @users)})))))

(defroutes routes
  (GET "/" [] ws)
  (resources "/")
  (not-found "Not Found"))

(defn broadcast-and-run! []
  (reset! running? true)
  (broadcast-message {:start true})
  (run-update-async))

(defn stop! []
  (reset! running? false)
  (broadcast-message {:stop true}))

(defn clear-users! []
  (reset! users {})
  (broadcast-message {:update (get-ratings @users)}))
