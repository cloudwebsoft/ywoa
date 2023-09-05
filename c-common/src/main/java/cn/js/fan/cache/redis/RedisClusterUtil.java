package cn.js.fan.cache.redis;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudwebsoft.framework.util.LogUtil;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;

public class RedisClusterUtil {
    private static JedisPoolConfig jedisPoolConfig;
    private static RedisClusterUtil redisClusterUtil;
    private static JedisCluster jedisCluster;
    private static Set<HostAndPort> hostAndPortSet;

    public RedisClusterUtil() {
    }

    public RedisClusterUtil(JedisPoolConfig jedisPoolConfig1) {
        jedisPoolConfig = jedisPoolConfig1;
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public static RedisClusterUtil getInstance() {
        if (jedisPoolConfig == null) {
            SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);

            String redisHosts = sysProperties.getRedisHost();
            String redisPorts = sysProperties.getRedisPort();
            String[] hosts = StrUtil.split(redisHosts, ",");
            if (hosts == null) {
                LogUtil.getLog(RedisClusterUtil.class).error("sys.cache.redis.host is empty");
                return null;
            }
            String[] ports = StrUtil.split(redisPorts, ",");
            if (ports == null) {
                LogUtil.getLog(RedisClusterUtil.class).error("sys.cache.redis.port is empty");
                return null;
            }
            if (hosts.length != ports.length) {
                LogUtil.getLog(RedisClusterUtil.class).error("Redis Cluster 主机与端口数不匹配");
                return null;
            }

            jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(Global.getInstance().getRedisMaxTotal()); // 默认为8
            jedisPoolConfig.setMaxIdle(Global.getInstance().getRedisMaxIdle()); // 默认为8
            jedisPoolConfig.setMinIdle(Global.getInstance().getRedisMinIdle());
            jedisPoolConfig.setMaxWaitMillis(Global.getInstance().getRedisMaxWaitMillis()); // 当资源池连接用尽后，调用者的最大等待时间，单位为毫秒
            // 多长空闲时间之后回收空闲连接
            jedisPoolConfig.setMinEvictableIdleTimeMillis(60000);
            /*// 跟验证有关
            jedisPoolConfig.setTestOnBorrow(false);
            // 跟验证有关
            jedisPoolConfig.setTestOnReturn(false);
            // 启动空闲连接的测试
            jedisPoolConfig.setTestWhileIdle(false);*/

            redisClusterUtil = new RedisClusterUtil(jedisPoolConfig);

            int k = 0;
            for (String host : hosts) {
                HostAndPort hostAndPort = new HostAndPort(host, StrUtil.toInt(ports[k], 6379));
                hostAndPortSet = new HashSet<>();
                hostAndPortSet.add(hostAndPort);
                k++;
            }

            jedisCluster = new JedisCluster(hostAndPortSet, 10000, 10000, 100, Global.getInstance().getRedisPassword(), jedisPoolConfig);
        }
        return redisClusterUtil;
    }

    public static void close(JedisCluster jedisCluster) {
        // JedisCluster set后会自动释放连接，调用的是jedis 的close方法，所以无需手工关闭
        // jedisCluster.close();
    }
}

