package org.almatrasporti.ETL_L1.transformers;

import org.almatrasporti.common.models.CANBusMessage;
import org.almatrasporti.common.utils.Config;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;
import java.util.regex.Pattern;

abstract class Transformer implements ITransformerAdapter {
    protected abstract CANBusMessage parse(String value);

    protected abstract CANBusMessage fix(CANBusMessage message);

    protected abstract String convert(CANBusMessage message);

    @Override
    public void transform(Pattern inputTopicPattern, String outputTopic, String errorTopic) {

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, this.getApplicationId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.getInstance().get("Kafka.servers") != null ? Config.getInstance().get("Kafka.servers") : "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream(inputTopicPattern);


        KStream<String, String> jsonLines = textLines
                .map((key, value) -> KeyValue.pair(key, this.parse(value)))
                .filter((key, value) -> value.isValid())
                .map((key, value) -> KeyValue.pair(key, this.fix(value)))
                .map((key, value) -> KeyValue.pair(key, this.convert(value)));

        jsonLines.to(outputTopic);

        KStream<String, String> errorLines = textLines
                .map((key, value) -> KeyValue.pair(key, this.parse(value)))
                .filter((key, value) -> !value.isValid())
                .map((key, value) -> KeyValue.pair(key, this.convert(value)));

        errorLines.to(errorTopic);

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}
