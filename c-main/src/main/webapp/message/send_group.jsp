<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<LINK href="../common.css" type=text/css rel=stylesheet>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<html>
<head>
<title>撰写群发消息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<script language=javascript>
<!--
function form1_onsubmit()
{
	errmsg = "";
	if (!form1.isToAll.checked)
		if (form1.receiver.value=="")
			errmsg += "请填写接收者！\n"
	if (form1.title.value=="")
		errmsg += "请填写标题！\n"
	if (form1.content.value=="")
		errmsg += "请填写内容！\n"
	if (errmsg!="")
	{
		alert(errmsg);
		return false;
	}
}
//-->
</script>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table width="320" border="0" cellspacing="1" cellpadding="3" align="center" bgcolor="#99CCFF" class="9black" height="260">
  <form name="form1" method="post" action="send_do.jsp" onSubmit="return form1_onsubmit()">
  <tr> 
    <td bgcolor="#CEE7FF" height="23">
        <div align="center"> <b>撰 写 群 发 消 息</b></div>
    </td>
  </tr>
  <tr> 
    <td bgcolor="#FFFFFF" height="50"> 
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
          <tr> 
            <td width="75"> 
              <div align="center"><a href="message.jsp?page=1"><img src="images/inboxpm.gif" width="40" height="40" border="0"></a></div>
            </td>
            <td width="75"> 
              <div align="center"><a href="mysend.jsp"><img src="images/m_outbox.gif" width="40" height="40" border="0"></a></div>
            </td>
            <td width="75"> 
              <div align="center"><img src="images/newpm.gif" width="40" height="40" border="0"></div>
            </td>
            <td width="75"> 
              <div align="center"> <img src="images/m_delete.gif" width="40" height="40"></div>
            </td>
          </tr>
        </table>
    </td>
  </tr>
  <tr> 
      <td bgcolor="#FFFFFF" height="152" valign="top">
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black" height="6">
          <tr> 
            <td></td>
          </tr>
        </table>
        <table width="300" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
          <tr> 
            <td width="68" height="27"> 
              <div align="center">接 收 者：</div>
            </td>
            <td width="217" height="27">
              <input type="text" name="receiver" class="input1" size="20" maxlength="20">
			  <%
			  com.redmoon.forum.Config cfg =  com.redmoon.forum.Config.getInstance();
			  String sender = cfg.getProperty("message_sender");
			  %>
			  <input type="hidden" name="sender" value="<%=sender%>">
			  <input name="type" type="hidden" value="<%=com.redmoon.forum.message.MessageDb.TYPE_SYSTEM%>">
			  <input name="isToAll" type="checkbox" value="true">全体
            </td>
          </tr>
          <tr> 
            <td width="68" height="26"> 
              <div align="center">消息标题：</div>
            </td>
            <td width="217" height="26">
              <input type="text" name="title" class="input1" size="30" maxlength="30">
            </td>
          </tr>
          <tr> 
            <td width="68" height="26"> 
              <div align="center">消息内容：</div>
            </td>
            <td width="217" height="26"> 
              <textarea name="content" cols="28" rows="3"></textarea>
            </td>
          </tr>
          <tr> 
            <td colspan="2" height="26"> 
              <div align="center">
                <input type="submit" name="Submit" value="发送消息" class="button1">
                &nbsp; 
                <input type="reset" name="Submit2" value="重写" class="button1">
              </div>
            </td>
          </tr>
        </table>
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black" height="6">
          <tr> 
            <td></td>
          </tr>
        </table>
      </td>
  </tr>
  <tr> 
    <td bgcolor="#CEE7FF" height="6"></td>
  </tr></form>
</table>
</body>
</html>
