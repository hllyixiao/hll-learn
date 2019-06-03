package com.hll.learn.other;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.TtlRunnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hell
 * @date 2019/4/9
 */
public class TransmittableThreadLocalTest {

    public static void main(String[] args) throws InterruptedException {
        //ThreadLocal t = new ThreadLocal();
        //InheritableThreadLocal t = new InheritableThreadLocal();
        TransmittableThreadLocal t= new TransmittableThreadLocal();
        //t.set("main");

        //ExecutorService executorService = Executors.newCachedThreadPool();
        ExecutorService executorService = Executors.newFixedThreadPool(10);


        for (int i = 0; i < 20; i++) {
            TtlRunnable tt = TtlRunnable.get(new Runnable() {
                @Override
                public void run() {
                    if (t.get() == null) {
                        String name = Thread.currentThread().getName();
                        t.set(name);
                        System.out.println("设置值：" + name);
                    } else {
                        System.out.println("获取值：" + t.get());
                    }
                }
            });
            
            Thread.sleep(2000);
            executorService.execute(tt);
        }

        executorService.shutdown();
    }
}
