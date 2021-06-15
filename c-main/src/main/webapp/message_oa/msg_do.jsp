<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ErrMsgException"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.idiofileark.*" %>
<%@ page import="com.redmoon.oa.message.*" %>
<LINK href="../common.css" type=text/css rel=stylesheet>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserLogin(request))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<%
IdiofilearkMgr imgr = new IdiofilearkMgr();

boolean isSuccess = false;
String op = ParamUtil.get(request, "op");
String outInfo = "";
int box = ParamUtil.getInt(request, "box", -1);
if (op.equals("")) {
	outInfo = "消息删除成功！";
	try {
		isSuccess = Msg.delMsgBySenderDustbin(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert_Back("消息删除失败："+e.getMessage()));
	}
}
if (op.equals("transmitToIdiofileark")) {
	outInfo = "消息转存至文件柜成功！";
	try {
		isSuccess = imgr.TransmitMsgToidiofileark(request);
	}
	catch (ErrMsgException e) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "消息转存至文件柜失败："+e.getMessage()));
	}
}
if (isSuccess) {
	if (box==MessageDb.DRAFT)
		out.print(StrUtil.Alert_Redirect(outInfo, "listdraft.jsp"));
	else if (box==MessageDb.OUTBOX)
		out.print(StrUtil.Alert_Redirect(outInfo, "listoutbox.jsp"));		
	else
		out.print(StrUtil.Alert_Redirect(outInfo, "message.jsp"));
	return;
}
%>
