<%@ page contentType="text/html;charset=utf-8"%><%@ page import="com.redmoon.forum.util.*"%><%@ page import="com.redmoon.forum.*"%><%@ page import="cn.js.fan.util.*"%><%
String visitboardcode = (String)request.getAttribute("boardcode");
// 论坛首页访问统计
if (visitboardcode!=null && visitboardcode.equals(com.redmoon.forum.Leaf.CODE_ROOT)) {
	VisitLogMgr.logBoardVisit(request, visitboardcode, com.redmoon.forum.Privilege.getUser(request));
}

MsgDb visitRootMsgDb = (MsgDb)request.getAttribute("rootMsgDb");
if (visitRootMsgDb!=null) {
	String isIncreaseHit = (String)request.getAttribute("isIncreaseHit");
	// 当showtopic不在第一页时，isIncreaseHit为0，在showblog.jsp、showtopic.jsp、showtopic_tree.jsp中用到
	if (isIncreaseHit==null) {
		if (visitRootMsgDb.isRootMsg()) {
			visitRootMsgDb.increaseHit();
		}
	}
	
	VisitLogMgr.logTopicVisit(request, visitRootMsgDb, com.redmoon.forum.Privilege.getUser(request));
	VisitLogMgr.logBoardVisit(request, visitRootMsgDb.getboardcode(), com.redmoon.forum.Privilege.getUser(request));
}
%>