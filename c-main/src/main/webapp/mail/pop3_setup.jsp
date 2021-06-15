<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="userservice" scope="page" class="com.redmoon.oa.person.UserService"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>配置邮箱</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
    <script src="../js/jquery-1.9.1.min.js"></script>
    <script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script language=javascript>
function form1_onsubmit() {
	if (form1.email.value=="")
	{
		jAlert("邮箱不能为空！","提示");
		return false;
	}
}
function isDel(id,email){
	jConfirm("您确定要删除么？","提示",function(r){
		if(!r){return;}
		else{
			window.location.href="pop3_setup_do.jsp?op=del&id="+id+"&email="+email;
		}
	})
}
</script>
<style>
	.loading{
	display: none;
	position: fixed;
	z-index:1801;
	top: 45%;
	left: 45%;
	width: 100%;
	margin: auto;
	height: 100%;
	}
	.SD_overlayBG2 {
	background: #FFFFFF;
	filter: alpha(opacity = 20);
	-moz-opacity: 0.20;
	opacity: 0.20;
	z-index: 1500;
	}
	.treeBackground {
	display: none;
	position: absolute;
	top: -2%;
	left: 0%;
	width: 100%;
	margin: auto;
	height: 200%;
	background-color: #EEEEEE;
	z-index: 1800;
	-moz-opacity: 0.8;
	opacity: .80;
	filter: alpha(opacity = 80);
	}
</style>
<body>
<div id="treeBackground" class="treeBackground"></div>
<div id='loading' class='loading'><img src='../images/loading.gif'></div>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tr>
    <td class="tdStyle_1">POP3邮箱设置</td>
  </tr>
</table>
<%
	String op = fchar.getNullString(request.getParameter("op"));
	if (op.equals("add")) {
		boolean re = false;
		EmailPop3Mgr epm = new EmailPop3Mgr();
		try {%>
			<script>
				$(".treeBackground").addClass("SD_overlayBG2");
				$(".treeBackground").css({"display":"block"});
				$(".loading").css({"display":"block"});
			</script>
			<%
			re = epm.create(request);
		}
		catch (ErrMsgException e) {
			out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
			return;
		}
		if (re) {
			// out.print(StrUtil.Alert_Redirect("操作成功！", "in_box.jsp?id=" + emp.getEmailPop3Db().getId()));
			%>
			<script>
			$(".loading").css({"display":"none"});
			$(".treeBackground").css({"display":"none"});
			$(".treeBackground").removeClass("SD_overlayBG2");
			parent.location.href = "mail_frame.jsp";
			</script>
			<%
			 out.print(StrUtil.Alert_Redirect("操作成功！", "mail_frame.jsp"));
		}
		return;
	}

	String sql;
	String myname = privilege.getUser(request);
	sql = "select id from email_pop3 where userName="+fchar.sqlstr(myname);
			
	EmailPop3Db epd = new EmailPop3Db();
	Iterator ir = epd.list(sql).iterator();
		
	int i = 0;
	String email = "",email_user="",email_pwd="",mailserver="";
	int id,port;

	while (ir.hasNext()) {
	epd = (EmailPop3Db)ir.next();
	i++;
	email = epd.getEmail();
	email_user = epd.getEmailUser();
	email_pwd = epd.getEmailPwd();
	id = epd.getId();
	mailserver = epd.getServer();
	port = epd.getPort();
%>
<form id=formadd<%=i%> action="pop3_setup_do.jsp?op=edit" method=post>
  <table class="tabStyle_1 percent98">
    <tr>
      <td colspan=2 class="tabStyle_1_title">邮箱设置</td>
    </tr>
    <tr>
      <td align="left">邮&nbsp;&nbsp;&nbsp; 箱：</td>
      <td align="left"><input name=email value="<%=email%>" size="25" style="width:160px">
        <input type="hidden" name="id" value="<%=id%>"></td>
    </tr>
    <tr align="center">
      <td width="19%" align="left">用户名：</td>
      <td width="81%" align="left"><input name=emailUser style="width:160px" id="emailUser" value="<%=email_user%>" size=25>      </td>
    </tr>
    <tr>
      <td align="left">密&nbsp;&nbsp;&nbsp; 码：</td>
      <td align="left"><input name=emailPwd type=password style="width:160px" id="emailPwd" value="<%=email_pwd%>" size=25></td>
    </tr>
    <tr align="center">
      <td align="left">SMTP服务器：</td>
      <td align="left"><input name=server id="server" style="width:160px" value="<%=mailserver%>" size=25></td>
    </tr>
    <tr>
      <td align="left">SMTP端口：</td>
      <td align="left"><input name=smtpPort style="width:160px" value="<%=epd.getSmtpPort()%>" size=25></td>
    </tr>
    <tr>
      <td align="left">POP3服务器：</td>
      <td align="left"><input name=serverPop3 style="width:160px" id="serverPop3" value="<%=epd.getServerPop3()%>" size=25></td>
    </tr>
    <tr>
      <td align="left">POP3端口：</td>
      <td align="left"><input name=port style="width:160px" value="<%=port%>" size=25></td>
    </tr>
    <tr>
      <td align="left">使用SSL</td>
      <td align="left"><input type="checkbox" id="isSsl" name="isSsl" value="1" <%=epd.isSsl()?"checked":""%> /></td>
    </tr>
    <tr>
      <td colspan="2" align="left"><input name="isDelete" value="1" type="checkbox" <%=epd.isDelete()?"checked":""%> />
        接收邮件时删除服务器上邮件</td>
    </tr>
    <tr>
      <td align="left">是否为默认邮箱</td>
      <td align="left"><input type="checkbox" id="isDefault" name="isDefault" value="1" <%=epd.isDefault()?"checked":""%> /></td>
    </tr>
    <tr>
      <td colspan="2" align="center"><input name="Submit3" type="submit" value="确定">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <input name="Submit4" type="button" value="删除" onClick="isDel(<%=id%>,<%=email%>)" ></td>
    </tr>
  </table>
</form>
<%
}
%>
<br>
<form id=form1 name="form1" action="?op=add" method="post" onSubmit="return form1_onsubmit()">
  <table class="tabStyle_1 percent98">
    <tr>
      <td colspan="2" class="tabStyle_1_title">增加邮箱</td>
    </tr>
    <tr>
      <td align="left">邮&nbsp;&nbsp;&nbsp;&nbsp;箱：</td>
      <td><input style="width:160px" type=text size="20" name="email"></td>
    </tr>
    <tr>
      <td width="19%" align="left">用户名：</td>
      <td width="81%"><input style="width:160px" name="emailUser" type=text id="emailUser" size="20">      </td>
    </tr>
    <tr>
      <td align="left">密&nbsp;&nbsp;&nbsp;&nbsp;码：</td>
      <td><input style="width:160px" name="emailPwd" type=password id="emailPwd" size="20"></td>
    </tr>
    <tr>
      <td align="left">SMTP服务器：</td>
      <td><input style="width:160px" name="server" type=text id="server" size="20"></td>
    </tr>
    <tr>
      <td align="left">SMTP端&nbsp;口：</td>
      <td><input style="width:160px" type=text size="20" name="smtpPort" value="25"></td>
    </tr>
    <tr>
      <td align="left">POP3服务器：</td>
      <td align="left"><input style="width:160px" name=serverPop3 id="serverPop3" size=20></td>
    </tr>
    <tr>
      <td align="left">POP3端口：</td>
      <td><input style="width:160px" type=text size="20" name="port" value="110"></td>
    </tr>
    <tr>
      <td align="left">使用SSL</td>
      <td>
      <input type="checkbox" id="isSsl" name="isSsl" value="1" />
      </td>
    </tr>
    <tr>
      <td colspan="2" align="left"><input name="isDelete" value="1" type="checkbox" />接收邮件时删除服务器上邮件</td>
    </tr>
    <tr>
      <td align="left">是否为默认邮箱</td>
      <td align="left"><input type="checkbox" id="isDefault" name="isDefault" value="1" <%=epd.isDefault()?"checked":""%> /></td>
    </tr>
    <tr>
      <td colspan="2" align="center"><input name="Submit" type="submit" value="确定">
        &nbsp;&nbsp;&nbsp;&nbsp;
        <input name="Submit2" type="reset" value="重置">      </td>
    </tr>
  </table>
</form>
<br>
</body>
</html>
