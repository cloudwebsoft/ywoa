package cn.js.fan.cache.jcs;

import org.apache.jcs.access.exception.CacheException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public interface ICache {
    public Object get(Object name);
    public void put(Object name, Object obj) throws CacheException;
    public void remove(Object name) throws CacheException;

    public void putInGroup(Object name, String groupName, Object obj) throws CacheException;
    public void remove(Object name, String groupName) throws CacheException;
    public void invalidateGroup(String groupName) throws CacheException;
    public Object getFromGroup(Object name, String group) throws CacheException;
    void clear() throws CacheException;
}
