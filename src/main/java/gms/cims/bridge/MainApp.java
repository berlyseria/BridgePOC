package gms.cims.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javac.jvm.Gen;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Properties;


public class MainApp {

    static GenericRecord genericRecord;

    public static void main(String... args) throws Exception {

        Arguments arguments = SetArguments("CIMSTEST.Financial.ClaimStatusClaimLink",
                "CIMSTEST.Financial.ClaimStatus",
                "ClaimStatusOutput",
                "CS_ClaimStatusID",
                "ClaimStatusJoin");

        Topology topology = buildTopology(arguments);
        Properties props = buildProperties(arguments);

        final KafkaStreams streams = new KafkaStreams(topology, props);
        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static Arguments SetArguments(String topic1, String topic2, String outputTopic, String commonKey, String applicationID){

        Arguments arguments = new Arguments();

        arguments.setLeftTopicName(topic1);
        arguments.setRightTopicName(topic2);
        arguments.setOutputTopicName(outputTopic);
        arguments.setCommonKey(commonKey);
        arguments.setApplicationID(applicationID);

        return arguments;
    }

    private static Topology buildTopology(Arguments args) {
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, GenericRecord> leftTopic = builder.stream(args.getLeftTopicName());
        KStream<String, GenericRecord> rightTopic = builder.stream(args.getRightTopicName());

        KTable<String, GenericRecord> keySetTopic1 = leftTopic.map((key, value) -> KeyValue.pair(SetKey(value, args.getCommonKey()), value)).toTable();
        KTable<String, GenericRecord> keySetTopic2 = rightTopic.map((key, value) -> KeyValue.pair(SetKey(value, args.getCommonKey()), value)).toTable();

        KTable<String, GenericRecord> joined = InnerJoinKTables(args, keySetTopic1, keySetTopic2, "ClaimStatus");

        joined.toStream().to(args.getOutputTopicName());

        return builder.build();
    }

    private static Properties buildProperties(Arguments args) {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, args.getApplicationID());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, args.getBroker());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, args.getAutoOffsetResetConfig());
        props.put(args.getSchemaRegistry(), args.getSchemaRegistryURL());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, GenericAvroSerde.class);

        return props;
    }

    private static String SetKey(GenericRecord value, String commonKey) {
        if (value==null) return null;
        else return value.get(commonKey).toString();
    }

    private static KTable<String, GenericRecord> InnerJoinKTables(Arguments args, KTable<String, GenericRecord> leftTopic, KTable<String, GenericRecord> rightTopic, String className) {

        KTable<String, GenericRecord> result = leftTopic.join(rightTopic,
                (left,right) -> {
                    JSONObject leftJSON = new JSONObject(left.toString());
                    JSONObject rightJSON = new JSONObject(right.toString());
                    ObjectMapper objectMapper = new ObjectMapper();
                    ClaimStatus claim = new ClaimStatus();

                    leftJSON.keys().forEachRemaining(k -> {
                        if (!rightJSON.has(k)) {
                            rightJSON.put(k, leftJSON.get(k));
                        }
                    });

                    try {
                        claim = objectMapper.readValue(rightJSON.toString(), ClaimStatus.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return claim;
                });
        return result;
    }
}

