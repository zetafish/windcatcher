(ns windcatcher.rocksdb
  (:require [taoensso.nippy :as nippy])
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
  (RocksDB/open (doto (Options.)
                  (.setCreateIfMissing true))
                path))

(defn close-db
  [db]
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
