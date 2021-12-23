# Camel Quarkus AMQ Message Redelivery on Broker side 

This demonstrates how AMQ Broker message redelivery can be used with Camel Quarkus.

Contains two use cases:

- Running with Quarkus and test containers, no broker side redelivery delay
- Running with external AMQ Broker which has fixed 5 seconds delivery delay

In both cases Camel is instructed to shut down in few seconds.

## Running the application in dev mode with Testcontainers and Quarkus

First make sure you have Docker daemon running, Artemis testcontainer needs that.

You can run your application in dev mode that enables live coding using:
```shell script
mvn compile quarkus:dev
```

Observe redelivery happening pretty much immediately in the logs, example here with 92 ms.

```
2022-01-04 18:41:52,853 INFO  [org.apa.qpi.jms.JmsConnection] (AmqpProvider :(8):[amqp://127.0.0.1:5672]) Connection ID:2e6ea2b2-d842-4e5f-8c33-76a1723fb7a0:4 connected to server: amqp://127.0.0.1:5672
2022-01-04 18:41:52,866 INFO  [info] (Camel (camel-2) thread #4 - JmsConsumer[exampleQueue]) Exchange[ExchangePattern: InOnly, Headers: {CamelMessageTimestamp=1641314512775, firedTime=Tue Jan 04 18:41:52 EET 2022, JMSCorrelationID=null, JMSCorrelationIDAsBytes=null, JMSDeliveryMode=2, JMSDestination=exampleQueue, JMSExpiration=0, JMSMessageID=ID:a8ae3c6c-058b-477a-86e1-2f144fc04ee5:3:1:1-1, JMSPriority=4, JMSRedelivered=true, JMSReplyTo=null, JMSTimestamp=1641314512775, JMSType=null, JMSXDeliveryCount=2, JMSXGroupID=null, JMSXUserID=null}, BodyType: String, Body: Message #1]
2022-01-04 18:41:52,867 ERROR [amqp-redelivery-route] (Camel (camel-2) thread #4 - JmsConsumer[exampleQueue]) got from amqp: Message #1, took 92 ms
```

## Running with external AMQ Broker

Tested with AMQ Broker 7.9.0 GA.

This uses artemis-maven-plugin first to start AMQ Broker with broker.xml and then runs CamelMain.java and then shuts down broker and exists.

Broker.xml has 5 seconds redelivery delay configured:

```
 <address-setting match="exampleQueue">
   <redelivery-delay>5000</redelivery-delay>
 </address-setting>
```

Possibly you need to change AMQ installation dir, see pom.xml. Default is:
```<activemq.basedir>${user.home}/tools/amq/amq-broker-7.9.0</activemq.basedir>```

Overriding that from CLI:

```shell
mvn clean verify -DskipTests -Dactivemq.basedir=/path/to/AMQ/Broker/root 
```
Running the example:

```shell script
 mvn clean verify -DskipTests
```

Example output, took a tad bit over 5 seconds more now because of the AMQ Broker delaying the message redelivery:

```
[INFO] Connection ID:55f0404b-4bd3-4ee2-ac17-cae0688554c2:8 connected to server: amqp://127.0.0.1:61616
[INFO] Exchange[ExchangePattern: InOnly, Headers: {CamelMessageTimestamp=1641314788674, firedTime=Tue Jan 04 18:46:28 EET 2022, JMSCorrelationID=null, JMSCorrelationIDAsBytes=null, JMSDeliveryMode=2, JMSDestination=exampleQueue, JMSExpiration=0, JMSMessageID=ID:08063b14-1cb3-43c8-ab00-4dba606f405a:3:1:1-1, JMSPriority=4, JMSRedelivered=true, JMSReplyTo=null, JMSTimestamp=1641314788674, JMSType=null, JMSXDeliveryCount=2, JMSXGroupID=null, JMSXUserID=null}, BodyType: String, Body: Message #1]
[ERROR] got from amqp: Message #1, took 5135 ms
```

If you already have the AMQ Broker running, change connection uri in the application.properties and run

```shell script
 mvn clean verify -DskipTests -DnoServer
```

# More about these things

This basically is a combination of [Artemis examples](https://activemq.apache.org/components/artemis/documentation/latest/examples.html) and Apache Camel docs like [Main](https://camel.apache.org/components/3.14.x/others/main.html#_examples) and [examples](https://github.com/apache/camel-examples/tree/main/examples).
