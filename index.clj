{:namespaces
 ({:source-url nil,
   :wiki-url "com.tnrglobal.cassie.core-api.html",
   :name "com.tnrglobal.cassie.core",
   :doc
   "Provides functions that make it easier to work with the\nCassandra Hector Java API."}),
 :vars
 ({:arglists
   ([name]
    [name
     hosts
     &
     {:keys [port auto-discover],
      :or {port 9160, auto-discover true}}]),
   :name "cluster",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/cluster",
   :doc
   "Returns Cluster instance connected to the named cluster on the\nprovided sequence of hosts; if no hosts are provided then\n'localhost' is assumed. You may set optional values with the\nprovided keys:\n\n  :port Cassandra RPC port, 9160\n  :auto-discover Discover hosts via Thrift, true\n\nOnce you have a handle on your cluster, you can use\nthe (with-cluster ...) form to pass it to the other functions that\nneed it.",
   :var-type "function",
   :line 119,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([keyspace-name families]),
   :name "column-families-def",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/column-families-def",
   :doc
   "Returns sequence of column family definitions for the provided\nkeyspace name. You should provide a sequence, each item containing\nthe name and comparator for the column family. The comparator should\neither be a key into the 'comparators' map or an Object that\nimplements the Comparator interface.\n\n  (column-families 'Blog' [['articles' :utf8]\n                           ['comments' :utf8]])",
   :var-type "function",
   :line 150,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([keyspace-name name comparator]),
   :name "column-family-def",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/column-family-def",
   :doc
   "Returns a new column family with the specified name for the\nprovided keyspace name.  The provided comparator is used to sort\ncolums in a row, use the 'comparators' map to get the one that you\nneed.",
   :var-type "function",
   :line 139,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([column-family key-serializer top-serializer]),
   :name "column-family-template",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/column-family-template",
   :doc
   "Creates a column family template for the provided column family in\nthe keyspace. The 'key-serializer' is used to serialize the incoming\nkeys of new items, the 'top-serializer' is used to serialize the\nname incoming column names of items; both should be either a key\nfrom the 'serializers' map or an Object that implements the\nSerializer interface. This function should be called in the context\nof a (with-keyspace ...) form.",
   :var-type "function",
   :line 311,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists
   ([hosts
     &
     {:keys [port auto-discover],
      :or {port 9160, auto-discover true}}]),
   :name "configurator",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/configurator",
   :doc
   "Returns a CassandraHostConfigurator instance set to use the\nspecified sequence of hosts. You may set optional values with the\nprovided keys:\n\n  :port Cassandra RPC port, 9160\n  :auto-discover Discover hosts via Thrift, true\n\nYou likely won't be using this function directly, instead you'll use\nthe 'cluster' function.",
   :var-type "function",
   :line 100,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists
   ([keyspace-name
     families
     columns
     &
     {:keys [block replication strategy],
      :or
      {block true,
       replication 1,
       strategy ThriftKsDef/DEF_STRATEGY_CLASS}}]),
   :name "create-keyspace",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/create-keyspace",
   :doc
   "Creates a new keyspace with the specified name on the provided\ncluster and creates the provided columns. The 'cluster' should be an\nvalue returned by the 'cluster' function, 'families' should be a\nsequence hash of column family definitions. The 'columns' should be\na map of column symbols, family names, column names, and\nserializers (see the 'template' function for more details). This\nfunction should be called in the context of a (with-cluster ...)\nform.\n\n  (create-keyspace my-cluster\n                   'Blog'\n                   [['articles' :utf8]['comments' :utf8]]\n                   {:post-date ['articles' 'postDate' :string :string :long]})\n\nYou may set option values with the provided keys:\n\n  :block Block until keyspace created, true",
   :var-type "function",
   :line 193,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([col-fam-template column-name id]),
   :name "delete-column-family-template",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/delete-column-family-template",
   :doc
   "Uses the provided column family template to delete the specified\ncolumn for the provided id.",
   :var-type "function",
   :line 357,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([keyspace-name]),
   :name "describe-keyspace",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/describe-keyspace",
   :doc
   "Returns a map that wraps a JavaBean describing the specified\nkeyspace or nil if that keyspace does not exists. This function\nshould be called in the context of a (with-cluster ...) form.",
   :var-type "function",
   :line 277,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([keyspace-name & {:keys [block], :or {block true}}]),
   :name "drop-keyspace",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/drop-keyspace",
   :doc
   "Drops the specified keyspace from the cluster.\n\nThe behvavior of this function may be customized with the following\nkeys.\n\n  :block blocks until delete is complete, true",
   :var-type "function",
   :line 267,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists
   ([column-defs
     family
     constraints
     &
     {:keys
      [id-key columns return-keys-only range row-count start-key],
      :or
      {id-key :id,
       columns nil,
       return-keys-only false,
       range nil,
       row-count 11,
       start-key ""}}]),
   :name "index-query",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/index-query",
   :doc
   "Returns a sequence of results that match the provided query\n constraints. The \"family\" is the name of the column family\n containing the secondary index.\n\n The query shound be a sequence of sequences, each defines a portion\n of the query. The first item in the query sequence will be a key\n representing the criteria (:equals,\n :greater-than, :less-than) the next item will be the key for the\n column and the last item the column value.\n\n  [[:equals :last-name \"Miles\"]\n   [:equals :city \"Easthampton\"]]\n\nYou may provide a list of columns to be returned from the column\nfamily holding the index with the \"columns\" parameter. If\n\"columns\" is set to nil then all columns in the family will be\nreturned.\n\nThe results will be returned as a sequence, eache item in the\nsequence will contain a row key followed by a map of the column data\nfor that row. This map will also come wrapped with metadata, this\nmetadata will have the value map keys for it's keys and a map of\ninformation about the values (i.e. :clock, :name-serializer, :ttl,\netc.)\n\n  [[\"19292xx\" {:name \"Mike\" :age 42}]\n   [\"18273cs\" {:name \"Jore\" :age 38}]]\n\nThe behavior of this function may be customized by providing the\nfollowing keys.\n\n  :id-key The field in the column definition with the unique row id, :id\n  :columns Columns in the family to return, all\n  :return-keys-only returns only keys, no other data; false\n  :range set a range of columns to retrieve, this should be expressed\n    as a map with the following keys: :start, :finish, :reversed,\n    :count). Note that if you set a range of columns, you can leave\n    the \"columns\" parameter nil.\n  :row-count number of rows to return; 11\n  :start-key the key used to start returning rows",
   :var-type "function",
   :line 395,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists
   ([column-defs
     family
     constraints
     &
     {:keys [id-key row-count start-key],
      :or {id-key :id, row-count 11, start-key nil}}]),
   :name "index-query-delete",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/index-query-delete",
   :doc
   "Deletes all rows matching an index query. This function will delete\nall of the items in batches according to the given\n\"row-count\". See the usage of \"index-query\" for information on\nhow this query should be constructed, note that we don't accept a\nlist of columns here as we will be deleting them all.\n\nThis function will execute a query and then page through the\nresults, deleting the returned rows. Depending on how big your data\nset is, this could take a while.\n\nThe \"family\" value may be either the name of a family or a\nsequence of family names. If a sequence is provided, the first\nfamily name is the one on which the index-query is run against. The\nrows retrieved will subsequently be removed from all of the family\nnames in the sequence. This is meant to support a case where one\nfamily contains the constraint columns and the other families\ncontain related data.\n\nThis behavior of this function may be customized with the following\nkeys.\n\n  :id-key The field in the column definition with the unique row id, :id\n  :row-count The number of rows to fetch per batch, 11\n  :start-key The key the rows will start from, nil",
   :var-type "function",
   :line 560,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([column-defs family column-name]),
   :name "key-for-column-name",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/key-for-column-name",
   :doc "Returns the keyname for the provided column name.",
   :var-type "function",
   :line 390,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([column-defs family column-name]),
   :name "key-serializer-for-column",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/key-serializer-for-column",
   :doc
   "Returns the key serializer key for the specified column family and\nname from the provided column definition map.",
   :var-type "function",
   :line 381,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists
   ([keyspace-name
     &
     {:keys [consistency failover retry-sleep],
      :or
      {failover (:try-all failover),
       consistency (:all-one consistency),
       retry-sleep 0}}]),
   :name "keyspace",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/keyspace",
   :doc
   "Returns a Keyspace instance for the specified keyspace name on the\nprovided cluster. This function should be called in the context of\na (with-cluster ...) form. You may set option values with the\nprovided keys:\n\n  :consistency Consistency level for read and writes, all-one\n  :failover Failover strategy, try-all with 1 retry and no sleep",
   :var-type "function",
   :line 292,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists
   ([keyspace-name
     column-families
     &
     {:keys [replication strategy],
      :or {replication 1, strategy ThriftKsDef/DEF_STRATEGY_CLASS}}]),
   :name "keyspace-def",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/keyspace-def",
   :doc
   "Returns a new keyspace definition with the provided name and column\nfamilies. You may set optional values with the provided keys:\n\n  :replication Replicaiton factor, 1\n  :strategy Replication strategy, ThriftKsDef/DEF_STRATEGY_CLASS",
   :var-type "function",
   :line 167,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([keyspace-name]),
   :name "keyspace-exists?",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/keyspace-exists?",
   :doc
   "Returns true if a keyspace with the provided name exists in the\nprovided cluster. This function should be called in the context of\na (with-cluster ...) form.",
   :var-type "function",
   :line 285,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists
   ([templ
     id-or-map
     &
     {:keys [id-key delete columns], :or {id-key :id, delete false}}]),
   :name "mapper",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/mapper",
   :doc
   "Either stores or retrieves data from Cassandra using the provided\ntemplate. If a map of data is supplied, that data is inserted into\nCassandra; note that the keys on the provided map need to math the\nkeys in the map definition that were used to create the supplied\ntemplate. If an id is provided, each reader in the template will be\ninvoked for the provided id and the results will be assembled into a\nmap of data. Note that there are no nil values in Cassandra, if\nthere isn't a value for column family, column name, id combination\nthen that key will not be present in the result map.\n\nSince there are no nil values in Cassandra, providing a map with a\nkey that has a value of nil will cause that row's column for that\nvalue to be deleted.\n\nYou may set option values with the provided keys:\n\n  :id-key Field in the map containing the unique id, :id\n  :delete Indicates rows with the given id should be removed, false\n  :columns Sequence of columns to delete or retrieve, all columns",
   :var-type "function",
   :line 695,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([result]),
   :name "mutator-result-map",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/mutator-result-map",
   :doc "Returns a map of data for the provided mutator result.",
   :var-type "function",
   :line 552,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([column-defs family column-name]),
   :name "name-serializer-for-column",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/name-serializer-for-column",
   :doc
   "Returns the name serializer key for the specified column family and\nname from the provided column definition map.",
   :var-type "function",
   :line 372,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([col-fam-template column-name serializer id]),
   :name "read-column-family-template",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/read-column-family-template",
   :doc
   "Uses the provided column family template to read the specified\ncolumn for the provided id and return's that value. The 'serializer'\nvalue should either be a key from the 'serializers' map or an Object\nthat implements the Serializer interface; this is used to\nde-serialize the value read into a byte array.",
   :var-type "function",
   :line 342,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([column-defs family column-name]),
   :name "serializer-for-column",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/serializer-for-column",
   :doc
   "Returns the serializer key for the specified column family and name\nfrom the provided column definition map.",
   :var-type "function",
   :line 363,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([map-in]),
   :name "template",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/template",
   :doc
   "Accepts a map that defines how data should be stored and returns a\nmap of templates and updater, reader and delete functions. This\nfunction should be called in the context of a (with-keyspace ...)\nform. You may then use the reader and updater functions from this\nmap to read and write data. Here's an example specification map:\n\n  {:posted_date ['meta'    'postedDate' :string :string :long]\n   :title       ['article' 'title'      :string :string :string]\n   :content     ['article' 'content'    :string :string :string]}\n\nThe returned map will have three keys, :templates, :updaters\nand :readers. You can update a value like so:\n\n  ((:title (:updaters template)) '1jk123jk23' 'Awesome Article')\n\nTo retrieve a value, you can do something like this:\n\n  ((:title (:readers template)) '1jk123jk23')\n\nOr to delete a value:\n\n  ((:title (:deleters template)) '1jk123jk23')",
   :var-type "function",
   :line 626,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([col-fam-template column-name serializer id value]),
   :name "update-column-family-template",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/update-column-family-template",
   :doc
   "Uses the provided column family template to update the specified\ncolumn for the provided id with the supplied value. The 'serializer'\nvalue should either be a key from the 'serializers' map or an Object\nthat implements the Serializer interface; this is used to serialize\nthe provided value to a byte array.",
   :var-type "function",
   :line 329,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([cluster & body]),
   :name "with-cluster",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/with-cluster",
   :doc
   "Evaluates the provided forms in the context of the provided cluster\ninstance. You want to surround any calls that require a cluster\ninstance with this form.",
   :var-type "macro",
   :line 82,
   :file "src/com/tnrglobal/cassie/core.clj"}
  {:arglists ([keyspace & body]),
   :name "with-keyspace",
   :namespace "com.tnrglobal.cassie.core",
   :source-url nil,
   :raw-source-url nil,
   :wiki-url
   "/com.tnrglobal.cassie.core-api.html#com.tnrglobal.cassie.core/with-keyspace",
   :doc
   "Evaluates the provided forms in the context of the provided\nkeyspace instance. You want to surround any calls that require a\nkeyspace instance with this form.",
   :var-type "macro",
   :line 91,
   :file "src/com/tnrglobal/cassie/core.clj"})}
