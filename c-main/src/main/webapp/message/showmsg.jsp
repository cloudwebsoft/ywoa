<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../inc/nocache.jsp"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="cn.js.fan.util.ErrMsgException"%>
<%@ page import="com.redmoon.forum.message.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<html>
<head>
<title><lt:Label res="res.label.message.message" key="showmsg"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="../common.css" type=text/css rel=stylesheet>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isUserLogin(request))
{ %>
<table width="320" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
  <tr> 
    <td><li><%=SkinUtil.LoadString(request,"res.label.message.message","msg")%></td>
  </tr>
</table>
<% 
	return;
} %>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.forum.message.MessageMgr"/>
<%
int id = ParamUtil.getInt(request, "id");
MessageDb md = Msg.getMessageDb(id);

String title,content,rq,receiver,sender;
int type;
boolean isreaded;
id = md.getId();
title = md.getTitle();
content = md.getContent();
type = md.getType();
rq = DateUtil.format(md.getRq(), "yyyy-MM-dd HH:mm:ss");
receiver = md.getReceiver();

com.redmoon.forum.person.UserMgr um = new com.redmoon.forum.person.UserMgr();
sender = md.getSender();
if (!sender.equals(md.USER_SYSTEM))
	sender = um.getUser(sender).getNick();

isreaded = md.isReaded();

md.setReaded(true);
md.save();
%>
<table width="320" border="0" cellspacing="1" cellpadding="3" align="center" bgcolor="#99CCFF" class="9black" height="260">
  <form name="form1" method="post" action="myreply.jsp?id=<%=id%>">
  <tr> 
    <td bgcolor="#CEE7FF" height="23">
        <div align="center"> <b><lt:Label res="res.label.message.message" key="showmsg"/></b></div>
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
              <div align="center"><a href="mysend.jsp"><img src="images/m_outbox.gif" alt="<lt:Label res="res.label.message.message" key="send_mail"/>" width="40" height="40" border="0"></a></div>
            </td>
            <td width="75"> 
              <div align="center"><a href="send.jsp"><img src="images/newpm.gif" width="40" height="40" border="0" alt="<lt:Label res="res.label.message.message" key="write_mail"/>"></a></div>
            </td>
            <td width="75"> 
              <div align="center"> 
                <img src="images/m_delete.gif" width="40" height="40" alt="<lt:Label res="res.label.message.message" key="del_mail"/>">
              </div>
            </td>
          </tr>
        </table>
    </td>
  </tr>
  <tr> 
      <td bgcolor="#FFFFFF" height="152" valign="top">
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
          <tr> 
            <td> 
              <div align="center"><font color="#05006C"><b><%=title%></b></font></div>
            </td>
          </tr>
          <tr>
            <td align="center">
			<%=sender%> <%=rq%> <input type="hidden" name="title" value="<%="RE:"+title%>">
			<input name="receiver" value="<%=sender%>" type="hidden">
			</td>
          </tr>
        </table>
        <hr size="1" width="300">
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
          <tr> 
            <td height="91" valign="top">
			<%
			String body = StrUtil.toHtml(content);
			%>
			<%=StrUtil.ubb(request, body, true)%>
			</td>
          </tr>
        </table>
        <table width="300" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
          <tr> 
            <td> 
              <div align="center">
			  <%if (md.getType()!=md.TYPE_SYSTEM) {%>
                <input type="submit" name="Button" value="<lt:Label res="res.label.message.message" key="myreply"/>" class="button1">
			  <%}%>
              </div>
            </td>
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
