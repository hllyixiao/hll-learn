package com.hll.learn.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * @author hell
 * @date 2019/5/12
 */
@Configuration
@ConditionalOnProperty(value = {"learn.common.cache.mode"}, havingValue = "REDIS_CLUSTER")
public class RedisClusterConfig {
    @Bean
    @ConfigurationProperties(prefix = "redis.pool")
    public GenericObjectPoolConfig getGenericObjectPoolConfig() {
        return new JedisPoolConfig();
    }

    @Autowired
    private GenericObjectPoolConfig genericObjectPoolConfig;

    /**
     * nodes:redis集群节点 .
     */
    @Value("${learn.common.redis.nodes:localhost:6379}")
    private String nodes;

    /**
     * connectionTimeout:连接超时时间.
     */
    @Value("${learn.common.redis.connectionTimeout:2000}")
    private int connectionTimeout;
    /**
     * soTimeout:socket超时.
     */
    @Value("${learn.common.redis.soTimeout:2000}")
    private int soTimeout;
    /**
     * maxAttempts:最大尝试次数.
     */
    @Value("${learn.common.redis.maxAttempts:5}")
    private int maxAttempts;
    /**
     * password:密钥.
     */
    @Value("${learn.common.redis.password:}")
    private String password;

    /**
     * redis集群Bean 注入.<br/>
     *
     * @return
     * @author
     */
    @Bean
    public JedisCluster getJedisCluster() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        if (nodes == null || nodes.length() == 0 || !nodes.contains(":")) {
            System.exit(1);
        }
        String[] pairs = nodes.split(",");

        for (String p : pairs) {
            String[] temp = p.split(":");
            if (temp.length == 2) {
                jedisClusterNodes.add(new HostAndPort(temp[0], Integer.valueOf(temp[1])));
            }
        }

        if (password == null || password.length() == 0) {
            return new JedisCluster(jedisClusterNodes, connectionTimeout, soTimeout, maxAttempts,
                    genericObjectPoolConfig);
        } else {
            return new JedisCluster(jedisClusterNodes, connectionTimeout, soTimeout, maxAttempts, password,
                    genericObjectPoolConfig);
        }
    }
}
