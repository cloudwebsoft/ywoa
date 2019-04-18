package cn.js.fan.cache;

import cn.js.fan.cache.jcs.ICache;
import cn.js.fan.cache.redis.RedisUtil;
import cn.js.fan.cache.redis.SerializableUtil;
import org.apache.jcs.access.exception.CacheException;
import redis.clients.jedis.Jedis;

import java.io.UnsupportedEncodingException;

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
		Jedis jedis = RedisUtil.getPool().getJedis();
		boolean isExists = jedis.exists(name.toString());
		if (isExists) {
			byte[] bytes = jedis.get(name.toString().getBytes());
			RedisUtil.close(jedis);
			return SerializableUtil.unserializableObj(bytes);
		}
		RedisUtil.close(jedis);
		return null;
	}

	@Override
	public void put(Object name, Object obj) throws CacheException {
		Jedis jedis = RedisUtil.getPool().getJedis();

		// 对象序列化
		byte[] bytes = SerializableUtil.serializableObj(obj);

		jedis.set(name.toString().getBytes(), bytes);
		RedisUtil.close(jedis);

	}

	@Override
	public void remove(Object name) throws CacheException {
		Jedis jedis = RedisUtil.getPool().getJedis();

		jedis.del(name.toString().getBytes());
		RedisUtil.close(jedis);

	}

	@Override
	public void putInGroup(Object name, String groupName, Object obj) throws CacheException {
		Jedis jedis = RedisUtil.getPool().getJedis();
		// 对象序列化
		try {
			byte[] bytes = SerializableUtil.serializableObj(obj);
			if (bytes != null) {
				String objStr = new String(bytes, "utf-8");
				// jedis.hset(groupName, name.toString(), objStr);
				jedis.hset(groupName.getBytes(), name.toString().getBytes(), bytes);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			RedisUtil.close(jedis);
		}
	}

	@Override
	public void remove(Object name, String groupName) throws CacheException {
		Jedis jedis = RedisUtil.getPool().getJedis();

		jedis.hdel(groupName.getBytes(),name.toString().getBytes());
		RedisUtil.close(jedis);

	}

	@Override
	public void invalidateGroup(String groupName) throws CacheException {
		Jedis jedis = RedisUtil.getPool().getJedis();

		jedis.del(groupName.getBytes());
		RedisUtil.close(jedis);

	}

	@Override
	public Object getFromGroup(Object name, String group) throws CacheException {
		Jedis jedis = RedisUtil.getPool().getJedis();

		boolean isExists = jedis.exists(group);
		boolean isExistsGroup = jedis.hexists(group, name.toString());
		if (isExists && isExistsGroup){
			byte[] bytes = jedis.hget(group.getBytes(), name.toString().getBytes());
			// 反序列化对象
			RedisUtil.close(jedis);
			return SerializableUtil.unserializableObj(bytes);
		}
		RedisUtil.close(jedis);

		return null;
	}

	@Override
	public void clear() throws CacheException {
		Jedis jedis = RedisUtil.getPool().getJedis();

		jedis.flushAll();
		RedisUtil.close(jedis);
	}
}
