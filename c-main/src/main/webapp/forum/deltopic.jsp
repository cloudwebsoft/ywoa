<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import="com.redmoon.forum.*"
import="cn.js.fan.util.*"
%>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="cn.js.fan.util.ErrMsgException" %>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);

String delid = request.getParameter("delid");
if (delid==null || !StrUtil.isNumeric(delid)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
	return;
}
long id = Long.parseLong(delid);
MsgMgr msgMgr = new MsgMgr();
MsgDb md = msgMgr.getMsgDb(id);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.forum.deltopic" key="deltopic"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<%
String delFlag = ParamUtil.get(request, "delFlag");
if (delFlag.equals("") && !md.isBlog()) {%>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%}%>
<%
String privurl = ParamUtil.get(request, "privurl");
String boardcode = StrUtil.getNullString(request.getParameter("boardcode"));
try {
boolean re = msgMgr.delTopic(application, request, id);
if (re) {
	if (md.isBlog()) {
		if (privurl.equals(""))
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "../blog/user/listtopic.jsp?blogId=" + md.getBlogId()));	
		else
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), privurl));	
		return;
	}
	else {
%>
		<BR><ol><lt:Label res="res.label.forum.deltopic" key="del_success"/></ol>
<%
		if (md.getReplyid()!=-1) {
			out.println(StrUtil.waitJump("<a href='"+privurl+"'>" + SkinUtil.LoadString(request, "res.label.forum.deltopic", "go_back") + "</a>",3,privurl));
			out.println("<ol><a href='listtopic.jsp?boardcode=" + StrUtil.UrlEncode(md.getboardcode()) + "'>" + SkinUtil.LoadString(request, "res.label.forum.deltopic", "back_to_bard") + "</a></ol>");
		}
		else {
			if (delFlag.equals("")) {
				privurl = "listtopic.jsp?boardcode=" + StrUtil.UrlEncode(md.getboardcode());
				out.println(StrUtil.waitJump("<a href='listtopic.jsp?boardcode=" + StrUtil.UrlEncode(md.getboardcode()) + "'>" + SkinUtil.LoadString(request, "res.label.forum.deltopic", "back_to_bard") + "</a>",3,privurl));
			}
			else {
				out.println(StrUtil.waitJump("<a href='"+privurl+"'>" + SkinUtil.LoadString(request, "res.label.forum.deltopic", "go_back") + "</a>",3,privurl));
			}
		}
	}
} else {%>
<p align=center><lt:Label key="info_op_fail"/></p>
<%}
}
catch (ErrMsgException e) {
	// out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	response.sendRedirect("../info.jsp?info=" + StrUtil.UrlEncode(e.getMessage()));
	return;
}
%>
<%if (delFlag.equals("") && !md.isBlog()) {%>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
<%}%>
</body>
</html>


