package com.redmoon.oa.job;

import java.util.Properties;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redmoon.oa.upgrade.service.IUpgradeService;
import com.redmoon.oa.upgrade.service.SpringHelper;
/**
 * 更新oa job
 * @author Administrator
 *
 */
public class UpgradeJob implements Job {

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		// TODO Auto-generated method stub
		//发布时需要修改为false,若是window系统支持自动升级
		Properties prop = System.getProperties();
		String os = prop.getProperty("os.name");
		if (os.startsWith("win") || os.startsWith("Win") )
		{
			SpringHelper.getBean(IUpgradeService.class).execute(false);
		}
	}
	

}
