package cn.js.fan.web;

import cn.js.fan.util.StrUtil;
import com.redmoon.kit.util.UploadReaper;
import org.apache.log4j.Logger;
import sun.security.action.GetPropertyAction;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.AccessController;
import java.util.*;

/**
 * 
 * <p>
 * Title: 用于初始化全局变量
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public final class Global {
	static Logger logger = Logger.getLogger(Global.class.getName());

	public static String author;
	public static String AppName;
	public static String server;
	public static String port;
	public static String virtualPath;
	public static String defaultDB; // 用于默认连接池

	public static String AppRealName;

	public static long MaxSize = 1024000; // webedit控件限定上传的最大值，单位：字节
	public static int FileSize = 1024000; // DocumentMgr
	// doUpload中限定单个文件的最大值，单位：K

	public static String INTERNET_FLAG_SECURE = "secure";
	public static boolean requestSupportCN = false;

	public static String internetFlag;

	static HashMap dbinfos = new HashMap();
	public static boolean localeSpecified = false; // 不能放在初始块static {new
	// Global();}之后，否则会使得static
	// {}初始化的值被重新赋值

	public static boolean isSubDomainSupported = false;

	public static boolean isTransactionSupported = true;

	public static boolean isGZIPEnabled = true;

	public static Global global = null;

	private static boolean debug = false;

	public static String db = "";

	public static String dbVersion = "";

	public static final String DB_MYSQL = "MySQL";
	public static final String DB_SQLSERVER = "SQLServer";
	public static final String DB_ORACLE = "Oracle";
    public static final String DB_POSTGRESQL = "PostGreSql";
    
    public static final String PLATFORM_RELATED_OA = "OA";
    
    public static String platformRelated = PLATFORM_RELATED_OA;

	/**
	 * 是否使用缓存
	 */
	public static boolean useCache = true;
	private static boolean cluster = false;

	static {
		global = new Global();
		global.init();
	}

	public Global() {
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void init() {
		System.out.println("Global start init.");

		Config config = new Config();

		if (debug)
			System.out.println("Global.java init " + config);

		author = config.getProperty("Application.author");

		AppRealName = config.getProperty("Application.name");

		AppName = AppRealName; // + " - Powered by CWBBS";

		server = config.getProperty("Application.server");
		port = config.getProperty("Application.port");
		virtualPath = StrUtil.getNullString(config
				.getProperty("Application.virtualPath"));
		String strMaxSize = StrUtil.getNullString(config
				.getProperty("Application.WebEdit.MaxSize")); // 以字节为单位
		if (StrUtil.isNumeric(strMaxSize))
			MaxSize = Long.parseLong(strMaxSize);
		// 最大同时上传的文件数
		String strmaxUploadingFileCount = StrUtil.getNullString(config
				.getProperty("Application.WebEdit.maxUploadingFileCount"));
		if (StrUtil.isNumeric(strmaxUploadingFileCount))
			maxUploadingFileCount = Integer.parseInt(strmaxUploadingFileCount);
		String strFileSize = StrUtil.getNullString(config
				.getProperty("Application.FileSize")); // 以K为单位
		if (StrUtil.isNumeric(strFileSize))
			FileSize = Integer.parseInt(strFileSize);

		smtpServer = StrUtil.getNullString(config
				.getProperty("Application.smtpServer"));
		String sPort = config.getProperty("Application.smtpPort");
		if (StrUtil.isNumeric(sPort))
			smtpPort = Integer.parseInt(sPort);
		smtpUser = StrUtil.getNullString(config
				.getProperty("Application.smtpUser"));
		smtpPwd = StrUtil.getNullString(config
				.getProperty("Application.smtpPwd"));

		smtpSSL = StrUtil.getNullString(
				config.getProperty("Application.smtpSSL")).equals("true");
		
		smtpCharset = StrUtil.getNullString(
				config.getProperty("Application.smtpCharset"));
		// System.out.println("smtpSSL=" + smtpSSL);

		realPath = config.getProperty("Application.realPath");
		if (realPath != null){
			realPath = StrUtil.replace(realPath, "\\", "/");
		}
		if (realPath.lastIndexOf("/") != realPath.length() - 1
				&& realPath.lastIndexOf("\\") != realPath.length() - 1)
			realPath += "/";

		email = StrUtil.getNullString(config.getProperty("Application.email"));

		internetFlag = StrUtil.getNullString(config
				.getProperty("Application.internetFlag"));

		desc = StrUtil.getNullStr(config.getProperty("Application.desc"));
		copyright = StrUtil.getNullStr(config
				.getProperty("Application.copyright"));

		icp = StrUtil.getNullStr(config.getProperty("Application.icp"));

		contact = StrUtil.getNullStr(config.getProperty("Application.contact"));

		version = StrUtil.getNullStr(config.getProperty("Application.version"));

		title = StrUtil.getNullStr(config.getProperty("Application.title"));

		String strIsRequestSupportCN = StrUtil.getNullStr(config
				.getProperty("Application.isRequestSupportCN"));
		if (strIsRequestSupportCN == null)
			System.out
					.println("Error:Global Application.isRequestSupportCN is not found.");

		requestSupportCN = strIsRequestSupportCN.equals("true");

		String lang = StrUtil.getNullStr(config.getProperty("i18n.lang"));
		String country = StrUtil.getNullStr(config.getProperty("i18n.country"));
		String strTimeZone = StrUtil.getNullStr(config
				.getProperty("i18n.timeZone"));

		String strIsSpecified = StrUtil.getNullStr(config
				.getProperty("i18n.isSpecified"));
		if (strIsSpecified == null)
			System.out.println("Error:Global i18n.isSpecified is not found.");
		localeSpecified = strIsSpecified.equals("true");
		locale = new Locale(lang, country);
		timeZone = TimeZone.getTimeZone(strTimeZone);

		if (debug) {
			System.out.println("Global.java: timeZone=" + strTimeZone
					+ " zoneID=" + timeZone.getID() + " lang=" + lang
					+ " country=" + country);
			System.out.println("Global.java init AppName=" + AppName);
		}
		String strUseCache = config.getProperty("Application.useCache");
		if (strUseCache == null)
			System.out
					.println("Error:Global Application.useCache is not found.");

		useCache = strUseCache.equals("true");

		String strIsSubDomainSupported = config
				.getProperty("Application.isSubDomainSupported");
		if (strIsSubDomainSupported == null)
			System.out
					.println(Global.class
							+ " Warn:Global Application.isSubDomainSupported is not found.");

		isSubDomainSupported = strIsSubDomainSupported.equals("true");

		String strIsTransactionSupported = config
				.getProperty("Application.isTransactionSupported");
		if (strIsTransactionSupported == null)
			System.out
					.println(Global.class
							+ " Warn:Global Application.isTransactionSupported is not found.");

		isTransactionSupported = strIsTransactionSupported.equals("true");

		String strIsGZIPEnabled = config
				.getProperty("Application.isGZIPEnabled");
		if (strIsGZIPEnabled == null)
			System.out.println(Global.class
					+ " Warn:Global Application.isGZIPEnabled is not found.");

		isGZIPEnabled = strIsGZIPEnabled.equals("true");
		db = config.getProperty("Application.db");

		dbVersion = config.getProperty("Application.dbVersion");
		if (dbVersion == null) {
			System.out.println(Global.class
					+ " Warn:Global Application.dbVersion is not found.");
		}

		dbinfos.clear();
		Vector v = config.getDBInfos();
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			DBInfo di = (DBInfo) ir.next();
			if (di.isDefault) {
				defaultDB = di.name;
				// logger.info("defaultDb=" + defaultDB);
			}
			dbinfos.put(di.name, di);
		}

		String strIsCluster = config.getProperty("Application.isCluster");
		if (strIsCluster == null)
			System.out.println(Global.class
					+ " Error:Global Application.isCluster is not found.");
		else
			cluster = strIsCluster.equals("true");
		
		platformRelated = config.getProperty("Application.platformRelated");
		if (platformRelated == null){
			platformRelated = PLATFORM_RELATED_OA;
		}
			

		/*
		 * 以下代码迁移至AppInit中，因为当Tomcat reload时，需在servlet的init方法中重新启动调度
		 * 调度放在Global中是不妥当的，如果在系统启动过程中写代码的时候不小心在Listener中调用了Global，就会导致调度开始，而此时
		 * proxool未初始化，调用连接就会失败，导致Tomcat启动不了，详见AppServletContextListener中的说明 //
		 * 调度初始化 Scheduler.initInstance(1000); // 单态模式 if (debug)
		 * System.out.println("Global.java: Scheduler initInstance end");
		 * 
		 * // 加载调度项 config.initScheduler(); if (debug)
		 * System.out.println("Global.java: initScheduler end");
		 */

		// 初始化缓存定时器
		// CacheTimer.initInstance();

		// 初始化临时文件删除器
		UploadReaper.initInstance(UploadReaper.getReapInterval());

		if (debug)
			System.out.println("Global.java: init end");
	}

	public static DBInfo getDBInfo(String key) {
		return (DBInfo) dbinfos.get(key);
	}

	public static void setDefaultDB(String db) {
		defaultDB = db;
	}

	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}

	public void setSmtpPwd(String smtpPwd) {
		this.smtpPwd = smtpPwd;
	}

	public void setRealPath(String realPath) {
		this.realPath = realPath;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setMaxUploadingFileCount(int maxUploadingFileCount) {
		this.maxUploadingFileCount = maxUploadingFileCount;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setRequestSupportCN(boolean requestSupportCN) {
		this.requestSupportCN = requestSupportCN;
	}

	public void setLocaleSpecified(boolean localeSpecified) {
		this.localeSpecified = localeSpecified;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}

	public void setCluster(boolean cluster) {
		this.cluster = cluster;
	}

	/**
	 * 获取相对的contextpath
	 * 
	 * @return String
	 */
	public static String getRootPath() {
		if (virtualPath.equals(""))
			return "";
		else
			return "/" + virtualPath;
		/*
		 * if (!virtualPath.equals("")) { if (Global.port.equals("80")) return
		 * "http://" + Global.server + "/" + Global.virtualPath; //
		 * "http://www.zjrj.cn"; else return "http://" + Global.server + ":" +
		 * Global.port + "/" + Global.virtualPath; // "http://www.zjrj.cn"; }
		 * else { if (Global.port.equals("80")) return "http://" +
		 * Global.server; else return "http://" + Global.server + ":" +
		 * Global.port; // "http://www.zjrj.cn"; }
		 */
	}

	/**
	 * 获取相对的contextpath
	 * 
	 * @return String
	 */
	public static String getRootPath(HttpServletRequest request) {
		if (request == null)
			return getRootPath();

		return request.getContextPath();
	}

	public static String getFullRootPath(HttpServletRequest request) {
		// 在流程的job中用到时，需传入request为null的参数
		if (request == null)
			return getFullRootPath();

		String scheme = request.getScheme().toLowerCase();
		if ("https".equalsIgnoreCase(scheme)) {
			scheme = "https";
		}

		String server = request.getServerName();
		int port = request.getServerPort();
		// LogUtil.getLog(Global.class).info("request.getContextPath()=" +
		// request.getContextPath());
		if (!request.getContextPath().equals("")) {
			if (port == 80)
				return scheme + "://" + server + request.getContextPath(); // "http://www.zjrj.cn";
			else
				return scheme + "://" + server + ":" + port
						+ request.getContextPath(); // "http://www.zjrj.cn";
		} else {
			if (port == 80)
				return scheme + "://" + server;
			else
				return scheme + "://" + server + ":" + port; // "http://www.zjrj.cn";
		}

		/*
		 * if (!virtualPath.equals("")) { if (port==80) return scheme + "://" +
		 * server + "/" + Global.virtualPath; // "http://www.zjrj.cn"; else
		 * return scheme + "://" + server + ":" + port + "/" +
		 * Global.virtualPath; // "http://www.zjrj.cn"; } else { if (port==80)
		 * return scheme + "://" + server; else return scheme + "://" + server +
		 * ":" + port; // "http://www.zjrj.cn"; }
		 */
	}

	/**
	 * 获取绝对的contextPath，注意当使用SSL时，端口号不正确
	 * 
	 * @return String
	 * @Deprecated
	 */
	public static String getFullRootPath() {
		if (!virtualPath.equals("")) {
			if (getInternetFlag().equals("secure")) {
				return "https://" + Global.server + "/" + Global.virtualPath; // "http://www.zjrj.cn";
			} else {
				if (Global.port.equals("80"))
					return "http://" + Global.server + "/" + Global.virtualPath; // "http://www.zjrj.cn";
				else {
					return "http://" + Global.server + ":" + Global.port + "/"
							+ Global.virtualPath; // "http://www.zjrj.cn";
				}
			}
		} else {
			if (getInternetFlag().equals("secure")) {
				return "https://" + Global.server;
			} else {
				if (Global.port.equals("80"))
					return "http://" + Global.server;
				else
					return "http://" + Global.server + ":" + Global.port; // "http://www.zjrj.cn";
			}
		}
	}

	/**
	 * 获取工程对应目录绝对路径
	 * 
	 * @param request
	 * @return
	 */
	public static String getAppPath(HttpServletRequest request) {
		String path = "";
		if (request != null) {
			path = request.getSession().getServletContext().getRealPath("");
			if (path.lastIndexOf("/") != path.length() - 1
					&& path.lastIndexOf("\\") != path.length() - 1) {
				path += "/";
			}
		} else {
			String temp = Global.class.getResource("/").getPath();   //Thread.currentThread().getContextClassLoader().getResource("").getPath();
			try {
				path = URLDecoder.decode(temp, (String) AccessController	.doPrivileged(new GetPropertyAction("file.encoding")));
			} catch (UnsupportedEncodingException e) {
				logger.error("getAppPath: " + e.getMessage());
				e.printStackTrace();
				path = temp;
			}
			if (path.startsWith("/")&& !File.separator.equals("/")) {
				path = path.substring(1, path.indexOf("WEB-INF"));
			} else {
				path = path.substring(0, path.indexOf("WEB-INF"));
			}
		}

		return path;
	}

	/**
	 * 获取工程对应目录绝对路径
	 * 
	 * @return
	 */
	public static String getAppPath() {
		return getAppPath(null);
	}

	public static String getSmtpServer() {
		return smtpServer;
	}

	public static int getSmtpPort() {
		return smtpPort;
	}

	public static String getSmtpUser() {
		return smtpUser;
	}

	public static String getSmtpPwd() {
		return smtpPwd;
	}

	public static boolean isSmtpSSL() {
		return smtpSSL;
	}

	public static String getSmtpCharset() {
		return smtpCharset;
	}

	public static void setSmtpCharset(String smtpCharset) {
		Global.smtpCharset = smtpCharset;
	}

	public static String getRealPath() {
		return realPath;
	}

	public static String getEmail() {
		return email;
	}

	public static int getMaxUploadingFileCount() {
		return maxUploadingFileCount;
	}

	public static String getInternetFlag() {
		return internetFlag;
	}

	public static String getDesc() {
		return desc;
	}

	public static String getCopyright() {
		return copyright;
	}

	public static String getIcp() {
		return icp;
	}

	public static String getContact() {
		return contact;
	}

	public static String getVersion() {
		return version;
	}

	public static String getTitle() {
		return title;
	}

	public static Locale getLocale() {
		return locale;
	}

	public static TimeZone getTimeZone() {
		return timeZone;
	}

	public boolean isRequestSupportCN() {
		return requestSupportCN;
	}

	public boolean isLocaleSpecified() {
		return localeSpecified;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public static boolean isCluster() {
		return cluster;
	}

	public static synchronized Global getInstance() {
		return global;
	}

	public static String getDefaultDB() {
		return defaultDB;
	}

	public static String smtpServer;
	public static int smtpPort;
	public static String smtpUser;
	public static String smtpPwd;
	/**
	 * 20140907 fgf SMTP是否采用SSL方式
	 */
	public static boolean smtpSSL;
	/**
	 * @Description: hw smtp编码
	 */
	public static String smtpCharset;
	public static String realPath;
	public static String email;
	public static int maxUploadingFileCount;
	public static String desc;
	public static String copyright;
	public static String icp;
	public static String contact;
	public static String version;
	public static String title;
	public static Locale locale;
	public static TimeZone timeZone;

}
