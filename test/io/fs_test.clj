(ns io.fs-test

(deftest write!-test
  (is (= 1 1)))   [io.fs :as subject]))

(deftest read!-test
  (is (= true
         (subject/foo))))

(deftest genfp-test
  (is (= 1 1)))