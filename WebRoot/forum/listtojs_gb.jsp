<%@ page contentType="text/html;charset=gb2312"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%
MsgDb md = new MsgDb();
String boardcode = ParamUtil.get(request, "boardcode");
if (boardcode.equals("")) {
%>
document.write("°æ¿é±àÂë²»ÄÜÎª¿Õ£¡");
<%
	return;
}
int len = 20;
try {
	len = ParamUtil.getInt(request, "len");
}
catch (Exception e) {}

int start = 0;
try {
	start = ParamUtil.getInt(request, "start");
}
catch (Exception e) {}

int end = 10;
try {
	end = ParamUtil.getInt(request, "end");
}
catch (Exception e) {}

String sql = "select id from sq_thread where boardcode="+StrUtil.sqlstr(boardcode)+" and msg_level<=" + MsgDb.LEVEL_TOP_BOARD + " ORDER BY msg_level desc,redate desc";
ThreadBlockIterator irmsg = md.getThreads(sql, boardcode, start, end);
while (irmsg.hasNext()) {
	md = (MsgDb)irmsg.next();
%>
document.write('<a href="<%=Global.getRootPath()%>/forum/showtopic.jsp?rootid=<%=md.getId()%>"><%=StrUtil.getLeft(md.getTitle(), len)%></a>' + '<br>');
<%	
}
//<script src="http://www.zjrj.cn/forum/listtojs.jsp?boardcode=sqzw&len=10"></script>
%>