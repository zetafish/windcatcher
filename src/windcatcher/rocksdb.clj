(ns windcatcher.rocksdb
  (:require [taoensso.nippy :as nippy]
            [taoensso.timbre :as log])
  (:import (org.rocksdb DbPath
                        RocksDB
                        Options)
           (java.util UUID)))

(def ^:dynamic *serializer* nippy/freeze)
(def ^:dynamic *deserializer* nippy/thaw)

(defprotocol IByteSerializable
  (^bytes serialize [obj])
  (^bytes deserialize [obj]))

(extend-protocol IByteSerializable
  nil
  (serialize [_] (*serializer* nil))
  (deserialize [_] nil)

  java.lang.Object
  (serialize [obj] (*serializer* obj))
  (deserialize [obj] (*deserializer* obj)))

(defn open-db
  [path]
  (log/info "Open DB")
  (RocksDB/open (doto (Options.)
                  (.setCreateIfMissing true))
                path))

(defn close-db
  [db]
  (log/info "Close DB")
  (.close db))

(defn get
  [db k]
  (deserialize (.get db (serialize k))))

(defn put!
  [db k v]
  (.put db (serialize k) (serialize v)))

(defn put-all!
  [db kvs]
  (let [batch (.createWriteBatch db)]
    (try
      (doseq [[k v] kvs]
        (.put batch (serialize k) (serialize v)))
      (.write db batch)
      (finally
        (.close batch)))))

(defn update!
  [db k f & args]
  (let [old (get db k)
        new (apply f old args)]
    (put! db k new)
    new))
