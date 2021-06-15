package com.redmoon.clouddisk.bean;

import java.util.ArrayList;

/**
 * @Description:
 * @author: 郝炜
 * @Date: 2015-4-2上午09:45:12
 */
public class SideBean {
	private String userName;
	private int flowCount;
	private int msgCount;
	private byte modFlag;
	private byte isCheck;
	private ArrayList<String> list;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getFlowCount() {
		return flowCount;
	}

	public void setFlowCount(int flowCount) {
		this.flowCount = flowCount;
	}

	public int getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(int msgCount) {
		this.msgCount = msgCount;
	}

	public byte getModFlag() {
		return modFlag;
	}

	public void setModFlag(byte modFlag) {
		this.modFlag = modFlag;
	}

	public byte getIsCheck() {
		return isCheck;
	}

	public void setIsCheck(byte isCheck) {
		this.isCheck = isCheck;
	}

	public ArrayList<String> getList() {
		return list;
	}

	public void setList(ArrayList<String> list) {
		this.list = list;
	}

}
