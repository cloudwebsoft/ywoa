package com.redmoon.oa.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import bsh.EvalError;
import bsh.Interpreter;

public class BeanShellScriptJob implements Job {

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		// TODO Auto-generated method stub
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String script = data.getString("script");
        Interpreter bsh = new Interpreter();
		try {
			bsh.eval(script);
		} catch (EvalError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
