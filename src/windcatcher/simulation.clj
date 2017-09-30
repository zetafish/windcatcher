(ns windcatcher.simulation
  (:require [mount.core :as mount]
            [clojure.core.async :as a]
            [taoensso.timbre :as log]
            [windcatcher.dal :as dal]))

(def population (* 60 1000 1000))

(def segment-offset 1000000)

(def total-segments 6000)

(def batch-size 50000)

(mount/defstate simulation
  :start (atom {}))

(defn rand-segments
  [n]
  (map #(+ segment-offset %)
       (repeatedly n #(rand-int total-segments))))

(defn make-ticker
  []
  (let [window (* 1 1E9)
        state (atom {:start (System/nanoTime)
                     :count 0
                     :total 0})]
    (fn
      []
      (swap! state #(-> %
                        (update :count inc)
                        (update :total + 1)))
      (let [old @state
            elapsed (- (System/nanoTime) (:start old))]
        (when (> elapsed window)
          (log/infof "Total: %s, TPS: %s"
                     (:total old)
                     (/ (* (:count old) 1E9) elapsed))
          (swap! state assoc
                 :start (System/nanoTime)
                 :count 0))))))

(defn run-simulation
  []
  (let [tick (make-ticker)]
    (loop [g 0 t 0]
      (let [add (rand-segments 100)
            del (rand-segments 20)]
        (dotimes [n population]
          (dal/update-user n add del)
          (tick)))
      (recur (inc g) (+ t population)))))

(defn make-ticker
  []
  (let [window (* 1 1E9)
        state (atom {:start (System/nanoTime)
                     :count 0
                     :total 0})]
    (fn
      []
      (swap! state #(-> %
                        (update :count inc)
                        (update :total + 1)))
      (let [old @state
            elapsed (- (System/nanoTime) (:start old))]
        (when (> elapsed window)
          (log/infof"Total: %s, TPS: %s"
                    (:total old)
                    (/ (* (:count old) 1E9) elapsed))
          (swap! state assoc
                 :start (System/nanoTime)
                 :count 0))))))

(defn run-simulation
  []
  (let [tick (make-ticker)]
    (loop [g 0 t 0]
      (let [add (rand-segments 600)
            del (rand-segments 600)]
        (dotimes [n population]
          (dal/update-user n add del)
          (tick)))
      (recur (inc g) (+ t population)))))

(defn start-job-queue
  []
  (let [ch (a/chan (a/dropping-buffer 1))]
    (a/go-loop []
      (when (a/<! ch)
        ()
        (run-simulation)))
    ch))

(defn stop-job-queue
  [ch]
  (a/close! ch))

(mount/defstate job-queue
  :start (start-job-queue)
  :stop (stop-job-queue job-queue))


(defn start-simulation
  []
  (log/infof "Request simulation")
  (a/put! job-queue :token))
