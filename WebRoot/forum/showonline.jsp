<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.base.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.forum.*"%>
<%@ page import = "com.redmoon.forum.person.*"%>
<%@ page import = "com.redmoon.forum.util.*"%>
<%@ page import = "com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><%=Global.AppName%> - <lt:Label res="res.label.forum.showonline" key="view_online"/></title>
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<div id="newdiv" name="newdiv">
<%
if (!privilege.canUserDo(request, "", "view_online")) {
	response.sendRedirect("../info.jsp?info=" + StrUtil.UrlEncode(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String querystring = StrUtil.getNullString(request.getQueryString());
String privurl=request.getRequestURL()+"?"+java.net.URLEncoder.encode(querystring,"utf-8");
%>
  <div class="tableTitle"><lt:Label res="res.label.forum.showonline" key="view_online"/></div>
<%
		int pagesize = 20;
		String sql = "select name from sq_online ORDER BY isguest,logtime";
		
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		OnlineUserDb ou = new OnlineUserDb();
		
		long total = ou.getObjectCount(sql);
		ObjectBlockIterator oir = ou.getOnlineUsers(sql, (curpage-1)*pagesize, curpage*pagesize);
		
		paginator.init(total, pagesize);
		
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
<TABLE class="tableCommon">
	<thead>
      <TR align=center> 
        <TD width=192 height=23><lt:Label res="res.label.forum.showonline" key="user_name"/></TD>
        <TD width=197 height=23><lt:Label res="res.label.forum.showonline" key="cur_pos"/></TD>
        <TD width=160 height=23>IP</TD>
        <TD width=166><lt:Label res="res.label.forum.showonline" key="ip_address"/></TD>
        <TD width=195 height=23><lt:Label res="res.label.forum.showonline" key="login_date"/></TD>
      </TR>
	</thead>
    <TBODY>
   <%		
com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
boolean isIPPositionDisplay = cfg.getBooleanProperty("forum.isIPPositionDisplay");
if (privilege.isMasterLogin(request)) {
	isIPPositionDisplay = true;
}
   
String id="",name="",ip="",logtime="",doing="",boardname="",myface="";
int layer = 1;
int i = 1;
int p = 0;
String RealPic = "";
Directory dir = new Directory();
Leaf leaf = null;
UserMgr um = new UserMgr();
UserDb user = null;
IPStoreDb ipd = new IPStoreDb();
while (oir.hasNext()) {
 	    ou = (OnlineUserDb)oir.next(); 
	    i++;
	    name = ou.getName();
		logtime = com.redmoon.forum.ForumSkin.formatDateTime(request, ou.getLogTime());
		doing = ou.getDoing();
		ip = ou.getIp();
		String ipRaw = ip;
		if (!privilege.isMasterLogin(request)) {
			p = ip.indexOf(".");
			p = ip.indexOf(".",p+1);
			ip = ip.substring(0,p)+".*.*";
		}
		
		String boardcode = StrUtil.getNullString(ou.getBoardCode());
		boardname = "";
		if (!boardcode.equals("")) {
			leaf = dir.getLeaf(boardcode);
			if (leaf==null)
				leaf = new Leaf();
			boardname = leaf.getName();
		}
		if (!ou.isGuest())
			user = um.getUser(name);
%>
      <TR align=center> 
	       <TD width=192 height=23 align="left">&nbsp; 
		  <%
		  if (ou.isGuest() || ou.isCovered()) {%>
			  	<img src="images/face/face.gif" width=16 height=16> 
		  <%} else {
		   	  if (user.getMyface().equals("")) {
				RealPic = user.getRealPic();
				if (user.getRealPic().equals(""))
					RealPic = "face.gif";
			  %>
					<img src="images/face/<%=RealPic%>" width=16 height=16> 
			  <%}else{%>
				  	<img src="<%=user.getMyfaceUrl(request)%>" width=16 height=16>
			  <%}
		  }
		  if (ou.isCovered()) {
		  		if (privilege.isMasterLogin(request)) {%>
		          <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=user.getNick()%></a>(<lt:Label res="res.label.forum.showonline" key="login_hide"/>) 
          <% 	}
		  		else {%>
				  <lt:Label res="res.label.forum.showonline" key="login_hide"/>
		  <%	}
		  } else {
			if (!ou.isGuest()) {
			%>
		        <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=user.getNick()%></a> 
		  	<%} else {%>
				<lt:Label res="res.label.forum.showonline" key="guest"/>
			<%}
		  }%></TD>
        <TD width=197 height=23>&nbsp;<a href="listtopic.jsp?boardcode=<%=StrUtil.UrlEncode(boardcode)%>"><%=boardname%></a></TD>
        <TD width=160 height=23><%=ip%></TD>
        <TD width=166>
		<%if (isIPPositionDisplay) {%>
		<%=StrUtil.getNullStr(ipd.getPosition(ipRaw))%>
		<%}%>
		&nbsp;</TD>
        <TD width=195 height=23><%=logtime%></TD>
      </TR>
<%}%>
    </TBODY>
  </TABLE>
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr> 
      <td width="2%" height="23">&nbsp;</td>
      <td height="23" valign="baseline"> <div align="right"> 
    <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock(request, "showonline.jsp?"+querystr));
	%>
	</div>	  </td>
    </tr>
  </table>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>