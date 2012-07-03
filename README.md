Cassie
======

Cassie provides a library that makes it easier to work with the
[Cassandra](http://cassandra.apache.org/) key/value data store from
[Clojure](http://clojure.org/). Unlike some of the other Cassandra
libraries, it's primary goal is not to expose all of Cassandra's
functionality; rather it's trying to make working with Cassandra
easier on the Clojure developer.

Cassie builds on the [Hector](http://wiki.apache.org/cassandra/Hector)
library. If you find there's functionality that you need that is
missing in Cassie, it is surely available through Hector.

Installation
------------

To use Cassie within your project, add the following to the
":dependencies" in your project's
[Leiningen](https://github.com/technomancy/leiningen/) definition
("project.clj").

    [tnrglobal/cassie "0.5"]

Using the Library
-----------------

Cassie works by taking a map of your Cassandra database layout and
then using that map to translate the values in Cassandra into Clojure
maps. The first step is to define your column families.

    (def family-defs [["customers" :utf8]])

In this example we have defined one column family called "customers"
that will use the "UTF8" comparator to sort and compare column names.

Next you define your columns.

    (def column-defs
	  ;; Map Key     Col Family    Column        key     name    value   index
      {:created-date ["customers" "createdDate" :string :string :long]
	   :first-name   ["customers" "firstName"   :string :string :string]
	   :last-name    ["customers" "lastName"    :string :string :string]
	   :id           ["customers" "id"          :string :string :string :key]
	   :zip-code     ["customers" "zipCode"     :string :string :string :key]})

The keys in this map represent the keys we'll use in our Clojure
maps. Next we have a sequence describing that key; we set the column
family, column name, key serializer, name serializer, value serializer
and the type of index for the column (that's a secondary index).

It's important to note that if your columns aren't named with String
objects, Cassie won't be as helpful to you. You probably won't find
that surprising, it'd be unpleasant to pass around maps where the keys
were just arbitrary byte arrays.

Once you have your layout defined, you're ready to create your
index. All you have to do is get a handle on your cluster and then
instruct Cassie to create your new keyspace.

    (cassie/with-cluster (cassie/cluster "Test Cluster" ["33.33.33.10"])
	  (cassie/create-keyspace "widgetShop" family-defs column-defs))

Easy! If you log into your Cassandra cluster with the
[Cassandra CLI tool](http://wiki.apache.org/cassandra/CassandraCli)
you will see that the keyspace "widgetShop" has been created with the
specified column families and columns.

### Mapping

Now that your keyspace and column families are setup you can go ahead
and insert some data. First, you create a "template" that will make it
easier to add, update and remove data from Cassandra (this is built
with
[Hector's templates](http://www.nervestaple.com/hector/me/prettyprint/cassandra/service/template/ColumnFamilyTemplate.html)). With
the template in hand, you can use a "mapper" to store and retrieve
maps of data.

    (cassie/with-cluster (cassie/cluster "Test Cluster" ["33.33.33.10"])
      (cassie/with-keyspace (cassie/keyspace "widgetShop")
	    (let [template (cassie/template column-defs)]

          (cassie/mapper template
			             {:created-date (.getTime (Date.))
		                  :first-name "Emily"
						  :last-name "Miles"
						  :id "12840283"
						  :zip-code "01027"})

		  (cassie/mapper template
		                 {:created-date (.getTime (Date.))
		                  :first-name "Matthew"
						  :last-name "Hewett"
						  :id "28309198"
						  :zip-code "01027"}))))

Here we connect to our cluster, set our keyspace and then store or two
maps of customer data in Cassandra. By default, the "mapper" requires
each map to cantain a key ":id", it uses that value as the row
key. This can be customized to use another key if you'd like.

When the "mapper" is passed a map of data, that data is stored in
Cassandra. When it's passed one value (the row key or id) then the
"mapper" will fetch the data from Cassandra and return it as a
map. The data in Cassandra can easily be updated by passing a map of
new data to the "mapper". If you provide a map that has a key or keys
with nil values, those columns in Cassandra for that row key will be
removed.

Fetching the data back is equally easy.

    (cassie/with-cluster (cassie/cluster "Test Cluster" ["33.33.33.10"])
      (cassie/with-keyspace (cassie/keyspace "widgetShop")
	    (let [template (cassie/template column-defs)]
		  (cassie/mapper template "28309198"))))

We get back the map of data that we just stored.

    {:zip-code "01027", :created-date 1341338762615, :id "28309198",
	 :first-name "Matthew", :last-name "Hewett"}

Lastly, you can use the mapper to delete a row of data.

    (cassie/with-cluster (cassie/cluster "Test Cluster" ["33.33.33.10"])
      (cassie/with-keyspace (cassie/keyspace "widgetShop")
	    (let [template (cassie/template column-defs)]
		  (cassie/mapper template "28309198" :delete true))))

### Querying

Cassie supports queries on secondary indexes as well. In this example,
:zip-code is a secondary index.

    (cassie/with-cluster (cassie/cluster "Test Cluster" ["33.33.33.10"])
      (cassie/with-keyspace (cassie/keyspace "widgetShop")
	    (cassie/index-query column-defs
	                        "customers"
                            [[:equals :zip-code "01027"]])))

Cassie will return a sequence of row keys and the column data for that
row.

    (["12840283" {:zip-code "01027", :last-name "Miles", :id "12840283",
	              :first-name "Emily", :created-date 1341338762582}]
	 ["28309198" {:zip-code "01027", :last-name "Hewett", :id "28309198",
	              :first-name "Matthew", :created-date 1341338762615}])

You can inspect the metadata of the column data's map to get
additional information like clock, serializers, etc.

    {:last-name {:clock 1341338762601000,
	             :name-serializer #<StringSerializer ...>,
				 :ttl 0,
				 :value-serializer #<StringSerializer ...>},
	 :first-name {:clock 1341338762592000,
	              :name-serializer #<StringSerializer ...>,
				  :ttl 0,
				  :value-serializer #<StringSerializer ...>}, ...}

The result of a query can also be use to delete data.

    (cassie/with-cluster (cassie/cluster "Test Cluster" ["33.33.33.10"])
      (cassie/with-keyspace (cassie/keyspace "widgetShop")
	    (cassie/index-query-delete column-defs
		                           "customers"
                                   [[:equals :zip-code "01027"]])))

Cassie will return some information on how long it took to delete the
matching rows and which hosts responded.

    [{:execution-time-micro 10830, :execution-time-nano 10830000,
	  :host-used #<CassandraHost 33.33.33.10(33.33.33.10):9160>}]

Future Direction
----------------

We are currently using this library in a project that leans heavily on
Cassandra. As we run into issues or require additional functionality,
we'll be adding that to this library.
