package com.redmoon.oa.job;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ErrMsgException;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.flow.WorkflowMgr;
import com.redmoon.oa.flow.WorkflowPredefineDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class OAStatJob extends QuartzJobBean {
    public OAStatJob() {
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
        // String flowCode = data.getString("flowCode");

    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri;
		try {
	    	String sql = "select sum(file_size) from oa_message_attach";
	    	ri = jt.executeQuery(sql);
	    	long msgSpace = 0;
	    	if (ri.hasNext()) {
	    		ResultRecord rr = (ResultRecord)ri.next();
	    		msgSpace = rr.getLong(1);
	    	}
	    	
	    	sql = "select sum(file_size) from flow_document_attach";
	    	ri = jt.executeQuery(sql);
	    	long flowFileSize = 0;
	    	if (ri.hasNext()) {
	    		ResultRecord rr = (ResultRecord)ri.next();
	    		flowFileSize = rr.getLong(1);
	    	}
	    	
	    	sql = "select sum(file_size) from document_attach";
	    	ri = jt.executeQuery(sql);
	    	long docFileSize = 0;
	    	if (ri.hasNext()) {
	    		ResultRecord rr = (ResultRecord)ri.next();
	    		docFileSize = rr.getLong(1);
	    	}			
	    	
	    	sql = "select sum(file_size) from oa_notice_attach";
	    	ri = jt.executeQuery(sql);
	    	long noticeFileSize = 0;
	    	if (ri.hasNext()) {
	    		ResultRecord rr = (ResultRecord)ri.next();
	    		noticeFileSize = rr.getLong(1);
	    	}		    	
	    	
	    	long allSpaceUsed = (msgSpace + flowFileSize + docFileSize + noticeFileSize) / 1024000;
	    	
	    	// 置已用空间
	    	com.redmoon.oa.android.CloudConfig cfg = com.redmoon.oa.android.CloudConfig.getInstance();
	    	cfg.setProperty("diskSpaceUsed", "" + allSpaceUsed);

		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
    }
}