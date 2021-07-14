package com.redmoon.oa.job;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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

public class OAStatJob implements Job {
    public OAStatJob() {
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
        // String flowCode = data.getString("flowCode");
        // System.out.println(getClass() + " execute：flowCode = " + data.getString("flowCode"));

    	String sql = "select sum(file_size) from netdisk_document_attach";
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri;
		try {
			ri = jt.executeQuery(sql);
	    	long netdiskSize = 0;
	    	if (ri.hasNext()) {
	    		ResultRecord rr = (ResultRecord)ri.next();
	    		netdiskSize = rr.getLong(1);
	    	}
	    	
	    	sql = "select sum(file_size) from oa_message_attach";
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
	    	
	    	long allSpaceUsed = (netdiskSize + msgSpace + flowFileSize + docFileSize + noticeFileSize) / 1024000;
	    	
	    	// 置已用空间
	    	com.redmoon.oa.android.CloudConfig cfg = com.redmoon.oa.android.CloudConfig.getInstance();
	    	cfg.setProperty("diskSpaceUsed", "" + allSpaceUsed);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
    }
}