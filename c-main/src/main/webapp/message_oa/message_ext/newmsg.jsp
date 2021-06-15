<%@ page contentType="text/html;charset=utf-8" %>
<%@ include file="../../inc/nocache.jsp"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.message.*"%>
<html>
<head>
<title>查看新消息</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<LINK href="../../common.css" type=text/css rel=stylesheet>
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
	openWin("showmsg.jsp?id=" + id, 320, 260);
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
    <div align="center"> <b>新 消 息</b></div>    </td>
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
            <div align="center"><a href="javascript:IKnow()">我知道了</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:window.close()">&nbsp;关闭窗口</a></div>            </td>
          </tr>
        </table>
	  </td>
  </tr>
  </form>
  <tr> 
    <td bgcolor="#CEE7FF" height="6"></td>
  </tr>
</table>
</body>
</html>
