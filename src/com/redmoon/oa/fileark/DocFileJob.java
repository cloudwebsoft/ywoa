package com.redmoon.oa.fileark;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.js.fan.web.Global;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-10-8下午11:02:32
 */
public class DocFileJob implements Job{

	/**
	 * @Description: 
	 * @param arg0
	 * @throws JobExecutionException
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		FileBakUp fbu = new FileBakUp();
		String path = Global.getRealPath()+"/upfile/zip";
		fbu.deleteDirs(path);
	}

}
