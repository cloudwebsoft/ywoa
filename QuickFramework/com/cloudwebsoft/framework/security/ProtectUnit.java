package com.cloudwebsoft.framework.security;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class ProtectUnit implements Serializable {
    transient Logger logger = Logger.getLogger(this.getClass().getName());
    
    /**
     * °üº¬
     */
    public static final int TYPE_INCLUDE = 0;
    /**
     * ÕýÔò
     */
    public static final int TYPE_REGULAR = 1;

    public ProtectUnit() {
    }

    public void renew() {
        if (logger==null)
            logger = Logger.getLogger(this.getClass().getName());
    }

    public void setRule(String rule) {
		this.rule = rule;
	}

	public String getRule() {
		return rule;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	private String rule;
    private int type;
}
