package cn.js.fan.base;

import cn.js.fan.db.PrimaryKey;

/**
 * <p>Title: configDB.xml中table的信息</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DBTable implements java.io.Serializable {
    public DBTable(String name, String objName) {
        this.name = name;
        this.objName = objName;
    }

    public void renew() {
        objectCache.renew();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setQueryCreate(String queryCreate) {
        this.queryCreate = queryCreate;
    }

    public void setQueryLoad(String queryLoad) {
        this.queryLoad = queryLoad;
    }

    public void setQuerySave(String querySave) {
        this.querySave = querySave;
    }

    public void setQueryDel(String queryDel) {
        this.queryDel = queryDel;
    }

    public void setQueryList(String queryList) {
        this.queryList = queryList;
    }

    public void setObjectCache(ObjectCache objectCache) {
        this.objectCache = objectCache;
    }

    public void setListCachable(boolean listCachable) {
        this.listCachable = listCachable;
    }

    public void setObjCachable(boolean objCachable) {
        this.objCachable = objCachable;
    }

    public String getName() {
        return name;
    }

    public String getObjName() {
        return objName;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public String getQueryCreate() {
        return queryCreate;
    }

    public String getQueryLoad() {
        return queryLoad;
    }

    public String getQuerySave() {
        return querySave;
    }

    public String getQueryDel() {
        return queryDel;
    }

    public String getQueryList() {
        return queryList;
    }

    public ObjectCache getObjectCache() {
        return objectCache;
    }

    public boolean isListCachable() {
        return listCachable;
    }

    public boolean isObjCachable() {
        return objCachable;
    }

    public ObjectCache getObjectCache(ObjectDb objectDb) {
        // logger.info("objectCache=" + objectCache);
        objectCache.setObjectDb(objectDb);
        return objectCache;
    }

    public String name;
    public String objName;
    public PrimaryKey primaryKey;
    private String queryCreate;
    private String queryLoad;
    private String querySave;
    private String queryDel;
    private String queryList;
    private ObjectCache objectCache;
    private boolean listCachable = true;
    private boolean objCachable = true;
}
