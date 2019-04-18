package com.redmoon.forum.ui;

import java.io.Serializable;

/**
 *
 * <p>Title: 横幅</p>
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
public class Theme implements Serializable {
    public static String basePath = "upfile/forum/theme";

    public Theme() {
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getCode() {
        return code;
    }

    public String getAuthor() {
        return author;
    }

    public String getBanner() {
        return banner;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getHeight() {
        return height;
    }

    private String code;
    private String author;
    private String banner;
    private String path;
    private String name;
    private String height;

}
