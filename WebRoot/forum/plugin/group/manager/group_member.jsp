<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String blogUserDir = ParamUtil.get(request, "blogUserDir");
String skinPath = "skin/default";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE><%=Global.AppName%></TITLE>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<%@ include file="../../../../inc/nocache.jsp"%>
<LINK href="../../../../common.css" type=text/css rel=stylesheet>
<SCRIPT>
function openWin(url,width,height) {
  var newwin = window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,top=50,left=120,width="+width+",height="+height);
}
</SCRIPT>
<META content="MSHTML 6.00.2600.0" name=GENERATOR>
<style type="text/css">
<!--
.STYLE1 {color: #FFFFFF}
-->
</style>
</HEAD>
<BODY leftmargin="0" topMargin=0>
<%
if (!privilege.isUserLogin(request)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

long id = ParamUtil.getLong(request, "id", -1);

if (!GroupPrivilege.isManager(request, id)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

GroupDb gd = new GroupDb();
gd = (GroupDb)gd.getQObjectDb(new Long(id));
if (gd==null) {
	out.print(StrUtil.Alert_Back("该朋友圈不存在!")); // SkinUtil.LoadString(request,"res.label.blog.user.userconfig", "activate_blog_fail")));
	return;
}

String user = privilege.getUser(request);
if (!GroupPrivilege.isManager(request, id)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

GroupUserDb bgu = new GroupUserDb();

String op = ParamUtil.get(request, "op");
if (op.equals("modify")) {
	QObjectMgr qom = new QObjectMgr();
	String userName = ParamUtil.get(request, "user_name");
	bgu = bgu.getGroupUserDb(id, userName);
	try {
		if (qom.save(request, bgu, "plugin_group_user_modify")) {
			bgu.refreshList();
			out.print("<BR>");
			out.print("<BR>");
			out.print(StrUtil.waitJump(SkinUtil.LoadString(request, "info_op_success"), 1, "group_member.jsp?id=" + id));
			return;
		}
		else {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.Alert(e.getMessage()));
	}
}
else if (op.equals("del")) {
	GroupUserDb gu = new GroupUserDb();
	String userName = ParamUtil.get(request, "userName");
	gu = gu.getGroupUserDb(id, userName);
	try {
		if (gu.del()) {
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "group_member.jsp?id=" + id));
			return;
		}
	}
	catch (ResKeyException e) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "info_op_fail")));
		return;
	}
}
%>
<table width="100%" border="0">
  <tr>
    <td align="center">
	&nbsp;<a href="group_member.jsp?id=<%=id%>">全部用户</a>
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="group_member.jsp?id=<%=id%>&show=checked">已审核通过用户</a>
	&nbsp;&nbsp;&nbsp;&nbsp; <a href="group_member.jsp?id=<%=id%>&show=not_checked">未审核用户</a>&nbsp;</td>
  </tr>
  <tr>
    <td height="1" align="center" background="../../images/comm_dot.gif"></td>
  </tr>
</table>
<TABLE borderColor=#edeced cellSpacing=0 cellPadding=1 width="98%" align=center 
border=1>
    <TBODY>
      <TR height=25> 
        <TD height="26" colSpan=3 align=center noWrap bgcolor="#617AA9"><span class="STYLE1">用户名</span></TD>
        <TD width="27%" align=center noWrap bgcolor="#617AA9"><span class="STYLE1">加入理由</span></TD>
        <TD width=11% align=center noWrap bgcolor="#617AA9"><span class="STYLE1">申请日期</span></TD>
        <TD width=9% align=center noWrap bgcolor="#617AA9"><span class="STYLE1">话题数</span></TD>
        <TD width=9% align=center noWrap bgcolor="#617AA9"><span class="STYLE1">相片数</span></TD>
        <TD width=8% height="26" align=center noWrap bgcolor="#617AA9"><span class="STYLE1">管理员</span></TD>
        <TD width=7% align=center noWrap bgcolor="#617AA9"><span class="STYLE1">审核通过</span></TD>
        <TD width=17% align=center noWrap bgcolor="#617AA9"><span class="STYLE1">操作</span></TD>
      </TR>
<%
UserMgr um = new UserMgr();
String sql = "select group_id,user_name from " + bgu.getTable().getName() + " where group_id=" + id + " order by add_date desc";
String show = ParamUtil.get(request, "show");
if (show.equals("checked")) {
	sql = "select group_id,user_name from " + bgu.getTable().getName() + " where group_id=" + id + " and check_status=" + GroupUserDb.CHECK_STATUS_PASSED + " order by add_date desc";
}
else if (show.equals("not_checked")) {
	sql = "select group_id,user_name from " + bgu.getTable().getName() + " where group_id=" + id + " and check_status=" + GroupUserDb.CHECK_STATUS_NOT + " order by add_date desc";
}
long count = bgu.getQObjectCount(sql);
QObjectBlockIterator qi = bgu.getQObjects(sql, 0, (int)count);
int i = 0;
while (qi.hasNext()) {
	bgu = (GroupUserDb)qi.next();
	i++;
%>
      <TR height=25>
	  <form name="form<%=i%>" action="?op=modify" method="post">
        <TD height="26" colSpan=3 align=middle noWrap bgcolor="#FFFFFF">
		<a target="_blank" href="../../userinfo.jsp?username=<%=StrUtil.UrlEncode(bgu.getString("user_name"))%>"><%=um.getUser(bgu.getString("user_name")).getNick()%></a>
		<input type="hidden" name="group_id" value="<%=id%>">
		<input type="hidden" name="user_name" value="<%=bgu.getString("user_name")%>">		<input type="hidden" name="id" value="<%=id%>"></TD>
        <TD align=middle noWrap bgcolor="#FFFFFF"><%=StrUtil.getNullStr(bgu.getString("apply_reason"))%>&nbsp;</TD>
        <TD align=center noWrap bgcolor="#FFFFFF"><%=ForumSkin.formatDate(request, bgu.getDate("add_date"))%></TD>
        <TD align=center noWrap bgcolor="#FFFFFF"><%=bgu.getInt("msg_count")%><input type="hidden" name="msg_count" value="<%=bgu.getInt("msg_count")%>"></TD>
        <TD align=center noWrap bgcolor="#FFFFFF">
		<%=bgu.getInt("photo_count")%>
		<input type="hidden" name="photo_count" value="<%=bgu.getInt("photo_count")%>">
		</TD>
        <TD height="26" align=center noWrap bgcolor="#FFFFFF">
		<input name="priv_all" value="1" type="checkbox" <%=bgu.getString("priv_all").equals("1")?"checked":""%>>		</TD>
        <TD align=center noWrap bgcolor="#FFFFFF"><input name="check_status" value="1" type="checkbox" <%=bgu.getString("check_status").equals("1")?"checked":""%>></TD>
        <TD align=middle noWrap bgcolor="#FFFFFF"><input name="submit" type=submit value="<lt:Label key="submit"/>">&nbsp;<input onClick="if (confirm('<lt:Label key="confirm_del"/>')) window.location.href='group_member.jsp?op=del&id=<%=id%>&userName=<%=StrUtil.UrlEncode(bgu.getString("user_name"))%>'" type=button value="<lt:Label key="op_del"/>"></TD>
	  </form>
      </TR>
<%}%>	  
    </TBODY>
</TABLE>
</BODY></HTML>
