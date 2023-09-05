package com.redmoon.oa.fileark;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.util.Date;
import cn.js.fan.util.DateUtil;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.Vector;

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
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class FullTextSearchIndexerJob extends QuartzJobBean {
    public FullTextSearchIndexerJob() {
    }

    /**
     * execute
     *
     * @param jobExecutionContext JobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void executeInternal(JobExecutionContext jobExecutionContext) throws
            JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        String fullTextSearchTime = cfg.get("fullTextSearchTime");
        Date d = DateUtil.parse(fullTextSearchTime, "yyyy-MM-dd HH:mm:ss");

        Indexer indexer = new Indexer();
        Document doc = new Document();
        
        doc.index(indexer, d, null, true);

        cfg.put("fullTextSearchTime", DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"));
    }
}
