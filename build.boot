(set-env!
  :resoource-paths #{"src"}
  :dependencies '[[org.clojure/core.async "0.3.443"]
                  [org.rocksdb/rocksdbjni "5.7.3"]
                  [com.taoensso/nippy "2.14.0-alpha1"]
                  [mount "0.1.11"]])
