<%@ page contentType="text/html;charset=utf-8"
import = "java.io.File"
import = "cn.js.fan.web.SkinUtil"
import = "cn.js.fan.util.ErrMsgException"
import = "com.redmoon.forum.*"
import = "com.redmoon.forum.Leaf"
import = "java.util.Calendar"
%><jsp:useBean id="form" scope="page" class="cn.js.fan.security.Form" /><%
boolean isSuccess = false;
String privurl = "";
String boardcode = "";

boolean cansubmit = false;
com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
int interval = cfg.getIntProperty("forum.addMsgInterval");
int maxtimespan = interval;
try {
	cansubmit = form.cansubmit(request, "addtopic", maxtimespan);// 防止重复刷新	
}
catch (ErrMsgException e) {
	out.println(e.getMessage());
	return;
}

if (cansubmit) {
	MsgMgr Topic = new MsgMgr();
	try {
		isSuccess = Topic.AddNewWE(application, request);
		//privurl = Topic.getprivurl();
	}
	catch (ErrMsgException e) {
		out.print("-" + SkinUtil.LoadString(request, "info_op_fail") + e.getMessage());
	}
	if (isSuccess) {
		boardcode = Topic.getCurBoardCode();
		MsgDb md = Topic.getMsgDb(Topic.getId());
		if (md.getCheckStatus()==MsgDb.CHECK_STATUS_NOT) {	
			out.println(SkinUtil.LoadString(request, "res.label.forum.addtopic", "need_check"));
		}
		else
			out.print("+" + SkinUtil.LoadString(request, "info_op_success"));
	}
}
%>

