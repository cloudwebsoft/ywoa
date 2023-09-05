package com.redmoon.oa.message;

import cn.js.fan.util.DateUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.Config;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import com.redmoon.oa.android.xinge.SendNotice;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;

import java.util.Date;
import java.util.Iterator;

import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.StrUtil;
import java.util.Calendar;
import cn.js.fan.util.ErrMsgException;
import org.springframework.scheduling.quartz.QuartzJobBean;

//持久化
@PersistJobDataAfterExecution
//禁止并发执行(Quartz不要并发地执行同一个job定义（这里指一个job类的多个实例）)
@DisallowConcurrentExecution
@Slf4j
public class SendMessageJob extends QuartzJobBean {
	public static final int SEND_MESSAGE_SCHEDULE = 20;
	
	public SendMessageJob() {
        super();
    }
	
	@Override
	public void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		String tableName = "oa_message";
		String sql_query = "select id from " + tableName + " where box=" + MessageDb.DRAFT + " and is_sent=0 and send_time<= " + SQLFilter.getDateStr(DateUtil.format(new java.util.Date(), "yyyy-MM-dd HH:mm:ss"), "yyyy-MM-dd HH:mm:ss");
		
        SendNotice se = new SendNotice();
		Config cfg = Config.getInstance();
		boolean xingeIsEnabled = cfg.getBoolean("xingeIsEnabled");
		boolean isUseClient = cfg.getBoolean("isUseClient");

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
					if (isUseClient) {
						if (xingeIsEnabled) {
							se.PushNoticeSingleByToken(receiver, md.getTitle(), md.getContent(), id);
						}
					}
	            } catch(ErrMsgException e) {
					LogUtil.getLog(getClass()).error(e);
	            }
	        }
	        
	        md = (MessageDb)md.getObjectDb(new Integer(id));
	        md.setBox(MessageDb.OUTBOX);
			md.setIsSent(1);
			md.save();
		}
	}
	
}
