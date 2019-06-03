package com.hll.learn.controller;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author hell
 * @date 2019/5/29
 */
@RestController
public class KafkaController {

    @Autowired
    private Producer<String, String> kafkaProducer;

    @Autowired
    private KafkaConsumer<String, String> kafkaConsumer;

    @GetMapping("/kafka/test")
    public void test(){
        String[] a=new String[]{"a","b","c","d","a","b","c","d","a","b","c","d","a"};
        for (int i = 0; i < 10; i++) {
            String msg = "my name is hll";

            kafkaProducer.send(new ProducerRecord<String, String>("HelloWorld", msg));
            System.out.println("Sent:" + msg);
        }
        test2();
//        kafkaConsumer.subscribe(Arrays.asList("HelloWorld"));
//        ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
//        for (ConsumerRecord<String, String> record : records) {
//            System.out.printf("offset = %d, value = %s", record.offset(), record.value());
//            System.out.println();
//        }
//    }

    }
    private void test2(){
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.11.129:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream("HelloWorld");
        KTable<String, Long> wordCounts = textLines.mapValues(textLine -> textLine.toLowerCase())
                .flatMapValues(textLine -> Arrays.asList(textLine.split(" ")))
                .selectKey((key, word) -> word)
                .groupByKey()
                .count(Materialized.as("Counts"));

        wordCounts.toStream().to("streams-wordcount-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.start();

        kafkaConsumer.subscribe(Arrays.asList("streams-wordcount-output"));
        ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
        for (ConsumerRecord<String, String> record : records) {
            System.out.printf("offset = %d, value = %s", record.offset(), record.value());
            System.out.println();
        }
    }
}
