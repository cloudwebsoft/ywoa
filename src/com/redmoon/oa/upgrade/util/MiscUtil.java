package com.redmoon.oa.upgrade.util;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.redmoon.oa.kernel.License;

public class MiscUtil implements IMiscUtil {
	private Logger logger = LoggerFactory.getLogger(MiscUtil.class);
	
	private String customerId = UUID.randomUUID().toString();
	private int maxSleepSecond = 14400;
	private String upgradeVersionUrl = "http://localhost:8080/oa_server/public/upgrade/url.action";
	private String upgradeStatusUrl = "http://localhost:8080/oa_server/public/upgrade/status.action";
	private String tomcatPath = "/Users/blackkensai/tmp/tomcat";
	private String tempPath = "/Users/blackkensai/tmp";
	private String projectName = "oa6.0";
	private boolean isVip = false;
	private String restartCommand = null;
	private String firstUseInfoUrl = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IMiscUtil#getSleepSecond()
	 */
	public long getSleepSecond() {
		return (long) (Math.random() * maxSleepSecond * 1000);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IMiscUtil#getUpgradeVersionUrl()
	 */
	public String getUpgradeVersionUrl() {
		return this.upgradeVersionUrl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IMiscUtil#getUpgradeStatusUrl()
	 */
	public String getUpgradeStatusUrl() {
		return this.upgradeStatusUrl;
	}

	public String getTempPath() {
		return this.tempPath + "/" + UUID.randomUUID().toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IMiscUtil#getTomcatPath()
	 */
	public String getTomcatPath() {
		return this.tomcatPath ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IMiscUtil#getCustomerId()
	 */
	public String getCustomerId() {
		License lic = License.getInstance();
		customerId = lic.getEnterpriseNum();
		return customerId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IMiscUtil#isVip()
	 */
	public boolean isVip() {
		return isVip ;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.util.IMiscUtil#restartTomcat()
	 */
	public void restartTomcat() {
		if(StringUtils.hasLength(this.restartCommand)) {
			try {
				Runtime.getRuntime().exec(restartCommand);
			} catch (IOException e) {
				logger.error("Failed to restart tomcat:", e);
			}
		}
	}

	public String getProjectName() {
		return this.projectName ;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public void setMaxSleepSecond(int maxSleepSecond) {
		this.maxSleepSecond = maxSleepSecond;
	}

	public void setUpgradeVersionUrl(String upgradeVersionUrl) {
		this.upgradeVersionUrl = upgradeVersionUrl;
	}

	public void setUpgradeStatusUrl(String upgradeStatusUrl) {
		this.upgradeStatusUrl = upgradeStatusUrl;
	}

	public void setTomcatPath(String tomcatPath) {
		this.tomcatPath = tomcatPath;
	}

	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setVip(boolean isVip) {
		this.isVip = isVip;
	}

	public void setRestartCommand(String restartCommand) {
		this.restartCommand = restartCommand;
	}

	@Override
	public String getFirstUseInfoUrl() {
		// TODO Auto-generated method stub
		return this.firstUseInfoUrl;
	}

	public void setFirstUseInfoUrl(String firstUseInfoUrl) {
		this.firstUseInfoUrl = firstUseInfoUrl;
	}
	
}
