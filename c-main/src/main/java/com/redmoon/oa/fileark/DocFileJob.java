package com.redmoon.oa.fileark;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import cn.js.fan.web.Global;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-10-8下午11:02:32
 */
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class DocFileJob extends QuartzJobBean {

	/**
	 * @Description: 
	 * @param arg0
	 * @throws JobExecutionException
	 */
	@Override
	public void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		FileBakUp fbu = new FileBakUp();
		String path = Global.getRealPath()+"/upfile/zip";
		fbu.deleteDirs(path);
	}

}
