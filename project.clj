(defproject tnrglobal/cassie "0.5"
  :description "A library for working with Apache Cassandra"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.hectorclient/hector-core "1.1-0"]]
  :dev-dependencies [[lein-autodoc "0.9.0"]
                     [clj-yaml "0.3.1"]
                     [swank-clojure/swank-clojure "1.3.3"]]
  :main com.tnrglobal.cassie.core)
