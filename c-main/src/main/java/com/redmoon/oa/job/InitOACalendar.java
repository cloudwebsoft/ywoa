package com.redmoon.oa.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

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
import org.springframework.scheduling.quartz.QuartzJobBean;

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
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class InitOACalendar extends QuartzJobBean {
	public InitOACalendar() {
	}
	
	/**
	 * execute
	 * 
	 * @param jobExecutionContext
	 *            JobExecutionContext
	 * @throws JobExecutionException
	 */
	@Override
	public void executeInternal(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		int year = DateUtil.getYear(new Date());
		OACalendarDb ocd = new OACalendarDb();
		if (!ocd.isYearInitialized(year)) {
			ocd.initCalendar(year);
		}
	}
}
