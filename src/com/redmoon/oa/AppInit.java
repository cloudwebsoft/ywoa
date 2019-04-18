package com.redmoon.oa;

import SuperDog.DogStatus;
import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.kernel.Scheduler;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Config;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.clouddisk.socketServer.CloudDiskThread;
import com.redmoon.dingding.service.eventchange.EventChangeService;
import com.redmoon.oa.job.SetMenuDutyMobileAppJob;
import com.redmoon.oa.job.SetUserDutyJob;
import com.redmoon.oa.kernel.InitializeThread;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.kernel.SchedulerManager;
import com.redmoon.oa.platform.PlatFormRelatedFactory;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.superCheck.CheckSuperKey;
import com.redmoon.oa.upgrade.service.IUpgradeService;
import com.redmoon.oa.upgrade.service.SpringHelper;
import com.redmoon.oa.util.ModifyFilePath;
import com.redmoon.oa.util.TwoDimensionCode;
import org.apache.jcs.access.exception.CacheException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.servlet.*;
import java.io.*;
import java.sql.SQLException;
import java.util.Date;

/**
 * <p>
 * Title: 系统初始化
 * </p>
 * 
 * <p>
 * Description: 目前仅作了调度的初始化
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class AppInit implements Servlet {
	public AppInit() {
	}

	/**
	 * destroy
	 * 
	 * @todo Implement this javax.servlet.Servlet method
	 */
	public void destroy() {
		System.out.println(Global.AppName + " has been stopped.");
		SchedulerManager sm = SchedulerManager.getInstance();
		sm.shutdown(); // 结束调度

		// 当realod时停止该线程，以免tomcat因该线程存在，不能完全clear，致出现log4j错误
		Scheduler.getInstance().doExit();
		CloudDiskThread.stopInstance();
	}

	/**
	 * getServletConfig
	 * 
	 * @return ServletConfig
	 * @todo Implement this javax.servlet.Servlet method
	 */
	public ServletConfig getServletConfig() {
		return null;
	}

	/**
	 * getServletInfo
	 * 
	 * @return String
	 * @todo Implement this javax.servlet.Servlet method
	 */
	public String getServletInfo() {
		return "";
	}

	/**
	 * init
	 * 
	 * @param servletConfig
	 *            ServletConfig
	 * @throws ServletException
	 * @todo Implement this javax.servlet.Servlet method
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		System.out.println(Global.AppName + " has been started.");
		// SchedulerManager sm = SchedulerManager.getInstance();
		SchedulerManager.getInstance();

		//清除缓存
		if (Global.useCache) {
			try {
				com.redmoon.oa.Config config = new com.redmoon.oa.Config();
				boolean isUseRedis = config.getBooleanProperty("isUseRedis");
				if (isUseRedis) {
					String redisHost = config.get("redisHost");
					int redisPort = config.getInt("redisPort");
					String redisPassword = config.get("redisPassword");

					Jedis jedis = new Jedis(redisHost, redisPort);
					if (!"".equals(redisPassword)) {
						jedis.auth(redisPassword);
					}
					String ping = jedis.ping();
					if ("PONG".equals(ping)) {
						RMCache.getInstance().clear();
						System.out.println(" Redis服务器连接成功！ ");
					} else {
						System.out.println(" Redis服务器连接失败！ ");
					}
				}
			} catch (CacheException e) {
				e.printStackTrace();
			} catch (JedisConnectionException e) {
				System.out.println(" Redis服务器连接失败！ ");
				e.printStackTrace();
			}
		}

		// 调度初始化
		Scheduler.initInstance(1000); // 单态模式
		System.out.println("AppInit: Scheduler initInstance");

		// 加载调度项
		Config config = new Config();
		config.initScheduler();
		System.out.println("AppInit: Scheduler unit inited");

		// 加载初始化线程
		InitializeThread.initInstance();

		CloudDiskThread.initInstance();

		// 恢复因系统停止而未发送的短信，置其为待发送状态
		if (SMSFactory.isUseSMS()) {
			com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
			int sendIncludeMinute = StrUtil.toInt(smscfg
					.getIsUsedProperty("sendIncludeMinute"), 10); // 10分钟内的待发送短信
			int sendMaxCountOnFail = StrUtil.toInt(smscfg
					.getIsUsedProperty("sendMaxCountOnFail"), 1); // 1次
			java.util.Date dd = new java.util.Date();
			java.util.Date d = DateUtil.addMinuteDate(dd, -sendIncludeMinute);
			String sql = "update sms_send_record set msg_flag='0' where is_sended=0 and msg_id=-1 and sendtime>? and send_count<? and is_timing=0";
			JdbcTemplate jt = new JdbcTemplate();
			try {
				int r = jt.executeUpdate(sql, new Object[] { d,
						new Integer(sendMaxCountOnFail) });
				System.out.println("AppInit: restore sms " + r);
			} catch (SQLException ex) {
				ex.printStackTrace();
				com.cloudwebsoft.framework.util.LogUtil.getLog(getClass())
						.error(StrUtil.trace(ex));
			}
		}
		// 启动时修改文件路径
		ModifyFilePath mfp = new ModifyFilePath();
		String filePath = PlatFormRelatedFactory.getPlatformGenerator()
				.getFilePath();
		if (filePath != null && !"".equals(filePath)) {
			mfp.modifyFilePath(filePath);
		}
		//生成客户端二维码下载链接
		TwoDimensionCode.generate2DCodeByMobileClient();
//		// 二维码
//		TwoDimensionCode.generate2DCodeByImage();
//		// ios二维码
//		TwoDimensionCode.generate2DCodeByImage4IOS();
		// 动态写入config_sso.xml文件中的key值
		com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
		String keyValue = cfg.get("key");
		if (keyValue != null && "".equals(keyValue)) {
			keyValue = RandomSecquenceCreator.getId(20);
			keyValue = StrUtil.PadString(keyValue, '0', 24, true); // key值必须是24位
			cfg.put("key", keyValue);
		}

		// 启动openfire
		com.redmoon.oa.Config oacfg = new com.redmoon.oa.Config();
		if (oacfg.getBooleanProperty("isLarkUsed")) {
			String path = Global.getAppPath();
			File file = new File(path);
			String basePath = file.getParent();
			path = basePath + "\\openfire\\bin\\openfire.bat";
			file = new File(path);
			if (!file.exists()) {
				path = basePath + "\\yimi_openfire1.0\\bin\\openfire.bat";
				file = new File(path);
			}
			try {
				StringBuilder sb = new StringBuilder();
				BufferedReader br = new BufferedReader(new FileReader(file));
				String buffer = null;
				while ((buffer = br.readLine()) != null) {
					if (buffer.startsWith("cd ")) {
						continue;
					}
					if (buffer.equals("set \"CURRENT_DIR=%cd%\"")) {
						buffer = "cd " + file.getParent() + "\r\n" + buffer;
					}
					sb.append(buffer).append("\r\n");
				}
				br.close();

				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
				bw.write(sb.toString());
				bw.close();
				Runtime.getRuntime().exec("cmd.exe /c \"" + path + "\"");
			} catch (IOException e) {
				e.printStackTrace();
				com.cloudwebsoft.framework.util.LogUtil.getLog(getClass())
						.error(path + "----" + StrUtil.trace(e));
			}
		}
		
		// 第一次使用上传数据(含第一次使用与注册后第一次使用)
		String customer_id = License.getInstance().getEnterpriseNum();
		try {
			String isRegisted = "0";
			if (oacfg.get("firstUseDate").equals("")) {
				if (customer_id.equals("yimi") || customer_id.equals("OA")
						|| customer_id.equals("ywrj")
						|| customer_id.equals("yimihome")) {
					isRegisted = "0";
				} else {
					isRegisted = "1";
					oacfg.put("firtstUserAfterReg", DateUtil.format(new Date(),
							"yyyy-mm-dd"));
				}
				SpringHelper.getBean(IUpgradeService.class).sendFirstUseInfo(
						isRegisted, customer_id, oacfg.get("version"));
			} else {
				if (oacfg.get("firtstUserAfterReg").equals("")
						&& !customer_id.equals("yimi")
						&& !customer_id.equals("OA")
						&& !customer_id.equals("ywrj")
						&& !customer_id.equals("yimihome")) {
					isRegisted = "1";
					oacfg.put("firtstUserAfterReg", DateUtil.format(new Date(),
							"yyyy-mm-dd"));
					SpringHelper.getBean(IUpgradeService.class)
							.sendFirstUseInfo(isRegisted, customer_id,
									oacfg.get("version"));
				}
			}
		} catch (Exception e) {
			com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(
					StrUtil.trace(e));
		}
		
		// OEM工作调度
		/**
		ClassJarFilesCheckJob ojob = new ClassJarFilesCheckJob();
		try {
			ojob.execute(null);
		} catch (JobExecutionException e) {
			com.cloudwebsoft.framework.util.LogUtil.getLog(getClass())
			.error(StrUtil.trace(e));
		}*/
		
		//超级狗校验
		try {
			CheckSuperKey csk = CheckSuperKey.getInstance();
			int status = csk.checkKey();
			
			//验证失败
			if (status != DogStatus.DOG_STATUS_OK){
				oacfg.put("systemIsOpen", "false");
				oacfg.put("systemStatus", "请使用正版授权系统");
				
			}else if(oacfg.getBooleanProperty("oem_filesEncrypt_validate")){
				if (!oacfg.getBooleanProperty("systemIsOpen")){
					oacfg.put("systemIsOpen", "true");
					oacfg.put("systemStatus", "系统正在维护中.....");
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			com.cloudwebsoft.framework.util.LogUtil.getLog(getClass())
			.error(StrUtil.trace(e));
			oacfg.put("systemIsOpen", "false");
			oacfg.put("systemStatus", "请使用正版授权系统");
		}
		
		// 除非一米专用的demo许可证
		if (customer_id != null && !customer_id.equals("yimi_demo")) {
			// 删除demo_login.jsp
			File file = new File (Global.getAppPath() + "/public/demo_login.jsp");
			if (file.exists()) {
				file.delete();
			}
		}
		
		if (Global.getRealPath().equals("C:/Program Files (x86)/Apache Software Foundation/Tomcat 6.0/deployment/yimioa/")) {
			config.setProperty("Application.realPath", Global.getAppPath());
			Global.init();
		}
		
		// add by lichao 20150805  启动时检测证书合法性 
		// 20170416 fgf 没必要放在这里，连接yimihome.com会导致启动变慢
		// LicenseManagerJob lb = new LicenseManagerJob();
		// lb.executeJob();
		
		// add by lichao 20150812  启动时将已经处理过的流程动作对应在oa_message中的消息记录置为已读
		// fgf 20161115 这里会导致启动变得很慢，而在流程中已加入了置消息为已读的处理，调用的是MessageDb中的setUserFlowReaded方法
		// SetOaMessageRead sb = new SetOaMessageRead();
		// sb.executeJob();
		
		// 关联用户的角色序号
		SetUserDutyJob sudjob = new SetUserDutyJob();
		sudjob.executeJob();
		
		// 20161223 没必要放在这里，会导致启动变慢
		// 修改users表中不匹配的unit_code值
		// SetUserUnitCodeJob suucjob = new SetUserUnitCodeJob();
		// suucjob.executeJob();
		
		//lzm 将oa_menu 表中的code 与mobile_app_icon_config表中 不一致的code同步
		SetMenuDutyMobileAppJob smdmajob = new SetMenuDutyMobileAppJob();
		smdmajob.executeJob();
		
/*    	try {
			boolean re = License.getInstance().checkSolution("pointsys");
			if (re) {
				// 初始化尚未初始化的当月人员积分
				PointSystemConfig pscfg = PointSystemConfig.getInstance();
				if (pscfg.getBooleanProperty("isUse")) {
					int d = pscfg.getIntProperty("calcuDay");
					if (d > 0 && DateUtil.getDay(new Date()) >= d) {
						PointSystemUtil.calcuUnInitUserScore();
					}
				}				
			}
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	*/

		// 注册钉钉通讯录回调事件
		com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
		if(dingdingCfg.isUseDingDing()) {
			EventChangeService.deleteEventCallBack();
			EventChangeService.registerEventChange();
		}
	}

	/**
	 * service
	 * 
	 * @param servletRequest
	 *            ServletRequest
	 * @param servletResponse
	 *            ServletResponse
	 * @throws ServletException
	 * @throws IOException
	 * @todo Implement this javax.servlet.Servlet method
	 */
	public void service(ServletRequest servletRequest,
			ServletResponse servletResponse) throws ServletException,
			IOException {
	}
}
