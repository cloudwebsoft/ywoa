package cn.js.fan.cache;

import cn.js.fan.cache.jcs.ICache;
import cn.js.fan.cache.redis.RedisUtil;
import cn.js.fan.cache.redis.SerializableUtil;
import org.apache.commons.jcs3.access.exception.CacheException;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2019/1/9 15:15
 */
public class RedisCache implements ICache {
    public RedisCache() {
    }

    @Override
    public Object get(Object name) {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getInstance().getJedis();
            boolean isExists = jedis.exists(name.toString());
            if (isExists) {
                byte[] bytes = jedis.get(name.toString().getBytes(StandardCharsets.UTF_8));
                if (bytes != null) {
                    return SerializableUtil.unserializableObj(bytes);
                }
            }
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
        return null;
    }

    @Override
    public void put(Object name, Object obj) throws CacheException {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getInstance().getJedis();
            // 对象序列化
            byte[] bytes = SerializableUtil.serializableObj(obj);
            jedis.set(name.toString().getBytes(StandardCharsets.UTF_8), bytes);
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
    }

    @Override
    public void put(Object name, Object obj, int expireSeconds) throws CacheException {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getInstance().getJedis();
            // 对象序列化
            byte[] bytes = SerializableUtil.serializableObj(obj);
            jedis.set(name.toString().getBytes(StandardCharsets.UTF_8), bytes);
            jedis.expire(name.toString().getBytes(StandardCharsets.UTF_8), expireSeconds);
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
    }

    @Override
    public void remove(Object name) throws CacheException {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getInstance().getJedis();
            jedis.del(name.toString().getBytes(StandardCharsets.UTF_8));
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
    }

    @Override
    public void remove(Object name, String groupName) throws CacheException {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getInstance().getJedis();
            jedis.hdel(groupName.getBytes(), name.toString().getBytes(StandardCharsets.UTF_8));
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
    }

    @Override
    public void invalidateGroup(String groupName) throws CacheException {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getInstance().getJedis();
            jedis.del(groupName.getBytes());
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
    }

    @Override
    public void putInGroup(Object name, String groupName, Object obj, int expireSeconds) throws CacheException {
        Jedis jedis = null;
        // 对象序列化
        try {
            jedis = RedisUtil.getInstance().getJedis();
            byte[] bytes = SerializableUtil.serializableObj(obj);
            if (bytes != null) {
                jedis.hset(groupName.getBytes(), name.toString().getBytes(StandardCharsets.UTF_8), bytes);
                jedis.expire(groupName.getBytes(), expireSeconds);
            }
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
    }

    @Override
    public void putInGroup(Object name, String groupName, Object obj) throws CacheException {
        Jedis jedis = null;
        // 对象序列化
        try {
            jedis = RedisUtil.getInstance().getJedis();
            byte[] bytes = SerializableUtil.serializableObj(obj);
            if (bytes != null) {
                jedis.hset(groupName.getBytes(), name.toString().getBytes(StandardCharsets.UTF_8), bytes);
            }
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
    }

    @Override
    public Object getFromGroup(Object name, String group) throws CacheException {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getInstance().getJedis();
            boolean isExists = jedis.exists(group);
            boolean isExistsGroup = jedis.hexists(group, name.toString());
            if (isExists && isExistsGroup) {
                byte[] bytes;
                bytes = jedis.hget(group.getBytes(), name.toString().getBytes(StandardCharsets.UTF_8));
                if (bytes != null) {
                    // 反序列化对象
                    return SerializableUtil.unserializableObj(bytes);
                }
            }
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
        return null;
    }

    @Override
    public void clear() throws CacheException {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getInstance().getJedis();
            // 只清空db0，默认选中的是db0
            // jedis.select(0);
            jedis.flushDB();
            // 清空全部库
            // jedis.flushAll();
        } finally {
            if (jedis != null) {
                RedisUtil.close(jedis);
            }
        }
    }
}
