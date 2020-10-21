package gms.cims.bridge;

import org.apache.avro.Schema;

public class Arguments {

    private static final String Broker = "localhost:9092";
    private static final String SchemaRegistry = "schema.registry.url";
    private static final String SchemaRegistryURL = "http://localhost:8081";
    private static final String AutoOffsetResetConfig = "earliest";

    private static String LeftTopicName;
    private static String RightTopicName;
    private static String OutputTopicName;
    private static String CommonKey;
    private static String ApplicationID;
    private static Schema ValueSchema;

    /* GETTERS AND SETTERS */
    public static String getBroker() {
        return Broker;
    }

    public static String getSchemaRegistry() { return SchemaRegistry; }

    public static String getSchemaRegistryURL() {
        return SchemaRegistryURL;
    }

    public static String getAutoOffsetResetConfig() {
        return AutoOffsetResetConfig;
    }

    public static String getLeftTopicName() {
        return LeftTopicName;
    }

    public static void setLeftTopicName(String leftTopicName) {
        LeftTopicName = leftTopicName;
    }

    public static String getRightTopicName() {
        return RightTopicName;
    }

    public static void setRightTopicName(String rightTopicName) {
        RightTopicName = rightTopicName;
    }

    public static String getOutputTopicName() {
        return OutputTopicName;
    }

    public static void setOutputTopicName(String outputTopicName) {
        OutputTopicName = outputTopicName;
    }

    public static String getCommonKey() {
        return CommonKey;
    }

    public static void setCommonKey(String commonKey) {
        CommonKey = commonKey;
    }

    public static String getApplicationID() { return ApplicationID; }

    public static void setApplicationID(String applicationID) {
        ApplicationID = applicationID;
    }

    public static Schema getValueSchema() { return ValueSchema; }

    public static void setValueSchema(Schema valueSchema) { ValueSchema = valueSchema; }
}
