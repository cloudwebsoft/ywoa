package com.redmoon.oa.tigase.upload.commons.vo;

public class UploadItem {
	private String oFileName;
	private String oUrl;
	private String tUrl;
	private Byte status;
	private String message;

	public UploadItem(String oFileName, String oUrl, Byte status, String message) {
		super();
		this.oFileName = oFileName;
		this.oUrl = oUrl;
		this.status = status;
		this.message = message;
	}

	public UploadItem(String oFileName, String oUrl, String tUrl) {
		super();
		this.oFileName = oFileName;
		this.oUrl = oUrl;
		this.tUrl = tUrl;
	}

	public UploadItem(String oFileName, String oUrl, String tUrl, Byte status,
			String message) {
		super();
		this.oFileName = oFileName;
		this.oUrl = oUrl;
		this.tUrl = tUrl;
		this.status = status;
		this.message = message;
	}

	public String getOFileName() {
		return oFileName;
	}

	public void setOFileName(String oFileName) {
		this.oFileName = oFileName;
	}

	public String getOUrl() {
		return oUrl;
	}

	public void setOUrl(String oUrl) {
		this.oUrl = oUrl;
	}

	public String getTUrl() {
		return tUrl;
	}

	public void setTUrl(String tUrl) {
		this.tUrl = tUrl;
	}

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
