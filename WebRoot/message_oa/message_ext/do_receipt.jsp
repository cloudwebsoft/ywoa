<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<%
	if (!privilege.isUserLogin(request)) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	int id = ParamUtil.getInt(request, "id");
	MessageDb md = Msg.getMessageDb(id);
	if (md==null || !md.isLoaded()) {
		out.print(StrUtil.Alert_Redirect("该消息已不存在！", "message.jsp"));
		return;
	}
	md.setReceiptState(MessageDb.RECEIPT_RETURNED);
	md.save();
	try {
		md.sendSysMsg(md.getSender(), "消息回执：" + md.getTitle(), "用户" + md.getReceiver() + "已经阅读了您的消息！");
		out.println(StrUtil.Alert_Redirect("回执发送成功！", "showmsg.jsp?id=" + id));
	} catch (ErrMsgException e) {
		out.println(StrUtil.Alert_Back("回执发送失败！"));
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>发送回执</title>
</head>

<body>
</body>
</html>