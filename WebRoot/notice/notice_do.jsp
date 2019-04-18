<%@ page contentType="text/html;charset=utf-8" import = "java.io.File" import = "cn.js.fan.util.*"%><%@ page import="com.redmoon.oa.person.*"%><%@ page import="cn.js.fan.web.*"%><%@page import="java.util.*"%><%@page import="com.redmoon.oa.dept.DeptDb"%><%@page import="com.redmoon.oa.dept.DeptUserDb"%><%@page import="com.redmoon.oa.sso.SyncUtil"%><%@page import="com.redmoon.oa.pvg.Privilege"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="com.redmoon.oa.notice.NoticeReplyDb"%>
<%@page import="com.redmoon.oa.notice.NoticeDb"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%

String op = ParamUtil.get(request, "op");
if (op.equals("addReply")) {
	String userName = privilege.getUser(request);
	String content = ParamUtil.get(request,"content");
	long noticeId = ParamUtil.getLong(request,"noticeId",0);
	NoticeReplyDb noticeReplyDb = new NoticeReplyDb();
	noticeReplyDb.setUsername(userName);
	noticeReplyDb.setNoticeid(noticeId);
	noticeReplyDb.setContent(content);
	boolean flag = noticeReplyDb.save();
	JSONObject json = new JSONObject();
	
	if(flag){
		java.util.Date rDate = new java.util.Date();
		NoticeDb noticeDb = new NoticeDb();
		noticeDb = noticeDb.getNoticeDb(noticeId);
		if(noticeDb.getIs_forced_response() == 1){
			NoticeReplyDb nrdb = new NoticeReplyDb();
			nrdb.setIsReaded("1");
			nrdb.setReadTime(rDate);
			nrdb.setNoticeid(noticeId);
			nrdb.setUsername(userName);
			nrdb.saveStatus();
		}
		json.put("res",0);
	}else{
		json.put("res",1);
	}

	out.print(json.toString());
}
%>
