<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%
MsgDb md = new MsgDb();
String boardcode = "sqzw";
boardcode = ParamUtil.get(request, "boardcode");
if (boardcode.equals(""))
	boardcode = "sqzw";
int len = 10;
try {
	len = ParamUtil.getInt(request, "len");
}
catch (ErrMsgException e) {
}
String sql = "select id from sq_thread where boardcode="+StrUtil.sqlstr(boardcode)+" and msg_level<=" + MsgDb.LEVEL_TOP_BOARD + " ORDER BY msg_level desc,redate desc";
ThreadBlockIterator irmsg = md.getThreads(sql, boardcode, 0, len);
while (irmsg.hasNext()) {
	md = (MsgDb)irmsg.next();
%>
document.write('<a href="<%=Global.getRootPath()%>/forum/showtopic.jsp?rootid=<%=md.getId()%>"><%=md.getTitle()%></a>' + '&nbsp;[<a href="<%=Global.getRootPath()%>/userinfo.jsp?username=<%=StrUtil.UrlEncode(md.getName())%>"><%=md.getName()%></a>&nbsp;<%=DateUtil.format(md.getAddDate(), "yy-MM-dd HH:mm")%>]<br>');
<%	
}
%>
//<script src="http://www.zjrj.cn/forum/listtojs.jsp?boardcode=sqzw&len=10"></script>

