package com.hll.learn.controller;

import com.hll.learn.redis.DistributedLockByRedis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author hell
 * @date 2019/2/26
 */
@RestController
public class RedisController {

    @Autowired(required = false)
    private JedisPool pool;
    @Autowired(required = false)
    private JedisCluster jedisCluster;
//    @Autowired
//    private JedisSentinelPool pool;

    @GetMapping("/redis/test")
    public void test(){
        Jedis jedis = pool.getResource();

        //管道 （批量操作）
        Pipeline pipeline = jedis.pipelined();
        for(int i = 0;i<10;i++){
            String content = i + "";
            pipeline.set("h"+content,content);
        }
        pipeline.sync();
        //pipeline.syncAndReturnAll();

        Map<String,Response> responses = new LinkedHashMap<String, Response>();
        for(int i = 0;i<10;i++){
            String content = i + "";
            Response<String> response = pipeline.get("h"+content);
            responses.put(content,response);
        }
        pipeline.sync();
        for(String key:responses.keySet()){
            System.out.println("key:"+key + ",value:" + responses.get(key).get());
        }


        // 事物
        jedis.watch("k2");
        jedis.unwatch();
        Transaction transaction = jedis.multi();
        transaction.incr("k2");
        transaction.exec();

        // String 操作
        System.out.println("-----------String 操作 start-----------");
        jedis.set("sk1","sv1");
        jedis.mset("sk2","sv2","sk3","sv3");
        System.out.println(jedis.get("sk3"));
        System.out.println(jedis.mget("sk1","sk2"));
        System.out.println(jedis.setnx("sk1","sk2"));
        System.out.println(jedis.getSet("sk1","sk2"));
        System.out.println(jedis.append("sk4","sk4sk4"));
        System.out.println(jedis.substr("sk4",0,3));
        System.out.println("-----------String 操作 end-----------");

        // List 操作
        System.out.println("-----------List 操作 start-----------");
        jedis.lpush("lk1","lv1");
        jedis.rpush("lk1","3456");
        jedis.lset("lk1",1,"lset");
        System.out.println(jedis.lrange("lk1",0,3));
        System.out.println(jedis.lindex("lk1",1));
        System.out.println(jedis.lpop("lk1"));
        System.out.println(jedis.rpop("lk1"));
        System.out.println("-----------List 操作 end-----------");

        // set操作
        jedis.sadd("setk1","setkv1");
        jedis.sadd("setk1","123");

        // value 操作
        System.out.println(jedis.exists("setk1"));
        System.out.println(jedis.type("setk1"));
        System.out.println(jedis.dbSize());
        System.out.println(jedis.keys("*"));
        jedis.del("sk3");
        jedis.flushDB();

        jedis.close();
    }

    @GetMapping("/redis/test2")
    public void test2(){
        // 测试集群
        jedisCluster.set("h7003","7003");
        String s = jedisCluster.get("h7003");
        System.out.println(s);
    }

    @GetMapping("/redis/test1")
    public void test1(){
        for (int i=0;i < 5; i++) {
            Thread t = new Thread() {
                @Override
                public void run() {
                    long time = System.currentTimeMillis()+1000;
                    DistributedLockByRedis d = new DistributedLockByRedis(pool.getResource());
                    for (;;) {
                        if (d.newLock("lock",String.valueOf(time))) {
                            System.out.println("当前线程: "+Thread.currentThread().getName() + "  lock");
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!d.unlock("lock",String.valueOf(time))) {
                                System.out.println("当前线程: "+Thread.currentThread().getName() + "  unlock失败");
                            }
                            break;
                        }
                    }
                }
            };
            t.start();
        }
    }
}
