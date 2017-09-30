(ns windcatcher.dal
  (:require [windcatcher.rocksdb :as db]
            [clojure.set :as set]
            [mount.core :as mount])
  (:import (java.util UUID)))

(mount/defstate store
  :start (db/open-db "data")
  :stop (db/close-db store))

(defn user-id->key
  [user-id]
  (str "user/" (UUID/nameUUIDFromBytes (.getBytes (str user-id)))))

(defn get-user
  [user-id]
  (let [k (user-id->key user-id)]
    (db/get store k)))

(defn update-user
  [user-id add del]
  (let [k (user-id->key user-id)]
    (db/update! store user-id
                (fn [user]
                  (update user :s set/union (set add))
                  (update user :s set/difference (set del))))))
