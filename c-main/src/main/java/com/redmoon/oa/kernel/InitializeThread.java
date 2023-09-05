package com.redmoon.oa.kernel;

import java.util.Date;

import cn.js.fan.util.DateUtil;

import com.redmoon.oa.oacalendar.OACalendarDb;

/**
 * @author 古月圣
 * 
 */
public class InitializeThread extends Thread {

	public static InitializeThread initializeThread = null;

	/**
	 * Creates a new CacheTimer object. The currentTime of Cache will be updated
	 * at the specified update interval.
	 *
	 */
	public InitializeThread() {
		this.setDaemon(true);
		this.setName("com.redmoon.oa.kernel.InitializeThread");
		start();
	}

	/**
	 * 单态模式
	 *
	 */
	public static synchronized void initInstance() {
		if (initializeThread == null) {
			initializeThread = new InitializeThread();
		}
	}

	@Override
	public void run() {
		// 如果工作日历未初始化，则初始化日历
		int year = DateUtil.getYear(new Date());
		OACalendarDb ocd = new OACalendarDb();
		if (!ocd.isYearInitialized(year)) {
			ocd.initCalendar(year);
		}
	}
}
