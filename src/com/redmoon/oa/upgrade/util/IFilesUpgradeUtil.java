package com.redmoon.oa.upgrade.util;

public interface IFilesUpgradeUtil {

	public abstract void upgrade(String sourceFolder, String destFolder);

	public abstract void clearFolder(String folder);

}