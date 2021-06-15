package com.redmoon.forum.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;
import java.util.Date;
import cn.js.fan.util.DateUtil;
import com.redmoon.forum.search.Indexer;
import com.redmoon.forum.MsgDb;
import java.util.Vector;
import com.redmoon.forum.Config;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
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
        String fullTextSearchTime = cfg.getProperty("forum.fullTextSearchTime");
        Date d = DateUtil.parse(fullTextSearchTime, "yyyy-MM-dd HH:mm:ss");

        Indexer indexer = new Indexer();
        MsgDb md = new MsgDb();
        Vector v = md.list(DateUtil.toLong(d), 0);
        indexer.index(v, true);

        cfg.put("fullTextSearchTime", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
    }
}
