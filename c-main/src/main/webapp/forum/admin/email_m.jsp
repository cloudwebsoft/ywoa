<%@ page contentType="text/html; charset=utf-8" %>
<%@ include file="../inc/inc.jsp" %>
<%@ page import="cn.js.fan.module.cms.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="cn.js.fan.module.pvg.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.email_m" key="group_send_email"/></title>
<link rel="stylesheet" href="../common.css">
<script language="JavaScript">
<!--
function form1_onsubmit() {

}
//-->
</script>
<LINK href="default.css" type=text/css rel=stylesheet>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="sendmail" scope="page" class="cn.js.fan.mail.SendMail"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String senderName = ParamUtil.get(request, "senderName");
String subject = ParamUtil.get(request, "subject");
try {
	com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "subject", subject, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

String content = "";

Vector v = new Vector();
UserDb user = new UserDb();

String op = ParamUtil.get(request, "op");
if (op.equals("send")) {
	String mailserver = Global.getSmtpServer();
	int smtp_port = Global.getSmtpPort();
	String name = Global.getSmtpUser();
	String pwd_raw = Global.getSmtpPwd();
	sendmail.initSession(mailserver, smtp_port, name, pwd_raw);

	String[] receivers = ParamUtil.getParameters(request, "receivers");
	if (receivers==null) {
		out.print(StrUtil.Alert_Back("请选择用户！"));
		return;
	}				
	for (int i = 0; i < receivers.length; i++) {
		if (receivers[i].equals("isToAll")){
			v = user.list();
		}else {
			if(receivers[i].equals("boardManager")){
				String sql = "select distinct name from sq_boardmanager";
				v = user.list(sql);
			}else{
				String sql = "select name from sq_user where group_code=" +StrUtil.sqlstr(receivers[i]);
				v = user.list(sql);
			}
		}
	}
	
	senderName = StrUtil.GBToUnicode(senderName);
	senderName += "<" + Global.getEmail() + ">";
	content = ParamUtil.get(request, "content");
	if (subject.equals("")) {
		out.print(StrUtil.Alert_Back("主题不能为空！"));
		return;
	}
	if (content.equals("")) {
		out.print(StrUtil.Alert_Back("内容不能为空！"));
		return;
	}
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head">Email<lt:Label res="res.label.email_m" key="group_send_mgr"/></td>
  </tr>
</table>
<br>
<TABLE class="frame_gray" cellSpacing=0 cellPadding=0 width="95%" align=center>
  <TBODY>
    <TR>
      <TD valign="top" bgcolor="#FFFBFF" class="thead">Email<lt:Label res="res.label.email_m" key="group_send"/></TD>
    </TR>
    <TR>
      <TD height=200 valign="top" bgcolor="#FFFBFF"><br>
        <table width="98%" border='0' align="center" cellpadding='0' cellspacing='0'>
          <form name="form1" action="?" method="post" onSubmit="return form1_onsubmit()">
		  <tr>
		    <td width="11%" height=28 align="left"><span class="unnamed2"><lt:Label res="res.label.email_m" key="sender"/></span></td>
		    <td width="89%" height="28" align="left"><span class="unnamed2">
		      <input name="senderName" class="singleboarder" value="<%=Global.email%>" size=40>
		      <input type="hidden" name="op" value="send">
		    </span></td>
		    </tr>
		  <tr>
		    <td height=28 align="left"><lt:Label res="res.label.email_m" key="topic"/></td>
		    <td height="28" align="left"><span class="unnamed2">
		      <input name="subject" class="singleboarder" value="<%=subject%>" size=60>
		    </span></td>
		    </tr>
		  <tr>
		    <td height=38 align="left"><lt:Label res="res.label.forum.admin.group_send_message" key="user_group"/></td>
		    <td height="38" align="left"><input type="checkbox" name="receivers" value="isToAll">
		      <lt:Label res="res.label.forum.admin.group_send_message" key="is_to_all"/>
		      <br>
              <%
UserGroupDb ugroup = new UserGroupDb();
Vector result = ugroup.list();
Iterator ir = result.iterator();
while (ir.hasNext()) {
 	UserGroupDb ug = (UserGroupDb)ir.next();
%>
              <input type="checkbox" name="receivers" value="<%=ug.getCode()%>">
              <%=ug.getDesc()%><br>
              <%
}
%>
              <input type="checkbox" name="receivers" value="boardManager">
              <lt:Label res="res.label.forum.admin.group_send_message" key="board_manager"/></td>
		    </tr>
		  <tr>
		    <td height=38 align="left"><span class="unnamed2">
		      <lt:Label res="res.label.email_m" key="content"/>
		    </span></td>
		    <td height="38" align="left">
<script type="text/javascript" src="../../FCKeditor/fckeditor.js"></script>
<script type="text/javascript">
<!--
var oFCKeditor = new FCKeditor( 'content' ) ;
oFCKeditor.BasePath = '../../FCKeditor/';
oFCKeditor.Config['CustomConfigurationsPath'] = '<%=request.getContextPath()%>/FCKeditor/fckconfig_cws_forum.jsp' ;
oFCKeditor.ToolbarSet = 'Simple'; // 'Basic' ;
oFCKeditor.Width = "100%";
oFCKeditor.Height = 250 ;

// 解决自动首尾加<p></p>的问题
oFCKeditor.Config["EnterMode"] = 'br' ;     // p | div | br （回车）
oFCKeditor.Config["ShiftEnterMode"] = 'p' ; // p | div | br（shift+enter)

oFCKeditor.Config["FormatOutput"]=false;
oFCKeditor.Config["FillEmptyBlocks"]=false;
oFCKeditor.Config["FormatIndentator"]=" ";
oFCKeditor.Config["FullPage"]=false;
oFCKeditor.Config["StartupFocus"]=true;
oFCKeditor.Config["EnableXHTML"]=false;
oFCKeditor.Config["FormatSource"]=false;
oFCKeditor.Config["SkinPath"]="skins/office2003/";

oFCKeditor.Create() ;
//-->
</script>			</td>
		    </tr>
		  <tr>
		    <td align="left">&nbsp;</td>
		    <td height="24" align="left"><span class="unnamed2">(
                <lt:Label res="res.label.email_m" key="msg"/>
)</span></td>
		    </tr>
		  <tr>
		    <td height=38 align="left">&nbsp;</td>
		    <td height="38" align="left"><span class="unnamed2">
		      <input type="submit" value="<%=SkinUtil.LoadString(request,"res.label.email_m","send_mail")%>">
		    </span></td>
		    </tr>
          </form>
        </TABLE>
	    <br>  
	  </td>
    </tr>
</table></TD>
    </TR>
  </TBODY>
  <%
			if (op.equals("send")) {				
				int failcount = 0;
				
				ir = v.iterator();
			    while (ir.hasNext()) {
					user = (UserDb)ir.next();
				%>
  <table width="90%"  border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td><%
					String username = user.getName();
					// sendmail.setmailFooterHTML("<br>------www.***.com------");
					// sendmail.getMailInfo(application, request);
					String to = user.getEmail();
					
					String content1 = content.replaceAll("\\$name", username);		
					try {		
						sendmail.initMsg(to, senderName, subject, content1, true);
						if ( sendmail.send())
						   ; // out.println(username + " " + to + " 邮件发送成功！<BR>");
						else {
							failcount++;
							out.println(username + " " + to + SkinUtil.LoadString(request,"res.label.email_m","reason") +sendmail.getErrMsg());
						}
						sendmail.clear();
					}
					catch (Exception e) {
						out.print(e.getMessage());
					}
					Thread.sleep(5);
					%>
      </td>
    </tr>
</table>
  <%}
	out.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + SkinUtil.LoadString(request,"res.label.email_m","counts_of_fail")  + failcount + SkinUtil.LoadString(request,"res.label.email_m","man"));
}%>
<br>
</td>
</tr>
</td>
</tr>
</body>                                        
</html>                            
  