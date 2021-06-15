<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>listtree</title>
<script src="../inc/common.js"></script>
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<div id="newdiv" name="newdiv">
<%	
long rootid = ParamUtil.getLong(request, "id");
String boardcode = ParamUtil.get(request, "boardcode");
String sql = "select id from sq_message where rootid=" + rootid + " and check_status=" + MsgDb.CHECK_STATUS_PASS + " ORDER BY orders";
String name="",lydate="",content="",topic="";
int layer = 1;
int i = 1;

UserMgr um = new UserMgr();
UserDb ud = null;
MsgDb md = new MsgDb();
long totalMsg = md.getMsgCount(sql, boardcode, rootid);
MsgBlockIterator irmsg = md.getMsgs(sql, boardcode, rootid, 0, totalMsg);
if (irmsg.hasNext()) {
	// 跳过根贴
	irmsg.next();
}
// 写跟贴
while (irmsg.hasNext()) {
	  i++;
	  md = (MsgDb)irmsg.next();
	  name = md.getName();
	  layer = md.getLayer();
	  ud = um.getUser(name);
 %>
<div>
<%
com.redmoon.forum.Config cfg1 = com.redmoon.forum.Config.getInstance();
int pagesize = cfg1.getIntProperty("forum.showTopicPageSize");
int CPages = (int)Math.ceil((double)i/pagesize);
layer = layer-1;
for (int k=1; k<=layer-1; k++){%>
<img src="" width=18 height=1>
<%}%>
<img src="<%=request.getContextPath()%>/forum/images/join.gif" width="18" height="16">
<a href="<%=request.getContextPath()%>/forum/<%=ForumPage.getShowTopicPage(request, rootid, CPages, ""+md.getId())%>"><%=DefaultRender.RenderFullTitle(request, md)%></a>	  
<%if (!name.equals("")) {%>
<a href="<%=request.getContextPath()%>/userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=ud.getNick()%></a>
<%}else{%>
<lt:Label res="res.label.forum.showtopic" key="anonym"/>
<%}%>
&nbsp;&nbsp;[<%=com.redmoon.forum.ForumSkin.formatDateTime(request, md.getAddDate())%>]
</div>
<%}%>
</div>
</body>
<SCRIPT language=javascript>
<!--
try {
	if ($("newdiv").innerHTML.trim()!=""){
		window.parent.document.getElementById("followDIV<%=rootid%>").innerHTML = $("newdiv").innerHTML;
	}
	window.parent.document.getElementById("followImg<%=rootid%>").loaded = "yes";
}
catch (e) {}
//-->
</script>
</html>
