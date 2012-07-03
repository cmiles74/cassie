;;
;; Test functions for the Cassie library.
;;
(ns com.tnrglobal.cassie.test.core
  (:use [clojure.test])
  (:require [com.tnrglobal.cassie.core :as cassie])
  (:import [java.util Date]))

;; These test presume that you've used the included Vagrant VM
;; definition file and accompanying cookbooks to spin up a Cassandra
;; instance for testing.

;; test cassandra nodes
(def ^:dynamic NODES ["33.33.33.10"])

;; our test cluster name
(def ^:dynamc CLUSTER-NAME "Test Cluster")

;; our test keyspace
(def ^:dynamic KEYSPACE-NAME "TestKeyspace")

;; test keyspace definitions
(def FAMILY-DEFS [["customers" :utf8]])
;; test column definitions
(def COLUMN-DEFS

  ;; datum name   family      column        key     name     value   index
  {:created-date ["customers" "createdDate" :string :string :long]
   :first-name   ["customers" "firstName"   :string :string :string]
   :last-name    ["customers" "lastName"    :string :string :string]
   :age          ["customers" "age"         :string :string :long]
   :city         ["customers" "city"        :string :string :string :key]
   :ssn          ["customers" "ssn"         :string :string :string :key]})

;; test data
(def TEST-DATA
  [{:created-date (.getTime (Date.))
    :first-name "Christopher"
    :last-name "Miles"
    :age 38
    :city "Easthampton"
    :ssn "283-12-1727"}
   {:created-date (.getTime (Date.))
    :first-name "Emily"
    :last-name "Miles"
    :age 2
    :city "Easthampton"
    :ssn "293-12-1947"}
   {:created-date (.getTime (Date.))
    :first-name "Zoe"
    :last-name "Cat"
    :age 7
    :city "Kittyville"
    :ssn "091-34-2756"}])

(defmacro with-test-cluster
  "Evaluates the provided expressions in the context if the test
  Cassandra cluster."
  [& body]
  `(cassie/with-cluster (cassie/cluster CLUSTER-NAME
                                        NODES)
     (let [result# ~@body]
       result#)))

(defmacro with-test-keyspace
  "Evaluates the provided expressions in the context of the test
  Cassandra cluster and keyspace."
  [& body]
  `(with-test-cluster
     (do

       ;; create our keyspace
       (cassie/create-keyspace KEYSPACE-NAME
                               FAMILY-DEFS
                               COLUMN-DEFS)

       ;; run our tests
       (try

         (cassie/with-keyspace (cassie/keyspace KEYSPACE-NAME)
           (let [result# ~@body]
             result#))

         ;; drop our test keyspace
         (finally (cassie/drop-keyspace KEYSPACE-NAME))))))

(defmacro with-test-data
  "Evaluates the provided expressions in the context of the test
  Cassandra cluster, the test keyspace and the pre-loaded test data."
  [& body]
  `(with-test-keyspace
     (let [template# (cassie/template COLUMN-DEFS)]
       (dorun (for [datum# TEST-DATA]
                (cassie/mapper template#
                               datum#
                               :id-key :ssn)))
       (let [result# ~@body]
         result#))))

(deftest keyspace

  (testing "Create Keyspace, Keyspace Exists"
    (with-test-cluster
      (try
        (let [result (cassie/create-keyspace KEYSPACE-NAME
                                             FAMILY-DEFS
                                             COLUMN-DEFS)]
          (is (cassie/keyspace-exists? KEYSPACE-NAME)))
        (finally
          (cassie/drop-keyspace KEYSPACE-NAME)))))

  (testing "Delete Keyspace"
    (with-test-cluster
      (let [result (cassie/create-keyspace KEYSPACE-NAME
                                           FAMILY-DEFS
                                           COLUMN-DEFS)]
        (cassie/drop-keyspace KEYSPACE-NAME)
        (is (not (cassie/keyspace-exists? KEYSPACE-NAME))))))

  (testing "Describe Keyspace"
    (with-test-keyspace
      (let [result (cassie/describe-keyspace KEYSPACE-NAME)]
        (is (and result
                 (= "TestKeyspace" (:name result))
                 (= "customers" (.getName
                                 (first (seq (:cfDefs result)))))))))))

(deftest data

  (testing "Template and Mapper (Store and Fetch)"
    (with-test-keyspace
      (let [template (cassie/template COLUMN-DEFS)
            store (cassie/mapper template
                                 (first TEST-DATA)
                                 :id-key :ssn)
            fetch (cassie/mapper template (:ssn (first TEST-DATA)))]
        (is (= (first TEST-DATA) fetch)))))

  (testing "Index Query"
    (with-test-data
      (let [result (cassie/index-query COLUMN-DEFS
                                       "customers"
                                       [[:equals "city" "Easthampton"]]
                                       :id-key :ssn)]

        (is (= 2 (count result))
            (not (nil? (meta (second (first result)))))))))

  (testing "Index Query Delete"
    (with-test-data
      (let [delete (cassie/index-query-delete COLUMN-DEFS
                                              "customers"
                                              [[:equals "city" "Easthampton"]]
                                              :id-key :ssn)
            result (cassie/index-query COLUMN-DEFS
                                       "customers"
                                       [[:equals "city" "Easthampton"]]
                                       :id-key :ssn)]
        (is (= 0 (count result))))))

  (testing "Delete Item"
    (with-test-data
      (let [template (cassie/template COLUMN-DEFS)
            delete (cassie/mapper template
                                  (:ssn (first TEST-DATA))
                                  :delete true)
            result (cassie/index-query COLUMN-DEFS
                                       "customers"
                                       [[:equals "city" "Easthampton"]]
                                       :id-key :ssn)]
        (is (= 1 (count result))))))

  (testing "Update"
    (with-test-data
      (let [template (cassie/template COLUMN-DEFS)
            update (cassie/mapper template
                                  {:ssn "091-34-2756"
                                   :first-name "Zoe"
                                   :last-name "Cat"
                                   :age nil
                                   :city "Catastan"}
                                  :id-key :ssn)
            fetch (cassie/mapper template "091-34-2756")]
        (is (and (nil? (:age fetch))
                 (= "Catastan" (:city fetch))))))))