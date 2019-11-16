package com.redmoon.oa.job;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import SuperDog.DogStatus;

import com.redmoon.oa.Config;
import com.redmoon.oa.superCheck.CheckSuperKey;
/**
 * OEM、SRC版本超级狗校验
 * @author jfy
 * @date Jul 4, 2015
 */
public class SuperCheckJob implements Job {
	Logger logger = Logger.getLogger( SuperCheckJob.class.getName() );
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		Config cfg = new Config();
		try {
			CheckSuperKey csk = CheckSuperKey.getInstance();
			int status = csk.checkKey();
			
			//验证失败
			if (status != DogStatus.DOG_STATUS_OK){
				cfg.put("systemIsOpen", "false");
				cfg.put("systemStatus", "请使用正版授权系统");
				
			}else if(cfg.getBooleanProperty("oem_filesEncrypt_validate")){
				if (!cfg.getBooleanProperty("systemIsOpen")){
					// cfg.put("systemIsOpen", "true");
					// cfg.put("systemStatus", "系统正在维护中.....");
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("super dog check error:" + e.getMessage());
			cfg.put("systemIsOpen", "false");
			cfg.put("systemStatus", "请使用正版授权系统");
		}
	}
	
}
