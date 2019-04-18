<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skincode = UserSet.getSkin(request);
if (skincode.equals(""))
	skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
if (skin==null)
	skin = skm.getSkin(UserSet.defaultSkin);
String skinPath = skin.getPath();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="GENERATOR" content="Microsoft FrontPage 4.0">
<meta name="ProgId" content="FrontPage.Editor.Document">
<link href="<%=skinPath%>/skin.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.forum.rank" key="rank"/> - <%=Global.AppName%></title>
<style type="text/css">
<!--
body {
	margin-top: 0px;
	margin-left: 0px;
	margin-right: 0px;
}
-->
</style></head>
<body>
<%@ include file="inc/header.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
	String sql = "",nick = "";
	int start = 0,end = 9;	
	Vector vt = null;
	Iterator ir = null;
	UserDb ud = new UserDb();
%>
<table width="98%" border="0" align="center">
  <tr>
    <td width="50%"><TABLE width="99%" border=0 align=left cellPadding=3 cellSpacing=1 bgcolor="#edeced">
      <TBODY>
        <TR align=center bgColor=#f8f8f8 class="td_title">
          <TD width=50% height=23><lt:Label res="res.label.forum.rank" key="nick"/></TD>
          <TD width="50%" height=23><lt:Label res="res.label.forum.rank" key="addcount"/></TD>
        </TR>
<%
    int addCount = 0;
	vt = ud.list(SQLBuilder.getRankAddCount(),start,end);
	ir = vt.iterator();
	while(ir.hasNext()){
		ud = (UserDb)ir.next();
		nick = ud.getNick();
		addCount = ud.getAddCount();
%>
        <TR align=center bgColor=#f8f8f8>
          <TD height=23 align="left"><%=nick%></TD>
          <TD height=23 align="left"><%=addCount%></TD>
        </TR>
<%
	}
%>
      </TBODY>
    </TABLE></td>
    <td width="50%"><TABLE width="99%" border=0 align=right cellPadding=3 cellSpacing=1 bgcolor="#edeced">
      <TBODY>
        <TR align=center bgColor=#f8f8f8 class="td_title">
          <TD width=50% height=23><lt:Label res="res.label.forum.rank" key="nick"/></TD>
          <TD width="50%" height=23><lt:Label res="res.label.forum.rank" key="credit"/></TD>
          </TR>
<%
    int credit = 0;
	vt = ud.list(SQLBuilder.getRankCredit(),start,end);
	ir = vt.iterator();
	while(ir.hasNext()){
		ud = (UserDb)ir.next();
		nick = ud.getNick();
		credit = ud.getCredit();
%>
        <TR align=center bgColor=#f8f8f8>
          <TD height=23 align="left"><%=nick%></TD>
          <TD height=23 align="left"><%=credit%></TD>
          </TR>
<%
	}
%>
      </TBODY>
    </TABLE></td>
  </tr>
  <tr>
    <td><TABLE width="99%" border=0 align=left cellPadding=3 cellSpacing=1 bgcolor="#edeced">
      <TBODY>
        <TR align=center bgColor=#f8f8f8 class="td_title">
          <TD width=50% height=23><lt:Label res="res.label.forum.rank" key="nick"/></TD>
          <TD width="50%" height=23><lt:Label res="res.label.forum.rank" key="experience"/></TD>
        </TR>
<%
    int experience = 0;
	vt = ud.list(SQLBuilder.getRankExperience(),start,end);
	ir = vt.iterator();
	while(ir.hasNext()){
		ud = (UserDb)ir.next();
		nick = ud.getNick();
		experience = ud.getExperience();
%>
        <TR align=center bgColor=#f8f8f8>
          <TD height=23 align="left"><%=nick%></TD>
          <TD height=23 align="left"><%=experience%></TD>
        </TR>
<%
	}
%>
      </TBODY>
    </TABLE></td>
    <td><TABLE width="99%" border=0 align=right cellPadding=3 cellSpacing=1 bgcolor="#edeced">
      <TBODY>
        <TR align=center bgColor=#f8f8f8 class="td_title">
          <TD width=50% height=23><lt:Label res="res.label.forum.rank" key="nick"/></TD>
          <TD width="50%" height=23><lt:Label res="res.label.forum.rank" key="gold"/></TD>
          </TR>
<%
    int gold = 0;
	vt = ud.list(SQLBuilder.getRankGold(),start,end);
	ir = vt.iterator();
	while(ir.hasNext()){
		ud = (UserDb)ir.next();
		nick = ud.getNick();
		gold = ud.getGold();
%>
        <TR align=center bgColor=#f8f8f8>
          <TD height=23 align="left"><%=nick%></TD>
          <TD height=23 align="left"><%=gold%></TD>
        </TR>
<%
	}
%>
      </TBODY>
    </TABLE></td>
  </tr>
</table>
<%@ include file="inc/footer.jsp"%>
</body>
</html>
