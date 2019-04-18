package com.redmoon.oa.upgrade.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.upgrade.dao.UpgradeDao;
import com.redmoon.oa.upgrade.domain.StatusRequest;
import com.redmoon.oa.upgrade.domain.VersionRequest;
import com.redmoon.oa.upgrade.domain.VersionResponse;
import com.redmoon.oa.upgrade.http.HttpDownloadClient;
import com.redmoon.oa.upgrade.http.RestClient;
import com.redmoon.oa.upgrade.util.IBeanshellUtil;
import com.redmoon.oa.upgrade.util.IDatabaseUpgradeUtil;
import com.redmoon.oa.upgrade.util.IFilesUpgradeUtil;
import com.redmoon.oa.upgrade.util.IMiscUtil;
import com.redmoon.oa.upgrade.util.IUnzipUtil;
import com.redmoon.oa.upgrade.util.IValidationUtil;

public class UpgradeService implements IUpgradeService {
	private static final String UPGRADE_BEANSHELL_FILE = "upgrade.sh";

	private static final String UPGRADE_SQL_FILE = "upgrade.sql";

	private static final String UPGRADE_FILES_DIRECTORY = "files";

	private static final String UPDATE_CONTENT_NAME = "updateContent.txt";

	private Logger logger = LoggerFactory.getLogger(UpgradeService.class);

	private UpgradeDao upgradeDao;
	private RestClient restClient;
	private HttpDownloadClient httpDownloadClient;
	private IValidationUtil validationUtil;
	private IUnzipUtil unzipUtil;
	private IFilesUpgradeUtil filesUpgradeUtil;
	private IBeanshellUtil beanshellUtil;
	private IMiscUtil miscUtil;
	private IDatabaseUpgradeUtil databaseUpgradeUtil;

	private String lastUrl = null;
	private boolean isUpgradeOk;

	private String exceptionToString(Exception e) {
		PrintStream ps = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ps = new PrintStream(out);
			e.printStackTrace(ps);
			return out.toString();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	/**
	 * @param currentVersion
	 * @return is finished
	 */
	private boolean tryOneUrl(String currentVersion) {

		VersionRequest request = new VersionRequest();
		request.setCustomer(this.miscUtil.getCustomerId());
		if (lastUrl != null) {
			request.setUrl(lastUrl);
		}
		// 获取本机IP地址
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress().toString();// 获得本机IP
			request.setIp(ip);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			LogUtil.getLog(getClass()).error(StrUtil.trace(e2));
		}

		request.setVersion(currentVersion);
		request.setVip(this.miscUtil.isVip());
		request.setAllUsers(String.valueOf(getAllUsers()));
		request.setLoginTotals(String.valueOf(getLoginTotals()));
		request.setPhoneLoginTotals(String.valueOf(getPhoneloginTotals()));
		request.setPhoneUsers(String.valueOf(getPhoneUsers()));	
		request.setFlowNums(String.valueOf(getFlowNums()));
		request.setDocumentNums(String.valueOf(getDocumentNums()));
		request.setMessageNums(String.valueOf(getMessageNums()));
		request.setNoticeNums(String.valueOf(getNoticeNums()));
		request.setWorkNoteCount(String.valueOf(statWorkNoteCount()));
		if (License.getInstance().isBiz()){
			request.setIsBiz("1");
		}
		else
		{
			request.setIsBiz("0");
		}
		
		VersionResponse response = null;
		response = this.restClient.fetchNewVersion(request);
		if (response == null || !response.isResult()
				|| !StringUtils.hasLength(response.getUrl())
				|| !StringUtils.hasLength(response.getVersion())) {
			logger.info("No upgrade available.");
			return true;
		}
		Config cfg = new Config();
		boolean autoUpgrade = cfg.get("isAutoUpgrade").equals("true");
		if (autoUpgrade) {
			this.upgradeDao.saveBeginUpgrade(this.miscUtil.getCustomerId(),
					response.getVersion(), response.getUrl());
			String tempPath = this.miscUtil.getTempPath();
			isUpgradeOk = false;

			try {
				File target = new File(tempPath, "upgrade.zip");
				this.httpDownloadClient.download(response.getUrl(), target);
				this.validationUtil.validate(target, response);
				String upgradeFileRoot = tempPath + "/output";
				this.unzipUtil.unzip(target, upgradeFileRoot);

				this.beanshellUtil.execute(new File(upgradeFileRoot,
						UPGRADE_BEANSHELL_FILE));
				this.databaseUpgradeUtil.upgrade(new File(upgradeFileRoot,
						UPGRADE_SQL_FILE));
				this.filesUpgradeUtil.upgrade(new File(upgradeFileRoot,
						UPGRADE_FILES_DIRECTORY).getAbsolutePath(),
						Global.getAppPath());
				// 版本号写入到config_oa.xml中
				cfg.put("version", response.getVersion());
				// 更新结束记录日志
				this.upgradeDao.saveEndUpgrade(this.miscUtil.getCustomerId(),
						response.getVersion(), response.getUrl(), 1,
						"Upgrade OK.");

				StatusRequest statusRequest = new StatusRequest();
				statusRequest.setCustomer(this.miscUtil.getCustomerId());
				statusRequest.setStatus(1);
				statusRequest.setUrl(response.getUrl());
				statusRequest.setVersion(response.getVersion());
				statusRequest.setMessage("Upgrade OK.");
				try {
					this.restClient.reportStatus(statusRequest);
				} catch (Exception e) {
					logger.error("Report status failed", e);
				}
				isUpgradeOk = true;
				// 发送消息
				String title = null;
				String content = null;
				if (isUpgradeOk) {
					title = cfg.get("upgradeRemindMsg_success_title");
					content = cfg.get("upgradeRemindMsg_success_content");
					String updateContent = "";
					try {
						updateContent = FileUtils.readFileToString(new File(
								upgradeFileRoot, UPDATE_CONTENT_NAME), "utf-8");
						content = content.replaceFirst("\\$content",
								updateContent);
						sendMsg(title, content);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						logger.info("获取更新内容错误");
					}

				} else {
					title = cfg.get("upgradeRemindMsg_error_title");
					content = cfg.get("upgradeRemindMsg_error_content");
					sendMsg(title, content);
				}
				return true;
			} catch (Exception e) {
				logger.error("Upgrade failed", e);
				this.upgradeDao.saveEndUpgrade(this.miscUtil.getCustomerId(),
						response.getVersion(), response.getUrl(), 0, this
								.exceptionToString(e));
				StatusRequest statusRequest = new StatusRequest();
				statusRequest.setCustomer(this.miscUtil.getCustomerId());
				statusRequest.setStatus(0);
				statusRequest.setUrl(response.getUrl());
				statusRequest.setVersion(response.getVersion());
				statusRequest.setMessage(this.exceptionToString(e));
				try {
					this.restClient.reportStatus(statusRequest);
				} catch (Exception e1) {
					logger.error("Report status failed", e1);
				}
			} finally {
				this.filesUpgradeUtil.clearFolder(tempPath);

			}
		}
		else{
			String title = "版本升级通知";
			String content = "系统有新版本（" + response.getVersion() + ")发布,请至官网(http://www.yimihome.com/list_patch.jsp?dirCode=bdxz)下载更新包更新系统";
			try{
				sendMsg(title, content);
			}catch(Exception e)	{
				logger.error("发送消息失败：" + e.getMessage());
			}
				
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.redmoon.oa.upgrade.client.IUpgradeClient#execute()
	 */
	public synchronized void execute(boolean immediate) {
		Config cfg = new Config();
		// sleep
		if (!immediate) {
			try {
				// 四小时内随机发生
				double rand = Math.random();
				Thread.sleep((int) (rand * this.miscUtil.getSleepSecond()));
			} catch (InterruptedException e) {

			}
		}
		// is upgrading?
		if (upgradeDao.isUpgrading()) {
			logger.info("upgrading...");
			return;
		}

		lastUrl = null;
		isUpgradeOk = false;

		// 版本号从config_oa.xml中获取
		String currentVersion = cfg.get("version");
		tryToUpgrade(currentVersion);

		if (isUpgradeOk) {
			this.miscUtil.restartTomcat();
		}

	}

	private boolean tryToUpgrade(String currentVersion) {
		for (int i = 0; i < 3; i++) {
			if (this.tryOneUrl(currentVersion)) {
				logger.info("Upgrade OK.");
				return true;
			}
		}
		return false;
		// throw new UpgradeException("Failed to upgrade.");
	}
	
	public synchronized void sendFirstUseInfo(String isRegistered, String customerId, String version){
		// 获取本机IP地址
		InetAddress addr;
		VersionRequest request = new VersionRequest();
		String ip = "";
		try {
			addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress().toString();// 获得本机IP
			
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			LogUtil.getLog(getClass()).error(StrUtil.trace(e2));
		}
		
		
		request.setIp(ip);
		request.setVersion(version);
		request.setCustomer(customerId);
		request.setIsRegisted(isRegistered);
		this.restClient.firstUseInfo(request);
		
	}

	public UpgradeDao getUpgradeDao() {
		return upgradeDao;
	}

	public void setUpgradeDao(UpgradeDao upgradeDao) {
		this.upgradeDao = upgradeDao;
	}

	public RestClient getRestClient() {
		return restClient;
	}

	public void setRestClient(RestClient restClient) {
		this.restClient = restClient;
	}

	public HttpDownloadClient getHttpDownloadClient() {
		return httpDownloadClient;
	}

	public void setHttpDownloadClient(HttpDownloadClient httpDownloadClient) {
		this.httpDownloadClient = httpDownloadClient;
	}

	public IValidationUtil getValidationUtil() {
		return validationUtil;
	}

	public void setValidationUtil(IValidationUtil validationUtil) {
		this.validationUtil = validationUtil;
	}

	public IUnzipUtil getUnzipUtil() {
		return unzipUtil;
	}

	public void setUnzipUtil(IUnzipUtil unzipUtil) {
		this.unzipUtil = unzipUtil;
	}

	public IFilesUpgradeUtil getFilesUpgradeUtil() {
		return filesUpgradeUtil;
	}

	public void setFilesUpgradeUtil(IFilesUpgradeUtil filesUpgradeUtil) {
		this.filesUpgradeUtil = filesUpgradeUtil;
	}

	public IBeanshellUtil getBeanshellUtil() {
		return beanshellUtil;
	}

	public void setBeanshellUtil(IBeanshellUtil beanshellUtil) {
		this.beanshellUtil = beanshellUtil;
	}

	public IMiscUtil getMiscUtil() {
		return miscUtil;
	}

	public void setMiscUtil(IMiscUtil miscUtil) {
		this.miscUtil = miscUtil;
	}

	public IDatabaseUpgradeUtil getDatabaseUpgradeUtil() {
		return databaseUpgradeUtil;
	}

	public void setDatabaseUpgradeUtil(IDatabaseUpgradeUtil databaseUpgradeUtil) {
		this.databaseUpgradeUtil = databaseUpgradeUtil;
	}

	/**
	 * 发送消息
	 * 
	 * @param title
	 * @param content
	 */
	@SuppressWarnings("static-access")
	private void sendMsg(String title, String content) {
		IMessage imsg = null;

		// 获取admin权限用户，并遍历发送消息
		Privilege privilege = new Privilege();
		Vector adminVector = privilege.getUsersHavePriv("admin");
		Iterator it = adminVector.iterator();
		while (it.hasNext()) {
			UserDb ud = (UserDb) it.next();
			try {
				ProxyFactory proxyFactory = new ProxyFactory(
						"com.redmoon.oa.message.MessageDb");
				imsg = (IMessage) proxyFactory.getProxy();
				imsg.sendSysMsg(ud.getName(), title, content);
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	/**
	 * 获取当前系统用户数
	 * @return
	 */
	private int getAllUsers(){
		int num = 0;
		num = upgradeDao.getAllUsers();
		return num;
	}
	/**
	 * 获取登陆总数
	 * @return
	 */
	private int getLoginTotals(){
		int num = 0;
		num = upgradeDao.getLoginTotals();
		return num;
	}
	/**
	 * 获取手机登陆总数
	 * @return
	 */
	private int getPhoneloginTotals(){
		int num = 0;
		num = upgradeDao.getPhoneloginTotals();
		return num;
	}
	/**
	 * 获取手机用户数
	 * @return
	 */
	private int getPhoneUsers(){
		int num = 0;
		num = upgradeDao.getPhoneUsers();
		return num;
	}
	/**
	 * 获取流程总数
	 * @return
	 */
	private int getFlowNums(){
		int num = 0;
		num = upgradeDao.getFlowNums();
		return num;
	}
	/**
	 * 获取文件柜文件数
	 * @return
	 */
	private int getDocumentNums(){
		int num = 0;
		num = upgradeDao.getDocumentNums();
		return num;
	}
	/**
	 * 获取内部消息数
	 * @return
	 */
	private int getMessageNums(){
		int num = 0;
		num = upgradeDao.getMessageNums();
		return num;
	}
	/**
	 * 获取通知总数
	 * @return
	 */
	private int getNoticeNums(){
		int num = 0;
		num = upgradeDao.getNoticeNums();
		return num;
	}
	/**
	 * 统计工作记事数
	 *
	 * @return
	 * @throws SQLException
	 */
	private int statWorkNoteCount(){
		int num = 0;
		num = upgradeDao.getWorkNoteCount();
		return num;
	}
}
