package com.redmoon.oa.upgrade.domain;

import java.io.Serializable;

public class StatusRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2042352374841875292L;
	private String customer;
	private int status;
	private String version;
	private String url;
	private String message;

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
