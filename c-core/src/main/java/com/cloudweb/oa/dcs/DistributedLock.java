package com.cloudweb.oa.dcs;

import cn.js.fan.cache.RedissonManager;
import cn.js.fan.cache.redis.RedisClusterUtil;
import cn.js.fan.cache.redis.RedisUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudwebsoft.framework.util.LogUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.SetParams;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class DistributedLock {

    @Autowired
    SysProperties sysProperties;

    public DistributedLock() {
    }

    /**
     * 加锁
     *
     * @param lockName       锁的key
     * @param acquireTimeout 获取超时时间
     * @param timeout        锁的超时时间，毫秒
     * @return 锁标识
     */
    public String lock(String lockName, long acquireTimeout, final long timeout) {
        String retIdentifier = null;
        if (sysProperties.isRedisCluster()) {
            RedissonClient redissonClient = RedissonManager.getRedisson();
            String lockKey = "lock:" + lockName;
            RLock myLock = redissonClient.getLock(lockKey);
            //lock提供带timeout参数，timeout结束强制解锁，防止死锁
            myLock.lock(2, TimeUnit.SECONDS);
            // 1. 最常见的使用方法
            //lock.lock();
            // 2. 支持过期解锁功能,10秒以后自动解锁, 无需调用unlock方法手动解锁
            //lock.lock(10, TimeUnit.SECONDS);
            // 3. 尝试加锁，最多等待3秒，上锁以后10秒自动解锁
//        try {
//            boolean res = mylock.tryLock(3, 10, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        } else {
            Jedis conn = null;
            try {
                // 获取连接
                conn = RedisUtil.getInstance().getJedis();
                // 随机生成一个value
                String identifier = UUID.randomUUID().toString();
                // 锁名，即key值
                String lockKey = "lock:" + lockName;
                // 超时时间，上锁后超过此时间则自动释放锁
                int lockExpire = (int) (timeout / 1000);

                // 获取锁的超时时间，超过这个时间则放弃获取锁
                long end = System.currentTimeMillis() + acquireTimeout;
                while (System.currentTimeMillis() < end) {
                    // 由于setnx和expire两个操作非原子性，如果setnx成功了，expire时，该应用发生故障，甚至网络断开导致expire不成功，则会产生一个死锁
                /*if (conn.setnx(lockKey, identifier) == 1) {
                    conn.expire(lockKey, lockExpire);
                    // 返回value值，用于释放锁时间确认
                    retIdentifier = identifier;
                    return retIdentifier;
                }

                // 返回-1代表key没有设置超时时间，为key设置一个超时时间
                if (conn.ttl(lockKey) == -1) {
                    conn.expire(lockKey, lockExpire);
                }*/

                    // NX是不存在时才set， XX是存在时才set， EX是秒，PX是毫秒
                    // set返回值String，如果写入成功是“OK”，写入失败返回空
                    // if ("OK".equals(conn.set(lockKey, identifier, "NX", "EX", lockExpire))) {
                    // 从2.9.3升至4.2.3，set方法的参数变了
                    if ("OK".equals(conn.set(lockKey, identifier, new SetParams().nx().ex(lockExpire)))) {
                        // 返回value值，用于释放锁时间确认
                        retIdentifier = identifier;
                        return retIdentifier;
                    }

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (JedisException e) {
                LogUtil.getLog(getClass()).error(e);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        }
        return retIdentifier;
    }

    /**
     * 释放锁
     *
     * @param lockName   锁的key
     * @param identifier 释放锁的标识
     * @return
     */
    public boolean unlock(String lockName, String identifier) {
        boolean retFlag = false;
        if (sysProperties.isRedisCluster()) {
            RedissonClient redissonClient = RedissonManager.getRedisson();
            String lockKey = "lock:" + lockName;
            RLock myLock = redissonClient.getLock(lockKey);
            myLock.unlock();
        } else {
            Jedis conn = null;
            String lockKey = "lock:" + lockName;
            try {
                conn = RedisUtil.getInstance().getJedis();
                while (true) {
                    // 监视lock，准备开始事务
                    conn.watch(lockKey);
                    // 通过前面返回的value值判断是不是该锁，若是该锁，则删除，释放锁
                    if (identifier.equals(conn.get(lockKey))) {
                        Transaction transaction = conn.multi();
                        transaction.del(lockKey);
                        List<Object> results = transaction.exec();
                        if (results == null) {
                            continue;
                        }
                        retFlag = true;
                    }
                    conn.unwatch();
                    break;
                }
            } catch (JedisException e) {
                LogUtil.getLog(getClass()).error(e);
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        }
        return retFlag;
    }
}
