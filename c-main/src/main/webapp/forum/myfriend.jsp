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
<title><lt:Label res="res.label.forum.myfriend" key="myfriend"/> - <%=Global.AppName%></title>
</head>
<body>
<div id="wrapper">
<%@ include file="inc/header.jsp"%>
<div id="main">
  <table height="25" cellspacing="0" cellpadding="1" width="98%" align="center" border="1" class="tableCommon">
    <tbody>
      <tr>
        <td>
		<lt:Label res="res.label.forum.inc.position" key="cur_position"/>
        <a href="<%=request.getContextPath()%>/forum/index.jsp">
            <lt:Label res="res.label.forum.inc.position" key="forum_home"/>
            </a>&nbsp;<b>&raquo;</b>&nbsp;<a href="<%=request.getContextPath()%>/usercenter.jsp">
              <lt:Label res="res.label.forum.menu" key="user_center"/>
              </a></td>
      </tr>
    </tbody>
  </table>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
//安全验证
String querystring = StrUtil.getNullString(request.getQueryString());
String privurl=request.getRequestURL()+"?"+StrUtil.UrlEncode(querystring,"utf-8");
if (!privilege.isUserLogin(request))
{
	response.sendRedirect("../door.jsp");
	return;
}
%>
<%
UserFriendDb ufd = new UserFriendDb();

String op = StrUtil.getNullString(request.getParameter("op"));
if (op.equals("del"))
{
	UserFriendMgr ufm = new UserFriendMgr();
	boolean re = false;
	try {
		re = ufm.delFriend(request);
	}
	catch (ErrMsgException e) {
		out.println(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	if (re)
		out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "myfriend.jsp"));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
	return;
}

if (op.equals("add")) {
	boolean re = false;
	userservice us = new userservice();
	try {
		re = us.AddFriend(request);
	}
	catch (ErrMsgException e) {
		out.println(StrUtil.Alert_Back(e.getMessage()));
		return;
	}
	String msg;
	if (re)
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_operate_success") + SkinUtil.LoadString(request, "res.label.forum.myfriend", "appling")));
	else
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail"))); // "加为好友失败！";
	return;
}

String sql = "select id from sq_friend where name=" + StrUtil.sqlstr(privilege.getUser(request)) + " order by rq desc";
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
    <div class="tableTitle">
	<lt:Label res="res.label.forum.myfriend" key="myfriend"/>
	</div>
    <table class="per98" width="98%" border="0" cellspacing="0" cellpadding="0">
      <tr>
	  <form name=form1 action="myfriend.jsp?op=add" method="post">
        <td align="right"><lt:Label res="res.label.forum.myfriend" key="nick"/>
          <input name="friend" size=15><input type="submit" value="<lt:Label res="res.label.forum.myfriend" key="add_friend"/>"><input name="type" value="nick" type=hidden>
		</td>
	  </form>
      </tr>
    </table>
  <TABLE class="tableCommon" width="98%" border=0 align=center>
    <thead>
      <TR> 
        <TD width="16%" align="center"><lt:Label res="res.label.forum.myfriend" key="user_name"/></TD>
        <TD width="12%" align="center">QQ</TD>
        <TD width="10%" align="center"><lt:Label res="res.label.forum.myfriend" key="birthday"/></TD>
        <TD width="20%" align="center"><lt:Label res="res.label.forum.myfriend" key="address"/></TD>
        <TD width="14%" align="center"><lt:Label res="res.label.forum.myfriend" key="tel"/></TD>
		<TD width="12%" align="center">状态</TD>
        <TD width="16%" align="center"><lt:Label key="op"/></TD>
      </TR>
	</thead>
      <%
int state = 0;	
String name="",OICQ="",birthday="",address="",phone="",myface="";
String RealPic = "";
int i = 1;
UserDb ud = new UserDb();
while (ir.hasNext())
{
	i++;
	ufd = (UserFriendDb)ir.next();
	ud = ud.getUser(ufd.getFriend());
	name = ud.getName();
	OICQ = ud.getOicq();
	state = ufd.getState();
	if (OICQ==null)
		OICQ = "";
	birthday = DateUtil.format(ud.getBirthday(), "yyyy-MM-dd");
	address = ud.getAddress();
	phone = ud.getPhone();
	RealPic = StrUtil.getNullString(ud.getRealPic());
	myface = StrUtil.getNullString(ud.getMyface());
%>
      <TR bgColor=#f8f8f8> 
        <TD width="16%">
		&nbsp;
		<%if (myface.equals("")) {%>
		  <img src="images/face/<%=RealPic.equals("") ? "face.gif" : RealPic%>" width=16 height=16> 
		<%}else{%>
		  <img src="<%=ud.getMyfaceUrl(request)%>" width=16 height=16>
		<%}%>				
        <a href="../userinfo.jsp?username=<%=StrUtil.UrlEncode(name)%>"><%=ud.getNick()%></a>
		</TD>
        <TD width="12%" align="center"><%=(state==1)?OICQ:"***"%></TD>
        <TD width="10%" align="center">
		<%=(state==1)?birthday:"***"%></TD>
        <TD width="20%" align="center"><%=(state==1)?address:"***"%></TD>
        <TD width="14%" align="center"><%=(state==1)?phone:"***"%></TD>
		<TD width="12%" align="center">
		<%
			if(state == 0) {
		%>
		<font color="#FF0000">[申请中]</font>
		<%
			} else {
		%>
		[好友]
		<%
			}
		%>
		</TD>
        <TD width="16%" align="center"><a href="#" onClick="if (confirm('<lt:Label key="confirm_del"/>')) window.location.href='myfriend.jsp?op=del&delid=<%=ufd.getId()%>'">
          <lt:Label key="op_del"/>
        </a></TD>
      </TR>
<%
}
%>
    </TBODY>
  </TABLE>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
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
