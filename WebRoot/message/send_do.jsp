<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../inc/nocache.jsp"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<html>
<head>
<title><lt:Label res="res.label.message.message" key="write_new_msg"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="../common.css" type=text/css rel=stylesheet>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String name = privilege.getUser(request);
String title,receiver,content,errmsg="";
title = ParamUtil.get(request, "title");
receiver = ParamUtil.get(request, "receiver");
content = ParamUtil.get(request, "content");
if(title.trim().length()==0||content.trim().length()==0)%><%
		errmsg += SkinUtil.LoadString(request,"res.label.message.message","title_content_can_not_null") + "\\n";
// if(name.equals(receiver))
//		errmsg += SkinUtil.LoadString(request,"res.label.message.message","do_not_send_to_yourself") + "\\n";
if(!errmsg.equals("")) {
	out.print(StrUtil.Alert_Back(errmsg));
	return;
}
%>
<table width="320" border="0" cellspacing="1" cellpadding="3" align="center" bgcolor="#99CCFF" class="9black" height="260">
  <tr> 
    <td bgcolor="#CEE7FF" height="23">
        <div align="center"> <b><lt:Label res="res.label.message.message" key="write_new_msg"/></b></div>
    </td>
  </tr>
  <tr> 
    <td bgcolor="#FFFFFF" height="50"> 
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center">
          <tr> 
            <td width="75"> 
              <div align="center"><a href="message.jsp?page=1"><img src="images/m_inbox.gif" width="40" height="40" border="0" alt="<lt:Label res="res.label.message.message" key="recever_mail"/>"></a></div>
            </td>
            <td width="75"> 
              <div align="center"><a href="mysend.jsp"><img src="images/m_outbox.gif" width="40" height="40" border="0" alt="<lt:Label res="res.label.message.message" key="send_mail"/>"></a></div>
            </td>
            <td width="75"> 
              <div align="center"><a href="send.jsp"><img src="images/newpm.gif" width="40" height="40" border="0" alt="<lt:Label res="res.label.message.message" key="write_mail"/>"></a></div>
            </td>
            <td width="75"> 
              <div align="center"> <img src="images/m_delete.gif" width="40" height="40" alt="<lt:Label res="res.label.message.message" key="del_mail"/>"></div>
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
            
          <td height="35"> <li>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.forum.message.MessageMgr"/>
<%
boolean isSuccess = false;
try {
	isSuccess = Msg.AddMsg(request);
}
catch (ErrMsgException e) {
	out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request,"info_operate_fail")+e.getMessage()));
}
%>
<% if (isSuccess) { 
  out.print(SkinUtil.LoadString(request,"info_op_success"));
}
%>
			  </td>
          </tr>
          <tr> 
            <td height="35"> 
              <div align="center"></div>
            </td>
          </tr>
          <tr> 
            <td height="35"> 
              <div align="center"></div>
            </td>
          </tr>
          <tr> 
            <td height="35"> 
              <div align="center"> </div>
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
  </tr>
</table>
</body>
</html>
