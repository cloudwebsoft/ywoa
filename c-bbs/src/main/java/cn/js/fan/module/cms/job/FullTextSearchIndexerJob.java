package cn.js.fan.module.cms.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;
import java.util.Date;
import cn.js.fan.util.DateUtil;
import cn.js.fan.module.cms.search.Indexer;
import java.util.Vector;
import cn.js.fan.module.cms.Config;
import cn.js.fan.module.cms.Document;

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

        Config cfg = new Config();

        String fullTextSearchTime = cfg.getProperty("cms.fullTextSearchTime");
        Date d = DateUtil.parse(fullTextSearchTime, "yyyy-MM-dd HH:mm:ss");

        Indexer indexer = new Indexer();
        Document doc = new Document();
        Vector v = doc.list(DateUtil.toLong(d), 0);
        indexer.index(v, true);

        cfg.put("fullTextSearchTime", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
    }
}
