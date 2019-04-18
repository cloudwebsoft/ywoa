package cn.js.fan.cache.redis;

import com.redmoon.oa.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2019/1/9 14:21
 */
public class RedisUtil {
    private static JedisPool jedisPool;
    private static RedisUtil redisUtil;

    public RedisUtil() {
    }

    public RedisUtil(JedisPool jedisPool1) {
        jedisPool = jedisPool1;
    }

    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    public static RedisUtil getPool() {
        if (jedisPool == null) {
            com.redmoon.oa.Config config = new com.redmoon.oa.Config();
            String redisHost = config.get("redisHost");
            int redisPort = config.getInt("redisPort");
            String redisPassword = config.get("redisPassword");
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

            if (!"".equals(redisPassword)) {
                jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort, 10000, redisPassword);
            } else {
                jedisPool = new JedisPool(redisHost, redisPort);
            }
            redisUtil = new RedisUtil(jedisPool);
        }
        return redisUtil;
    }

    public static void close(Jedis jedis) {
        jedis.close();
    }
}
