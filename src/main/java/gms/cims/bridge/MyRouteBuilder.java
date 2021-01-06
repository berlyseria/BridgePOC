package gms.cims.bridge;

import org.apache.camel.builder.RouteBuilder;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        // here is a sample which processes the input files
        // (leaving them in place - see the 'noop' flag)
        // then performs content based routing on the message using XPath
        from("kafka:" + Arguments.SourceTopic + "?brokers=" + Arguments.Broker
                + "&maxPollRecords=5000"
                + "&consumersCount=1"
                + "&groupId=" + Arguments.GroupId
                + "&keyDeserializer=" + StringDeserializer.class.getName()
                + "&valueDeserializer=" + KafkaAvroDeserializer.class.getName())
                .process(new MessageProcessor())
                .log("Message received from Kafka : ${body}")
                .to("kafka:" + Arguments.DestinationTopic + "?brokers=" + Arguments.Broker
                        + "&serializerClass=" + KafkaAvroSerializer.class.getName());
    }

}
