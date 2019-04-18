<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.base.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.music.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String op = ParamUtil.get(request, "op");
MusicUserDb mud = new MusicUserDb();
if (op.equals("del")) {
	long id = ParamUtil.getLong(request, "id");
	mud = (MusicUserDb)mud.getQObjectDb(new Long(id));
	if (mud.del()) {
		out.print(StrUtil.Alert_Redirect("操作成功！", "music_order_list.jsp"));
	}
	else
		out.print(StrUtil.Alert_Back("操作失败！"));
	return;
}  
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="GENERATOR" content="Microsoft FrontPage 4.0">
<meta name="ProgId" content="FrontPage.Editor.Document">
<link href="default.css" rel="stylesheet" type="text/css">
<title>点歌记录</title>
<style type="text/css">
<!--
body {
	margin-top: 0px;
	margin-left: 0px;
	margin-right: 0px;
}
-->
</style></head>
<body>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<div id="newdiv" name="newdiv">
  <table width='100%' cellpadding='0' cellspacing='0' >
    <tr>
      <td class="head">点歌记录</td>
    </tr>
  </table>
  <%
		String sql = "select id from sq_forum_music_user order by order_date desc";
		int pagesize = 20;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		
		long total = mud.getQObjectCount(sql);
		com.cloudwebsoft.framework.base.QObjectBlockIterator oir = mud.getQObjects(sql, (curpage-1)*pagesize, curpage*pagesize);
		
		paginator.init(total, pagesize);
		
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}
%>
  <br>
  <TABLE width="98%" border=0 align=center cellPadding=3 cellSpacing=1>
    <TBODY>
      <TR align=center bgColor=#f8f8f8 class="td_title">
        <TD width="17%" height=23 class="thead">歌曲</TD>
        <TD width="24%" height=23 class="thead">点歌用户</TD>
        <TD width="24%" height=23 class="thead">用户</TD>
        <TD width="22%" class="thead">点歌日期</TD>
        <TD width="13%" height=23 class="thead">操作</TD>
      </TR>
      <%
MusicFileDb mfd2 = new MusicFileDb();
UserMgr um = new UserMgr();
while (oir.hasNext()) {
 	    mud = (MusicUserDb)oir.next();
		MusicFileDb mfd = mfd2.getMusicFileDb(mud.getLong("music_id"));
		UserDb toUser = um.getUser(mud.getString("user_name"));
		UserDb fromUser = um.getUser(mud.getString("order_user"));
%>
      <TR align=center bgColor=#f8f8f8>
        <TD height=23 align="left"><%=mfd.getName()%></TD>
        <TD height=23><%=fromUser.getNick()%></TD>
        <TD height=23><%=toUser.getNick()%></TD>
        <TD><%=ForumSkin.formatDateTime(request, mud.getDate("order_date"))%></TD>
        <TD height=23><a href="#" onClick="if(confirm('您确定要删除吗？')) window.location.href='music_order_list.jsp?op=del&id=<%=mud.getLong("id")%>'">删除</a></TD>
      </TR>
      <%}%>
    </TBODY>
  </TABLE>
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr>
      <td width="2%" height="23">&nbsp;</td>
      <td height="23" valign="baseline"><div align="right">
          <%
	  String querystr = "";
 	  out.print(paginator.getPageBlock(request, "music_order.jsp?"+querystr));
	%>
      </div></td>
    </tr>
  </table>
</div>
</body>
</html>
