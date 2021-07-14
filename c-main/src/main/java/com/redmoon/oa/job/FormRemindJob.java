package com.redmoon.oa.job;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.aop.ProxyFactory;
import com.cloudwebsoft.framework.aop.Pointcut.MethodNamePointcut;
import com.cloudwebsoft.framework.aop.base.Advisor;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.ibm.icu.util.Calendar;
import com.redmoon.oa.flow.FormRemindDb;
import com.redmoon.oa.message.IMessage;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.message.MobileAfterAdvice;
import com.redmoon.oa.oacalendar.OACalendarDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserSetupDb;
import com.redmoon.oa.pvg.RoleDb;
import com.redmoon.oa.sms.IMsgUtil;
import com.redmoon.oa.sms.SMSFactory;

public class FormRemindJob implements Job {
	
    public static String putFieldValue(ResultRecord rr, String str) {
        Pattern p = Pattern.compile(
                "\\{\\$([A-Z0-9a-z-_\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String field = m.group(1);
            m.appendReplacement(sb, rr.getString(field));
        }
        m.appendTail(sb);

        return sb.toString();
    }
    
	public void remindExpire() {
        IMessage imsg = null;
        ProxyFactory proxyFactory = new ProxyFactory(
                "com.redmoon.oa.message.MessageDb");
        Advisor adv = new Advisor();
        MobileAfterAdvice mba = new MobileAfterAdvice();
        adv.setAdvice(mba);
        adv.setPointcut(new MethodNamePointcut("sendSysMsg", false));
        //proxyFactory.addAdvisor(adv);
        imsg = (IMessage) proxyFactory.getProxy();
        
        FormRemindDb frd = new FormRemindDb();
        RoleDb rd = new RoleDb();

		String sql = "select id from " + frd.getTable().getName()
				+ " where kind=" + FormRemindDb.KIND_EXPIRE;
		Iterator ir = frd.list(sql).iterator();
		while (ir.hasNext()) {
			frd = (FormRemindDb) ir.next();

			String tableName = frd.getString("table_name");
			String dateField = frd.getString("date_field");
			int d = frd.getInt("ahead_day");
			int h = frd.getInt("ahead_hour");
			int m = frd.getInt("ahead_minute");
			
			/**
			 * 每隔10分钟扫描一次，落在10分钟区间范围内的都予提醒
			 *                           date_field
			 * --------------------------|--------------------
			 * -----------------------|-----|-----------------
			 *                        dt    dt2
			 * dt=now+dlt   dt2=now+dlt+10分钟
			 * 
			 * date_field>=dt and date_field<dt2
			 */
			
			java.util.Date dt = new java.util.Date();
			if (d>=0)
				dt = DateUtil.addDate(dt, d);
			if (h>=0)
				dt = DateUtil.addHourDate(dt, h);
			if (m>=0)
				dt = DateUtil.addMinuteDate(dt, m);
			
			java.util.Date dt2 = DateUtil.addMinuteDate(dt, 10);
			
			String dtformat = "yyyy-MM-dd HH:mm:00";
			String strdt = DateUtil.format(dt, dtformat);
			String strdt2 = DateUtil.format(dt2, dtformat);

			sql = "select * from " + tableName + " where " + dateField + ">=" + SQLFilter.getDateStr(strdt, "yyyy-MM-dd HH:mm:ss") + " and "
				+ dateField + "<" + SQLFilter.getDateStr(strdt2, "yyyy-MM-dd HH:mm:ss");
			
			// System.out.println(getClass() + " " + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " sql=" + sql);
			ResultIterator ri;
			try {
				JdbcTemplate jt = new JdbcTemplate();
				ri = jt.executeQuery(sql);
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord)ri.next();
					
			        String t = frd.getString("title");
			        String c = frd.getString("content");
			        
			        t = putFieldValue(rr, t);
			        c = putFieldValue(rr, c);
			        
			        String[] users = StrUtil.split(frd.getString("users"), ",");
			        String[] roles = StrUtil.split(frd.getString("roles"), ",");
			        
			        int len = 0;
			        if (users!=null)
			        	len = users.length;
		            for (int i = 0; i < len; i++) {
		                imsg.sendSysMsg(users[i], t, c);
		            }
			        
		            len = 0;
			        if (roles!=null)
			        	len = roles.length;
			        for (int i=0; i<len; i++) {
			        	rd = rd.getRoleDb(roles[i]);
			        	if (rd.isLoaded()) {
			        		Iterator irUser = rd.getAllUserOfRole().iterator();
			        		while (irUser.hasNext()) {
			        			UserDb user = (UserDb)irUser.next();
				                imsg.sendSysMsg(user.getName(), t, c);
			        		}
			        	}
			        }
			        
			        String userField = frd.getString("user_field");
			        if (userField!=null && !"".equals(userField)) {
			        	String userStr = StrUtil.getNullStr(rr.getString(userField));
				        String[] usersField = StrUtil.split(userStr, ",");
				        len = 0;
				        if (usersField!=null)
				        	len = usersField.length;
			            for (int i = 0; i < len; i++) {
			                imsg.sendSysMsg(usersField[i], t, c);
			            }
			        }
				}				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
    
	public void remindEveryYear() {
        FormRemindDb frd = new FormRemindDb();
        RoleDb rd = new RoleDb();

		String sql = "select id from " + frd.getTable().getName()
				+ " where kind=" + FormRemindDb.KIND_EVERY_YEAR;
		Iterator ir = frd.list(sql).iterator();
		while (ir.hasNext()) {
			frd = (FormRemindDb) ir.next();

			String tableName = frd.getString("table_name");
			String dateField = frd.getString("date_field");
			int d = frd.getInt("ahead_day");
			int h = frd.getInt("ahead_hour");
			int m = frd.getInt("ahead_minute");
			
			/**
			 * 每隔10分钟扫描一次，落在10分钟区间范围内的都予提醒
			 *                           date_field
			 * --------------------------|--------------------
			 * -----------------------|-----|-----------------
			 *                        dt    dt2
			 * dt=now+dlt   dt2=now+dlt+10分钟
			 * 
			 * date_field>=dt and date_field<dt2
			 */
			
			java.util.Date now = new java.util.Date();
			java.util.Date dt = null;
			if (d>0)
				dt = DateUtil.addDate(now, d);
			if (h>0)
				dt = DateUtil.addHourDate(now, h);
			if (m>0)
				dt = DateUtil.addMinuteDate(now, m);
			
			java.util.Date dt2 = DateUtil.addMinuteDate(dt, 10);
            String nowMonth = "" + DateUtil.format(now, "MM");
            String nowDate = "" + DateUtil.format(now, "dd");
            
			sql = "select * from " + tableName + " where " + SQLFilter.month(dateField) + "=" + StrUtil.sqlstr(nowMonth) + " and "
				+ SQLFilter.day(dateField) + "=" + StrUtil.sqlstr(nowDate);
			
			// 条件
	        String filter = StrUtil.getNullStr(frd.getString("filter"));
	        
	    	boolean isScript = filter.indexOf("ret=")!=-1 || filter.indexOf("ret ")!=-1;
	    	if (isScript) {
				Interpreter bsh = new Interpreter();
				try {
					// sb.append("fdao=\"" + fdao + "\";");

					// bsh.set("fileUpload", fu);

					bsh.eval(filter);

					String ret = "";
					Object obj = bsh.get("ret");
					if (obj != null) {
						ret = (String) obj;
						
						sql += " and " + ret;
					}
					else {
						// ret = "1=1";
						String errMsg = (String) bsh.get("errMsg");
						LogUtil.getLog(getClass()).error(
								"bsh errMsg=" + errMsg);					
					}
				} catch (EvalError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
	    	}
	    	else {
	    		sql += " and " + filter;
	    	}
	    	
	    	UserMgr um = new UserMgr();
	        String charset = Global.getSmtpCharset();
	        cn.js.fan.mail.SendMail sendmail = new cn.js.fan.mail.SendMail(charset);
	        String senderName = StrUtil.GBToUnicode(Global.AppName);
	        senderName += "<" + Global.getEmail() + ">";	
	        String mailserver = Global.getSmtpServer();
	        int smtp_port = Global.getSmtpPort();
	        String name = Global.getSmtpUser();
	        String pwd_raw = Global.getSmtpPwd();
	        boolean isSsl = Global.isSmtpSSL();
	        try {
	            sendmail.initSession(mailserver, smtp_port, name,
	                                 pwd_raw, "", isSsl);
	        } catch (Exception ex) {
	            LogUtil.getLog(getClass()).error(StrUtil.trace(ex));
	        }
	        
			ResultIterator ri;
			try {
				MessageDb md = new MessageDb();
				JdbcTemplate jt = new JdbcTemplate();
				ri = jt.executeQuery(sql);
				while (ri.hasNext()) {
					ResultRecord rr = (ResultRecord)ri.next();
					
					java.util.Date df = rr.getDate("date_field");
					
					Calendar cal = Calendar.getInstance();
					cal.setTime(df);
					cal.set(Calendar.YEAR, DateUtil.getYear(now));
					df = cal.getTime();
					
					// 落于10分钟间隔范围内，则发消息
					if (DateUtil.compare(dt, df)==2 && DateUtil.compare(dt2, df)==1) {
				        String t = frd.getString("title");
				        String c = frd.getString("content");
				        
				        t = putFieldValue(rr, t);
				        c = putFieldValue(rr, c);
				        
				        String[] users = StrUtil.split(frd.getString("users"), ",");
				        String[] roles = StrUtil.split(frd.getString("roles"), ",");
				        
						boolean isMsg = frd.getInt("is_msg")==1;
						boolean isMail = frd.getInt("is_email")==1;
						boolean isSms = frd.getInt("is_sms")==1;				        
				        
				        int len = 0;
				        if (users!=null)
				        	len = users.length;
			            for (int i = 0; i < len; i++) {
			            	if (isMsg) {
			            		md.sendSysMsg(users[i], t, c);
			            	}
			            	
			    			if (isSms && SMSFactory.isUseSMS()) {
			    				IMsgUtil imu = SMSFactory.getMsgUtil();
			    				if (imu != null) {
			    					try {
			    						imu.send(users[i], t, MessageDb.SENDER_SYSTEM);
			    					} catch (ErrMsgException e) {
			    						// TODO Auto-generated catch block
			    						e.printStackTrace();
			    					}
			    				}
			    			}
			    			
			    			UserDb ud = um.getUserDb(users[i]);
			    			if (isMail && ud.getEmail() != null && !ud.getEmail().equals("")) {			    				
			    				sendmail.initMsg(ud.getEmail(), senderName, t, c, true);
			    				sendmail.send();
			    				sendmail.clear();
			    			}			    			
			            }
				        
				        if (roles!=null)
				        	len = roles.length;
				        for (int i=0; i<len; i++) {
				        	rd = rd.getRoleDb(roles[i]);
				        	if (rd.isLoaded()) {
				        		Iterator irUser = rd.getAllUserOfRole().iterator();
				        		while (irUser.hasNext()) {
				        			UserDb user = (UserDb)irUser.next();
				        			
				        			if (isMsg) {
				        				md.sendSysMsg(user.getName(), t, c);
				        			}
				        			
				        			if (isSms && SMSFactory.isUseSMS()) {
					    				IMsgUtil imu = SMSFactory.getMsgUtil();
					    				if (imu != null) {
					    					try {
					    						imu.send(user.getName(), t, MessageDb.SENDER_SYSTEM);
					    					} catch (ErrMsgException e) {
					    						// TODO Auto-generated catch block
					    						e.printStackTrace();
					    					}
					    				}
					    			}
					    			
					    			if (isMail && user.getEmail() != null && !user.getEmail().equals("")) {			    				
					    				sendmail.initMsg(user.getEmail(), senderName, t, c, true);
					    				sendmail.send();
					    				sendmail.clear();
					    			}				        			
				        		}
				        	}
				        }
				        
				        String[] usersField = StrUtil.split(frd.getString("user_field"), ",");
				        if (usersField!=null)
				        	len = usersField.length;
			            for (int i = 0; i < len; i++) {
			            	if (isMsg) {
			            		md.sendSysMsg(usersField[i], t, c);
			            	}
			            	
			            	if (isSms && SMSFactory.isUseSMS()) {
			    				IMsgUtil imu = SMSFactory.getMsgUtil();
			    				if (imu != null) {
			    					try {
			    						imu.send(usersField[i], t, MessageDb.SENDER_SYSTEM);
			    					} catch (ErrMsgException e) {
			    						// TODO Auto-generated catch block
			    						e.printStackTrace();
			    					}
			    				}
			    			}
			    			
			            	UserDb user = um.getUserDb(usersField[i]);
			    			if (isMail && user.getEmail() != null && !user.getEmail().equals("")) {			    				
			    				sendmail.initMsg(user.getEmail(), senderName, t, c, true);
			    				sendmail.send();
			    				sendmail.clear();
			    			}			            	
			            }
					}
				}				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ErrMsgException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}	
	
	public void execute(JobExecutionContext jobExecutionContext)
				throws JobExecutionException {
		remindExpire();
		remindEveryYear();
	}
}
