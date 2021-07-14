package cn.js.fan.module.cms.ui;

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

    private String code;
    private String author;
    private String path;
    private String name;
    private String tableBorderClr;

}
