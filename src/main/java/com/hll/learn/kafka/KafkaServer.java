package com.hll.learn.kafka;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 * @author hell
 * @date 2019/5/29
 */
@Configuration
public class KafkaServer {
    /**
     * broker集群地址
     */
    @Value("${kafka.common.kafka.broker.address:192.168.11.129:9092}")
    private String brokerAddress;
    /**
     * zookeeper地址
     */
    @Value("${kafka.common.kafka.zookeeper.connect:192.168.11.129:2181}")
    private String zookeeperConnect;

    //==================producer配置================================\\
    // 数据备份的可用性, acks=all： 这意味着leader需要等待所有备份都成功写入日志
    // 需要server接收到数据之后发出的确认接收的信号，此项配置就是指procuder需要多少个这样的确认信号
    @Value("${kafka.common.kafka.producer.acks:all}")
    private String acks;
    // 默认的批量处理消息字节数，单位字节
    @Value("${kafka.common.kafka.producer.batchSize:16384}")
    private String batchSize;
    // 最大请求字节数，单位字节
    @Value("${kafka.common.kafka.producer.maxRequestSize:33554432}")
    private String maxRequestSize;
    // producer可以用来缓存数据的内存大小，单位字节
    @Value("${kafka.common.kafka.producer.bufferMemory:33554432}")
    private String bufferMemory;
    // eventHandler定期的刷新metadata的间隔,-1只有在发送失败时才会重新刷新
    @Value("${kafka.common.kafka.producer.metadataRefresh:-1}")
    private String metadataRefresh;
    @Value("${kafka.common.kafka.producer.retries:0}")
    private String retries;
    //强制元数据时间间隔
    @Value("${kafka.common.kafka.producer.metadata.max.age:120000}")
    private String metadataMaxAge;
    @Value("${kafka.common.kafka.producer.max.block.ms:6000}")
    private String maxBlockMS;
    /**
     * 压缩方式
     */
    @Value("${kafka.common.kafka.producer.compression.type:}")
    private String compressionType;
    //=================end producer================================\\


    //=================consumer配置===============================\\
    @Value("${kafka.common.kafka.consumer.groupId:kafka}")
    private String groupId;
    // ConsumerConfig.AUTO_OFFSET_RESET_DOC
    @Value("${kafka.common.kafka.consumer.auto.offset.reset:earliest}")
    private String offsetReset;
    // 为true时consumer所fetch的消息的offset将会自动的同步到zookeeper
    @Value("${kafka.common.kafka.consumer.autoCommitFlag:true}")
    private String autoCommitFlag;
    // consumer向zookeeper提交offset的频率，单位毫秒
    @Value("${kafka.common.kafka.consumer.autoCommitInterval:1000}")
    private String autoCommitInterval;
    // 会话的超时限制，单位毫秒
    @Value("${kafka.common.kafka.consumer.sessionTimeout:15000}")
    private String sessionTimeout;
    // 消费者能读取的最大消息,单位字节。这个值应该大于或等于message.max.bytes;默认值 50*1024*1024
    @Value("${kafka.common.kafka.consumer.fetch.messageMaxBytes:52428800}")
    private String fetch_max_bytes;
    @Value("${kafka.common.kafka.consumer.fetch.max.wait.ms:6000}")
    private String fetch_max_wait_ms;
    //kafka 平衡的时间间隔
    @Value("${kafka.common.kafka.consumer.max.poll.interval.ms:120000}")
    private String max_poll_interval_ms;
    /**
     * ConcurrentMessageListenerContainer delegates to 1 or more KafkaMessageListenerContainer s to provide multi-threaded consumption.
     */
    @Value("${kafka.common.kafka.listener.concurrency:1}")
    private Integer kafka_listener_concurrency;
    /**
     * Set the max time to block in the consumer waiting for records. ms
     */
    @Value("${kafka.common.kafka.listener.pollTimeout:1000}")
    private Integer kafka_listener_pollTimeout;


    //====================生产者========================//
    /**
     * 设置生产者的配置
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "kafka.common.kafka.producer")
    public Properties producerProperties() {
        return new Properties();
    }
    @Autowired
    private ProducerFactory<String, String> producerFactory;
    /**
     * Kafka生产者工厂类,主要设置相关参数
     * @return
     */
    @Bean
    public ProducerFactory<String, String> defaultKafkaProducerFactory(Properties producerProperties) {
        Map<String, Object> props = Maps.newConcurrentMap();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddress);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, maxBlockMS);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, maxRequestSize);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.METADATA_MAX_AGE_CONFIG,metadataMaxAge);
        //压缩配置
        if(compressionType != null && !"".equals(compressionType)){
            props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        }
        resetProperties(producerProperties, props);
        return new DefaultKafkaProducerFactory<String, String>(props);
    }
    /**
     * KafkaTemplate包装了一个生产者，并提供了将数据发送到Kafka主题的方便方法。
     *提供异步和同步方法，异步方法返回未来。
     * @param producerProperties
     * @return
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(Properties producerProperties) {
        return new KafkaTemplate<>(defaultKafkaProducerFactory(producerProperties));
    }
    /**
     * 消费者实例
     * @return
     */
    @Bean
    public Producer<String, String> kafkaProducer() {
        Producer<String, String> kafkaProducer = producerFactory.createProducer();
        return kafkaProducer;
    }
    //======================消费者====================//
    /**
     * 设置消费者的配置
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "kafka.common.kafka.consumer")
    public Properties consumerProperties() {
        return new Properties();
    }
    @Autowired
    private ConsumerFactory<String, String> consumerFactory;
    /**
     * 消费者工厂类,主要设置相关配置
     * @return
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory(Properties consumerProperties) {
        Map<String, Object> propsMap = new HashMap<String, Object>();
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                this.brokerAddress);
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, autoCommitFlag);
        propsMap.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG,
                autoCommitInterval);
        propsMap.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeout);
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        propsMap.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetch_max_bytes);
        propsMap.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG,
                fetch_max_wait_ms);
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetReset);
        propsMap.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,max_poll_interval_ms);
        resetProperties(consumerProperties, propsMap);
        if (propsMap.containsKey("security.krb5.conf"))
            System.setProperty("java.security.krb5.conf", String.valueOf(propsMap.get("security.krb5.conf")));
        if (propsMap.containsKey("security.login.conf"))
            System.setProperty("java.security.auth.login.config", String.valueOf(propsMap.get("security.login.conf")));
        return new DefaultKafkaConsumerFactory<String, String>(propsMap);	}
    /**
     * @KafkaListener Annotation 容器监听工厂类
     * @return
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(kafka_listener_concurrency);
        //Set the max time to block in the consumer waiting for records
        factory.getContainerProperties().setPollTimeout(kafka_listener_pollTimeout);
        factory.setBatchListener(true);
        return factory;
    }

    /**
     * 消费者实例
     * @return
     */
	@Bean
	public KafkaConsumer<String, String> kafkaConsumer() {
		KafkaConsumer<String, String> kafkaConsumer = (KafkaConsumer<String, String>) consumerFactory.createConsumer();
		return kafkaConsumer;
	}

    @Value("${kafka.common.kafka.records.task.size:50}")
    private int kafkaTaskSize;
    /**
     * 定義線程池，用於規則執行使用
     * @return
     */
    @Bean
    public ListeningExecutorService listeningExecutorService() {
        return MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(kafkaTaskSize, new ThreadFactoryBuilder().setNameFormat("invoke-kafka-%d").build()));
    }

    /**
     * 对x_y_z这样的key转换为x.y.z
     * @param properties
     * @param propsMap
     * @return
     */
    private Map<String, Object> resetProperties(Properties properties,
                                                Map<String, Object> propsMap) {
        for (Map.Entry<Object, Object> property : properties.entrySet()) {
            propsMap.put(((String) property.getKey()).replace("_", "."),
                    property.getValue());
        }

        return propsMap;
    }

   //==================kafka stream===========================/
//    @Bean
//    public StreamsBuilder streamsBuilder(){
//        Properties config = new Properties();
//        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count");
//        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddress);
//        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//    }
}
