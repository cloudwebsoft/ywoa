<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
%>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.forum.manager" key="toptic_mgr"/></title>
<%@ include file="../inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="wrapper">
<%@ include file="../inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="topic" scope="page" class="com.redmoon.forum.MsgMgr"/>
<%
String privurl = request.getParameter("privurl");
String action = StrUtil.getNullString(request.getParameter("action"));
boolean re = false;
try {
	if (action.equals("setOnTop")) {
		long id = ParamUtil.getLong(request, "id");
		int value = ParamUtil.getInt(request, "value");
		re = topic.setOnTop(request,id,value);
	}
	else if (action.equals("setLocked")) {
		long id = ParamUtil.getLong(request, "id");
		int value = ParamUtil.getInt(request, "value");	
		re = topic.setLocked(request,id,value);
	}
	else if (action.equals("setElite")) {
		long id = ParamUtil.getLong(request, "id");
		int value = ParamUtil.getInt(request, "value");	
		re = topic.setElite(request, id, value);
	}
	else if (action.equals("delBatch")) {
		String strIds = ParamUtil.get(request, "ids");
		String[] idsary = StrUtil.split(strIds, ",");
		if (idsary!=null) {
			int len = idsary.length;
			MsgMgr mm = new MsgMgr();
			for (int i=0; i<len; i++) {
				mm.delTopic(application, request, Long.parseLong(idsary[i]));
			}
			re = true;
		}
	}
	else if (action.equals("setEliteBatch")) {
		String strIds = ParamUtil.get(request, "ids");
		String[] idsary = StrUtil.split(strIds, ",");
		if (idsary!=null) {
			int len = idsary.length;
			MsgMgr mm = new MsgMgr();
			for (int i=0; i<len; i++) {
				re = topic.setElite(request, StrUtil.toLong(idsary[i]), 1);
			}
		}
	}
	else if (action.equals("setUnEliteBatch")) {
		String strIds = ParamUtil.get(request, "ids");
		String[] idsary = StrUtil.split(strIds, ",");
		if (idsary!=null) {
			int len = idsary.length;
			MsgMgr mm = new MsgMgr();
			for (int i=0; i<len; i++) {
				re = topic.setElite(request, StrUtil.toLong(idsary[i]), 0);
			}
		}
	}	
	else if (action.equals("setLockBatch")) {
		String strIds = ParamUtil.get(request, "ids");
		String[] idsary = StrUtil.split(strIds, ",");
		if (idsary!=null) {
			int len = idsary.length;
			MsgMgr mm = new MsgMgr();
			for (int i=0; i<len; i++) {
				re = topic.setLocked(request, StrUtil.toLong(idsary[i]), 1);
			}
		}
	}
	else if (action.equals("setUnLockBatch")) {
		String strIds = ParamUtil.get(request, "ids");
		String[] idsary = StrUtil.split(strIds, ",");
		if (idsary!=null) {
			int len = idsary.length;
			MsgMgr mm = new MsgMgr();
			for (int i=0; i<len; i++) {
				re = topic.setLocked(request, StrUtil.toLong(idsary[i]), 0);
			}
		}
	}	
	else if (action.equals("setOnTopBatch")) {
		String strIds = ParamUtil.get(request, "ids");
		String[] idsary = StrUtil.split(strIds, ",");
		if (idsary!=null) {
			int len = idsary.length;
			MsgMgr mm = new MsgMgr();
			for (int i=0; i<len; i++) {
				re = topic.setOnTop(request, StrUtil.toLong(idsary[i]), MsgDb.LEVEL_TOP_BOARD);
			}
		}
	}
	else if (action.equals("setUnOnTopBatch")) {
		String strIds = ParamUtil.get(request, "ids");
		String[] idsary = StrUtil.split(strIds, ",");
		if (idsary!=null) {
			int len = idsary.length;
			MsgMgr mm = new MsgMgr();
			for (int i=0; i<len; i++) {
				re = topic.setOnTop(request, StrUtil.toLong(idsary[i]), MsgDb.LEVEL_NONE);
			}
		}
	}	
	//if (action.equals("setGuide"))
	//	re = topic.setGuide(request,id,value);
	if (re) {
	%>
	<ol><%=SkinUtil.LoadString(request,"info_operate_success")%></ol>
	<%
	out.println(StrUtil.waitJump("<a href='"+privurl+"'>" + SkinUtil.LoadString(request,"res.label.forum.manager","back_to_front_page") + "</a>",3,privurl));
	} else {%>
	<p align=center><%=SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request,"info_operate_fail"))%></p>
	<%}
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
}
%><br>
</div>
<%@ include file="../inc/footer.jsp"%>
</div>
</body>
</html>


