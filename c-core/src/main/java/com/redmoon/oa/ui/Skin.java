package com.redmoon.oa.ui;

import java.io.*;

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

    public void setMenuHeight(int menuHeight) {
        this.menuHeight = menuHeight;
    }

    public void setTopHeight(int topHeight) {
        this.topHeight = topHeight;
    }

    public void setBottomHeight(int bottomHeight) {
        this.bottomHeight = bottomHeight;
    }

    public void setLeftWidth(int leftWidth) {
        this.leftWidth = leftWidth;
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

    public int getMenuHeight() {
        return menuHeight;
    }

    public int getTopHeight() {
        return topHeight;
    }

    public int getBottomHeight() {
        return bottomHeight;
    }

    public int getLeftWidth() {
        return leftWidth;
    }

    private String code;
    private String author;
    private String path;
    private String name;
    private String tableBorderClr;
    private boolean defaultSkin;
    private int menuHeight;
    private int topHeight;
    private int bottomHeight;
    private int leftWidth;
    
    public String getLeftMenuTopBtn() {
		return leftMenuTopBtn;
	}

	public void setLeftMenuTopBtn(String leftMenuTopBtn) {
		this.leftMenuTopBtn = leftMenuTopBtn;
	}

	private String leftMenuTopBtn;

}
