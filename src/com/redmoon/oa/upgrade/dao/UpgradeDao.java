package com.redmoon.oa.upgrade.dao;

public interface UpgradeDao {

	public void saveBeginUpgrade(String customer, String version, String url);

	public void saveEndUpgrade(String customer, String version, String url,
			int result, String message);
	
	public boolean isUpgrading();
	
	public String getCurrentVersion();
	
	public int getAllUsers();
	
	public int getLoginTotals();
	
	public int getPhoneloginTotals();
	
	public int getPhoneUsers();
	
	public int getFlowNums();
	
	public int getDocumentNums();
	
	public int getMessageNums();
	
	public int getNoticeNums();
	
	public int getWorkNoteCount();
	
}
