package com.redmoon.oa.job;

import cn.js.fan.util.ErrMsgException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.shell.BSHShell;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import bsh.EvalError;
import bsh.Interpreter;
import org.springframework.scheduling.quartz.QuartzJobBean;

@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class BeanShellScriptJob extends QuartzJobBean {

	@Override
	public void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String script = data.getString("script");
		try {
			BSHShell bs = new BSHShell();
			bs.eval(script);
		} catch (ErrMsgException e) {
			LogUtil.getLog(getClass()).error(e);
		}
	}

}
