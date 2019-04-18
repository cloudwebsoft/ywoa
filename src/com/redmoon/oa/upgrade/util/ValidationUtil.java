package com.redmoon.oa.upgrade.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;

import com.redmoon.oa.upgrade.domain.VersionResponse;
import com.redmoon.oa.upgrade.service.UpgradeException;

public class ValidationUtil implements IValidationUtil {
	/* (non-Javadoc)
	 * @see com.redmoon.oa.upgrade.util.IValidationUtil#validate(java.io.File, com.redmoon.oa.upgrade.domain.VersionResponse)
	 */
	public void validate(File file, VersionResponse versionResponse) {
		if (file.length() != versionResponse.getSize()) {
			throw new UpgradeException("Size of file " + file.toString()
					+ " not match: expected " + versionResponse.getSize()
					+ " actual " + file.length());
		}
		try {
			String md5 = md5(file);
			if (!md5.equals(versionResponse.getMd5())) {
				throw new UpgradeException("Md5 of file " + file.toString()
						+ " not match: expected " + versionResponse.getMd5()
						+ " actual " + md5);
			}
		} catch (IOException e) {
			throw new UpgradeException("Md5 of file " + file.toString()
					+ " failed.", e);
		}
	}

	private String md5(File file) throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			return DigestUtils.md5Hex(inputStream);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {

				}
			}
		}
	}
}
