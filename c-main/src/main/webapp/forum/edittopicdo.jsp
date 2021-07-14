<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.util.*"
%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.forum.person.UserSet"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil" />
<jsp:useBean id="Topic" scope="page" class="com.redmoon.forum.MsgMgr" />
<%
String editFlag = ParamUtil.get(request, "editFlag");
String op = ParamUtil.get(request, "op");
if (op.equals("changeattachname")) {
	int msgId = ParamUtil.getInt(request, "msgId");
	int attach_id = ParamUtil.getInt(request, "attach_id");
	String newname = ParamUtil.get(request, "newname");
	String newDesc = ParamUtil.get(request, "newDesc");
	MsgDb msgDb = new MsgDb();
	msgDb = msgDb.getMsgDb(msgId);
	boolean re = msgDb.updateAttachmentName(attach_id, newname, newDesc);
	
	if (re) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_success")));
		%>
		<script>
		// window.parent.location.reload(true);
		</script>
		<%
	}
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	
	return;
}

if (op.equals("delAttach")) {
	int msgId = ParamUtil.getInt(request, "msgId");
	int attach_id = ParamUtil.getInt(request, "attach_id");
	MsgDb msgDb = new MsgDb();
	msgDb = msgDb.getMsgDb(msgId);
	boolean re = msgDb.delAttachment(attach_id);
	if (re) {
		%>
		<script>
		if (window.confirm("<%=SkinUtil.LoadString(request, "res.label.forum.deltopic", "del_attach_success")%>"))
			window.parent.location.reload(true);
		</script>
		<%
	}
	else
		out.print(StrUtil.Alert(SkinUtil.LoadString(request, "info_op_fail")));
	return;
}

boolean isSuccess = false;
String privurl = "", boardcode="";
try {
	isSuccess = Topic.editTopic(application, request);
	privurl = Topic.getprivurl();
	boardcode = Topic.getCurBoardCode();
}
catch (ErrMsgException e) {
	out.println(StrUtil.Alert_Back(e.getMessage()));
	return;
}
if (Topic.isBlog() && editFlag.equals("blog")) {
	long editid = Long.parseLong(Topic.getFileUpload().getFieldValue("editid"));
	MsgDb msgDb = Topic.getMsgDb(editid);
	if (msgDb.getIsWebedit()==MsgDb.WEBEDIT_REDMOON)
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "edittopic_we.jsp?editFlag=blog&boardcode=" + boardcode + "&editid=" + editid));	
	else if (msgDb.getIsWebedit()==MsgDb.WEBEDIT_UBB)
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "edittopic.jsp?editFlag=blog&boardcode=" + boardcode + "&editid=" + editid));	
	else	
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "edittopic_new.jsp?editFlag=blog&boardcode=" + boardcode + "&editid=" + editid));	
	return;
}
// 取得皮肤路径
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.forum.edittopic" key="edittopic"/> - <%=Global.AppName%></title>
<%@ include file="../inc/nocache.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<%if (editFlag.equals("")) {%>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<%}%>
<%
if (isSuccess) {
%><br>
<ol><%=SkinUtil.LoadString(request, "info_op_success")%></ol>
<%
out.println(StrUtil.waitJump("<a href='"+privurl+"'>" + SkinUtil.LoadString(request, "res.label.forum.deltopic", "go_back") + "</a>",3,privurl));
}
else
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
%>
<%if (editFlag.equals("")) {%>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
<%}%>
</body>
</html>


