<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
<title><lt:Label res="res.label.usercenter" key="friend_apply"/> - <%=Global.AppName%></title>
</head>
<body>
<div id="wrapper">
  <%@ include file="inc/header.jsp"%>
  <div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
//安全验证
String querystring = StrUtil.getNullString(request.getQueryString());
String privurl=request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");
if (!privilege.isUserLogin(request)) {
	response.sendRedirect("../door.jsp");
	return;
}
UserFriendDb ufd = new UserFriendDb();
String op = StrUtil.getNullString(request.getParameter("op"));
if (op.equals("del")) {
	int delid = ParamUtil.getInt(request, "id");
	ufd = ufd.getUserFriendDb(delid);
	boolean re = ufd.del();
	if (re)
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "myfriend_apply.jsp"));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	return;
}
else if(op.equals("accept")) {
	UserFriendMgr userFriendMgr = new UserFriendMgr();
	boolean re = false;
	try {
		re = userFriendMgr.accept(request);
	}
	catch (ErrMsgException e) {
		out.println(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re)
		out.println(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "myfriend.jsp"));
	else
		out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_operate_fail")));
	return;
} else if(op.equals("refuse")) {
	UserFriendMgr userFriendMgr = new UserFriendMgr();
	boolean re = false;
	try {
		re = userFriendMgr.refuse(request);
	}
	catch (ErrMsgException e) {
		out.println(StrUtil.Alert_Back(e.getMessage()));
	}
	if (re)
		out.println(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_operate_success"), "myfriend_apply.jsp"));
	else
		out.println(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_operate_fail")));
	return;
}

String sql = "select id from sq_friend where friend=" + StrUtil.sqlstr(privilege.getUser(request)) + " and state=0 order by rq desc";
int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
			
ListResult lr = ufd.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = null;
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}
%><br />
<div class="tableTitle"><lt:Label res="res.label.usercenter" key="friend_apply"/></div>
<TABLE width="98%" class="tableCommon80" border=0 align=center cellPadding=0 cellSpacing=1>
  <thead>
	<TD width="16%" align="center"><lt:Label res="res.label.forum.myfriend" key="user_name"/></TD>
	<TD width="14%" align="center">日期</TD>
	<TD width="16%" align="center"><lt:Label key="op"/></TD>
  </thead>
<%
int state = 0;	
String name="",myface="",RealPic = "";
int i = 1;
UserDb ud = new UserDb();
while (ir.hasNext()){
	i++;
	ufd = (UserFriendDb)ir.next();
	ud = ud.getUser(ufd.getName());
	name = ud.getName();
	state = ufd.getState();
	RealPic = StrUtil.getNullString(ud.getRealPic());
	myface = StrUtil.getNullString(ud.getMyface());
%>
      <TR> 
        <TD width="16%" height=23>
		<%if (myface.equals("")) {%>
		  <img src="images/face/<%=RealPic.equals("") ? "face.gif" : RealPic%>" width=16 height=16> 
		<%}else{%>
		  <img src="<%=ud.getMyfaceUrl(request)%>" width=16 height=16>
		<%}%>				
        <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>" target="_blank"><%=ud.getNick()%></a>
		</TD>
        <TD width="14%" height=23 align="center"><%=ForumSkin.formatDateTime(request, ufd.getRq())%></TD>
		<TD width="16%" height=23 align="center">
		<a href="myfriend_apply.jsp?op=accept&id=<%=ufd.getId()%>">接受</a>&nbsp; <a title="拒绝好友时，仅向对方发送一条拒绝短消息" href="myfriend_apply.jsp?op=refuse&id=<%=ufd.getId()%>">拒绝</a>&nbsp;&nbsp;<a href="javascript:if (confirm('<lt:Label key="confirm_del"/>')) window.location.href='myfriend_apply.jsp?op=del&id=<%=ufd.getId()%>'">删除</a>
		</TD>
      </TR>
<%}%>
</TABLE>
<table class="per80" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="right">
    <%
	  String querystr = "";
 	  out.print(paginator.getCurPageBlock("myfriend.jsp?"+querystr));
	%>
	</td>
  </tr>
</table>
</div>
<%@ include file="inc/footer.jsp"%>
</div>
</body>
</html>
