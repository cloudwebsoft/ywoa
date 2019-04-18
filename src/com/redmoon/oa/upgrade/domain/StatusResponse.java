package com.redmoon.oa.upgrade.domain;

import java.io.Serializable;

public class StatusResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5899798348285916514L;
	private boolean result;

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

}
