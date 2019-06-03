package com.hll.learn.zk.zkapi;

import com.sun.glass.ui.EventLoop;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author hell
 * @date 2019/4/1
 */
public class ZkTest {

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        distributeLock();
    }

    private static void distributeLock(){
        ExecutorService executorService = Executors.newCachedThreadPool();
        int count = 10;

        for (int i = 0; i < 3; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        DistributedLockZk distributeLockDemo = new DistributedLockZk(
                                "192.168.59.129:2181");
                        boolean lock = distributeLockDemo.lock("test_");
                        if (lock) {
                            System.out.println(Thread.currentThread().getName()+"获得锁");
                            Thread.sleep(6000);
                            System.out.println(Thread.currentThread().getName()+"释放锁");
                            distributeLockDemo.unlock();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            executorService.execute(runnable);
        }
        //executorService.shutdown();
    }

    private static void test1() throws IOException {
        String ips = "192.168.59.129:2181";
        String path = "/mytest/h";

        ZkSession zkSession = new ZkSession(ips);
        //ZooKeeper zk = zkSession.getZooKeeper();

        //Stat stat = zk.exists(path, false);

        String d = "helinlianghahahahah";

        zkSession.createZKNode(path,d.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        //System.out.println(zk.exists(path,false));
        //ZooKeeper.States state = zk.getState();
        //System.out.println(zk.getACL(path, state));
        //System.out.println(zk.getChildren(path, true));

        System.out.println(zkSession.getState());
        zkSession.deleteNode(path);
    }
}
