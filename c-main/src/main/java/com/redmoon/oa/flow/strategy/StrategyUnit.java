package com.redmoon.oa.flow.strategy;

import com.cloudwebsoft.framework.util.LogUtil;

import java.io.Serializable;

public class StrategyUnit implements Serializable {

    public StrategyUnit(String code) {
        this.code = code;
    }

    public void renew() {
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getAuthor() {
        return author;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    /**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	public IStrategy getIStrategy() {
        IStrategy ist = null;
        try {
            ist = (IStrategy) Class.forName(className).newInstance();
            ist.setX(x);
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return ist;
    }

    private String code;
    private String author;
    private String className;
    private String name;
    private int x = 0;

}
