package com.cloudwebsoft.framework.base;

import cn.js.fan.db.PrimaryKey;
import java.util.Map;
import java.util.HashMap;

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
public class QDBTable implements java.io.Serializable {
    Map sqls = new HashMap();

    public QDBTable(String name, String objName) {
        this.name = name;
        this.objName = objName;
    }

    public String getSql(String name) {
        String sql = (String)sqls.get(name);
        if (sql==null)
            return "";
        else
            return sql;
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

    public void setConnName(String connName) {
        this.connName = connName;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public void setFormValidatorFile(String formValidatorFile) {
        this.formValidatorFile = formValidatorFile;
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

    public String getConnName() {
        return connName;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public String getFormValidatorFile() {
        return formValidatorFile;
    }

    public boolean isListCachable() {
        return listCachable;
    }

    public boolean isObjCachable() {
        return objCachable;
    }

    public String name;
    public String objName;
    public PrimaryKey primaryKey;
    private String queryCreate;
    private String queryLoad;
    private String querySave;
    private String queryDel;
    private String queryList;
    private String connName;
    private int blockSize;
    private String formValidatorFile;
    private boolean listCachable = true;
    private boolean objCachable = true;
}
