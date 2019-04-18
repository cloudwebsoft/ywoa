package com.redmoon.oa.upgrade.service;

public class UpgradeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2943789614041550991L;

	public UpgradeException() {
		super();
	}

	public UpgradeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UpgradeException(String message) {
		super(message);
	}

	public UpgradeException(Throwable cause) {
		super(cause);
	}

}
