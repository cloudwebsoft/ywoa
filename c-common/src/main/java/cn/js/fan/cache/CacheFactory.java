package cn.js.fan.cache;

import cn.js.fan.cache.jcs.ICache;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2019/1/9 15:13
 */
public class CacheFactory {
	public static final int CACHE_JCS = 0;
	public static final int CACHE_REDIS = 1;
	public static final int CACHE_REDIS_CLUSTER = 2;

	public static ICache getInstance(int type){
		if (CACHE_REDIS == type){
			return new RedisCache();
		}
		else if (CACHE_REDIS_CLUSTER == type) {
			return new RedisClusterCache();
		}
		else {
			return new JcsCache();
		}
	}
}
