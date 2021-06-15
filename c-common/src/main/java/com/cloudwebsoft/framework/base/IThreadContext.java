package com.cloudwebsoft.framework.base;

import java.util.List;

public interface IThreadContext {

    boolean isSceneFlow();

    void setSceneFlow();

    void addCacheKey(Object key);

    void addCacheKey(Object key, Object groupName);

    void remove();

    List getCachedKeys();
}
