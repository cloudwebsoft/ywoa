package com.cloudweb.oa.enums;

public enum LogType {
    /**
     * 操作类型
     */
    LOGIN(0, "login"),
    LOGOUT(1, "logout"),
    AUTHORIZE(2, "authority");

    private int type;
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    LogType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
