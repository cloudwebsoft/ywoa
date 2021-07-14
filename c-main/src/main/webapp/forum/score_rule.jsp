<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.io.*,
				 cn.js.fan.db.*,
				 cn.js.fan.util.*,
				 cn.js.fan.web.*,
				 com.redmoon.forum.*,
				 com.redmoon.forum.plugin.*,
				 org.jdom.*,
                 java.util.*,
				 com.redmoon.forum.person.*"
%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.forum.score_rule" key="score_rule"/> - <%=Global.AppName%></title>
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<br>
<div class="tableTitle"><lt:Label res="res.label.forum.score_rule" key="score_rule"/></div>
<TABLE width="100%" class="tableCommon">
	<thead>
      <TR align=center> 
        <TD width=9% height=23><lt:Label res="res.label.forum.score_rule" key="type"/></TD>
        <TD width=7% height=23><lt:Label res="res.label.forum.score_rule" key="regist"/></TD>
        <TD width=7%><lt:Label res="res.label.forum.score_rule" key="login"/></TD>
        <TD width=9%><lt:Label res="res.label.forum.score_rule" key="add"/></TD>
        <TD width=9%><lt:Label res="res.label.forum.score_rule" key="reply"/></TD>
        <TD width=9%><lt:Label res="res.label.forum.score_rule" key="del"/></TD>
        <TD width=9%><lt:Label res="res.label.forum.score_rule" key="elite"/></TD>
        <TD width=9%><lt:Label res="res.label.forum.score_rule" key="advertiseLink"/></TD>
        <TD width=9%><lt:Label res="res.label.forum.admin.config_score" key="attachment_add"/></TD>
        <TD width=9%><lt:Label res="res.label.forum.admin.config_score" key="attachment_del"/></TD>
        <TD height=23><lt:Label res="res.label.forum.score_rule" key="danWei"/></TD>
      </TR>
	</thead>
    <TBODY>
<%
XMLConfig cfg = new XMLConfig("score.xml", false, "iso-8859-1");

Element root = cfg.getRootElement();
Iterator ir = root.getChildren().iterator();
ScoreMgr sm = new ScoreMgr();
while (ir.hasNext()) {
	Element e = (Element) ir.next();
	ScoreUnit su = sm.getScoreUnit(e.getAttributeValue("code"));
%>
      <TR align=center> 
        <TD width=9% height=23 align="left"><%=su.getName(request)%></TD>
        <TD width=7% height=23><%=e.getChildText("regist")%></TD>
        <TD width=7% height=23><%=e.getChildText("login")%></TD>
        <TD width=9% height=23><%=e.getChildText("add")%></TD>
        <TD width=9% height=23><%=e.getChildText("reply")%></TD>
        <TD width=9% height=23><%=e.getChildText("del")%></TD>
        <TD width=9% height=23><%=e.getChildText("elite")%></TD>
        <TD width=9% height=23><%=e.getChildText("advertiseLink")%></TD>
        <TD width=9%><%=e.getChildText("attachment_add")%></TD>
        <TD width=9%><%=e.getChildText("attachment_del")%></TD>
        <TD width=14% height=23><%=e.getChildText("danWei")%></TD>
      </TR>
<%}%>	  
    </TBODY>
</TABLE>
	
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr> 
      <td width="2%" height="23">&nbsp;</td>
      <td height="23" valign="baseline">&nbsp;  </td>
    </tr>
  </table>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
