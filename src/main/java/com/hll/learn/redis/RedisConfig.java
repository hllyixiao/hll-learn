package com.hll.learn.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Jedis是一个非常小的Redis java客户端。
 * @author hell
 * @date 2019/2/28
 */
@Configuration
public class RedisConfig {

    @Value("${redis.host:192.168.59.129}")
    private String host;
    @Value("${redis.port:6379}")
    private int port;
    @Value("${redis.pass:}")
    private String pass;


    /** 连接池配置 **/
    @Value("${redis.pool.maxIdle:300}")
    private int maxIdle;  // 设置最大闲置个数
    @Value("${redis.pool.minIdle:10}")
    private int minIdle;  // 设置最小闲置个数
    @Value("${redis.maxTotal:600}")
    private int maxTotal; // 设置最大的连接数
    // 当资源池连接用尽后,调用者的最大等待时间(单位为毫秒)
    @Value("${redis.maxWaitMillis:10000}")
    private int maxWaitMillis;
    // 向资源池借用连接时是否做连接有效性检测(ping),无效连接会被移除
    // 业务量很大时候建议设置为false(多一次ping的开销),默认false
    @Value("${redis.testOnBorrow:true}")
    private boolean testOnBorrow;

    /**
     * jedis连接池配置对象
     * @return
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig(){
        final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        return jedisPoolConfig;
    }

    @Bean
    public JedisPool jedisPool(@Qualifier("jedisPoolConfig") JedisPoolConfig jedisPoolConfig){
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,host,port);
        return jedisPool;
    }

}
