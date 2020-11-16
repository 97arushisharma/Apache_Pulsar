# Apache_Pulsar
This repository contains the sample apache pulsar producer and consumer with various properties enabled. Build the project using the following command:
```
mvn clean install
```

Then use the following command to run the module:
```
java -cp pulsar-1.0-SNAPSHOT-jar-with-dependencies.jar PulsarConsumer consumer.properties
```
Here, `pulsar-1.0-SNAPSHOT-jar-with-dependencies.jar` is the fat jar created in target directory and `consumer.properties` is a config file present in the `resources/` directory of the project.


## PULSAR COMMANDS

### For running Standalone- 
bin/pulsar standalone

### For consuming from a topic - 
bin/pulsar-client consume my-topic -s "first-subscription"

### To consume all the messages in the backlog-
bin/pulsar-client consume --subscription-name my-subscription --num-messages 0 my-topic

### For sending a message to a topic - 
bin/pulsar-client produce my-topic --messages "hello-pulsar"

### For displaying topic stats(It is very useful command to see how many messages have been published to a topic) - 
bin/pulsar-admin persistent stats my-topic
bin/pulsar-admin persistent stats-internal my-topic


### To get the quotas and ttl of a topic(here public/default is a namespace)-
pulsar-admin namespaces get-backlog-quotas public/default/my-topic
pulsar-admin namespaces set-message-ttl public/default/ \
  --messageTTL 120 // 2 mins


### To create and list a namespaces within a tenant - 
pulsar-admin namespaces clear-backlog public/default/
pulsar-admin namespaces create public/sqlstream
pulsar-admin namespaces list public

### To view per partition stats of a topic-
bin/pulsar-admin persistent partitioned-stats public/sqlstream/trial-1 --per-partition

### To create a partitioned topic-
bin/pulsar-admin topics create-partitioned-topic   persistent://public/sqlstream/trial-1 --partitions 4

### To delete a partitioned topic-
bin/pulsar-admin topics delete-partitioned-topic persistent://public/sqlstream/trial-1


### To start standalone pulsar without logging on screen
Nohup bin/pulsar standalone &

### To list all the bookies
bookkeeper shell listbookies -a

### To view topic stats
for i in `seq 1 100`; do echo $(bin/pulsar-admin persistent stats public/sqlstream/trial-1 | egrep "msgRateIn|msgThroughputIn|averageMsgSize"); done
for i in `seq 1 100`; do echo $(bin/pulsar-admin persistent stats-internal public/sqlstream/trial-1 | egrep "size|numberOfEntries|currentLedgerEntries|currentLedgerSize"); done
for i in `seq 1 100`; do echo $(bin/pulsar-admin persistent partitioned-stats public/sqlstream/trial-1 --per-partition | egrep "msgInCounter|persistent"); done


### Things to note:
* We need to create namespace explicitly for a client.
* Give Absolute paths in all confs
* Message retention: Keep the data for at least X hours (even if acknowledged)
* Time-to-live: Discard data after some time (by automatically acknowledging)
* Set sendTimeoutMs to 0 for infinite polling when a producer client goes down and keep the same producerName. We also have to set the send timeout to 0—meaning an “infinite” timeout—because we cannot stop retrying if we want to make sure the message is processed effectively, or else we would fall back on the at-most-once field. See: [https://www.splunk.com/en_us/blog/it/effectively-once-semantics-in-apache-pulsar.html](https://www.splunk.com/en_us/blog/it/effectively-once-semantics-in-apache-pulsar.html)
* Since release 1.20, however, Pulsar has supported message deduplication at the system level. This can be enabled in the namespace configuration. Here's an example:
		$ pulsar-admin namespaces set-deduplication $MY_NAMESPACE --enable
With this setting, Pulsar brokers will ensure that duplicated messages will be discarded rather than persisted.

### FOR TLS : 

It establishes the connection only in the first trial. After that no connection is made with bookies.
bin/pulsar-client --url pulsar+ssl://127.0.0.1:6651 consume -s "first-subscription" --num-messages 0 my-topic

Add the following properties to the standalone.conf and broker.conf files and follow the link below:
[http://pulsar.apache.org/docs/en/security-tls-transport/](http://pulsar.apache.org/docs/en/security-tls-transport/)
```
brokerServicePortTls=6651
webServicePortTls=8443
functionsWorkerEnabled=true

# Deprecated - Use webServicePortTls and brokerServicePortTls instead
tlsEnabled=true

# Tls cert refresh duration in seconds (set 0 to check on every new connection)
tlsCertRefreshCheckDurationSec=300

# Path for the TLS certificate file
tlsCertificateFilePath=/home/sqlstream/Downloads/my-ca/broker.cert.pem

# Path for the TLS private key file
tlsKeyFilePath=/home/sqlstream/Downloads/my-ca/broker.key-pk8.pem

# Path for the trusted TLS certificate file.
# This cert is used to verify that any certs presented by connecting clients
# are signed by a certificate authority. If this verification
# fails, then the certs are untrusted and the connections are dropped.
tlsTrustCertsFilePath=/home/sqlstream/Downloads/my-ca/certs/ca.cert.pem
```

### CONSUMER:

I am answering this to doc this issue - do not use the loops to consume messages, instead adopt the MessageListener subscribed to consumer via
consumer.messageListener(new Myconsumer())
or
consumer.messageListener((consumer, msg)->{//do something})
Docs didnt mention this, but I found surfing the consumer api.

### Links:

https://stackoverflow.com/questions/61525447/apache-pulsar-message-delivery-semantics
https://stackoverflow.com/questions/57523574/exactly-once-delivery-end-to-end-with-pulsar-and-flink
https://stackoverflow.com/questions/61445563/apache-pulsar-maximum-supported-message-size
https://stackoverflow.com/questions/59204479/apache-pulsar-java-client-taking-too-much-memory-oom
