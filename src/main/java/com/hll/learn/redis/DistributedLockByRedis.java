package com.hll.learn.redis;

import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

/**
 * 用redis实现分布式锁 满足条件:
 * (1)互斥:在任何时刻，对于同一条数据，只有一台应用可以获取到分布式锁
 * (2)不能发生死锁:一台服务器挂了，程序没有执行完，但是redis中的锁却永久存在了，
 *    那么已加锁未执行完的数据，就永远得不到处理了，直到人工发现，或者监控发现；
 * (3)高可用性:可以保证程序的正常加锁，正常解锁
 * (4)加锁解锁必须由同一台服务器进行，不能出现你加的锁，别人给你解锁了。
 *
 * @author hell
 * @date 2019/2/28
 */
public class DistributedLockByRedis {

    private Jedis jedis;

    public DistributedLockByRedis(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public boolean newLock(String key,String value){
        boolean lock = false;
        SetParams setParams = new SetParams();
        setParams.nx(); // 同setnx功能
        setParams.ex(20); // 设置指定的过期时间(单位秒) px(毫秒)
        // 能保证nx与ex的原子操作
        String status = jedis.set(key, value, setParams);
        if ("OK".equals(status)) {
            lock = true;
        }
        return lock;
    }


    /**
     * 这里必须满足各个服务器的时间相同
     * @param key
     * @param value
     * @return
     */
    public boolean oldLock(String key,String value){
        //  “set if not exits”
        try {
            if(jedis.setnx(key,value) == 1L){
                // 可以成功设置,也就是key不存在
                return true;
            }

            // 判断锁超时 - 防止原来的操作异常，没有运行解锁操作  （防止死锁）
            String currentValue = jedis.get(key);
            // currentValue不为空且小于当前时间，表示锁过期了
            if(!StringUtils.isEmpty(currentValue)
                    && Long.parseLong(currentValue) < System.currentTimeMillis()){
                // 锁过期，则设置当前的value （返回key的旧值，并设置新值,是原子操作）
                String oldValue =jedis.getSet(key,value);

                // 假设两个线程同时进来这里，因为key被占用了，而且锁过期了。
                // 获取的值currentValue=A(get取的旧的值肯定是一样的),
                // 两个线程的value都是B（时间戳一样）,key都是K，锁时间已经过期了。
                // 而这里面的getSet一次只会一个执行，也就是一个执行之后，
                // 上一个的value已经变成了B。只有一个线程获取的值会是A，另一个线程拿到的值是B。
                if(!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)){
                    // oldValue不为空且oldValue等于currentValue，
                    // 也就是校验是不是上个对应的key的时间戳，也是防止并发
                    return true;
                }
            }
        } catch (Exception e) {

        } finally {
            jedis.close();
        }
        return false;
    }

    /**
     * 解锁
     * @param key
     * @param value
     * @return
     */
    public boolean unlock(String key,String value){
        boolean unlock = false;
        try {
            String currentValue = jedis.get(key);
            if(!StringUtils.isEmpty(currentValue) && currentValue.equals(value)){
                System.out.println("当前线程: "+Thread.currentThread().getName() + "  unlock");
                // 删除key
                jedis.del(key);
                unlock = true;
            }
        } catch (Exception e) {
            System.out.println("错误");
        }
        return unlock;
    }
}

