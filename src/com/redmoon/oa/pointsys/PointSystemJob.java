package com.redmoon.oa.pointsys;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-3-15下午09:37:24
 */
public class PointSystemJob implements Job {
	@Override
	public void execute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		PointSystemUtil.calcuAllUserScore();
	}
}
