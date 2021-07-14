package com.cloudweb.oa.servlet;

import cn.js.fan.cache.jcs.RMCache;
import cn.js.fan.kernel.Scheduler;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Config;
import cn.js.fan.web.Global;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.dingding.service.eventchange.EventChangeService;
import com.redmoon.oa.kernel.InitializeThread;
import com.redmoon.oa.kernel.SchedulerManager;
import com.redmoon.oa.sms.SMSFactory;
import com.redmoon.oa.util.TwoDimensionCode;
import org.apache.jcs.access.exception.CacheException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.*;
import java.sql.SQLException;

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
     */
    @Override
    public void destroy() {
        System.out.println(Global.AppName + " has been stopped.");
        SchedulerManager sm = SchedulerManager.getInstance();
		// 结束调度
        sm.shutdown();

        // 当realod时停止该线程，以免tomcat因该线程存在，不能完全clear，致出现log4j错误
        Scheduler.getInstance().doExit();
    }

    /**
     * getServletConfig
     *
     * @return ServletConfig
     */
    @Override
    public javax.servlet.ServletConfig getServletConfig() {
        return null;
    }

    /**
     * getServletInfo
     *
     * @return String
     */
    @Override
    public String getServletInfo() {
        return "";
    }

    /**
     * init
     *
     * @param servletConfig ServletConfig
     * @throws ServletException
     */
    @Override
    public void init(javax.servlet.ServletConfig servletConfig) throws ServletException {
        System.out.println(Global.AppName + " has been started.");

		// 检查redis服务器连接
		if (Global.getInstance().isUseCache()) {
			try {
				boolean isUseRedis = Global.getInstance().isUseRedis();
				if (isUseRedis) {
					String redisHost = Global.getInstance().getRedisHost();
					int redisPort =  Global.getInstance().getRedisPort();
					String redisPassword = Global.getInstance().getRedisPassword();

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

		Config config = new Config();

		if (Global.getInstance().isSchedule()) {
			// 调度初始化
			SchedulerManager.getInstance();

			Scheduler.initInstance(1000);
			System.out.println("AppInit: Scheduler initInstance");

			// 加载调度项
			config.initScheduler();
			System.out.println("AppInit: Scheduler unit inited");
		}

		// 加载工作日历初始化线程
		InitializeThread.initInstance();

		// CloudDiskThread.initInstance();

		// 恢复因系统停止而未发送的短信，置其为待发送状态
		if (SMSFactory.isUseSMS()) {
			com.redmoon.oa.sms.Config smscfg = new com.redmoon.oa.sms.Config();
			int sendIncludeMinute = StrUtil.toInt(smscfg.getIsUsedProperty("sendIncludeMinute"), 10); // 10分钟内的待发送短信
			int sendMaxCountOnFail = StrUtil.toInt(smscfg.getIsUsedProperty("sendMaxCountOnFail"), 1); // 1次
			java.util.Date dd = new java.util.Date();
			java.util.Date d = DateUtil.addMinuteDate(dd, -sendIncludeMinute);
			String sql = "update sms_send_record set msg_flag='0' where is_sended=0 and msg_id=-1 and sendtime>? and send_count<? and is_timing=0";
			JdbcTemplate jt = new JdbcTemplate();
			try {
				int r = jt.executeUpdate(sql, new Object[]{d, new Integer(sendMaxCountOnFail)});
				System.out.println("AppInit: restore sms " + r);
			} catch (SQLException ex) {
				ex.printStackTrace();
				com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
			}
		}

		// 置配置cache.ccf、log4j.properties、config_cws.xml文件路径及参数
		Global.getInstance().initOnStart();

		// 生成客户端二维码下载链接
		TwoDimensionCode.generate2DCodeByMobileClient();

		// 动态写入config_sso.xml文件中的key值
		com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
		String keyValue = cfg.get("key");
		if (keyValue != null && "".equals(keyValue)) {
			keyValue = RandomSecquenceCreator.getId(20);
			// key值必须是24位
			keyValue = StrUtil.PadString(keyValue, '0', 24, true);
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
				com.cloudwebsoft.framework.util.LogUtil.getLog(getClass()).error(path + "----" + StrUtil.trace(e));

            }
        }


        // 注册钉钉通讯录回调事件
        com.redmoon.dingding.Config dingdingCfg = com.redmoon.dingding.Config.getInstance();
        if (dingdingCfg.isUseDingDing()) {
            EventChangeService.deleteEventCallBack();
            EventChangeService.registerEventChange();
        }
    }

    /**
     * service
     *
     * @param servletRequest  ServletRequest
     * @param servletResponse ServletResponse
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(ServletRequest servletRequest,
                        ServletResponse servletResponse) throws ServletException,
            IOException {
    }
}
