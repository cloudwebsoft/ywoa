package cn.js.fan.cache.jcs;

import org.apache.commons.jcs3.access.exception.CacheException;

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
    Object get(Object name);
    void put(Object name, Object obj) throws CacheException;
    void put(Object name, Object obj, int expireSeconds) throws CacheException;

    void remove(Object name) throws CacheException;

    void putInGroup(Object name, String groupName, Object obj) throws CacheException;
    void putInGroup(Object name, String groupName, Object obj, int expireSeconds) throws CacheException;
    void remove(Object name, String groupName) throws CacheException;
    void invalidateGroup(String groupName) throws CacheException;
    Object getFromGroup(Object name, String group) throws CacheException;
    void clear() throws CacheException;
}
