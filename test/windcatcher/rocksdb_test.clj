(ns windcatcher.rocksdb-test
  (:require [windcatcher.rocksdb :as sut]
            [clojure.test :refer :all]))

(deftest serialize-test
  (is (= [78 80 89 0 3] (seq (sut/serialize nil))))
  (is (= [78 80 89 0 105 3 104 105 33]
         (seq (sut/serialize "hi!")))))

(deftest deserialize-test
  (is (= "hi!"
         (sut/deserialize
           (byte-array  [78 80 89 0 105 3 104 105 33]))))
  (is (= nil (sut/deserialize (byte-array [78 80 89 0 3])))))
