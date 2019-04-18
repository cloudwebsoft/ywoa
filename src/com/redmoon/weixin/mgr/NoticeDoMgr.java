package com.redmoon.weixin.mgr;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.struts2.ServletActionContext;

import cn.js.fan.db.ListResult;
import cn.js.fan.db.SQLFilter;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.HtmlUtil;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.android.Constant;
import com.redmoon.oa.notice.NoticeDb;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @Description: 
 * @author: 
 * @Date: 2016-8-12下午04:44:06
 */
public class NoticeDoMgr {
	private final int PAGE_SIZE = 10;
	public JSONObject noticeList(String userId,int curPage,boolean isNoticeAll,boolean isNoticeMgr){
		HttpServletRequest request = ServletActionContext.getRequest();
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("res",0);
		StringBuilder sqlSb = new StringBuilder();
		String curDay = DateUtil.format(new java.util.Date(), "yyyy-MM-dd");
		if(isNoticeAll) {
			sqlSb.append("select id from oa_notice o where 1=1");
		}else if(isNoticeMgr){
			sqlSb.append( "select o.id from oa_notice o,oa_notice_reply r where o.id = r.notice_id and r.user_name = "+StrUtil.sqlstr(userId));
		}else{
			sqlSb.append("select o.id from oa_notice o,oa_notice_reply r where o.id = r.notice_id and r.user_name = ").append(StrUtil.sqlstr(userId)).append(" and o.begin_date<=");
			sqlSb.append( SQLFilter.getDateStr(curDay, "yyyy-MM-dd")).append(" and (o.end_date is null or o.end_date>=").append(SQLFilter.getDateStr(curDay, "yyyy-MM-dd")).append(")");
		}
        sqlSb.append(" order by id desc");
        NoticeDb nd = new NoticeDb();
		try {
			ListResult lr = nd.listResult(sqlSb.toString(), curPage, PAGE_SIZE);
			Vector vt = lr.getResult();
			Iterator ri = vt.iterator();
			JSONArray arr = new JSONArray();
			while(ri.hasNext()){
				JSONObject noticeObj = new JSONObject();
				NoticeDb noticeDb = (NoticeDb) ri.next();
				long id = noticeDb.getId();
				String title = noticeDb.getTitle();
				String content = noticeDb.getContent();
				String createDate = DateUtil.format(noticeDb.getCreateDate(),DateUtil.DATE_FORMAT);
				noticeObj.put("id", id);
				noticeObj.put("title", title);
				noticeObj.put("content",StrUtil.getAbstract(request,content, 50));
				noticeObj.put("createDate", createDate);
				arr.add(noticeObj);
			}
			jsonObj.put("datas", arr);
		} catch (ErrMsgException e) {
			// TODO Auto-generated catch block
			jsonObj.put("res", -1);
			LogUtil.getLog(NoticeDoMgr.class).error(e.getMessage());
		}
		System.out.println(jsonObj.toString());
        
		
		return jsonObj;
		
	}

}
