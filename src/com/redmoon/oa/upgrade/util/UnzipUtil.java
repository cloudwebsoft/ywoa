package com.redmoon.oa.upgrade.util;

import java.io.File;

import com.redmoon.oa.upgrade.service.UpgradeException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class UnzipUtil implements IUnzipUtil {
	/* (non-Javadoc)
	 * @see com.redmoon.oa.upgrade.util.IUnzipUtil#unzip(java.io.File, java.lang.String)
	 */
	public void unzip(File zipfile, String dest) {
		new File(dest).mkdirs();
		ZipFile zip;
		try {
			zip = new ZipFile(zipfile);
			zip.extractAll(dest);
		} catch (ZipException e) {
			throw new UpgradeException("Unzip failed.", e);
		}
		
	}
}
