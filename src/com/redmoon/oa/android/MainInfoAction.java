package com.redmoon.oa.android;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.notice.NoticeDb;
import com.redmoon.oa.person.PlanDb;
import com.redmoon.oa.worklog.WorkLogDb;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;

public class MainInfoAction {
	
	private String skey = "";
	private String result = "";
	private String title = "";

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSkey() {
		return skey;
	}

	public void setSkey(String skey) {
		this.skey = skey;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	public String execute() {
		JSONObject json = new JSONObject();
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(getSkey());
		String sql = "";
		try {
			if (re) {
				json.put("res", "-2");
				json.put("msg", "时间过期");
				setResult(json.toString());
				return "SUCCESS";
			}
			//1,获得当前待办数目
			int plan_count = getPlanCount(privilege.getUserName(skey));
			//2,内部邮件数目
			MessageDb md = new MessageDb();
			//内部邮件的数目
			int msgNewCount = md.getNewInnerMsgCount(privilege.getUserName(skey));
			//系统消息的数目
			int sysCount = md.getNewSysMsgCount(privilege.getUserName(skey));
			//3,取得待办流程的条数
			int flowWaitCount = WorkflowDb.getWaitCount(privilege.getUserName(skey));
			json.put("plan_count",String.valueOf(plan_count));
			json.put("msgNewCount",String.valueOf(msgNewCount));
			json.put("sysCount",String.valueOf(sysCount));
			json.put("flowWaitCount",String.valueOf(flowWaitCount));
			//内部邮件箱 未读最新一条
	    	String msgSql = "SELECT title FROM oa_message WHERE type<>"+md.TYPE_SYSTEM+" AND receiver="+ StrUtil.sqlstr(privilege.getUserName(skey))+" AND box=0 AND is_dustbin=0 AND is_sent=1 order by send_time desc limit 1";
	    	String msgInfo = getNewMessage(msgSql);
	    	json.put("msgInfo",msgInfo);
	    	//系统消息 唯独最新一条
	    	String sysNoticSql = "SELECT title FROM oa_message WHERE type="+md.TYPE_SYSTEM+" AND receiver="+ StrUtil.sqlstr(privilege.getUserName(skey))+" AND box=0 AND is_dustbin=0 AND is_sent=1 order by send_time desc limit 1";
	    	String sysNoticInfo = getNewMessage(sysNoticSql);
	    	json.put("sysNoticInfo",sysNoticInfo);
	    	
	    	//公告通知
	    	String curDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
	    	String noticeSql = "select n.title from oa_notice_reply r,oa_notice n  where n.id =  r.notice_id and r.user_name = "+StrUtil.sqlstr(privilege.getUserName(skey))+" and n.begin_date<=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + " and (n.end_date is null or n.end_date>=" + SQLFilter.getDateStr(curDay, "yyyy-MM-dd") + ") order by  n.begin_date desc limit 1";
			String noticeInfo = getNewMessage(noticeSql);
			json.put("noticeInfo",noticeInfo);
			//待办流程
			String flowSql ="select title from flow_my_action m, flow f where m.flow_id=f.id and (m.user_name="+StrUtil.sqlstr(privilege.getUserName(skey))+" or m.proxy="+StrUtil.sqlstr(privilege.getUserName(skey))+") and f.status<>-10 and (is_checked=0 or is_checked=2) and sub_my_action_id=0 order by receive_date desc limit 1";
			//String flowSql = "select title from flow_my_action m, flow f where m.flow_id=f.id and f.status<>" + WorkflowDb.STATUS_NONE + " and (m.user_name=" + StrUtil.sqlstr(privilege.getUserName(skey)) + " or m.proxy=" + StrUtil.sqlstr(privilege.getUserName(skey)) + ") and (is_checked=0 or is_checked=2) and sub_my_action_id=0 ";
			String flowInfo =getNewMessage(flowSql);
			json.put("flowInfo", flowInfo);
			//日程安排
			String planSql = "select title from user_plan where is_closed=0 and userName="
				+ StrUtil.sqlstr(privilege.getUserName(skey))
				+ " order by mydate desc, enddate desc limit 1";	
			String planInfo = getNewMessage(planSql);
			json.put("planInfo", planInfo);
			//工作记事
			String myWorkSql = "select content from work_log where userName="+StrUtil.sqlstr(privilege.getUserName(skey))+" and log_type=" + WorkLogDb.TYPE_NORMAL +" and myDate like '"+curDay+"%'";
			String myWorkInfo = getNewMessage(myWorkSql);
			json.put("myWorkInfo", myWorkInfo);
			//定位签到
			String LocationSql = "select remark from oa_location where user_name="+StrUtil.sqlstr(privilege.getUserName(skey))+" order by  create_date desc ";
	    	String locationInfo = getNewMessage(LocationSql);
	    	json.put("locationInfo", locationInfo);
			json.put("res", "0");
			json.put("msg", "操作成功");
			
			
			
			json.put("result", result);		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setResult(json.toString());
		return "SUCCESS";
	}
	
    public int getPlanCount(String userName){
    	String sql = "select count(id) from user_plan where is_closed=0 and userName="
			+ StrUtil.sqlstr(userName)
			+ " order by mydate desc, enddate desc";		
   	  	JdbcTemplate jt = new JdbcTemplate();
         ResultIterator ri = null;
         try {
             ri = jt.executeQuery(sql);
             if (ri.hasNext()) {
                 ResultRecord rr = (ResultRecord) ri.next();
                 return rr.getInt(1);
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
         }
         return 0;
   }
    
    public String getNewMessage(String sql){
    	JdbcTemplate jt = new JdbcTemplate();
    	ResultIterator ri = null;
    	String result = "";
    	try {
			ri = jt.executeQuery(sql);
			while(ri.hasNext()){
				ResultRecord rr = (ResultRecord)ri.next();
				return rr.getString(1);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
	
	
}
