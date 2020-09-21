package gms.cims.bridge;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.ExpectedBodyTypeException;
import org.apache.camel.Processor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class MessageProcessor implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAvroMessageConsumerProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            JSONObject kafkaBodyMessageObject = new JSONObject(exchange.getIn().getBody(String.class));
            exchange.getIn().setBody(GetRecordFromJsonObject(kafkaBodyMessageObject, LoadSchemaFromFile("destinationValue.avsc")));
        } catch (ExpectedBodyTypeException e) {
            LOG.error(e.toString() + exchange.getIn().getBody(String.class));
        }
    }

    private Schema LoadSchemaFromFile(String schemaFileName) throws IOException {
        return new Schema.Parser().parse(getClass().getClassLoader().getResourceAsStream(schemaFileName));
    }

    private GenericData.Record GetRecordFromJsonObject(JSONObject kafkaBodyMessageObject, Schema schema) {

        GenericRecordBuilder record = new GenericRecordBuilder(schema);

        JSONObject sourceData = kafkaBodyMessageObject.getJSONObject("source");
        JSONObject data_before = kafkaBodyMessageObject.getJSONObject("before");
        JSONObject data_after = kafkaBodyMessageObject.getJSONObject("after");

        JSONObject result = buildJsonObject(sourceData, data_before, data_after);

        result.keys().forEachRemaining(key ->{
            System.out.println(key + ": " + result.get(key));
            // record.set(key.toString(), result.getString(result.get(key).toString()));
        });

        // TO DO: BUILD SCHEMA FILE

        // SET RECORD
//        record.set("UserID", data_before.getString("UserID"));
//        record.set("CO_ContractID", data_before.getInt("CO_ContractID"));
//        // TO DO: SERIALIZE BYTES TYPE
//        // record.set("RowVersion", data_before.get("RowVersion"));
//        record.set("LastUpdate_before", data_before.get("LastUpdate"));
//        record.set("IsIndividual_before", data_before.get("IsIndividual"));
//        record.set("LastUpdate_after", data_after.get("LastUpdate"));
//        record.set("IsIndividual_after", data_after.get("IsIndividual"));
//        record.set("CCP_ID", data_before.get("CCP_ID"));
//        record.set("CCP_AdminFee_before", data_before.get("CCP_AdminFee"));
//        record.set("CCP_AdminFee_after", data_after.get("CCP_AdminFee"));

        return record.build();
    }

    private JSONObject buildJsonObject (JSONObject source, JSONObject before, JSONObject after){
        JSONObject result = new JSONObject();

        // Get source topic information:
        source.keys().forEachRemaining(key -> {
            if(key.equals("schema") || key.equals("table")){
                result.put(key, source.get(key));
            }
        });

        // System.out.println("from source:" + result);

        before.keys().forEachRemaining(key -> {
            String holder = key + "_before";
            Object value = before.get(key);
            if(value instanceof String || value instanceof Integer || value instanceof Double || value instanceof Long || value instanceof Boolean || value instanceof Byte){
                result.put(holder, value);
            }
        });

        // System.out.println("from before:" + result);

        after.keys().forEachRemaining(key -> {
            String holder = key + "_after";
            Object value = after.get(key);

             if(value instanceof String || value instanceof Integer || value instanceof Double || value instanceof Long || value instanceof Boolean || value instanceof Byte){
                if(after.has(key)){
                    holder = key + "_after";
//                    System.out.println(key + " After: " + value + " Before: " + before.get(key));

                    if(!value.equals(before.get(key))){
                        result.put(holder, value);
                    }
                }
             }
        });
        // System.out.println("from after:" + result);

        return result;
    }

}
