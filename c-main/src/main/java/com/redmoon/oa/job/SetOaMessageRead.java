package com.redmoon.oa.job;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.cloudwebsoft.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.flow.MyActionDb;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @Description: 将已经处理过的流程动作对应在oa_message中的消息记录置为已读。
 * @author: lichao
 * @Date: 2015-8-4下午03:22:39
 */
//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class SetOaMessageRead extends QuartzJobBean {
    public SetOaMessageRead() {
    	
    }

    @Override
	public void executeInternal(JobExecutionContext jobExecutionContext) throws  JobExecutionException {
    	executeJob();
    }
    
    //独立出来，可直接调用执行
    public void executeJob() {
		String sql = "select id, flow_id from flow_my_action where is_checked= " + MyActionDb.CHECK_STATUS_CHECKED ;
		
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		
		try {
			ri = jt.executeQuery(sql);
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		
        Collection<Integer> myactionIdArr = new ArrayList<Integer>();  
        Collection<Integer> flowIdArr = new ArrayList<Integer>();  
        
		while (ri.hasNext()) {
			rd = ri.next();
			
			int myactionId = rd.getInt("id");
			myactionIdArr.add(myactionId);
			if(!myactionIdArr.contains(myactionId)){
				myactionIdArr.add(myactionId);
			}
			
			int flowId = rd.getInt("flow_id");
			if(!flowIdArr.contains(flowId)){
				flowIdArr.add(flowId);
			}
		}

		sql = "select id,action from oa_message where isreaded= 0 and action is not null and action <>'' ";

		jt = new JdbcTemplate();
		ri = null;
		rd = null;
		
		try {
			ri = jt.executeQuery(sql);
		} catch (SQLException e) {
			LogUtil.getLog(getClass()).error(e);
		}
		
		MessageDb msb = new MessageDb();
		while (ri.hasNext()) {
			rd = ri.next();
			
			int id = rd.getInt("id");
			String action = rd.getString("action");
						
			if(!"".equals(action) && action.contains("myActionId=")){
				String myactionId = action.substring(action.indexOf("myActionId=") + "myActionId=".length(), action.length());
				
				if(myactionIdArr.contains(Integer.parseInt(myactionId))){
					msb.getMessageDb(id);
					msb.setReaded(true);
					msb.save();
				}
			} else if (!"".equals(action) && action.contains("flowId=")) { 
				String flowId = action.substring(action.indexOf("flowId=") + "flowId=".length(), action.length());

				if(flowIdArr.contains(Integer.parseInt(flowId))){
					msb.getMessageDb(id);
					msb.setReaded(true);
					msb.save();
				}
			}
		}
    }

}