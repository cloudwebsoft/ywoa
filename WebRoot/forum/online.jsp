<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt"%>
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
<title>online</title>
</head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<div id="newdiv" name="newdiv">
<table width="100%" style="padding-top:8px">
<tr><td>
<%
String querystring = StrUtil.getNullString(request.getQueryString());
String privurl = request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");
String boardcode = StrUtil.getNullString(request.getParameter("boardcode"));
String sql = "";
if (boardcode.equals(""))
	sql = "select name from sq_online ORDER BY isguest,logtime";
else
	sql = "select name from sq_online where boardcode="+StrUtil.sqlstr(boardcode)+" ORDER BY isguest,logtime";
	
if (!SecurityUtil.isValidSql(sql)) {
	out.print(StrUtil.p_center(SkinUtil.LoadString(request, SkinUtil.ERR_SQL)));
	return;
}
		
OnlineUserDb ou = new OnlineUserDb();
int total = ou.getObjectCount(sql);

int pagesize = 35; 	// 20;

int curpage,totalpages;
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
totalpages = paginator.getTotalPages();
curpage	= paginator.getCurrentPage();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}	

ObjectBlockIterator oir = ou.getOnlineUsers(sql, (curpage-1)*pagesize, curpage*pagesize);

String id="",name="",ip="",logtime="",doing="",myface="",boardname="";
boolean isguest = false;
String RealPic = "";
boolean covered = false;
int layer = 1;
int i = 1;
UserDb user = new UserDb();
Directory dir = new Directory();
Leaf leaf = null;
int rowCount = 7; // 一行显示7个用户
IPStoreDb ipd = new IPStoreDb();
int n = 0;

com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
boolean isIPPositionDisplay = cfg.getBooleanProperty("forum.isIPPositionDisplay");
if (privilege.isMasterLogin(request)) {
	isIPPositionDisplay = true;
}

BoardManagerDb bmd = new BoardManagerDb();
while (oir.hasNext()) {
	if (n==0)
		out.print("<tr>");
	ou = (OnlineUserDb) oir.next();
	i++;
	name = ou.getName();
	logtime = DateUtil.format(ou.getLogTime(), "yyyy-MM-dd HH:mm:ss");
	doing = ou.getDoing();
	isguest = ou.isGuest();
	covered = ou.isCovered();
	if (!isguest)
		user = user.getUser(name);
	RealPic = user.getRealPic();
	myface = user.getMyface();
	ip = ou.getIp();
	String ipRaw = ip;
	if (!privilege.isMasterLogin(request)) {
		int p = ip.indexOf(".");
		p = ip.indexOf(".",p+1);
		ip = ip.substring(0,p)+".*.*";
	}	
	boardcode = StrUtil.getNullString(ou.getBoardCode());
	boardname = "";
	if (!boardcode.equals("")) {
		leaf = dir.getLeaf(boardcode);
		if (leaf==null)
			leaf = new Leaf();
		boardname = leaf.getName();
	}
		
	out.print("<td width='14%'>");
	if (!isguest) {
		if (!covered) {
			String icon = "member";
			if (bmd.getBoardManagerDb(Leaf.CODE_ROOT, name).isLoaded()) {
				icon = "admin";
			}
			else if (bmd.isUserManager(name))
				icon = "manager";
%>
		  <a target="_blank" href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>" class="online_info">
		  <img src="<%=skinPath%>/images/<%=icon%>.gif" width=16 height=16 align="absmiddle" border=0> 
		  <%=user.getNick()%>
		  <span>
		  <lt:Label res="res.label.forum.showonline" key="cur_pos"/>：<%=boardname%><br>
		  <lt:Label res="res.label.forum.showonline" key="login_date"/>：<%=ForumSkin.formatDateTime(request, ou.getLogTime())%><br>
		  <lt:Label res="res.label.forum.showonline" key="reg_date"/>：<%=ForumSkin.formatDate(request, user.getRegDate())%><br>
		  <%if (isIPPositionDisplay) {%>
		  <lt:Label res="res.label.forum.showonline" key="ip_address"/>：<%=ipd.getPosition(ipRaw)%><br>
		  <%}%>
		  IP：<%=ip%></span></a>&nbsp;
<% 		}
		else {%>
			<img src="<%=skinPath%>/images/guest.gif" align="absmiddle">&nbsp;<%=SkinUtil.LoadString(request, "res.label.forum.index", "login_hide")%>
<%		}
	}
	else {
%>
		<a href="#" class="online_info">
		<img src="<%=skinPath%>/images/guest.gif" align="absmiddle" border="0">&nbsp;<%=SkinUtil.LoadString(request, "res.label.forum.index", "guest")%>
		<span>
		<lt:Label res="res.label.forum.showonline" key="cur_pos"/>：<%=boardname%><br>
		<lt:Label res="res.label.forum.showonline" key="login_date"/>：<%=ForumSkin.formatDateTime(request, ou.getLogTime())%><br>
		<lt:Label res="res.label.forum.showonline" key="ip_address"/>：<%=StrUtil.getNullStr(ipd.getPosition(ipRaw))%><br>
		IP：<%=ip%></span></a>&nbsp; 	
	<%}
	out.print("</td>");
	n ++;
	if (n>=rowCount) {
		n = 0;
		out.print("</tr>");
	}
}
if (n<rowCount) {
	out.print("</tr>");
}
%>
</td></tr></table>
<table width="100%"><tr><td style="border-top:1px solid #cccccc;padding:5px 0px">
<%=StrUtil.format(SkinUtil.LoadString(request, "res.label.forum.index", "online_status"), new Object[] {new Integer(total), new Integer(pagesize), new Integer(totalpages), new Integer(curpage)})%>
<%if (totalpages!=1) {%>
<lt:Label res="res.label.forum.index" key="jumpto"/>
<%}%>
<%if (curpage!=1) {%>
[<a href="javascript:loadonline('<%=boardcode%>',1)"><lt:Label res="res.label.forum.index" key="first"/></a>]
[<a href="javascript:loadonline('<%=boardcode%>',<%=curpage-1%>)"><lt:Label res="res.label.forum.index" key="previous"/></a>]
<%}%>
<%if (curpage!=totalpages) {%>
[<a href="javascript:loadonline('<%=boardcode%>',<%=curpage+1%>)"><lt:Label res="res.label.forum.index" key="next"/></a>]
[<a href="javascript:loadonline('<%=boardcode%>',<%=totalpages%>)"><lt:Label res="res.label.forum.index" key="last"/></a>]
<%}%>
[<a href="showonline.jsp"><lt:Label res="res.label.forum.index" key="detail"/></a>]
</td>
</tr></table>
</div>
</body>
<SCRIPT language=javascript>
<!--
function trim(str){
   	var i = 0;
    while ((i < str.length)&&((str.charAt(i) == " ")||(str.charAt(i) == "　"))){i++;}
   	var j = str.length-1;
   	while ((j >= 0)&&((str.charAt(j) == " ")||(str.charAt(j) == "　"))){j--;}
   	if( i > j ) 
   		return "";
   	else
   		return str.substring(i,j+1);
}

try {
	var str = trim(newdiv.innerHTML);
	if (str!="")
	{
		window.parent.document.getElementById("followDIV000").innerHTML = str;
	}
	window.parent.document.getElementById("followImg000").loaded = "yes";
}
catch (e) {}
//-->
</script>
</html>
