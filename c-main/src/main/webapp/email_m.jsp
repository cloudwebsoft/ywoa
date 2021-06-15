<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.email_m" key="group_send_email"/></title>
<link rel="stylesheet" href="../common.css">
<script language="JavaScript">
<!--
function selTemplate(id)
{
	form1.templateId.value = id;
	window.location.href = "../email_m.jsp?templateId=" + id;
}

function form1_onsubmit() {
	if (form1.templateId.value==-1) {
		alert("<%=SkinUtil.LoadString(request,"res.label.email_m","select_template")%>");
		return false;
	}
}
//-->
</script>
<link href="common.css" rel="stylesheet" type="text/css">
<LINK href="fileark/default.css" type=text/css rel=stylesheet>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">Email<lt:Label res="res.label.email_m" key="group_send_mgr"/></td>
  </tr>
</table>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<jsp:useBean id="sendmail" scope="page" class="cn.js.fan.mail.SendMail"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int templateId = -1;
try {
	templateId = ParamUtil.getInt(request, "templateId");
}
catch (ErrMsgException e) {
}

Document template = new Document();
if (templateId!=-1) {
	template = template.getDocument(templateId);
}

String senderName = ParamUtil.get(request, "senderName");
String subject = ParamUtil.get(request, "subject");

if (subject.equals("")) {
	subject = StrUtil.getNullString(template.getTitle());
}
%>
<br>
<TABLE class="frame_gray"  
cellSpacing=0 cellPadding=0 width="95%" align=center>
  <TBODY>
    <TR>
      <TD valign="top" bgcolor="#FFFBFF" class="thead">Email<lt:Label res="res.label.email_m" key="group_send"/></TD>
    </TR>
    <TR>
      <TD height=200 valign="top" bgcolor="#FFFBFF"><br>
        <table width="98%" border='0' align="center" cellpadding='0' cellspacing='0'>
          <form name="form1" action="?" method="post" onSubmit="return form1_onsubmit()">
		  <tr>
            <td height=20 align="left"><span class="unnamed2"><a href="javascript:showModalDialog('doc_template_select_frame.jsp',window.self,'dialogWidth:640px;dialogHeight:480px;status:no;help:no;')">
              </a></span></td>
          <td width="11%" align="left"><span class="unnamed2"><lt:Label res="res.label.email_m" key="template_ID"/></span></td>
            <td width="83%" height="28" align="left"><span class="unnamed2">
            <input name="templateId" class="btn" value="<%=templateId%>" size=3 readonly>
<a href="javascript:showModalDialog('fileark/doc_template_select_frame.jsp',window.self,'dialogWidth:800px;dialogHeight:480px;status:no;help:no;')">
            <lt:Label res="res.label.email_m" key="select_template"/></a>&nbsp;( -1 <lt:Label res="res.label.email_m" key="has_not_selected_template"/> ) <input type="hidden" name="op" value="send">
            ( <lt:Label res="res.label.email_m" key="msg"/> )</span></td>
		  </tr>
		  <tr>
		    <td height=20 align="left">&nbsp;</td>
		    <td height=20 align="left"><span class="unnamed2"><lt:Label res="res.label.email_m" key="sender"/></span></td>
		    <td height="28" align="left"><span class="unnamed2">
		      <input name="senderName" class="btn" value="<%=senderName.equals("")?Global.AppName:senderName%>" size=20>
		    </span></td>
		    </tr>
		  <tr>
		    <td height=20 align="left">&nbsp;</td>
		    <td height=20 align="left"><lt:Label res="res.label.email_m" key="topic"/></td>
		    <td height="28" align="left"><span class="unnamed2">
		      <input name="subject" class="btn" value="<%=subject%>" size=40>
		    </span></td>
		    </tr>
		  <tr>
		    <td height=38 colspan="2" align="left">&nbsp;</td>
		    <td height="38" align="left"><span class="unnamed2">
		      <input type="submit" value="<%=SkinUtil.LoadString(request,"res.label.email_m","send_mail")%>">
		    </span></td>
		    </tr>
          <tr>
            <td colspan="3" valign="top"><table width="100%" border='0' cellspacing='0' cellpadding='0'>
                <tr >
                  <td width="6%" class="stable">        
                <tr>
                    <td height="23" colspan=3 align="center" class="stable">
					<%
					if (templateId!=-1)
						out.print(template.getContent(1));
					%>
				    </td>
                </tr></form>
        </TABLE>
	    <br>  
		</td>
    </tr>
</table></TD>
    </TR>
  </TBODY>
  <%
			String op = ParamUtil.get(request, "op");
			if (op.equals("send")) {
				String mailserver = Global.getSmtpServer();
				int smtp_port = Global.getSmtpPort();
				String name = Global.getSmtpUser();
				String pwd_raw = Global.getSmtpPwd();
				sendmail.initSession(mailserver, smtp_port, name, pwd_raw);
	
				String sql = "select name from sq_user ORDER BY RegDate desc";
				UserDb user = new UserDb();
				int count = user.getUserCount(sql);
				ObjectBlockIterator ir = user.getUsers(sql, 0, count);

				senderName = StrUtil.GBToUnicode(senderName);
				senderName += "<" + Global.getEmail() + ">";
				String content = template.getContent(1);
				
				int failcount = 0;
				
			    while (ir.hasNext()) {
					user = (UserDb)ir.next();
					// out.print("username=" + user.getName());
				%>
  <table width="90%"  border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td><%
					String username = user.getName();
					// sendmail.setmailFooterHTML("<br>------镇江人家------");
					// sendmail.getMailInfo(application, request);
					String to = user.getEmail();
					
					String content1 = content.replaceAll("\\$name", username);				
					//if (username.equals("bluewind")) {
						sendmail.initMsg(to, senderName, subject, content1, true);
						if ( sendmail.send())
						   ; // out.println(username + " " + to + " 邮件发送成功！<BR>");
						else
						{
							failcount++;
						    out.println(username + " " + to + SkinUtil.LoadString(request,"res.label.email_m","reason") +sendmail.getErrMsg());
						}
						sendmail.clear();
					//}
					Thread.sleep(5);
					%>
      </td>
    </tr>
</table>
  <%}
			out.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + SkinUtil.LoadString(request,"res.label.email_m","counts of fail")  + failcount + SkinUtil.LoadString(request,"res.label.email_m","man"));
			}%>
<br>
</td>
</tr>
</td>
</tr>
</body>                                        
</html>                            
  