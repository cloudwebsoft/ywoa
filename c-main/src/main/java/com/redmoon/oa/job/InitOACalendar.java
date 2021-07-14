package com.redmoon.oa.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redmoon.oa.Config;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
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
public class InitOACalendar implements Job {
	public InitOACalendar() {
	}
	
	/**
	 * execute
	 * 
	 * @param jobExecutionContext
	 *            JobExecutionContext
	 * @throws JobExecutionException
	 * @todo Implement this org.quartz.Job method
	 */
	public void execute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		int year = DateUtil.getYear(new Date());
		OACalendarDb ocd = new OACalendarDb();
		if (!ocd.isYearInitialized(year)) {
			ocd.initCalendar(year);
		}
	}
}
