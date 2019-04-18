<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../inc/nocache.jsp"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<html>
<head>
<title>查看新消息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="../common.css" type=text/css rel=stylesheet>
<SCRIPT language=javascript src="../inc/risewindow.js"></SCRIPT>
<script>
function window_onload() {
	START();
	getMsg();
	focus();
}

function getMsg() {
	divMsg.innerHTML = window.opener.getDivMsg();
}

function openWin(url,width,height)
{
  var newwin = window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}

function showmsg(id) {
	// openWin("showmsg.jsp?id=" + id, 320, 260);
	// window.opener.top.mainFrame.location.href = "<%=request.getContextPath()%>/message_oa/message_ext/showmsg.jsp?id=" + id;
	window.opener.top.mainFrame.addTab("消息", "<%=request.getContextPath()%>/message_oa/message_ext/showmsg.jsp?id=" + id);
	window.close();
}

function IKnow() {
	form1.submit();
}
</script>
</head>
<body bgcolor="#FFFFFF" text="#000000" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="window_onload()">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<jsp:useBean id="Msg" scope="page" class="com.redmoon.oa.message.MessageMgr"/>
<table width="100%" height="183" border="0" align="center" cellpadding="3" cellspacing="1" bgcolor="#99CCFF" class="9black">
  <tr> 
    <td width="100%" height="23" bgcolor="#CEE7FF">
    <div align="center"> <b>新 消 息</b></div></td>
  </tr>
  <form id="form1" name="form1" action="iknow.jsp" method="post">
  <tr> 
      <td bgcolor="#FFFFFF" valign="top"><hr size="1" width="260">
        <table width="100%" height="103" border="0" align="center" cellpadding="0" cellspacing="0" class="9black">
          <tr> 
            <td width="100%" height="103" valign="top">
			<div id="divMsg" name="divMsg"></div>
			</td>
          </tr>
        </table>
        <table width="100%" border="0" cellspacing="0" cellpadding="0" align="center" class="9black">
          <tr> 
            <td width="100%" height="21"> 
            <div align="center">
            <a href="javascript:IKnow()">我知道了</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:window.close()">&nbsp;关闭窗口(<span id="sec"></span>)</a>
            </div>            </td>
          </tr>
        </table>
	  </td>
  </tr>
  </form>
  <tr> 
    <td bgcolor="#CEE7FF" height="6"></td>
  </tr>
</table>
<%
UserSetupDb usd = new UserSetupDb();
usd = usd.getUserSetupDb(privilege.getUser(request));
if (usd.isMessageSoundPlay()) {
%>
<object NAME='player' classid=clsid:22d6f312-b0f6-11d0-94ab-0080c74c7e95 width=350 height=70 style="display:none">
  <param name=showstatusbar value=1>
  <param name=filename value='<%=request.getContextPath()%>/message/msg.wav'>
  <param name="AUTOSTART" value="true" />
  <embed src='<%=request.getContextPath()%>/message/msg.wav'> </embed>
</object>
<%}%>
</body>
<script>
function tickout(secs) {
	sec.innerText = secs;
	if (--secs > 0) {
	  	setTimeout('tickout(' +secs + ')', 1000);
	}
	else {
		window.close();
	}
}
tickout(10);
</script>
</html>
