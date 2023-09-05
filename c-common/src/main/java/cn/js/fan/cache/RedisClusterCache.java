package cn.js.fan.cache;

import cn.js.fan.cache.jcs.ICache;
import cn.js.fan.cache.redis.RedisClusterUtil;
import cn.js.fan.cache.redis.SerializableUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.jcs3.access.exception.CacheException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.nio.charset.StandardCharsets;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2019/1/9 15:15
 */
public class RedisClusterCache implements ICache {
    public RedisClusterCache() {
    }

    @Override
    public Object get(Object name) {
        JedisCluster jedisCluster = null;
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            boolean isExists = jedisCluster.exists(name.toString());
            if (isExists) {
                byte[] bytes = jedisCluster.get(name.toString().getBytes(StandardCharsets.UTF_8));
                if (bytes != null) {
                    return SerializableUtil.unserializableObj(bytes);
                }
            }
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
        return null;
    }

    @Override
    public void put(Object name, Object obj) throws CacheException {
        JedisCluster jedisCluster = null;
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            // 对象序列化
            byte[] bytes = SerializableUtil.serializableObj(obj);
            jedisCluster.set(name.toString().getBytes(StandardCharsets.UTF_8), bytes);
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
    }

    @Override
    public void put(Object name, Object obj, int expireSeconds) throws CacheException {
        JedisCluster jedisCluster = null;
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            // 对象序列化
            byte[] bytes = SerializableUtil.serializableObj(obj);
            jedisCluster.set(name.toString().getBytes(StandardCharsets.UTF_8), bytes);
            jedisCluster.expire(name.toString().getBytes(StandardCharsets.UTF_8), expireSeconds);
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
    }

    @Override
    public void remove(Object name) throws CacheException {
        JedisCluster jedisCluster = null;
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            jedisCluster.del(name.toString().getBytes(StandardCharsets.UTF_8));
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
    }

    @Override
    public void remove(Object name, String groupName) throws CacheException {
        JedisCluster jedisCluster = null;
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            jedisCluster.hdel(groupName.getBytes(), name.toString().getBytes(StandardCharsets.UTF_8));
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
    }

    @Override
    public void invalidateGroup(String groupName) throws CacheException {
        JedisCluster jedisCluster = null;
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            jedisCluster.del(groupName.getBytes());
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
    }

    @Override
    public void putInGroup(Object name, String groupName, Object obj, int expireSeconds) throws CacheException {
        JedisCluster jedisCluster = null;
        // 对象序列化
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            byte[] bytes = SerializableUtil.serializableObj(obj);
            if (bytes != null) {
                jedisCluster.hset(groupName.getBytes(), name.toString().getBytes(StandardCharsets.UTF_8), bytes);
                jedisCluster.expire(groupName.getBytes(), expireSeconds);
            }
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
    }

    @Override
    public void putInGroup(Object name, String groupName, Object obj) throws CacheException {
        JedisCluster jedisCluster = null;
        // 对象序列化
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            byte[] bytes = SerializableUtil.serializableObj(obj);
            if (bytes != null) {
                jedisCluster.hset(groupName.getBytes(), name.toString().getBytes(StandardCharsets.UTF_8), bytes);
            }
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
    }

    @Override
    public Object getFromGroup(Object name, String group) throws CacheException {
        JedisCluster jedisCluster = null;
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            boolean isExists = jedisCluster.exists(group);
            boolean isExistsGroup = jedisCluster.hexists(group, name.toString());
            if (isExists && isExistsGroup) {
                byte[] bytes;
                bytes = jedisCluster.hget(group.getBytes(), name.toString().getBytes(StandardCharsets.UTF_8));
                if (bytes != null) {
                    // 反序列化对象
                    return SerializableUtil.unserializableObj(bytes);
                }
            }
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
        return null;
    }

    @Override
    public void clear() throws CacheException {
        JedisCluster jedisCluster = null;
        try {
            jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            for (JedisPool pool : jedisCluster.getClusterNodes().values()) {
                try (Jedis jedis = pool.getResource()) {
                    jedis.flushAll();
                }
                catch (Exception ex){
                    LogUtil.getLog(getClass()).error(ex);
                }
            }
        } finally {
            if (jedisCluster != null) {
                RedisClusterUtil.close(jedisCluster);
            }
        }
    }
}
