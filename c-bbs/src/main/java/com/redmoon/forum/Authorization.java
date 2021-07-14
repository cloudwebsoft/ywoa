package com.redmoon.forum;

import java.io.Serializable;

/**
 * <p>Title: 用户登录信息</p>
 *
 * <p>Description:用户登录信息，存储于session中 </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Authorization implements Serializable {
    public Authorization(String name, boolean guest) {
        this.name = name;
        this.guest = guest;
        this.stayTime = System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public long getStayTime() {
        return stayTime;
    }

    public boolean isArrestChecked() {
        return arrestChecked;
    }

    public void setStayTime(long stayTime) {
        this.stayTime = stayTime;
    }

    public void setArrestChecked(boolean arrestChecked) {
        this.arrestChecked = arrestChecked;
    }

    long stayTime = 0;
    String name = "";
    boolean guest = true;
    private boolean arrestChecked = false; // 是否已检测过被捕
}
