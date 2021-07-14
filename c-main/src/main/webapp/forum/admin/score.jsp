<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin.sweet.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.score" key="score"/></title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0'>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table width='100%' cellpadding='0' cellspacing='0' >
  <tr>
    <td class="head"><lt:Label res="res.label.forum.admin.entrance" key="plugin_manage"/></td>
  </tr>
</table>
<br>
<table width="98%" border='0' align="center" cellpadding='0' cellspacing='0' class="frame_gray">
  <tr> 
    <td height=20 align="left" class="thead"><lt:Label res="res.label.forum.admin.score" key="score"/></td>
  </tr>
  <tr> 
    <td valign="top"><br>
      <table width="92%"  border="0" align="center" cellpadding="0" cellspacing="1" bgcolor="#999999">
      <tr align="center" bgcolor="#F8F7F9">
        <td width="13%" height="24" bgcolor="#EFEBDE"><strong><lt:Label res="res.label.forum.admin.score" key="code"/></strong></td>
      <td width="16%" height="24" bgcolor="#EFEBDE"><strong><lt:Label res="res.label.forum.admin.score" key="name"/></strong></td>
        <td width="38%" height="24" bgcolor="#EFEBDE"><strong><lt:Label res="res.label.forum.admin.score" key="desc"/></strong></td>
      <td width="15%" bgcolor="#EFEBDE"><strong><strong><lt:Label res="res.label.forum.admin.score" key="type"/></strong></td>
        <td width="18%" bgcolor="#EFEBDE"><strong><lt:Label key="op"/></strong></td>
      </tr>
	<%
	ScoreMgr em = new ScoreMgr();
	Vector v = em.getAllScore();
	Iterator ir = v.iterator();
	while (ir.hasNext()) {
		ScoreUnit eu = (ScoreUnit)ir.next();
	%>
      <tr align="center">
        <td height="24" bgcolor="#FFF7FF"><%=eu.getCode()%></td>
      	<td height="24" bgcolor="#FFF7FF"><%=eu.getName()%></td>
        <td height="24" bgcolor="#FFF7FF"><%=eu.getDesc()%></td>
      <td bgcolor="#FFF7FF">
	  <%
	  if (eu.getType().equals(eu.TYPE_FORUM)) {
	  	out.print(SkinUtil.LoadString(request, "res.label.forum.admin.score", "all_board"));
	  }
	  else
	  	out.print(SkinUtil.LoadString(request, "res.label.forum.admin.score", "some_board"));
	  %>
	  </td>
        <td height="24" bgcolor="#FFF7FF">
		<%if (!eu.getType().equals(eu.TYPE_FORUM)) {%>
		<a href="score_modify.jsp?scoreCode=<%=StrUtil.UrlEncode(eu.getCode())%>"><lt:Label res="res.label.forum.admin.score" key="manage"/></a>
		<%}%>
		</td>
      </tr>
	<%}%>
    </table>
    <br></td>
  </tr>
</table>
</td> </tr>             
      </table>                                        
       </td>                                        
     </tr>                                        
 </table>                                        
</body>                                        
</html>                            
  