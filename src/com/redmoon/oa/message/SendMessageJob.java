package com.redmoon.oa.message;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redmoon.oa.android.xinge.SendNotice;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

import java.util.Iterator;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.StrUtil;
import java.util.Calendar;
import cn.js.fan.util.ErrMsgException;

public class SendMessageJob implements Job {
	public static final int SEND_MESSAGE_SCHEDULE = 20;
	
	public SendMessageJob() {
        super();
    }
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		String tableName = "oa_message";
		//Calendar now = Calendar.getInstance();
		//String sql_query = "select id from " + tableName + " where box=" + MessageDb.DRAFT + " and is_sent=0 and send_time<=" + SQLFilter.getDateStr(StrUtil.FormatDate(now.getTime(), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss");
		String sql_query = "select id from " + tableName + " where box=" + MessageDb.DRAFT + " and is_sent=0 and send_time<= now()";
		
        SendNotice se = new SendNotice();
		MessageDb md = new MessageDb();
		Iterator i = md.list(sql_query).iterator();
		while(i.hasNext()) {
			md = (MessageDb)i.next();
			md.setBox(MessageDb.INBOX);
			int id = md.getId();
			String receiversAll = md.getReceiversAll();
			String[] ary = receiversAll.split(",");
	        UserMgr um = new UserMgr();
	        
	        for (int k=0; k<ary.length; k++) {
	            UserDb user = um.getUserDb(ary[k]);
	            if (!user.isLoaded()) {
	            	continue;
	            }
	            String receiver = ary[k];
	            try {
	            	md.transmit(receiver, id);
	            	
	    	        //add by lichao 手机端消息推送
	    	        se.PushNoticeSingleByToken(receiver, md.getTitle(), md.getContent(), id);
	    	        // System.out.println(md.getContent()+"---------------");
	            } catch(ErrMsgException e) {
	            	e.printStackTrace();
	            }
	        }
	        
	        md = (MessageDb)md.getObjectDb(new Integer(id));
	        md.setBox(MessageDb.OUTBOX);
			md.setIsSent(1);
			md.save();
		}
	}
	
}
