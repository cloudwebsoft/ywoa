<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%
long id = ParamUtil.getLong(request, "id");
GroupDb gd = new GroupDb();
gd = (GroupDb)gd.getQObjectDb(new Long(id));
if (gd==null) {
	return;
}

UserMgr um = new UserMgr();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3c.org/TR/1999/REC-html401-19991224/loose.dtd">
<HTML xmlns="http://www.w3.org/1999/xhtml">
<HEAD id=Head1><TITLE><%=gd.getString("name")%> - <%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8"><LINK 
href="<%=GroupSkin.getSkin(gd.getString("skin_code")).getPath()%>/css.css" type=text/css 
rel=stylesheet>
<META content="MSHTML 6.00.2900.3132" name=GENERATOR></HEAD>
<BODY>
<%@ include file="group_header.jsp"%>
<DIV class="content xw">
<%@ include file="group_left.jsp"%>
<DIV class=rw ?>
<DIV class="admin block">
<DIV class=title>
<DIV class=cName>
<%
String kind = ParamUtil.get(request, "kind");
if (kind.equals("activeUser")) {%>
活跃圈友&nbsp;
<%}else{%>
最新加入
<%}%>
</DIV></DIV>
<DIV class=txt id=memberList>
<DIV class=memberNote style="COLOR: #666">
<DIV class=l>注：<IMG src="skin/default/owner.gif"> - 圈主 
<IMG src="skin/default/manager.gif"> - 管理员 <IMG 
src="skin/default/member.gif"> - 成员 <IMG 
src="skin/default/guest.gif"> - 待批准</DIV>
<DIV class=r>排序方式： <A id=LinkButton1 href="group_member.jsp?id=<%=id%>&kind=activeUser">活跃成员</A> <A id=LinkButton2 
href="group_member.jsp?id=<%=id%>&kind=newUser">最新加入</A> </DIV>
<DIV class=hackbox></DIV></DIV>
<TABLE class=groupList1>
  <THEAD>
  <TR>
    <TH class=image style="WIDTH: 75px">照片</TH>
    <TH class=membername>昵称</TH>
    <TH class=sex style="WIDTH: 52px">性别</TH>
    <TH class=joinTime>加入时间</TH></TR></THEAD>
  <TBODY>
<%
String sql;
GroupUserDb gud = new GroupUserDb();
if (kind.equals("newUser")) {
	sql = gud.getListUserSql(id, "newUser");
}
else {
	sql = gud.getListUserSql(id, "activeUser");
}
int pagesize = 10;
long total = gud.getQObjectCount(sql, "" + id);
Paginator paginator = new Paginator(request, total, pagesize);
int curpage = paginator.getCurPage();

QObjectBlockIterator obi = gud.getQObjects(sql, ""+id, (curpage-1)*pagesize, curpage*pagesize);
UserDb user = null;
while (obi.hasNext()) {
	gud = (GroupUserDb)obi.next();
	user = um.getUser(gud.getString("user_name"));
%>  
  <TR>
    <TD class=image style="WIDTH: 75px">
	<%if (user.getMyface().equals("")) {%>
		<img src="../../images/face/<%=user.getRealPic()%>" width="32" height="32"> 
	<%}else{%>
		<img src="<%=user.getMyfaceUrl(request)%>"> 
	<%}%>
	</TD>
    <TD class=membername>
	<%if (user.getName().equals(gd.getString("creator"))) {%>
	<IMG alt=圈主 src="skin/default/owner.gif">
	<%}else if (gud.getInt("priv_all")==1) {%>
	<IMG alt=成员 src="skin/default/manager.gif">
	<%}else if (gud.getInt("check_status")==GroupUserDb.CHECK_STATUS_PASSED) {%>
	<IMG alt=成员 src="skin/default/member.gif">
	<%}else{%>
	<IMG alt=成员 src="skin/default/guest.gif">
	<%}%>
	  <A 
      href="<%=request.getContextPath()%>/userinfo.jsp?username=<%=StrUtil.UrlEncode(user.getName())%>" target="_blank"><%=StrUtil.toHtml(user.getNick())%> </A></TD>
    <TD class=sex style="WIDTH: 52px">
<%
		String Gender = user.getGender();
		if (Gender.equals("M"))
			Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_man"); // "男";
		else if (Gender.equals("F"))
			Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_woman"); // "女";
		else
			Gender = SkinUtil.LoadString(request, "res.label.forum.showtopic", "sex_none"); // "不详";
%>
<%=Gender%>
	</TD>
    <TD class=joinTime style="COLOR: #666"><%=com.redmoon.forum.ForumSkin.formatDateTime(request, gud.getDate("add_date"))%></TD></TR>
<%}%>	
</TBODY></TABLE>
<DIV class=more style="PADDING-BOTTOM: 5px">
<DIV id=AspNetPager1>
<%
	  String querystr = "id=" + id;
 	  out.print(paginator.getPageBlock(request, "group_member.jsp?"+querystr));
%>
</DIV>
</DIV></DIV></DIV></DIV></DIV>
<%@ include file="group_footer.jsp"%>
</BODY></HTML>
