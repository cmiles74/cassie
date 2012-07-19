;;
;; Functions for interacting with Cassandra.
;;
(ns ^{:doc "Provides functions that make it easier to work with the
            Cassandra Hector Java API."}
  com.tnrglobal.cassie.core
  (:import [java.util Arrays]
           [me.prettyprint.hector.api.factory HFactory]
           [me.prettyprint.cassandra.service CassandraHostConfigurator]
           [me.prettyprint.cassandra.service.template ThriftColumnFamilyTemplate
            ColumnFamilyUpdater]
           [me.prettyprint.hector.api.ddl ComparatorType ColumnIndexType]
           [me.prettyprint.cassandra.service ThriftKsDef]
           [me.prettyprint.cassandra.service FailoverPolicy BatchSizeHint]
           [me.prettyprint.cassandra.model AllOneConsistencyLevelPolicy
            QuorumAllConsistencyLevelPolicy]
           [me.prettyprint.cassandra.serializers AsciiSerializer
            BigIntegerSerializer BooleanSerializer
            ByteBufferSerializer BytesArraySerializer
            CharSerializer CompositeSerializer DateSerializer
            DoubleSerializer DynamicCompositeSerializer
            FloatSerializer IntegerSerializer LongSerializer
            ObjectSerializer ShortSerializer StringSerializer
            TimeUUIDSerializer TypeInferringSerializer UUIDSerializer]
           [me.prettyprint.cassandra.model BasicColumnFamilyDefinition
            BasicColumnDefinition BasicKeyspaceDefinition]
           [me.prettyprint.cassandra.model.thrift ThriftSliceQuery]))

;; Cassandra Cluster instance
(def ^:dynamic *CLUSTER* nil)

;; Cassandra Keyspace instance
(def ^:dynamic *KEYSPACE* nil)

;; value comparators
(def comparators {:ascii ComparatorType/ASCIITYPE
                  :bytes ComparatorType/BYTESTYPE
                  :bytes-array ComparatorType/BYTESTYPE
                  :composite ComparatorType/COMPOSITETYPE
                  :dynamic-composite ComparatorType/DYNAMICCOMPOSITETYPE
                  :integer ComparatorType/INTEGERTYPE
                  :lexical-uid ComparatorType/LEXICALUUIDTYPE
                  :long ComparatorType/LONGTYPE
                  :string ComparatorType/UTF8TYPE
                  :time-uuid ComparatorType/TIMEUUIDTYPE
                  :utf8 ComparatorType/UTF8TYPE
                  :uuid ComparatorType/UUIDTYPE})

;; value serializers
(def serializers {:ascii #(AsciiSerializer/get)
                  :big-integer #(BigIntegerSerializer/get)
                  :boolean #(BooleanSerializer/get)
                  :byte-buffer #(ByteBufferSerializer/get)
                  :bytes-array #(BytesArraySerializer/get)
                  :char #(CharSerializer/get)
                  :composite #(CompositeSerializer/get)
                  :date #(DateSerializer/get)
                  :double #(DoubleSerializer/get)
                  :dynamic-composite #(DynamicCompositeSerializer/get)
                  :float #(FloatSerializer/get)
                  :integer #(IntegerSerializer/get)
                  :long #(LongSerializer/get)
                  :object #(ObjectSerializer/get)
                  :short #(ShortSerializer/get)
                  :string #(StringSerializer/get)
                  :time-uuid #(TimeUUIDSerializer/get)
                  :type-inferring #(TypeInferringSerializer/get)
                  :uuid #(UUIDSerializer/get)})

;; consistency policies
(def consistency {:all-one #(AllOneConsistencyLevelPolicy.)
                  :quorum #(QuorumAllConsistencyLevelPolicy.)})

;; failover policies
(def failover {:faill-fast FailoverPolicy/FAIL_FAST
               :try-all FailoverPolicy/ON_FAIL_TRY_ALL_AVAILABLE
               :try-one-next FailoverPolicy/ON_FAIL_TRY_ONE_NEXT_AVAILABLE})

;; index types
(def index-types {:key ColumnIndexType/KEYS})

(defmacro with-cluster
  "Evaluates the provided forms in the context of the provided cluster
  instance. You want to surround any calls that require a cluster
  instance with this form."
  [cluster & body]
  `(binding [*CLUSTER* ~cluster]
     (let [result# ~@body]
       result#)))

(defmacro with-keyspace
  "Evaluates the provided forms in the context of the provided
  keyspace instance. You want to surround any calls that require a
  keyspace instance with this form."
  [keyspace & body]
  `(binding [*KEYSPACE* ~keyspace]
     (let [result# ~@body]
       result#)))

(defn configurator
  "Returns a CassandraHostConfigurator instance set to use the
  specified sequence of hosts. You may set optional values with the
  provided keys:

    :port Cassandra RPC port, 9160
    :auto-discover Discover hosts via Thrift, true

  You likely won't be using this function directly, instead you'll use
  the 'cluster' function."
  [hosts & {:keys [port auto-discover]
            :or {port 9160 auto-discover true}}]

  (let [configurator (CassandraHostConfigurator.)]
    (.setHosts configurator (apply str (interpose "," hosts)))
    (.setPort configurator port)
    (.setAutoDiscoverHosts configurator auto-discover)
    configurator))

(defn cluster
  "Returns Cluster instance connected to the named cluster on the
  provided sequence of hosts; if no hosts are provided then
  'localhost' is assumed. You may set optional values with the
  provided keys:

    :port Cassandra RPC port, 9160
    :auto-discover Discover hosts via Thrift, true

  Once you have a handle on your cluster, you can use
  the (with-cluster ...) form to pass it to the other functions that
  need it."
  ([name] (cluster name ["localhost"]))
  ([name hosts & {:keys [port auto-discover]
            :or {port 9160 auto-discover true}}]
     (HFactory/getOrCreateCluster name
                                  (configurator hosts
                                                :port port
                                                :auto-discover auto-discover))))

(defn column-family-def
  "Returns a new column family with the specified name for the
  provided keyspace name.  The provided comparator is used to sort
  colums in a row, use the 'comparators' map to get the one that you
  need."
  [keyspace-name name comparator]
  (BasicColumnFamilyDefinition.
   (HFactory/createColumnFamilyDefinition keyspace-name
                                          name
                                          comparator)))

(defn column-families-def
  "Returns sequence of column family definitions for the provided
  keyspace name. You should provide a sequence, each item containing
  the name and comparator for the column family. The comparator should
  either be a key into the 'comparators' map or an Object that
  implements the Comparator interface.

    (column-families 'Blog' [['articles' :utf8]
                             ['comments' :utf8]])"
  [keyspace-name families]
  (doall (for [family families]
           (column-family-def keyspace-name
                              (first family)
                              (if (keyword? (second family))
                                (comparators (second family))
                                (second family))))))

(defn keyspace-def
  "Returns a new keyspace definition with the provided name and column
  families. You may set optional values with the provided keys:

    :replication Replicaiton factor, 1
    :strategy Replication strategy, ThriftKsDef/DEF_STRATEGY_CLASS"
  [keyspace-name column-families
   & {:keys [replication strategy]
      :or {replication 1
           strategy ThriftKsDef/DEF_STRATEGY_CLASS}}]

  (let [keyspace (BasicKeyspaceDefinition.)]

    ;; set our keyspace attributes
    (doto keyspace
      (.setName keyspace-name)
      (.setStrategyClass strategy)
      (.setReplicationFactor replication))

    ;; add all of our column families
    (doall (for [family column-families]
             (.addColumnFamily keyspace family)))

    ;; return the keyspace definition
    keyspace))

(defn create-keyspace
  "Creates a new keyspace with the specified name on the provided
  cluster and creates the provided columns. The 'cluster' should be an
  value returned by the 'cluster' function, 'families' should be a
  sequence hash of column family definitions. The 'columns' should be
  a map of column symbols, family names, column names, and
  serializers (see the 'template' function for more details). This
  function should be called in the context of a (with-cluster ...)
  form.

    (create-keyspace my-cluster
                     'Blog'
                     [['articles' :utf8]['comments' :utf8]]
                     {:post-date ['articles' 'postDate' :string :string :long]})

  You may set option values with the provided keys:

    :block Block until keyspace created, true"
  [keyspace-name families columns
   & {:keys [block replication strategy]
      :or {block true replication 1 strategy ThriftKsDef/DEF_STRATEGY_CLASS}}]

  ;; create the keyspace
  (.addKeyspace *CLUSTER*
                (keyspace-def keyspace-name
                              nil
                              :replication replication
                              :strategy strategy)
                block)

  ;; create our column families
  (let [family-defs
        (doall
         (for [family families]
           (let [column-family (column-family-def
                                keyspace-name
                                (first family)
                                (if (keyword? (second family))
                                  (comparators (second family))
                                  (second family)))]
             ;; create the columns
             (dorun
              (for [key-in (keys columns)]
                (if (=  (first family) (nth (columns key-in) 0))
                  (let [column (BasicColumnDefinition.)]
                    (doto column

                      ;; set name
                      (.setName (.toByteBuffer ((serializers
                                                 (nth (columns key-in) 3)))
                                               (nth (columns key-in) 1)))

                      ;; set validator
                      (.setValidationClass (.getClassName
                                            (comparators
                                             (nth (columns key-in) 4)))))

                    ;; setup our index
                    (if (< 5 (count (columns key-in)))
                      (do
                        (.setIndexName column (nth (columns key-in) 1))
                        (.setIndexType column
                                       (index-types (nth (columns key-in) 5)))))

                    ;; add the column to the family
                    (.addColumnDefinition column-family column)))))

             ;; add our column family to our keyspace
             (.addColumnFamily *CLUSTER* column-family block)
             column-family)))]

    ;; return our family definitions
    family-defs))

(defn drop-keyspace
  "Drops the specified keyspace from the cluster.

  The behvavior of this function may be customized with the following
  keys.

    :block blocks until delete is complete, true"
  [keyspace-name & {:keys [block] :or {block true}}]
  (.dropKeyspace *CLUSTER* keyspace-name block))

(defn describe-keyspace
  "Returns a map that wraps a JavaBean describing the specified
  keyspace or nil if that keyspace does not exists. This function
  should be called in the context of a (with-cluster ...) form."
  [keyspace-name]
  (let [descr (.describeKeyspace *CLUSTER* keyspace-name)]
    (if descr (bean descr))))

(defn keyspace-exists?
  "Returns true if a keyspace with the provided name exists in the
  provided cluster. This function should be called in the context of
  a (with-cluster ...) form."
  [keyspace-name]
  (if (describe-keyspace keyspace-name) true))

(defn keyspace
  "Returns a Keyspace instance for the specified keyspace name on the
  provided cluster. This function should be called in the context of
  a (with-cluster ...) form. You may set option values with the
  provided keys:

    :consistency Consistency level for read and writes, all-one
    :failover Failover strategy, try-all with 1 retry and no sleep"
  [keyspace-name
   & {:keys [consistency failover retry-sleep]
      :or {failover (:try-all failover)
           consistency (:all-one consistency)
           retry-sleep 0}}]

  (HFactory/createKeyspace keyspace-name
                           *CLUSTER*
                           (consistency)
                           failover))

(defn column-family-template
  "Creates a column family template for the provided column family in
  the keyspace. The 'key-serializer' is used to serialize the incoming
  keys of new items, the 'top-serializer' is used to serialize the
  name incoming column names of items; both should be either a key
  from the 'serializers' map or an Object that implements the
  Serializer interface. This function should be called in the context
  of a (with-keyspace ...) form."
  [column-family key-serializer top-serializer]
  (ThriftColumnFamilyTemplate. *KEYSPACE*
                               column-family
                               (if (keyword? key-serializer)
                                 ((serializers key-serializer))
                                 key-serializer)
                               (if (keyword? top-serializer)
                                 ((serializers top-serializer))
                                 top-serializer)))

(defn update-column-family-template
  "Uses the provided column family template to update the specified
  column for the provided id with the supplied value. The 'serializer'
  value should either be a key from the 'serializers' map or an Object
  that implements the Serializer interface; this is used to serialize
  the provided value to a byte array."
  [col-fam-template column-name serializer id value]
  (let [updater (.createUpdater col-fam-template id)
        serializer-out (if (keyword? serializer)
                         ((serializers serializer)) serializer)]
    (.setValue updater column-name value serializer-out)
    (.update col-fam-template updater)))

(defn read-column-family-template
  "Uses the provided column family template to read the specified
  column for the provided id and return's that value. The 'serializer'
  value should either be a key from the 'serializers' map or an Object
  that implements the Serializer interface; this is used to
  de-serialize the value read into a byte array."
  [col-fam-template column-name serializer id]
  (let [serializer-out (if (keyword? serializer)
                         ((serializers serializer)) serializer)
        reader (.querySingleColumn col-fam-template
                                   id
                                   column-name
                                   serializer-out)]
    (if reader (.getValue reader))))

(defn delete-column-family-template
  "Uses the provided column family template to delete the specified
  column for the provided id."
  [col-fam-template column-name id]
  (.deleteColumn col-fam-template id column-name))

(defn serializer-for-column
  "Returns the serializer key for the specified column family and name
  from the provided column definition map."
  [column-defs family column-name]
  (nth (first
        (filter #(= column-name (nth % 1))
                (filter #(= family (first %)) (vals column-defs))))
       4))

(defn name-serializer-for-column
  "Returns the name serializer key for the specified column family and
  name from the provided column definition map."
  [column-defs family column-name]
  (nth (first
        (filter #(= column-name (nth % 1))
                (filter #(= family (first %)) (vals column-defs))))
       3))

(defn key-serializer-for-column
  "Returns the key serializer key for the specified column family and
  name from the provided column definition map."
  [column-defs family column-name]
  (nth (first
        (filter #(= column-name (nth % 1))
                (filter #(= family (first %)) (vals column-defs))))
       2))

(defn key-for-column-name
  "Returns the keyname for the provided column name."
  [column-defs family column-name]
  (first (flatten (filter #(= column-name (nth (second %) 1)) column-defs))))

(defn index-query
  "Returns a sequence of results that match the provided query
   constraints. The \"family\" is the name of the column family
   containing the secondary index.

   The query shound be a sequence of sequences, each defines a portion
   of the query. The first item in the query sequence will be a key
   representing the criteria (:equals,
   :greater-than, :less-than) the next item will be the key for the
   column and the last item the column value.

    [[:equals :last-name \"Miles\"]
     [:equals :city \"Easthampton\"]]

  You may provide a list of columns to be returned from the column
  family holding the index with the \"columns\" parameter. If
  \"columns\" is set to nil then all columns in the family will be
  returned.

  The results will be returned as a sequence, eache item in the
  sequence will contain a row key followed by a map of the column data
  for that row. This map will also come wrapped with metadata, this
  metadata will have the value map keys for it's keys and a map of
  information about the values (i.e. :clock, :name-serializer, :ttl,
  etc.)

    [[\"19292xx\" {:name \"Mike\" :age 42}]
     [\"18273cs\" {:name \"Jore\" :age 38}]]

  The behavior of this function may be customized by providing the
  following keys.

    :id-key The field in the column definition with the unique row id, :id
    :columns Columns in the family to return, all
    :return-keys-only returns only keys, no other data; false
    :range set a range of columns to retrieve, this should be expressed
      as a map with the following keys: :start, :finish, :reversed,
      :count). Note that if you set a range of columns, you can leave
      the \"columns\" parameter nil.
    :row-count number of rows to return; 11
    :start-key the key used to start returning rows"
  [column-defs family constraints
   & {:keys [id-key columns return-keys-only range row-count start-key]
      :or {id-key :id
           columns nil
           return-keys-only false
           range nil
           row-count 11
           start-key ""}}]

  ;; create a new query
  (let [key-ser (nth (column-defs id-key) 2)
        name-ser (nth (column-defs id-key) 3)
        val-ser (nth (column-defs id-key) 4)
        query (HFactory/createIndexedSlicesQuery *KEYSPACE*
                                                 ((serializers key-ser))
                                                 ((serializers name-ser))
                                                 ((serializers val-ser)))]

    ;; set our paramters
    (.setColumnFamily query family)
    (.setStartKey query start-key)

    ;; add our constraints
    (dorun (for [constraint constraints]
             (cond
               (= :equals (first constraint))
               (.addEqualsExpression query
                                     (nth (column-defs (nth constraint 1)) 1)
                                     (nth constraint 2))

               (= :greater-than (first constraint))
               (.addGteExpression query
                                  (nth (column-defs (nth constraint 1)) 1)
                                  (nth constraint 2))

               (= :less-than (first constraint))
               (.addLteExpression query
                                  (nth (column-defs (nth constraint 1)) 1)
                                  (nth constraint 2)))))

    ;; return only keys?
    (if return-keys-only
      (.setReturnKeysOnly query))

    ;; set column range or columns
    (if range
      (.setRange query
                 (:start range) (:finish range)
                 (:reversed range) (:count range))
      (if (not return-keys-only)
        (if columns
          (.setColumnNames query (into-array (if (coll? columns) columns
                                                 [columns])))
          (.setColumnNames query
                           (into-array (map #(nth % 1)
                                            (filter #(= family (first %))
                                                    (vals column-defs))))))))

    ;; set row count and start key
    (if row-count (.setRowCount query row-count))
    (if start-key (.setStartKey query start-key))

    ;; fetch out result
    (let [result (.execute query)]

      ;; parse the result into a map
      (with-meta
        (doall
         (for [row (.getList (.get result))]
           (let [key (.getKey row)
                 columns (for [column (.getColumns
                                       (.getColumnSlice row))]
                           (with-meta {;; column name
                                       (if (instance? java.lang.String
                                                      (.getName column))
                                         (key-for-column-name column-defs
                                                              family
                                                              (.getName column))
                                         (.getName column))

                                       ;; column value
                                       (if (instance? java.lang.String
                                                      (.getName column))
                                         (.fromByteBuffer
                                          ((serializers
                                            (serializer-for-column
                                             column-defs
                                             family
                                             (.getName column))))
                                          (.getValueBytes column))
                                         (.getValueBytes column))}

                             ;; set metadata
                             {:clock (.getClock column)
                              :name-serializer (.getNameSerializer
                                                column)
                              :ttl (.getTtl column)
                              :value-serializer ((serializers
                                                 (serializer-for-column
                                                  column-defs
                                                  family
                                                  (.getName column))))}))]
             (let [map-out (apply merge columns)]
               [key (if map-out
                      (with-meta map-out
                        (apply merge
                               (map #(hash-map (first (keys %)) (meta %))
                                    columns)))
                      map-out)]))))

        ;; set metadata
        {:execution-time-micro (.getExecutionTimeMicro result)
         :execution-time-nano (.getExecutionTimeNano result)
         :host-used (.getHostUsed result)
         :query (.getQuery result)}))))

(defn mutator-result-map
  "Returns a map of data for the provided mutator result."
  [result]
  {:execution-time-micro (.getExecutionTimeMicro result)
   :execution-time-nano (.getExecutionTimeNano result)
   :host-used (.getHostUsed result)})


(defn index-query-delete
  "Deletes all rows matching an index query. This function will delete
  all of the items in batches according to the given
  \"row-count\". See the usage of \"index-query\" for information on
  how this query should be constructed, note that we don't accept a
  list of columns here as we will be deleting them all.

  This function will execute a query and then page through the
  results, deleting the returned rows. Depending on how big your data
  set is, this could take a while.

  The \"family\" value may be either the name of a family or a
  sequence of family names. If a sequence is provided, the first
  family name is the one on which the index-query is run against. The
  rows retrieved will subsequently be removed from all of the family
  names in the sequence. This is meant to support a case where one
  family contains the constraint columns and the other families
  contain related data.

  You may also provide a callback handler function in order to receive
  updates on the status of the deleting. This function should accept
  the following parameters.

    rows-processed The number of rows processed in this batch
    keys A sequence of keys processed in this batch
    next-key The row key used to find the start of the next batch to process

  The callback function isn't executed in another thread.

  This behavior of this function may be customized with the following
  keys.

    :id-key The field in the column definition with the unique row id, :id
    :row-count The number of rows to fetch per batch; 11
    :start-key The key the rows will start from; nil
    :callback-fn  Function to handle update callbacks, nil"
  [column-defs family constraints
   & {:keys [id-key row-count start-key callback-fn]
      :or {id-key :id  row-count 10  start-key nil callback-fn nil}}]

  (loop [result []
         start-key nil]


    ;; query for the next 10 rows
    (let [key-ser (nth (column-defs id-key) 2)
          rows (index-query column-defs
                            (if (coll? family) (first family) family)
                            constraints
                            :id-key id-key
                            :return-keys-only true
                            :start-key start-key
                            :row-count (inc row-count))
          keys-in (take row-count (map first rows))
          next-key (if (< row-count) (first (last rows)))]

      ;; create a mutator to handle our deletes
      (let [mutator (HFactory/createMutator *KEYSPACE*
                                            ((serializers key-ser))
                                            (BatchSizeHint. (dec row-count) 0))]

        ;; setup to delete the row for each family
        (dorun (for [family-this (if (coll? family) family [family])]
                 (.addDeletion mutator
                               keys-in
                               family-this)))

        (cond

          ;; this isn't our last batch
          next-key
          (let [result-this (mutator-result-map (.execute mutator))]

            ;; invoke the callback function with our status
            (if callback-fn
              (callback-fn (count keys-in) keys-in next-key))

            ;; delete this batch, recur for the next
            (recur (conj result result-this)
                   next-key))

          ;; delete this batch and return our results
          :else
          (let [result-this (mutator-result-map (.execute mutator))]

            ;; invoke the callback function with our status
            (if callback-fn
              (callback-fn (count keys-in) keys-in next-key))

            ;; delete the last batch
            (conj result result-this)))))))

(defn template
  "Accepts a map that defines how data should be stored and returns a
  map of templates and updater, reader and delete functions. This
  function should be called in the context of a (with-keyspace ...)
  form. You may then use the reader and updater functions from this
  map to read and write data. Here's an example specification map:

    {:posted_date ['meta'    'postedDate' :string :string :long]
     :title       ['article' 'title'      :string :string :string]
     :content     ['article' 'content'    :string :string :string]}

  The returned map will have three keys, :templates, :updaters
  and :readers. You can update a value like so:

    ((:title (:updaters template)) '1jk123jk23' 'Awesome Article')

  To retrieve a value, you can do something like this:

    ((:title (:readers template)) '1jk123jk23')

  Or to delete a value:

    ((:title (:deleters template)) '1jk123jk23')"
  [map-in]

  ;; generate set of templates
  (let [colfamtmpls
        (into {}  (for [key-in (keys map-in)]
                    {key-in (column-family-template (nth (map-in key-in) 0)
                                                    (nth (map-in key-in) 2)
                                                    (nth (map-in key-in) 3))}))

        ;; generate a set of updaters from our templates
        updaters
        (into {} (for [key-in (keys map-in)]
                   {key-in (fn [id value]
                             (let [updater (update-column-family-template
                                            (colfamtmpls key-in)
                                            (nth (map-in key-in) 1)
                                            (nth (map-in key-in) 4)
                                            id
                                            value)]))}))

        ;; generate a set of readers from our templates
        readers
        (into {} (for [key-in (keys map-in)]
                   {key-in (fn [id]
                             (read-column-family-template
                              (colfamtmpls key-in)
                              (nth (map-in key-in) 1)
                              (nth (map-in key-in) 4)
                              id))}))

        ;; generate a set of delete templates
        deleters
        (into {} (for [key-in (keys map-in)]
                   {key-in (fn [id]
                             (delete-column-family-template
                              (colfamtmpls key-in)
                              (nth (map-in key-in) 1)
                              id))}))]

    ;; return a map of our templates, updaters and readers
    {:definition map-in
     :templates colfamtmpls
     :updaters updaters
     :readers readers
     :deleters deleters}))

(defn mapper
  "Either stores or retrieves data from Cassandra using the provided
  template. If a map of data is supplied, that data is inserted into
  Cassandra; note that the keys on the provided map need to math the
  keys in the map definition that were used to create the supplied
  template. If an id is provided, each reader in the template will be
  invoked for the provided id and the results will be assembled into a
  map of data. Note that there are no nil values in Cassandra, if
  there isn't a value for column family, column name, id combination
  then that key will not be present in the result map.

  Since there are no nil values in Cassandra, providing a map with a
  key that has a value of nil will cause that row's column for that
  value to be deleted.

  You may set option values with the provided keys:

    :id-key Field in the map containing the unique id, :id
    :delete Indicates rows with the given id should be removed, false
    :columns Sequence of columns to delete or retrieve, all columns"
  [templ id-or-map & {:keys [id-key delete columns]
                      :or {id-key :id delete false}}]
  (cond

    ;; write data
    (map? id-or-map)
    (doall (for [key-in (keys id-or-map)]
             (if (id-or-map key-in)

               ;; update the value
               (((:updaters templ) key-in) (id-or-map id-key)
                (id-or-map key-in))

               ;; delete the value
               (((:deleters templ) key-in) (id-or-map id-key)))))

    ;; read or delete data
    :else
    (if delete

      ;; delete data
      (do (doall (for [key-in (if columns columns
                                  (keys (:definition templ)))]
                   (((:deleters templ) key-in) id-or-map)))
          true)

      ;; read data
      (apply merge
             (filter #(not (nil? (first (vals %))))
                     (for [key-in (if columns columns
                                  (keys (:definition templ)))]
                       {key-in (((:readers templ) key-in) id-or-map)}))))))