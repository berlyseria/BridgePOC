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
import java.util.concurrent.atomic.AtomicReference;

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

        // build schema registry
        result.keys().forEachRemaining(key ->{
            System.out.println(key + ": " + result.get(key));
             // record.set(key, result.getString(result.get(key).toString()));
        });

        return record.build();
    }

    private JSONObject buildJsonObject (JSONObject source, JSONObject before, JSONObject after){
        JSONObject result = new JSONObject();

        // Get source topic information:
        source.keys().forEachRemaining(key -> {
            if(key.equals("schema") || key.equals("table")){
                result.put(key, source.get(key));
                before.keys().forEachRemaining(beforeKey -> {
                    Object value = before.get(beforeKey);
                    if(value instanceof String || value instanceof Integer || value instanceof Double || value instanceof Long || value instanceof Boolean || value instanceof Byte){
                        result.put(beforeKey, value);
                    }
                });

                after.keys().forEachRemaining(afterKey -> {
                    String holder = afterKey + "_after";
                    Object value = after.get(afterKey);

                    if(value instanceof String || value instanceof Integer || value instanceof Double || value instanceof Long || value instanceof Boolean || value instanceof Byte){
                        if(after.has(afterKey)){
                            holder = afterKey + "_after";

                            if(!value.equals(before.get(afterKey))){
                                result.put(holder, value);
                            }
                        }
                    }
                });
            }
        });

        return result;
    }

}
