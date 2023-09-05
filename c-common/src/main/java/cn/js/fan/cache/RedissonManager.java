package cn.js.fan.cache;

import cn.js.fan.cache.redis.RedisClusterUtil;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedissonManager {
    private static Config config = new Config();
    private static RedissonClient redisson = null;
    private static final String RAtomicName = "genId_";
    public static void init(){
        try {
            SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);
            ClusterServersConfig clusterServersConfig = config.useClusterServers()
                    .setScanInterval(200000)//设置集群状态扫描间隔
                    .setMasterConnectionPoolSize(10000)//设置对于master节点的连接池中连接数最大为10000
                    .setSlaveConnectionPoolSize(10000)//设置对于slave节点的连接池中连接数最大为500
                    .setIdleConnectionTimeout(10000)//如果当前连接池里的连接数量超过了最小空闲连接数，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉。时间单位是毫秒。
                    .setConnectTimeout(30000)//同任何节点建立连接时的等待超时。时间单位是毫秒。
                    .setTimeout(3000)//等待节点回复命令的时间。该时间从命令发送成功时开始计时。
                    .setRetryInterval(3000); //当与某个节点的连接断开时，等待与其重新建立连接的时间间隔。时间单位是毫秒。
                    // .addNodeAddress("redis://127.0.0.1:7000","redis://127.0.0.1:7001","redis://127.0.0.1:7002","redis://127.0.0.1:7003","redis://127.0.0.1:7004","redis://127.0.0.1:7005");
            JedisCluster jedisCluster = RedisClusterUtil.getInstance().getJedisCluster();
            Map<String, JedisPool> map = jedisCluster.getClusterNodes();
            for(Map.Entry<String,JedisPool> entry:map.entrySet()) {
                String keyEntry = entry.getKey();
                String strArray[] = keyEntry.split(":");
                String host = strArray[0];
                Integer port = Integer.parseInt(strArray[1]);
                clusterServersConfig.addNodeAddress("redis://" + host + ":" + port).setPassword(sysProperties.getRedisPassword());
            }

            redisson = Redisson.create(config);

            RAtomicLong atomicLong = redisson.getAtomicLong(RAtomicName);
            atomicLong.set(0);//自增设置为从0开始
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static RedissonClient getRedisson(){
        if(redisson == null){
            RedissonManager.init(); //初始化
        }
        return redisson;
    }
}
