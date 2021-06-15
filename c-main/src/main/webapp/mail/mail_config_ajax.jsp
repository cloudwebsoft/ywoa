<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="com.redmoon.oa.emailpop3.pop3.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="java.util.*"%>
<%@ page import="javax.mail.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="userservice" scope="page" class="com.redmoon.oa.person.UserService"/><jsp:useBean id="fnumber" scope="page" class="cn.js.fan.util.NumberUtil"/><jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
	if (!privilege.isUserLogin(request)) {
		out.println(SkinUtil.makeInfo(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	int id = -1;
	try {
		id = ParamUtil.getInt(request, "id");
	} catch (ErrMsgException e) {
		out.print(SkinUtil.makeInfo(request, "请选择邮箱！"));
		return;
	}
	EmailPop3Db epd = new EmailPop3Db();
	epd = epd.getEmailPop3Db(id);
	if (!epd.getUserName().equals(privilege.getUser(request))) {
		out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	String email = epd.getEmail();
	String email_user = epd.getEmailUser();
	String email_pwd = epd.getEmailPwd();
	String mailserver = epd.getServer();
	int port = epd.getPort();
	
	boolean re = false;
	String op = ParamUtil.get(request, "op");
	if (op.equals("edit")) {
		try {
			EmailPop3Mgr epm = new EmailPop3Mgr();
			re = epm.modify(request);
		}
		catch (ErrMsgException e) {
			out.print("修改失败！");
			return;
		}
		if(re) {
			out.print("修改成功！");
			return;
		} else {
			out.print("修改失败！");
			return;
		}
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>邮箱配置</title>
</head>
<body>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td class="confOption">邮&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;箱：</td>
    <td><input class="inputA" name="email" id="email" value="<%=email%>"><input id="emailId" type="hidden" name="emailId" value="<%=id%>"></td>
  </tr>
  <tr>
    <td class="confOption">用&nbsp;&nbsp;户&nbsp;&nbsp;名：</td>
    <td><input class="inputA" name="emailUser" id="emailUser" value="<%=email_user%>" />
    </td>
  </tr>
  <tr>
    <td class="confOption">密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码：</td>
    <td><input class="inputA" name="emailPwd" type=password id="emailPwd" value="<%=email_pwd%>" /></td>
  </tr>
  <tr>
    <td class="confOption">SMTP服务器：</td>
    <td><input class="inputA" name="server" id="server" value="<%=mailserver%>" /></td>
  </tr>
  <tr>
    <td class="confOption">SMTP端口：</td>
    <td><input class="inputA" name="smtpPort" id="smtpPort" value="<%=epd.getSmtpPort()%>" /></td>
  </tr>
  <tr>
    <td class="confOption">POP3服务器：</td>
    <td><input class="inputA" name="serverPop3" id="serverPop3" value="<%=epd.getServerPop3()%>" /></td>
  </tr>
  <tr>
    <td class="confOption">POP3端口：</td>
    <td><input class="inputA" name="port" id="port" value="<%=port%>" /></td>
  </tr>
  <tr>
    <td class="confOption">使用SSL</td>
    <td><input type="checkbox" id="isSsl" name="isSsl" value="1" <%=epd.isSsl()?"checked":""%> /></td>
  </tr>  
  <tr>
    <td colspan="2">
	  接收邮件时删除服务器上邮件：
	  <select name="isDelete" id="isDelete">
<%
	if(epd.isDelete()) {
%>
	  	<option selected="selected" value="1">是</option>
		<option value="0">否</option>
<%
	} else {
%>
		<option value="1">是</option>
		<option selected="selected" value="0">否</option>
<%
	}
%>
	  </select>
	</td>
  </tr>
  <tr>
    <td class="confOption">是否为默认邮箱</td>
    <td><input type="checkbox" id="isDefault" name="isDefault" value="1" <%=epd.isDefault()?"checked":""%> /></td>
  </tr> 
  <tr>
    <td colspan="2" align="center">
	  <input name="ok" class="inputB" type="button" value="确定" onclick="editMailConf()" />&nbsp;&nbsp;&nbsp;&nbsp;
	  <input name="close" class="inputB" type="button" value="取消" onclick="o('confMail').style.display='none'" />
	</td>
  </tr>
</table>
</body>
</html>
