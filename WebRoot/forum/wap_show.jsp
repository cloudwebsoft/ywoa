<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>看贴 - <%=Global.AppRealName%></title>
<%@ include file="../inc/nocache.jsp"%>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
long rootid = ParamUtil.getLong(request, "rootid");
MsgDb rootMsgDb = new MsgDb();
rootMsgDb = rootMsgDb.getMsgDb(rootid);
if (!rootMsgDb.isLoaded()) {
	out.print("贴子不存在！");
	return;
}

Leaf lf = new Leaf();
lf = lf.getLeaf(rootMsgDb.getboardcode());
%>
<a href="wap/index.jsp">首页</a>&nbsp;&nbsp;<a href="wap/list.jsp?boardcode=<%=StrUtil.UrlEncode(lf.getCode())%>"><%=lf.getName()%></a>
<a href="wap/reply.jsp?replyid=<%=rootid%>">回复</a><br /><br />
<%
String showUserName = ParamUtil.get(request, "showUserName");
String sql = SQLBuilder.getShowtopicSql(request, response, out, rootMsgDb, showUserName);	// "select id from sq_message where rootid=" + rootid + " ORDER BY lydate asc"; //orders"; 这样会使得顺序上不按时间，平板式时会让人觉得奇怪

int pagesize = 10;
long total = rootMsgDb.getMsgCount(sql, rootMsgDb.getboardcode(), rootid);
		
Paginator paginator = new Paginator(request, total, pagesize);
int curpage = paginator.getCPage(request);
//设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
		
int start = (curpage-1)*pagesize;
int end = curpage*pagesize;

UserMgr um = new UserMgr();		
MsgBlockIterator irmsg = rootMsgDb.getMsgs(sql, rootMsgDb.getboardcode(), rootid, start, end);
while (irmsg.hasNext()) {
	MsgDb md = (MsgDb)irmsg.next();
	UserDb user = um.getUser(md.getName());
	if (md.getId()==rootid) {
%>
标题：<%=md.getTitle()%><br />
作者：<%=user.isLoaded()?user.getNick():"匿名"%><br />
日期：<%=DateUtil.format(md.getAddDate(), "yy-MM-dd HH:mm")%><br />
内容：<%=StrUtil.ubb(request, StrUtil.getLeft(md.getContent(), 100), true)%><br />
<%	
	}
	else {%>
<br />	
回复：<br />
<%=md.getContent()%><br />
<%=user.isLoaded()?user.getNick():"匿名"%><br />
<%	}
}
%><br /><br />
<a href="wap/index.jsp">首页</a>&nbsp;&nbsp;<a href="wap/list.jsp?boardcode=<%=StrUtil.UrlEncode(lf.getCode())%>"><%=lf.getName()%></a>
<a href="wap/reply.jsp?replyid=<%=rootid%>">回复</a>
</body></html>