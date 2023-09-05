package cn.js.fan.web;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.util.IpUtil;
import cn.js.fan.util.PropertiesUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.util.XMLConfig;
import com.cloudweb.oa.base.IConfigUtil;
import com.cloudweb.oa.entity.SysConfig;
import com.cloudweb.oa.service.ISysConfigService;
import com.cloudweb.oa.utils.CommonConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysProperties;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.UploadReaper;
import org.apache.xmlbeans.SystemProperties;
import org.jdom.Element;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import sun.security.action.GetPropertyAction;

import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
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

	/**
	 * 是否使用缓存
	 */
	public static boolean useCache = true;

	/**
	 * 是否集群
	 */
	private static boolean cluster = false;

	/**
	 * 是否启用调度
	 */
	private boolean schedule = true;

	private String clusterNo = "";

	private boolean clusterNoDisplay = true;

	public boolean isFormalOpen() {
		return formalOpen;
	}

	public void setFormalOpen(boolean formalOpen) {
		this.formalOpen = formalOpen;
	}

	private boolean formalOpen = false;

	static {
		global = new Global();
		global.init();
	}

	public Global() {
	}

	public void init() {
		LogUtil.getLog(getClass()).info("Global start init.");

		// config中不能使用默认构造函数，因为其中的configUtil.getXml("config_sys")用到了cache，因为在本方法的后面位置需要对jcs cache进行初始化，应在初始化后才能使用jcs cache
		boolean isNotUseCache = true;
		Config config = new Config(isNotUseCache);

		debug = "true".equals(config.getProperty("Application.isDebug"));

		/*if (debug) {
			LogUtil.getLog(getClass()).info("Global.java init " + config);
		}*/

		SysProperties systemProperties = SpringUtil.getBean(SysProperties.class);
		clusterNo = systemProperties.getId();
		// clusterNo = config.getProperty("Application.clusterNo");
		author = config.getProperty("Application.author");
		AppRealName = config.getProperty("Application.name");
		AppName = AppRealName;

		server = config.getProperty("Application.server");
		port = config.getProperty("Application.port");
		virtualPath = StrUtil.getNullString(config.getProperty("Application.virtualPath"));
		String strMaxSize = StrUtil.getNullString(config.getProperty("Application.WebEdit.MaxSize")); // 以字节为单位
		if (StrUtil.isNumeric(strMaxSize)) {
			MaxSize = Long.parseLong(strMaxSize);
		}
		// 最大同时上传的文件数
		String strmaxUploadingFileCount = StrUtil.getNullString(config.getProperty("Application.WebEdit.maxUploadingFileCount"));
		if (StrUtil.isNumeric(strmaxUploadingFileCount)) {
			maxUploadingFileCount = Integer.parseInt(strmaxUploadingFileCount);
		}
		String strFileSize = StrUtil.getNullString(config.getProperty("Application.FileSize")); // 以K为单位
		if (StrUtil.isNumeric(strFileSize)) {
			FileSize = Integer.parseInt(strFileSize);
		}

		smtpServer = StrUtil.getNullString(config.getProperty("Application.smtpServer"));
		String sPort = config.getProperty("Application.smtpPort");
		if (StrUtil.isNumeric(sPort)) {
			smtpPort = Integer.parseInt(sPort);
		}
		smtpUser = StrUtil.getNullString(config.getProperty("Application.smtpUser"));
		smtpPwd = StrUtil.getNullString(config.getProperty("Application.smtpPwd"));
		smtpSSL = "true".equals(StrUtil.getNullString(config.getProperty("Application.smtpSSL")));
		smtpCharset = StrUtil.getNullString(config.getProperty("Application.smtpCharset"));
		SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
		realPath = sysUtil.getUploadPath();

		/*realPath = config.getProperty("Application.realPath");
		if (realPath != null){
			realPath = StrUtil.replace(realPath, "\\", "/");
		}
		if (realPath.lastIndexOf("/") != realPath.length() - 1
				&& realPath.lastIndexOf("\\") != realPath.length() - 1) {
			realPath += "/";
		}*/

		email = StrUtil.getNullString(config.getProperty("Application.email"));
		internetFlag = StrUtil.getNullString(config.getProperty("Application.internetFlag"));
		desc = StrUtil.getNullStr(config.getProperty("Application.desc"));
		copyright = StrUtil.getNullStr(config.getProperty("Application.copyright"));
		icp = StrUtil.getNullStr(config.getProperty("Application.icp"));
		contact = StrUtil.getNullStr(config.getProperty("Application.contact"));
		version = StrUtil.getNullStr(config.getProperty("Application.version"));
		title = StrUtil.getNullStr(config.getProperty("Application.title"));
		String strIsRequestSupportCN = StrUtil.getNullStr(config.getProperty("Application.isRequestSupportCN"));
		requestSupportCN = "true".equals(strIsRequestSupportCN);
		String lang = StrUtil.getNullStr(config.getProperty("i18n.lang"));
		String country = StrUtil.getNullStr(config.getProperty("i18n.country"));
		String strTimeZone = StrUtil.getNullStr(config.getProperty("i18n.timeZone"));
		String strIsSpecified = StrUtil.getNullStr(config.getProperty("i18n.isSpecified"));
		localeSpecified = "true".equals(strIsSpecified);
		locale = new Locale(lang, country);
		timeZone = TimeZone.getTimeZone(strTimeZone);

		if (debug) {
			LogUtil.getLog(getClass()).info("Global.java: timeZone=" + strTimeZone
					+ " zoneID=" + timeZone.getID() + " lang=" + lang
					+ " country=" + country);
		}
		LogUtil.getLog(getClass()).info("Global.java init AppName=" + AppName);

		String strIsSubDomainSupported = config
				.getProperty("Application.isSubDomainSupported");
		if (strIsSubDomainSupported == null) {
			LogUtil.getLog(getClass()).warn("Global Application.isSubDomainSupported is not found.");
		}

		isSubDomainSupported = "true".equals(strIsSubDomainSupported);

		String strIsTransactionSupported = config.getProperty("Application.isTransactionSupported");
		if (strIsTransactionSupported == null) {
			LogUtil.getLog(getClass()).warn("Global Application.isTransactionSupported is not found.");
		}

		isTransactionSupported = "true".equals(strIsTransactionSupported);

		String strIsGZIPEnabled = config
				.getProperty("Application.isGZIPEnabled");
		if (strIsGZIPEnabled == null) {
			LogUtil.getLog(getClass()).warn("Global Application.isGZIPEnabled is not found.");
		}

		isGZIPEnabled = "true".equals(strIsGZIPEnabled);
		db = config.getProperty("Application.db");

		dbVersion = config.getProperty("Application.dbVersion");
		if (dbVersion == null) {
			LogUtil.getLog(getClass()).warn("Global Application.dbVersion is not found.");
		}

		dbinfos.clear();
		Vector v = config.getDBInfos();
		Iterator ir = v.iterator();
		while (ir.hasNext()) {
			DBInfo di = (DBInfo) ir.next();
			if (di.isDefault) {
				defaultDB = di.name;
			}
			dbinfos.put(di.name, di);
		}

		String strIsCluster = config.getProperty("Application.isCluster");
		if (strIsCluster == null) {
			LogUtil.getLog(getClass()).warn("Global Application.isCluster is not found.");
		} else {
			cluster = "true".equals(strIsCluster);
		}

		String strIsSchedule = config.getProperty("Application.isSchedule");
		if (strIsSchedule==null) {
			LogUtil.getLog(getClass()).warn("Global Application.isSchedule is not found.");
		}
		else {
			schedule = "true".equals(strIsSchedule);
		}

		/*String strIsClusterNoDisplay = config.getProperty("Application.isClusterNoDisplay");
		if (strIsClusterNoDisplay!=null) {
			clusterNoDisplay = "true".equals(strIsClusterNoDisplay);
		}*/
		SysProperties sysProperties = SpringUtil.getBean(SysProperties.class);
		clusterNoDisplay = sysProperties.isShowId();

		// 系统是否正式启用
		formalOpen = "true".equals(config.getProperty("Application.isFormalOpen"));
		/*
		 * 以下代码迁移至AppInit中，因为当Tomcat reload时，需在servlet的init方法中重新启动调度
		 * 调度放在Global中是不妥当的，如果在系统启动过程中写代码的时候不小心在Listener中调用了Global，就会导致调度开始，而此时
		 * proxool未初始化，调用连接就会失败，导致Tomcat启动不了，详见AppServletContextListener中的说明 //
		 * 调度初始化 Scheduler.initInstance(1000); // 单态模式 if (debug)
		 * LogUtil.getLog(getClass()).info("Global.java: Scheduler initInstance end");
		 *
		 * // 加载调度项 config.initScheduler(); if (debug)
		 * LogUtil.getLog(getClass()).info("Global.java: initScheduler end");
		 */

		// 初始化缓存定时器
		// CacheTimer.initInstance();

		// 初始化临时文件删除器
		UploadReaper.initInstance(UploadReaper.getReapInterval());

		/*String strIsUseRedis = config.getProperty("Application.isUseRedis");
		if (strIsUseRedis!=null) {
			useRedis = "true".equals(strIsUseRedis);
		}
		redisHost = config.getProperty("Application.redisHost");
		redisPort = StrUtil.toInt(config.getProperty("Application.redisPort"), -1);
		redisPassword = config.getProperty("Application.redisPassword");

		redisMaxTotal = StrUtil.toInt(config.getProperty("Application.redisMaxTotal"), 8);
		redisMaxIdle = StrUtil.toInt(config.getProperty("Application.redisMaxIdle"), 8);
		redisMinIdle = StrUtil.toInt(config.getProperty("Application.redisMinIdle"), 0);
		redisMaxWaitMillis = StrUtil.toInt(config.getProperty("Application.redisMaxWaitMillis"), 10000);
		redisDb = StrUtil.toInt(config.getProperty("Application.redisDb"), 0);

		// 只能放在方法的末尾处，否则在RMCache.getInstance().getCanCache()中会调用Global.getInstance().isUseRedis()，而如果此段代码放在前面，那么调用时isUseRedis尚未初始化
		String strUseCache = config.getProperty("Application.useCache");
		if (strUseCache == null) {
			LogUtil.getLog(getClass()).info("Error:Global Application.useCache is not found.");
		}
		useCache = "true".equals(strUseCache);
		*/

		useRedis = CommonConstUtil.CACHE_TYPE_REDIS.equals(sysProperties.getCacheType());
		redisHost = sysProperties.getRedisHost();
		redisPort = sysProperties.getRedisPort();
		redisPassword = sysProperties.getRedisPassword();
		redisMaxTotal = sysProperties.getRedisMaxTotal();
		redisMaxIdle = sysProperties.getRedisMaxIdle();
		redisMinIdle = sysProperties.getRedisMinIdle();
		redisMaxWaitMillis = sysProperties.getRedisMaxWaitMillis();
		redisDb = sysProperties.getRedisDb();

		useCache = sysProperties.isCache();

		if (!cluster) {
			if (useCache && !useRedis) {
				IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
				String cfgPath = configUtil.getFilePath();
				PropertiesUtil propertiesUtil = new PropertiesUtil(cfgPath + "/cache.ccf");
				if (propertiesUtil.getSafeProperties() != null) {
					LogUtil.getLog(getClass()).info("Init cache.ccf.");
					String cloudHome = Global.getAppPath();
					propertiesUtil.setValue("jcs.auxiliary.DC.attributes.DiskPath", cloudHome + "CacheTemp");
					try {
						propertiesUtil.saveFile(cfgPath + "/cache.ccf");
					} catch (Exception e) {
						LogUtil.getLog(getClass()).error(e);
					}
				}
			}
		}

		// 如果前台配置后与原来的不一样，则刷新RMCache
		boolean isEqual = (useCache == RMCache.getInstance().getCanCache());
		if (!isEqual) {
			RMCache.refresh();
		}

		if (debug) {
			LogUtil.getLog(getClass()).info("Global.java: init end");
		}
	}

	public static DBInfo getDBInfo(String key) {
		return (DBInfo) dbinfos.get(key);
	}

	public static void setDefaultDB(String db) {
		defaultDB = db;
	}

	public void setSmtpServer(String server) {
		smtpServer = server;
	}

	public void setSmtpPort(int port) {
		smtpPort = port;
	}

	public void setSmtpUser(String user) {
		smtpUser = user;
	}

	public void setSmtpPwd(String pwd) {
		smtpPwd = pwd;
	}

	public void setRealPath(String path) {
		realPath = path;
	}

	public void setEmail(String mail) {
		email = mail;
	}

	public void setMaxUploadingFileCount(int maxCount) {
		maxUploadingFileCount = maxCount;
	}

	public void setTitle(String t) {
		title = t;
	}

	public void setRequestSupportCN(boolean supportCN) {
		requestSupportCN = supportCN;
	}

	public void setLocaleSpecified(boolean specified) {
		localeSpecified = specified;
	}

	public void setUseCache(boolean use) {
		useCache = use;
	}

	public void setCluster(boolean clust) {
		cluster = clust;
	}

	public void setSchedule(boolean schedule) {
		this.schedule = schedule;
	}

	/**
	 * 获取相对的contextpath
	 * 
	 * @return String
	 */
	public static String getRootPath() {
		if (virtualPath.equals("")) {
			return "";
		} else {
			return "/" + virtualPath;
		}
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
		if (request == null) {
			return getRootPath();
		}

		return request.getContextPath();
	}

	public static String getFullRootPath(HttpServletRequest request) {
		// 在流程的job中用到时，需传入request为null的参数
		if (request == null) {
			return getFullRootPath();
		}

		String scheme = request.getScheme().toLowerCase();
		if ("https".equalsIgnoreCase(scheme)) {
			scheme = "https";
		}

		String server = request.getServerName();
		int port = request.getServerPort();
		if (!request.getContextPath().equals("")) {
			if (port == 80) {
				return scheme + "://" + server + request.getContextPath(); // "http://www.zjrj.cn";
			} else {
				return scheme + "://" + server + ":" + port
						+ request.getContextPath(); // "http://www.zjrj.cn";
			}
		} else {
			if (port == 80) {
				return scheme + "://" + server;
			} else {
				return scheme + "://" + server + ":" + port; // "http://www.zjrj.cn";
			}
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
				if (Global.port.equals("80")) {
					return "http://" + Global.server + "/" + Global.virtualPath; // "http://www.zjrj.cn";
				} else {
					return "http://" + Global.server + ":" + Global.port + "/"
							+ Global.virtualPath; // "http://www.zjrj.cn";
				}
			}
		} else {
			if (getInternetFlag().equals("secure")) {
				return "https://" + Global.server;
			} else {
				if (Global.port.equals("80")) {
					return "http://" + Global.server;
				} else {
					return "http://" + Global.server + ":" + Global.port; // "http://www.zjrj.cn";
				}
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
		// 判断是否在jar文件中运行
		URL url = Global.class.getResource("");
		String protocol = url.getProtocol();
		if (CommonConstUtil.RUN_MODE_JAR.equals(protocol)) {
			// jar包中不支持读取路径
			return getRealPath();
		}

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
				path = URLDecoder.decode(temp, AccessController.doPrivileged(new GetPropertyAction("file.encoding")));
			} catch (UnsupportedEncodingException e) {
				LogUtil.getLog(Global.class).error("getAppPath: " + e.getMessage());
				LogUtil.getLog(Global.class).error(e);
				path = temp;
			}
			if (path.startsWith("/") && !"/".equals(File.separator)) {
				path = path.substring(1, path.indexOf("WEB-INF"));
			} else {
				path = path.substring(0, path.indexOf("WEB-INF"));
			}
		}

		return path;
	}

	public boolean isDebug() {
		return debug;
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

	public boolean isSchedule() {
		return schedule;
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

	public String getClusterNo() {
		return clusterNo;
	}

	public void setClusterNo(String clusterNo) {
		this.clusterNo = clusterNo;
	}

	public void setServer(String serv) {
		server = serv;
	}

	/**
	 * 在AppInit中调用，对程序的路径初始化，以避免需要setup带来的麻烦
	 * @return
	 */
	public void initOnStart() {
		try{
			SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
			String cloudHome = Global.getAppPath();
			String uploadPath = sysUtil.getUploadPath();
			LogUtil.getLog(getClass()).info("cloudHome=" + cloudHome + " uploadPath=" + uploadPath);

			IConfigUtil configUtil = SpringUtil.getBean(IConfigUtil.class);
			if (!configUtil.isRunJar()) {
				// 如果没有集群，则修改 realPath
				if (!Global.isCluster()) {
					Config cwsCfg = new Config();
					// cwsCfg.setProperty("Application.realPath", cloudHome);
					Global.getInstance().setRealPath(uploadPath);

					try {
						InetAddress addr = InetAddress.getLocalHost();
						String ip = addr.getHostAddress();
						// 如果是公网地址，则置Application.server
						if (!IpUtil.isInnerIP(ip)) {
							cwsCfg.setProperty("Application.server", ip);
							Global.getInstance().setServer(ip);
						}
					} catch (UnknownHostException e2) {
						LogUtil.getLog(getClass()).error(StrUtil.trace(e2));
					}

					XMLConfig reportCfg = new XMLConfig("../reportConfig.xml",true,"utf-8"); // ../reportConfig.xml路径也有效
					try {
						Element root = reportCfg.getRootElement();
						Iterator ir = root.getChildren().iterator();
						while (ir.hasNext()) {
							Element e = (Element) ir.next();

							if ("cachedReportDir".equals(e.getChild("name").getValue())) {
								e.getChild("value").setText(cloudHome + "report/cached");
								File rptCacheDir = new File(cloudHome + "report/cached");
								if (!rptCacheDir.exists()) {
									rptCacheDir.mkdir();
								}
								break;
							}
						}
						reportCfg.writemodify();
					} catch (Exception e) {
						LogUtil.getLog(getClass()).error(e);
					}
				}
			}
		}
		catch(Exception ex){
			LogUtil.getLog(getClass()).error("modify file path error :" + ex.getMessage());
		}
	}

	private boolean useRedis = false;
	private String redisHost;
    private String redisPort;
    private String redisPassword;
    private int redisMaxTotal = 8;
    private int redisMaxIdle = 8;
    private int redisMinIdle = 0;
    private int redisMaxWaitMillis = 10000;
    private int redisDb = 0;

	public boolean isClusterNoDisplay() {
		return clusterNoDisplay;
	}

	public void setClusterNoDisplay(boolean clusterNoDisplay) {
		this.clusterNoDisplay = clusterNoDisplay;
	}

	public long getMaxSize() {
		return MaxSize;
	}

	public int getFileSize() {
		return FileSize;
	}

	public String getServer() {
		return server;
	}

	public boolean isUseRedis() {
		return useRedis;
	}

	public void setUseRedis(boolean useRedis) {
		this.useRedis = useRedis;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public String getRedisPort() {
		return redisPort;
	}

	public void setRedisPort(String redisPort) {
		this.redisPort = redisPort;
	}

	public String getRedisPassword() {
		return redisPassword;
	}

	public void setRedisPassword(String redisPassword) {
		this.redisPassword = redisPassword;
	}

	public int getRedisMaxTotal() {
		return redisMaxTotal;
	}

	public void setRedisMaxTotal(int redisMaxTotal) {
		this.redisMaxTotal = redisMaxTotal;
	}

	public int getRedisMaxIdle() {
		return redisMaxIdle;
	}

	public void setRedisMaxIdle(int redisMaxIdle) {
		this.redisMaxIdle = redisMaxIdle;
	}

	public int getRedisMinIdle() {
		return redisMinIdle;
	}

	public void setRedisMinIdle(int redisMinIdle) {
		this.redisMinIdle = redisMinIdle;
	}

	public int getRedisMaxWaitMillis() {
		return redisMaxWaitMillis;
	}

	public void setRedisMaxWaitMillis(int redisMaxWaitMillis) {
		this.redisMaxWaitMillis = redisMaxWaitMillis;
	}

	public int getRedisDb() {
		return redisDb;
	}

	public void setRedisDb(int redisDb) {
		this.redisDb = redisDb;
	}
}
