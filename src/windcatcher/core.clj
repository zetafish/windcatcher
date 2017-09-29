(ns windcatcher.core
  (:gen-class)
  (:require [mount.core :as mount]
            [windcatcher.rocksdb :as db]
            [windcatcher.dal :as dal]
            [clojure.string :as str])
  (:import (java.util UUID)))

(mount/defstate db
  :start (db/open-db "data")
  :stop (db/close-db db))

(def population (* 60 1000 1000))

(def segment-offset 1000000)

(def total-segments 6000)

(def batch-size 50000)

(defn user
  [n]
  (UUID/nameUUIDFromBytes (.getBytes (str n))))

(defn rand-segments
  [n]
  (map #(+ segment-offset %)
       (repeatedly n #(rand-int total-segments))))

(defn rand-update
  [n]
  {:user (user n)
   :add (rand-segments 600)
   :del (rand-segments 600)})

(defn add-shutdown-hook
  [hook]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. hook)))

(defn tps
  []
  (let [mark (atom nil)]
    (fn []
      (let [old @mark
            new (reset! mark (System/nanoTime))]
        (when old
          (int (/ 1E9 (- new old))))))))

(defn make-ticker
  []
  (let [window (* 1 1E9)
        state (atom {:start (System/nanoTime)
                     :count 0
                     :total 0})]
    (fn []
      (swap! state #(-> %
                        (update :count inc)
                        (update :total inc)))
      (let [old @state
            elapsed (- (System/nanoTime) (:start old))]
        (when (> elapsed window)
          (println (format "Total: %s, TPS: %s"
                           (:total old)
                           (/ (* (:count old) 1E9) elapsed)))
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
          (let [update {:user (user n)
                        :add add
                        :del del}]
            (dal/apply-membership-delta db update)
            (tick))))
      (recur (inc g) (+ t population)))))


(defn -main [& args]
  (mount/start)
  (add-shutdown-hook mount/stop)
  (run-simulation))
