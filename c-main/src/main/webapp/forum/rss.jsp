<%@ page contentType="text/html;charset=utf-8"%><%@ page import="com.redmoon.forum.tools.*"%><%@ page import="cn.js.fan.util.ParamUtil"%><%
RSSGenerator rg = new RSSGenerator();
String boardCode = ParamUtil.get(request, "boardCode");
String op = ParamUtil.get(request, "op");
if (!op.equals("blog")) {
	if (boardCode.equals(""))
		rg.generateForumRSS(out, "rss_2.0", 20);
	else
		rg.generateBoardRSS(out, "rss_2.0", 20, boardCode);
}
else {
	long blogId = ParamUtil.getLong(request, "blogId");
	String blogUserDir = ParamUtil.get(request, "blogUserDir");
	rg.generateBlogRSS(out, "rss_2.0", 10, blogId, blogUserDir);
}
%>