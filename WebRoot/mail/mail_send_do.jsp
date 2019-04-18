<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
MailMsgMgr mmm = new MailMsgMgr();

int id = ParamUtil.getInt(request, "id");
MailMsgDb mmd = null;
boolean re = false;
SendMail sm = new SendMail();
int oldType = -1;
try {
	mmd = mmm.getMailMsgDb(request, id);
	oldType = mmd.getType();
	sm.getMailInfo(request, mmd);
	re = sm.send();
	if (re) {
    	mmd.setType(MailMsgDb.TYPE_SENDED);
		mmd.save();
	}
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
if (re) {
	if (oldType==MailMsgDb.TYPE_DRAFT) {
		EmailPop3Db epd = new EmailPop3Db();
		epd = epd.getEmailPop3DbByEmail(mmd.getEmailAddr());
		out.print(StrUtil.Alert_Redirect("操作成功！", "list_box.jsp?id=" + epd.getId() + "&box=0"));
	}
	else {
		out.print(StrUtil.Alert_Back("操作成功！"));
	}
	return;
}
else {
	out.print(StrUtil.Alert_Back("操作失败！原因：" + sm.geterrinfo()));
	return;
}
%>