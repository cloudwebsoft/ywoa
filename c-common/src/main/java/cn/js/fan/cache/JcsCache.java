package cn.js.fan.cache;

import cn.js.fan.cache.jcs.ICache;
import cn.js.fan.util.PropertiesUtil;
import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.access.GroupCacheAccess;
import org.apache.commons.jcs3.access.exception.CacheException;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

/**
 * @Author:
 * @Description:
 * @Date: 2019/1/9 15:15
 */
public class JcsCache implements ICache {
	private String cacheName = "RMCache";
	private static CacheAccess<Object, Object> cache;
	GroupCacheAccess<Object, Object> groupCache;

	public JcsCache() {
		try {
			IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
			String cfgPath = configUtil.getFilePath();
			File cfgFile = new File(cfgPath + "/cache.ccf");
			LogUtil.getLog(getClass()).info("JcsCache " + cfgPath + "/cache.ccf");
			// 如果文件存在，则置配置文件
			if (cfgFile.exists()) {
				LogUtil.getLog(getClass()).info("JcsCache cache.ccf is found.");
				PropertiesUtil propertiesUtil = new PropertiesUtil(cfgPath + "/cache.ccf");
				Properties props = propertiesUtil.getSafeProperties();
				JCS.setConfigProperties(props);
			}

			if (cache == null) {
				cache = JCS.getInstance(cacheName);
			}
			if (groupCache == null) {
				groupCache = JCS.getGroupCacheInstance(cacheName);
			}
		} catch (CacheException e) {
			LogUtil.getLog(getClass()).error(e.getMessage());
		}
	}

	@Override
	public Object get(Object name) {
		return cache.get(name);
	}

	@Override
	public void put(Object name, Object obj) throws CacheException {
		cache.put(name, obj);
	}

	@Override
	public void put(Object name, Object obj, int expireSeconds) throws CacheException {
		cache.put(name, obj);
	}

	@Override
	public void remove(Object name) throws CacheException {
		cache.remove(name);
	}

	@Override
	public void putInGroup(Object name, String groupName, Object value) throws CacheException {
		groupCache.putInGroup(name, groupName, value);
	}

	@Override
	public void putInGroup(Object name, String groupName, Object value, int expireSeconds) throws CacheException {
		groupCache.putInGroup(name, groupName, value);
	}

	@Override
	public void remove(Object name, String groupName) throws CacheException {
		groupCache.removeFromGroup(name, groupName);
	}

	@Override
	public void invalidateGroup(String groupName) throws CacheException {
		groupCache.invalidateGroup(groupName);
	}

	@Override
	public Object getFromGroup(Object name, String group) throws CacheException {
		return groupCache.getFromGroup(name, group);
	}

	@Override
	public void clear() throws CacheException{
		cache.clear();
	}
}
