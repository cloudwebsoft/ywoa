<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.module.nav.*"%>
<%@ page import="com.redmoon.forum.miniplugin.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.plugin.sweet.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<HTML><HEAD><TITLE>Menu</TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<LINK href="images/default.css" type=text/css rel=stylesheet>
<STYLE type=text/css>
.ttl {
	CURSOR: hand; COLOR: #ffffff; PADDING-TOP: 4px;
}
ul {
margin:0px;
padding:0px;
}
li {
	line-height: 2.0;
	list-style-type: none;
	list-style-position: inside;
	padding:2px 0px;
}
</STYLE>
<script language="javascript" src="../../inc/common.js"></script>
<SCRIPT language=javascript>
function showHide(obj) {
  var oStyle;
	if (!isIE())
		oStyle = obj.parentNode.parentNode.parentNode.rows[1].style;
	else
		oStyle = obj.parentElement.parentElement.parentElement.rows[1].style;
    oStyle.display == "none" ? oStyle.display = "" : oStyle.display = "none";
}
</SCRIPT>
<META content="MSHTML 6.00.3790.259" name=GENERATOR></HEAD>
<BODY bgColor=#9aadcd leftMargin=0 topMargin=0>
<BR>
<%
String rootpath = request.getContextPath();
cn.js.fan.module.cms.ui.menu.LeafChildrenCacheMgr dlcmHe = new cn.js.fan.module.cms.ui.menu.LeafChildrenCacheMgr("bbs");
java.util.Vector vtHe = dlcmHe.getChildren();
Iterator irHe = vtHe.iterator();
int p = 0;
while (irHe.hasNext()) {
	String style = "";
	if (p!=0)
		style = "display:none";
	p++;
	cn.js.fan.module.cms.ui.menu.Leaf menuleaf = (cn.js.fan.module.cms.ui.menu.Leaf) irHe.next();
	if (!menuleaf.canUserSee(request) || menuleaf.getCode().equals(cn.js.fan.module.cms.ui.menu.Leaf.CODE_BOTTOM))
		continue;	
%>
<TABLE cellSpacing=0 cellPadding=0 width=159 align=center border=0>
  <TBODY>
    <TR>
      <TD width=23><IMG height=25 src="images/box_topleft.gif" width=23></TD>
      <TD class=ttl onclick=showHide(this) width=129 background="images/box_topbg.gif"><%=menuleaf.getName()%></TD>
      <TD width=7><IMG height=25 src="images/box_topright.gif" width=7></TD>
    </TR>
    <TR style="<%=style%>">
      <TD style="PADDING-RIGHT: 3px; PADDING-LEFT: 3px; PADDING-BOTTOM: 3px; PADDING-TOP: 3px" background="images/box_bg.gif" colSpan=3>
	  <ul>
	  <%
		cn.js.fan.module.cms.ui.menu.LeafChildrenCacheMgr dl = new cn.js.fan.module.cms.ui.menu.LeafChildrenCacheMgr(menuleaf.getCode());
		Iterator headir1 = dl.getChildren().iterator();
		while (headir1.hasNext()) {
			cn.js.fan.module.cms.ui.menu.Leaf lf2 = (cn.js.fan.module.cms.ui.menu.Leaf) headir1.next();
			if (!lf2.canUserSee(request) || lf2.getCode().equals(cn.js.fan.module.cms.ui.menu.Leaf.CODE_BOTTOM))
				continue;
			if (lf2.getType()==cn.js.fan.module.cms.ui.menu.Leaf.TYPE_LINK) {		
	  %>
				<li>
				<%if (lf2.getIcon().equals("")) {%><img align="absmiddle" src="<%=request.getContextPath()%>/images/icons/arrow.gif" /><%}else{%><img align="absmiddle" src="../images/icons/<%=lf2.getIcon()%>" /><%}%>&nbsp;&nbsp;<a href="<%=lf2.getLink(request)%>" hidefocus="true" target="<%=lf2.getTarget()%>"><%=lf2.getName(request)%></a>			
				</li>
	  		<%}else{%>
				<%=cn.js.fan.module.cms.ui.menu.PresetLeaf.getMenuItem(request, lf2)%>
			<%}%>
	  <%}%>
	  </ul>
	  </TD>
    </TR>
    <TR>
      <TD colSpan=3><IMG height=10 src="images/box_bottom.gif" width=159></TD>
    </TR>
  </TBODY>
</TABLE>
<%}%>
</BODY></HTML>
