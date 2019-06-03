package com.hll.learn.zk.zkapi;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;

/**
 * @author hell
 * @date 2019/4/1
 */
public class ZkSession implements Watcher {

    // 超时时间
    private static final Integer timeout = 5000;
    private ZooKeeper zooKeeper;

    public ZkSession(String ips) throws IOException {
        this.zooKeeper = new ZooKeeper(ips,timeout,new ZkSession());
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public ZkSession() {
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("接收到watch通知 + "+event.getPath());
    }

    public ZooKeeper.States getState(){
        return zooKeeper.getState();
    }

    /**
     * 同步或者异步创建节点，都不支持子节点的递归创建，异步有一个callback函数
     * 参数：
     * path：节点创建的路径
     * data：节点所存储的数据的byte[]
     * acl：控制权限策略
     *          Ids.OPEN_ACL_UNSAFE --> world:anyone:cdrwa
     *          CREATOR_ALL_ACL --> auth:user:password:cdrwa
     * createMode：节点类型, 是一个枚举
     *          PERSISTENT：持久节点
     *          PERSISTENT_SEQUENTIAL：持久顺序节点
     *          EPHEMERAL：临时节点
     *          EPHEMERAL_SEQUENTIAL：临时顺序节点
     */
    public void createZKNode(String path, byte[] data, List<ACL> acls,CreateMode createMode) {
        String result;
        try {
            result = zooKeeper.create(path, data, acls, createMode);
            System.out.println("创建节点：" + result + "  成功...");
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除节点数据
     * @param path
     */
    public void deleteNode(final String path){
        try {
            zooKeeper.delete(path,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
