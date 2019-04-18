package com.redmoon.oa.upgrade.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.redmoon.oa.upgrade.service.UpgradeException;

public class FilesUpgradeUtil implements IFilesUpgradeUtil {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.redmoon.oa.upgrade.util.IFilesUpgradeUtil#upgrade(java.lang.String,
	 * java.lang.String)
	 */
	public void upgrade(String sourceFolder, String destFolder) {
		if (!new File(sourceFolder).exists()) {
			return;
		}
		try {
			FileUtils.copyDirectory(new File(sourceFolder),
					new File(destFolder));
		} catch (IOException e) {
			throw new UpgradeException("Upgrade files failed.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.redmoon.oa.upgrade.util.IFilesUpgradeUtil#clearFolder(java.lang.String
	 * )
	 */
	public void clearFolder(String folder) {
		try {
			FileUtils.deleteDirectory(new File(folder));
		} catch (IOException e) {
			throw new UpgradeException("Clean folder failed.", e);
		}
	}
}
