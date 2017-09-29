(set-env!
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.8.0" :scope "provider"]
                  [org.clojure/core.async "0.3.443"]
                  [org.rocksdb/rocksdbjni "5.7.3"]
                  [com.taoensso/nippy "2.14.0-alpha1"]
                  [mount "0.1.11"]
                  [amazonica "0.3.85"
                   :exclusions [com.amazonaws/aws-java-sdk
                                com.amazonaws/amazon-kinesis-client]]
                  [com.amazonaws/aws-java-sdk-core "1.11.93"]
                  [com.amazonaws/aws-java-sdk-s3 "1.11.93"]
                  [com.amazonaws/aws-java-sdk-sts "1.11.93"]
                  [tempfile "0.2.0"]
                  [com.taoensso/timbre "4.10.0"]])

(task-options!
  aot {:namespace #{'windcatcher.core}}
  pom {:project 'windcatcher
       :version "0.1.0"}
  jar {:main 'windcatcher.core})

(deftask build
  []
  (comp
    (aot)
    (pom)
    (uber)
    (jar)
    (target)))

(deftask run
  [a args ARG [str] "args for the app"]
  (require '[windcatcher.core :as core])
  (apply (resolve 'core/-main) args))
