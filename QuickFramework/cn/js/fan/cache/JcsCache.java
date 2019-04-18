package cn.js.fan.cache;

import cn.js.fan.cache.jcs.ICache;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * @Author: qcg
 * @Description:
 * @Date: 2019/1/9 15:15
 */
public class JcsCache implements ICache {
	private String cacheName = "RMCache";
	private Logger logger = Logger.getLogger(JcsCache.class);
	private static JCS cache;
	public JcsCache() {
		try {
			if (cache == null) {
				cache = JCS.getInstance(cacheName);
			}
		} catch (CacheException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public Object get(Object name) {
		Object obj = cache.get(name);
		return obj;
	}

	@Override
	public void put(Object name, Object obj) throws CacheException {
		cache.put(name, obj);
	}

	@Override
	public void remove(Object name) throws CacheException {
		cache.remove(name);
	}

	@Override
	public void putInGroup(Object name, String groupName, Object value) throws CacheException {
		cache.putInGroup(name, groupName, value);
	}

	@Override
	public void remove(Object name, String groupName) throws CacheException {
		cache.remove(name, groupName);
	}

	@Override
	public void invalidateGroup(String groupName) throws CacheException {
		cache.invalidateGroup(groupName);
		// 20060414发现invalidateGroup有时不对清空所有的key，导致在listtopic.jsp时因缓存未刷新，新发的贴子出不来，因此在这里再检查一下，手工删除
		java.util.Set set = cache.getGroupKeys(groupName);
		Iterator ir = set.iterator();
		Object obj = null;
		while (ir.hasNext()) {
			obj = ir.next();
			remove(obj, groupName);
		}
	}

	@Override
	public Object getFromGroup(Object name, String group) throws CacheException {
		Object obj = cache.getFromGroup(name, group);
		return obj;
	}

	@Override
	public void clear() throws CacheException{
		cache.clear();
	}
}
