package com.cloudweb.oa.enums;

public enum LogLevel {
    /**
     * 等级
     */
    NORMAL(1, "normal"),
    INFO(4, "info"),
    WARN(5, "warn"),
    ERROR(9, "error");

    private int level;
    private String desc;

    LogLevel(int level, String desc) {
        this.level = level;
        this.desc = desc;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
