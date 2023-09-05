package com.redmoon.oa.kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

import cn.js.fan.web.Global;
import com.cloudweb.oa.utils.ConfigUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

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
			Resource resource = new ClassPathResource("publickey.dat");
			InputStream inputStream = resource.getInputStream();

			java.io.ObjectInputStream in = new java.io.ObjectInputStream(inputStream);
			PublicKey pubkey = (PublicKey) in.readObject();
			in.close();

			ConfigUtil configUtil = SpringUtil.getBean(ConfigUtil.class);
			inputStream = configUtil.getFile("license.dat");
			if (inputStream != null) {
				in = new java.io.ObjectInputStream(inputStream);
				// 取得license.xml
				String info = (String) in.readObject();
				// 取得签名
				byte[] signed = (byte[]) in.readObject();
				in.close();

				java.security.Signature signetcheck = java.security.Signature.getInstance("DSA");
				signetcheck.initVerify(pubkey);
				signetcheck.update(info.getBytes(StandardCharsets.UTF_8));
				if (signetcheck.verify(signed)) {
					toXML(info);
					valid = true;
				} else {
					valid = false;
					LogUtil.getLog(getClass()).info("Cloud Web license is invalid. 请联系官方获取技术支持！");
				}
			}
		} catch (java.lang.Exception e) {
			licenseStr = "（未找到许可证文件，您的配置可能不正确，请参考安装说明运行 "
					+ Global.getFullRootPath() + "/setup/index.jsp）";
			LogUtil.getLog(getClass()).error(e);
		}
		return valid;
	}
}
