package com.redmoon.oa.android.i;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.*;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.HtmlUtil;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Privilege;
import com.redmoon.oa.android.sales.SalesModuleDao;
import com.redmoon.oa.android.system.MobileAppIconConfigMgr;
import com.redmoon.oa.flow.Leaf;
import com.redmoon.oa.flow.WorkflowActionDb;
import com.redmoon.oa.flow.WorkflowDb;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.person.UserDb;


 /**
 * @Description: 获取信息列表接口
 * @author: lichao
 * @Date: 2015-7-15上午10:41:40
 */
public class GetInforListAction {
	private static int RES_SUCCESS = 0;                      //成功
	private static int RES_FAIL = -1;                        //失败
	private static int RES_EXPIRED = -2;                     //SKEY过期
	
	private static int RETURNCODE_SUCCESS = 0;               //获取成功
	private static int RETURNCODE_SUCCESS_NULL = -1;         //获取成功，但无数据
	
	private static int TYPE_FLOW = 1;                        //代办流程
	private static int TYPE_NOTICE = 2;                      //未读通知公告
	private static int TYPE_CHECK = 3;                       //注册审批，仅admin
	
	private String skey = "";
	private int type;
	private String headUrl = "";
	private String createdate = "";
	private String id = "";
	private String title = "";
	private int flowType;
	private String flowName = "";
	private String mobile = "";
	private String realName = "";
	private String result = "";
	
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
		boolean flag = true;
		JSONArray jArray = new JSONArray();
		JSONObject jReturn = new JSONObject();
		JSONObject jResult = new JSONObject();
		
		Privilege privilege = new Privilege();
		boolean re = privilege.Auth(skey);
		
		if(re){
			try {
				jReturn.put("res",RES_EXPIRED);
				jResult.put("returnCode", "");
				jReturn.put("result", jResult);
				
				setResult(jReturn.toString());
				return "SUCCESS";
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
/*		java.text.SimpleDateFormat f= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		 
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;*/
		
		try {
			String userName = privilege.getUserName(skey);
			
			//注册审批，仅admin获取
			/*
			String sql = "select * from users where isPass=0 order by regDate desc";
			String registerSql = "select count(id) from users where isPass=0 order by regDate desc";
			
			if("admin".equals(userName)){
				ri = jt.executeQuery( sql);
				while (ri.hasNext()) {
					rd = (ResultRecord) ri.next();
					type = TYPE_CHECK;
					id = rd.getString("name");
					headUrl = "images/mobileAppIcons/regAndPass.png";
					
					if(null != rd.getDate("regDate")){
						createdate = f.format(rd.getDate("regDate"));
					}

					mobile = rd.getString("mobile");
					realName = rd.getString("realName");
					
					JSONObject jObject = new JSONObject();
					jObject.put("type", type);
					jObject.put("id", id);
					jObject.put("headUrl", headUrl);
					jObject.put("createdate", createdate);
					jObject.put("mobile", mobile);
					jObject.put("realName", realName);
					jArray.put(jObject);
				}
			}

			//未读通知公告
			sql = "select b.id, b.create_date, b.title ,a.user_name from oa_notice_reply a, oa_notice b "
			    + "where a.notice_id = b.id and a.is_readed = 0 and a.user_name = ? order by b.create_date desc limit 10";
			String noticeSql = "select count(b.id) from oa_notice_reply a, oa_notice b "
			    + "where a.notice_id = b.id and a.is_readed = 0 and a.user_name = "+StrUtil.sqlstr(userName)+" order by b.create_date ";
			
			ri = jt.executeQuery(sql, new Object[] { userName });
			while (ri.hasNext()) {
				rd = (ResultRecord) ri.next();
				
				id = "";
				headUrl = "";
				createdate = "";
				
				type = TYPE_NOTICE;
				id = rd.getString("id");
				headUrl = "images/mobileAppIcons/notice.png";
				
				if(null != rd.getDate("create_date")){
					createdate = f.format(rd.getDate("create_date"));
				}

				title = rd.getString("title");

				JSONObject jObject = new JSONObject();
				jObject.put("type", type);
				jObject.put("id", id);
				jObject.put("headUrl", headUrl);
				jObject.put("createdate", createdate);
				jObject.put("title", title);
				jArray.put(jObject);
			}
			
			//代办流程
			sql = "select m.id,m.receive_date,f.type_code,f.title,m.user_name,m.action_status from flow_my_action m, flow f "
			    + "where m.flow_id=f.id and f.status<>-10  and f.status<>-11 and (user_name=? or proxy=?) and (is_checked=0 or is_checked=2) "
				+ "and sub_my_action_id=0 order by receive_date desc limit 10";
			
			String flowSql = 
				"select count(m.id) from flow_my_action m, flow f "
			    + "where m.flow_id=f.id and f.status<>-10 and f.status<>-11 and (user_name="+StrUtil.sqlstr(userName)+" or proxy="+StrUtil.sqlstr(userName)+") and (is_checked=0 or is_checked=2) "
				+ "and sub_my_action_id=0 order by receive_date desc";
			
			
			ri = jt.executeQuery(sql, new Object[] { userName, userName });
			while (ri.hasNext()) {
				rd = (ResultRecord) ri.next();

				id = "";
				headUrl = "";
				createdate = "";
				
				type = TYPE_FLOW;
				id = rd.getString("id");
				
				if(null != rd.getDate("receive_date")){
					createdate = f.format(rd.getDate("receive_date"));
				}
				
				String typeCode = rd.getString("type_code");
				Leaf lf = new Leaf();
				lf = lf.getLeaf(typeCode);
				if(lf!=null && lf.isLoaded()){
					flowType = lf.getType();
				}
				
				MobileAppIconConfigMgr mr = new MobileAppIconConfigMgr();
				headUrl = mr.getImgUrl(typeCode, 2);
				
				
				flowName = StringEscapeUtils.unescapeHtml(StrUtil.getNullStr(rd.getString("title")));
				
				int action_status = rd.getInt("action_status");
				if(action_status == WorkflowActionDb.RESULT_VALUE_RETURN){
					flowName+= "(被返回)";
				}
				String uName = rd.getString("user_name");
				UserDb ub = new UserDb(uName);
				realName = ub.getRealName();

				JSONObject jObject = new JSONObject();
				jObject.put("type", type);
				jObject.put("id", id);
				jObject.put("headUrl", headUrl);
				jObject.put("createdate", createdate);
				jObject.put("flowType", flowType);
				jObject.put("flowName", flowName);
				jObject.put("realName", realName);
				jArray.put(jObject);
			}
			*/

			jReturn.put("res", RES_SUCCESS);
			
			jResult.put("returnCode", RETURNCODE_SUCCESS);	
			jResult.put("datas", jArray);
			// 20180418 fgf 将消息从原来的聚合待办流程、通知公告改为新消息，包括系统消息和内部邮件
			String sql = "select count(id) from oa_message where is_sent=1 and receiver="+StrUtil.sqlstr(privilege.getUserName())+" and box=" + MessageDb.INBOX + " and is_dustbin=0 and isreaded=0";				
			int unReadCount = SalesModuleDao.getCountInfoById(sql);
			
			/*
			if(userName.equals("admin")){
				unReadCount += SalesModuleDao.getCountInfoById(registerSql);
			}
			unReadCount += SalesModuleDao.getCountInfoById(noticeSql);
			unReadCount += SalesModuleDao.getCountInfoById(flowSql);
			*/
			jResult.put("unReadCount", unReadCount);

			jReturn.put("result", jResult);
			
			// System.out.println(jResult + " sql=" + sql);
		} catch (JSONException e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
			LogUtil.getLog(getClass()).error(StrUtil.trace(e));
		} finally {
			if(!flag){
				try {
					jReturn.put("res", RES_FAIL);
					jResult.put("returnCode", "");
					jReturn.put("result", jResult);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		
		setResult(jReturn.toString());
		return "SUCCESS";
	}
	
	
}
