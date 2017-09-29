(ns windcatcher.dal
  (:require [windcatcher.rocksdb :as db]
            [clojure.set :as set]))

(defn update-membership
  [m add del]
  (-> m
      (update :s set/union (set add))
      (update :s set/difference (set del))))

(defn apply-membership-delta
  [db {:keys [user add del]}]
  (db/update! db user update-membership add del))

#_(-> {}
    (update-membership [1 2 3] [])
    (update-membership [10 20 30] [2 3]))
