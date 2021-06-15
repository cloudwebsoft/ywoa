package com.redmoon.oa.kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PublicKey;

import cn.js.fan.web.Global;

public class LicenseUtil extends License {
	private static LicenseUtil license = null;

	private static Object initLock = new Object();
	
	static String licenseFilePath;
	
	public static void setLicenseFilePath(String path) {
		licenseFilePath = path;
	}
	
	public static LicenseUtil getInstance() {
		if (license == null) {
			synchronized (initLock) {
				license = new LicenseUtil();
				license.init();
			}
		}
		return license;
	}	

	/**
	 * 根据公钥对license.dat进行验证并重建XML Document
	 * 
	 * @return boolean
	 */
	@Override
	public boolean verify() {
		try {
			String filePath = Global.getAppPath() + "WEB-INF/";
			java.io.ObjectInputStream in = new java.io.ObjectInputStream(
					new java.io.FileInputStream(filePath + "publickey.dat"));
			PublicKey pubkey = (PublicKey) in.readObject();
			in.close();
			in = new java.io.ObjectInputStream(new java.io.FileInputStream(licenseFilePath));
			// 取得license.xml
			String info = (String) in.readObject();
			// 取得签名
			byte[] signed = (byte[]) in.readObject();
			in.close();

			java.security.Signature signetcheck = java.security.Signature.getInstance("DSA");
			signetcheck.initVerify(pubkey);
			signetcheck.update(info.getBytes("UTF-8"));
			if (signetcheck.verify(signed)) {
				toXML(info);
				valid = true;
			} else {
				valid = false;
				System.out.println("Cloud Web license is invalid. 请联系官方获取技术支持！");
			}
		} catch (java.lang.Exception e) {
			licenseStr = "（未找到许可证文件，您的配置可能不正确，请参考安装说明运行 "
					+ Global.getFullRootPath() + "/setup/index.jsp）";
			e.printStackTrace();
		}
		return valid;
	}
}
