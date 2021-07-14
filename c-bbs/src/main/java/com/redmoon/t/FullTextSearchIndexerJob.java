package com.redmoon.t;

import java.util.Date;
import java.util.Vector;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import cn.js.fan.util.DateUtil;

public class FullTextSearchIndexerJob implements Job {
	   public FullTextSearchIndexerJob() {
	    }

	    /**
	     * execute
	     *
	     * @param jobExecutionContext JobExecutionContext
	     * @throws JobExecutionException
	     * @todo Implement this org.quartz.Job method
	     */
	    public void execute(JobExecutionContext jobExecutionContext) throws
	            JobExecutionException {
	        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();

	        Config cfg = Config.getInstance();
	        String fullTextSearchTime = cfg.getProperty("t.fullTextSearchTime");
	        Date d = DateUtil.parse(fullTextSearchTime, "yyyy-MM-dd HH:mm:ss");

	        Indexer indexer = new Indexer();
	        TMsgDb tmd = new TMsgDb();
	        Vector v = tmd.list(d, null);
	        indexer.index(v, true);

	        cfg.put("fullTextSearchTime", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
	    }
}
