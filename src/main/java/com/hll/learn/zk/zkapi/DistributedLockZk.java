package com.hll.learn.zk.zkapi;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author hell
 * @date 2019/4/16
 */
public class DistributedLockZk implements Watcher {
    /**
     * zookeeper原生api去实现一个分布式锁
     */
    ZooKeeper zk = null;
    private String root = "/myTestlocks";
    /**
     * 表示当前获取到的锁名称-也就是节点名称
     */
    private String myZonode;
    /**
     * 表示当前等待的节点
     */
    private String waitNode;
    private CountDownLatch latch;
    /**
     *  超时时间
     */
    private static final int SESSION_TIMEOUT = 100000;

    public DistributedLockZk(String config) {
        try {
            zk = new ZooKeeper(config, SESSION_TIMEOUT, this);
            // 判断是不是已经存在locks节点，不需要监听root节点
            Stat stat = zk.exists(root, false);
            // 如果不存在，则创建根节点
            if (stat == null) {
                zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        // 如果计数器不为空话话，释放计数器锁
        if (this.latch != null) {
            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            this.latch.countDown();
        }
    }

    /**
     * 获取锁的方法
     */
    public boolean lock(String name) {
        if (tryLock(name)) {
            return true;
        }
        try {
            return waitLock(waitNode, SESSION_TIMEOUT);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 释放锁操作的方法
     */
    public void unlock() {
        try {
            zk.delete(myZonode, -1);
            myZonode = null;
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }

    }

    private boolean tryLock(String name) {
        // lock_0000000001
        String splitStr = name;
        try {
            // 创建一个有序的临时节点，赋值给myznode
            myZonode = zk.create(root + "/" + splitStr, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
            List<String> subNodes = zk.getChildren(root, false);
            // 将所有的子节点排序
            Collections.sort(subNodes);
            if (myZonode.equals(root + "/" + subNodes.get(0))) {
                // 当前客户端创建的临时有序节点是locks下节点中的最小的节点，表示当前的客户端能够获取到锁
                return true;
            }
            // 否则的话,监听比自己小的节点 locks/lock_0000000003
            String subMyZnode = myZonode.substring((myZonode.lastIndexOf("/") + 1));
            // 获取比当前节点小的节点
            waitNode = subNodes.get(Collections.binarySearch(subNodes, subMyZnode) - 1);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean waitLock(String lower, long waitTime)
            throws KeeperException, InterruptedException {
        // 获取节点状态，并添加监听
        Stat stat = zk.exists(root + "/" + lower, true);
        if (stat != null) {
            // 实例化计数器，让当前的线程等待
            this.latch = new CountDownLatch(1);
            this.latch.await(waitTime, TimeUnit.MILLISECONDS);
            this.latch = null;
        }
        return true;
    }
}
