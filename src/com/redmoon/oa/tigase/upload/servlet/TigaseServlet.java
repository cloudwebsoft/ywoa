package com.redmoon.oa.tigase.upload.servlet;

import java.io.File;

/**
 * @Description:
 * @author: 古月圣
 * @Date: 2016-7-4下午05:34:16
 */
public abstract class TigaseServlet {
	private String userId;
	private String access_token;
	private File[] file1;
	private String[] file1ContentType;
	private String[] file1FileName;
	private String result;

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the accessToken
	 */
	public String getAccess_token() {
		return access_token;
	}

	/**
	 * @param accessToken
	 *            the accessToken to set
	 */
	public void setAccess_token(String accessToken) {
		this.access_token = accessToken;
	}

	/**
	 * @return the file1
	 */
	public File[] getFile1() {
		return file1;
	}

	/**
	 * @param file1
	 *            the file1 to set
	 */
	public void setFile1(File[] file1) {
		this.file1 = file1;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @return the file1ContentType
	 */
	public String[] getFile1ContentType() {
		return file1ContentType;
	}

	/**
	 * @param file1ContentType
	 *            the file1ContentType to set
	 */
	public void setFile1ContentType(String[] file1ContentType) {
		this.file1ContentType = file1ContentType;
	}

	/**
	 * @return the file1FileName
	 */
	public String[] getFile1FileName() {
		return file1FileName;
	}

	/**
	 * @param file1FileName
	 *            the file1FileName to set
	 */
	public void setFile1FileName(String[] file1FileName) {
		this.file1FileName = file1FileName;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	public abstract String execute();
}
