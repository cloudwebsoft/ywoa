package com.cloudwebsoft.framework.security;

import java.io.Serializable;

public class ProtectUnit implements Serializable {

    /**
     * 包含
     */
    public static final int TYPE_INCLUDE = 0;

    /**
     * 正则
     */
    public static final int TYPE_REGULAR = 1;

    public ProtectUnit() {
    }

    public void renew() {
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
