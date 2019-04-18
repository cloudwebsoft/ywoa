package com.redmoon.clouddisk.socketServer;

import java.net.*;
import java.util.Date;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.clouddisk.db.KeyDb;
import com.redmoon.clouddisk.tools.ToolsUtil;
import com.redmoon.oa.netdisk.SideBarMgr;

/**
 * @author 古月圣
 * 
 */
public class CloudDiskThread extends Thread {

	public static CloudDiskThread clouddiskThread = null;
	private static boolean running = true;
	private static int TIME_OUT = 10000;
	public static String OA_REALPATH = null;
	public static String OA_KEY = null;

	/**
	 * Creates a new CacheTimer object. The currentTime of Cache will be updated
	 * at the specified update interval.
	 * 
	 * @param updateInterval
	 *            the interval in milleseconds that updates should be done.
	 */
	public CloudDiskThread() {
		this.setDaemon(true);
		this.setName("com.redmoon.clouddisk.socketServer.CloudDiskThread");
		start();
	}

	/**
	 * 单态模式
	 * 
	 * @param updateInterval
	 *            long
	 */
	public static synchronized void initInstance() {
		com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
				.getInstance();
		String key = ToolsUtil.randomString(KeyDb.KEY_LENGTH,
				ToolsUtil.CASE_SENSITIVE);
		OA_KEY = key + key + key;
		if (cfg.getBooleanProperty("isUsed")) {
			// html侧边栏的同步
			if (cfg.getBooleanProperty("is_openSideHTML")) {
				SideBarMgr sbMgr = new SideBarMgr();
				sbMgr.syncAllFlag();
			}

			long st = DateUtil.toLong(new Date());
			while (true) {
				try {
					InetAddress addr = InetAddress.getByName("127.0.0.1");
					new Socket(addr, cfg.getIntProperty("clouddiskPort"));
					running = false;
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LogUtil.getLog("stopping:").error(StrUtil.trace(e));
					break;
				} catch (Exception e) {
					LogUtil.getLog("stopping:").error(StrUtil.trace(e));
					break;
				}
				long et = DateUtil.toLong(new Date());
				if (et - st > TIME_OUT) {
					break;
				}
			}
			running = true;
			clouddiskThread = new CloudDiskThread();
		}
	}

	public static synchronized void stopInstance() {
		running = false;
	}

	public void run() {
		ServerSocket server = null;
		
		/*
		String temp = Thread.currentThread().getContextClassLoader()
				.getResource("").getPath();
		try {
			OA_REALPATH = java.net.URLDecoder
					.decode(temp,
							(String) AccessController
									.doPrivileged(new GetPropertyAction(
											"file.encoding")));
		} catch (UnsupportedEncodingException e) {
			LogUtil.getLog("get oa path:").error(StrUtil.trace(e));
			OA_REALPATH = temp;
		}

		if (OA_REALPATH.startsWith("/")) {
			OA_REALPATH = OA_REALPATH.substring(1, OA_REALPATH
					.indexOf("WEB-INF"));
		} else {
			OA_REALPATH = OA_REALPATH.substring(0, OA_REALPATH
					.indexOf("WEB-INF"));
		}
		if (!OA_REALPATH.endsWith("/")) {
			OA_REALPATH += "/";
		}
		*/
		
		OA_REALPATH = Global.getRealPath();

		System.out.println("云盘系统正在运行...");
		try {
			com.redmoon.clouddisk.Config cfg = com.redmoon.clouddisk.Config
					.getInstance();
			server = new ServerSocket(cfg.getIntProperty("clouddiskPort"));
			ServerWorker worker;
			Thread thread;
			while (running) {
				Socket theSocket = server.accept();
				theSocket.setSoTimeout(300000);
				worker = new ServerWorker(theSocket);
				thread = new Thread(worker);
				thread.start();
			}
		} catch (Exception e) {
			LogUtil.getLog("startup:").error(StrUtil.trace(e));
		} finally {
			try {
				server.close();
			} catch (Exception e) {
				LogUtil.getLog("startup:").error(StrUtil.trace(e));
			}
			System.out.println("云盘系统已经关闭...");
		}
	}

	/**
	 * @param args
	 */
	/*
	 * public static void main(String[] args) { ServerSocket server = null;
	 * System.out.println("云盘系统正在运行..."); try { server = new
	 * ServerSocket(PORT_NUMBER); ServerWorker worker; Thread thread; while
	 * (true) { Socket theSocket = server.accept(); worker = new
	 * ServerWorker(theSocket); thread = new Thread(worker); thread.start(); } }
	 * catch (Exception e) { LogUtil.getLog("startup:").error(StrUtil.trace(e));
	 * } finally { try { server.close(); } catch (Exception e) {
	 * LogUtil.getLog("startup:").error(StrUtil.trace(e)); }
	 * System.out.println("云盘系统已经关闭..."); } }
	 */
}
