(ns oblivion-potions.utils-test
  (:require
   [clojure.test :refer :all]
   [oblivion-potions.utils :refer :all]))

(deftest test-map-with-index-value
  (testing "Happy path"
    (let [result {"a" 1
                  "b" 2}]
      (is (= result (map-with-index-value ["a" "b"]))))))
