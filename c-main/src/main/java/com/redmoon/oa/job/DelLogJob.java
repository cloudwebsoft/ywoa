package com.redmoon.oa.job;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redmoon.oa.Config;

import cn.js.fan.util.DateUtil;
import cn.js.fan.util.PropertiesUtil;
import cn.js.fan.util.ZipUtil;
import cn.js.fan.web.Global;

/**
 * @Description: 备份并删除OA日志
 * @author: 古月圣
 * @Date: 2015-11-4上午10:17:04
 */
public class DelLogJob implements Job {

	@Override
	public void execute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		Config cfg = new Config();
		int days = cfg.getInt("del_log_days");

		if (days <= 0) {
			return;
		}

		PropertiesUtil pu = new PropertiesUtil(Global.getRealPath()
				+ "WEB-INF/log4j.properties");
		String logPath = pu.getValue("log4j.appender.R.File");
		ArrayList<String> list = new ArrayList<String>();
		File f = new File(logPath);
		if (f.exists()) {
			f = new File(f.getParent());
			if (f.exists()) {
				File[] logs = f.listFiles();
				for (File log : logs) {
					if (log.getName().indexOf(".log.") > -1
							&& DateUtil.addDate(new Date(log.lastModified()),
									days).before(new Date())) {
						list.add(f.getName() + File.separator + log.getName());
					}
				}
				ZipUtil.zip(f.getParent(), list, f.getPath(), "oa_log_bak"
						+ DateUtil.format(new Date(), "yyyyMMddHHmmssS"));
				for (String logName : list) {
					File logFile = new File(f.getParent() + File.separator
							+ logName);
					logFile.delete();
				}
			}
		}
	}
}
