package com.redmoon.oa.pvg;

import java.io.Serializable;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Authorization implements Serializable {
    public Authorization(String name) {
        this.name = name;
        stayTime = System.currentTimeMillis();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStayTime(long stayTime) {
        this.stayTime = stayTime;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public String getName() {
        return name;
    }

    public long getStayTime() {
        return stayTime;
    }

    public String getUnitCode() {
        return unitCode;
    }

    private String name;
    /**
     * 在线时长
     */
    private long stayTime;

    /**
     * 用户所在单位
     */
    private String unitCode;
}
