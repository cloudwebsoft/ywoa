<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.web.*"
import = "cn.js.fan.util.ErrMsgException"
import="com.redmoon.forum.*"
%>
<%@ page import="cn.js.fan.web.Global" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="com.redmoon.forum.person.UserSet"%><jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil" /><jsp:useBean id="form" scope="page" class="cn.js.fan.security.Form" /><%
boolean cansubmit = false;
com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
int interval = cfg.getIntProperty("forum.addMsgInterval");
int maxtimespan = interval;
try {
	cansubmit = form.cansubmit(request,"addtopic", maxtimespan);// 防止重复刷新	
}
catch (ErrMsgException e) {
	out.println(e.getMessage());
	return;
}

boolean isSuccess = false;
String privurl = "";
String boardcode = "";
MsgDb replyMsgDb = null;
try {
	MsgMgr msgMgr = new MsgMgr();
	isSuccess = msgMgr.AddReplyWE(application, request);
	privurl = msgMgr.getprivurl();
	boardcode = msgMgr.getCurBoardCode();
	replyMsgDb = msgMgr.getMsgDb(msgMgr.getId());	
}
catch (ErrMsgException e) {
	out.print("-" + SkinUtil.LoadString(request, "info_op_fail") + " "+e.getMessage());
	return;
}

if (replyMsgDb.getCheckStatus()==MsgDb.CHECK_STATUS_NOT) {
	out.println(SkinUtil.LoadString(request, "res.label.forum.addtopic", "need_check"));
}
else
	out.print("+" + SkinUtil.LoadString(request, "info_op_success"));
%>


