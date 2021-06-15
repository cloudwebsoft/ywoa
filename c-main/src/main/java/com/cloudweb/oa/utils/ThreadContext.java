package com.cloudweb.oa.utils;

import cn.js.fan.cache.jcs.RMCache;
import com.cloudwebsoft.framework.base.IThreadContext;
import com.redmoon.kit.util.FileUpload;
import org.apache.jcs.access.exception.CacheException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Tomcat 的线程池重用了线程
 *  要特别注意在代码运行完后，显式地去清空设置的数据，调用local.remove
 *
 *  ThreadLocal 对象建议使用 static 修饰。
 *  这个变量是针对一个线程内所有操作共享的，所以设置为静态变量，所有此类实例共享此静态变量 ，
 *  也就是说在类第一次被使用时装载，只分配一块存储空间，所有此类的对象(只 要是这个线程内定义的)都可以操控这个变量。
 */
@Component
public class ThreadContext implements IThreadContext {

    public static final String REQUEST = "request";
    public static final String FILEUPLOAD = "fileupload";

    /**
     * 应用场景
     */
    public static final String SCENE = "scene";
    /**
     * 应用场景：流程
     */
    public static final String SCENE_FLOW = "flow";

    public static final String IS_ABNORMAL = "isAbnormal";

    private static ThreadLocal<Map> local = new ThreadLocal<Map>();

    private List<CacheKey> cacheKeys = new ArrayList();

    // 获取数据库连接
    public Map get() {
        return local.get();
    }

    public void set(Map map) {
        local.set(map);
    }

    @Override
    public void addCacheKey(Object key) {
        if (isSceneFlow()) {
            cacheKeys.add(new CacheKey(key));
        }
    }

    @Override
    public void addCacheKey(Object key, Object groupName) {
        if (isSceneFlow()) {
            cacheKeys.add(new CacheKey(key, groupName));
        }
    }

    @Override
    public List<CacheKey> getCachedKeys() {
        return cacheKeys;
    }

    public void setScene(String scene) {
        Map map = local.get();
        if (map==null) {
            map = new HashMap();
        }
        map.put(SCENE, scene);
        local.set(map);
    }

    public static String getScene() {
        Map map = local.get();
        if (map==null) {
            return null;
        }
        return (String)map.get(SCENE);
    }

    /**
     * 场景是否为流程
     * @return
     */
    @Override
    public boolean isSceneFlow() {
        String scene = getScene();
        return SCENE_FLOW.equals(scene);
    }

    @Override
    public void setSceneFlow() {
        setScene(SCENE_FLOW);
    }

    public void setAbnormal(boolean abnormal) {
        Map map = local.get();
        if (map==null) {
            map = new HashMap();
        }
        map.put(IS_ABNORMAL, abnormal);
        local.set(map);
    }

    public boolean isAbnormal() {
        Map map = local.get();
        if (map==null) {
            return false;
        }
        Boolean re = (Boolean)map.get(IS_ABNORMAL);
        if (re == null) {
            return false;
        }
        else {
            return re;
        }
    }

    public void setFileUpload(FileUpload fileUpload) {
        Map map = local.get();
        if (map==null) {
            map = new HashMap();
        }
        map.put(FILEUPLOAD, fileUpload);
        local.set(map);
    }

    public FileUpload getFileUpload() {
        Map map = local.get();
        if (map==null) {
            return null;
        }
        return (FileUpload)map.get(FILEUPLOAD);
    }

    public void setRequest(HttpServletRequest request) {
        Map map = local.get();
        if (map==null) {
            map = new HashMap();
        }
        map.put(REQUEST, request);
        local.set(map);
    }

    public HttpServletRequest getRequest() {
        Map map = local.get();
        if (map==null) {
            return null;
        }
        return (HttpServletRequest)map.get(REQUEST);
    }

    @Override
    public void remove() {
        // 如果中间有异常，则清缓存
        if (isAbnormal()) {
            try {
                for (CacheKey cacheKey : cacheKeys) {
                    if (cacheKey.isGroup()) {
                        RMCache.getInstance().remove(cacheKey.getKey(), (String) cacheKey.getGroupName());
                    } else {
                        RMCache.getInstance().remove(cacheKey.getKey());
                    }
                }
            } catch (CacheException e) {
                e.printStackTrace();
            }
        }
        local.remove();
    }

    private class CacheKey {
        Object key;
        Object groupName;
        boolean group;

        public CacheKey(Object key) {
            this.key = key;
            group = false;
        }

        public CacheKey(Object key, Object groupName) {
            this.key = key;
            this.groupName = groupName;
            group = true;
        }

        public Object getKey() {
            return key;
        }

        public Object getGroupName() {
            return groupName;
        }

        public boolean isGroup() {
            return group;
        }
    }
}
