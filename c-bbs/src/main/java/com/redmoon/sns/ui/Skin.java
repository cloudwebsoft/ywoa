package com.redmoon.sns.ui;

import java.io.Serializable;

public class Skin implements Serializable {
    public Skin() {
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTableBorderClr(String tableBorderColor) {
        this.tableBorderClr = tableBorderColor;
    }

    public void setDefaultSkin(boolean defaultSkin) {
        this.defaultSkin = defaultSkin;
    }

    public String getCode() {
        return code;
    }

    public String getAuthor() {
        return author;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getTableBorderClr() {
        return tableBorderClr;
    }

    public boolean isDefaultSkin() {
        return defaultSkin;
    }

    private String code;
    private String author;
    private String path;
    private String name;
    private String tableBorderClr;
    private boolean defaultSkin;

}
