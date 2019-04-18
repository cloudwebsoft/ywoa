package com.redmoon.oa.upgrade.util;


public interface IMiscUtil {

	public abstract long getSleepSecond();

	public abstract String getUpgradeVersionUrl();

	public abstract String getUpgradeStatusUrl();

	public abstract String getTomcatPath();

	public abstract String getCustomerId();

	public abstract boolean isVip();

	public abstract void restartTomcat();

	public abstract String getTempPath();

	public abstract String getProjectName();
	
	public abstract String getFirstUseInfoUrl();
}