package com.redmoon.oa.flow.strategy;

import java.io.Serializable;
import org.apache.log4j.Logger;

public class StrategyUnit implements Serializable {
    transient Logger logger = Logger.getLogger(this.getClass().getName());

    public StrategyUnit(String code) {
        this.code = code;
    }

    public void renew() {
        if (logger==null)
            logger = Logger.getLogger(this.getClass().getName());
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
            logger.error(e.getMessage());
        }
        return ist;
    }

    private String code;
    private String author;
    private String className;
    private String name;
    private int x = 0;

}
