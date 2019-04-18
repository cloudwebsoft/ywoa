<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.group.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);

String catalogCode = ParamUtil.get(request, "catalogCode");
com.redmoon.forum.plugin.group.Directory dir = new com.redmoon.forum.plugin.group.Directory();
com.redmoon.forum.plugin.group.Leaf curLeaf = null;
if (!catalogCode.equals("")) {
	curLeaf = dir.getLeaf(catalogCode);
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<title>朋友圈</title>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<link href="../../<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</HEAD>
<BODY>
<div id="wrapper">
<%@ include file="../../inc/header.jsp"%>
<div id="main">
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
  <table width="100%" align=center class="tableCommon">
    <tbody>
      <tr>
        <td height=25>
		<span style="float:left">
		&nbsp;<a>
          <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
          </a>&nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp">
            <lt:Label res="res.label.forum.inc.position" key="forum_home"/>
          </a>&nbsp;<b>&raquo;</b>&nbsp;<a href="group_list.jsp">朋友圈</a>
          <%if (curLeaf!=null) {%>
          &nbsp;<b>&raquo;</b>&nbsp;<%=curLeaf.getName()%>
          <%}%>
		</span>
		<span style="float:right"><a href="group_list.jsp?listType=member">成员排行</a>&nbsp;&nbsp;&nbsp;<a href="group_list.jsp?listType=topic">话题排行</a></span>
		</td>
      </tr>
    </tbody>
  </table>
  <br>
  <table width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td width="21%" height="69" valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td>
		  <%
		  	UserMgr um = new UserMgr();
			if (privilege.isUserLogin(request)) {
				String userName = privilege.getUser(request);	  
				UserDb user = um.getUser(userName);
		  %>
		    <table width="100%" cellpadding="0" cellspacing="0" class="tableCommon">
              <tr>
                <td height="22"><%=user.getNick()%>&nbsp;朋友圈欢迎您</td>
              </tr>
              <tr>
                <td height="24">
				<a href="group_mine.jsp?listType=listmine">我创建的朋友圈</a></td>
              </tr>
              <tr>
                <td height="24"><a href="group_mine.jsp?listType=listattend">我加入的朋友圈</a></td>
              </tr>
              <tr>
                <td height="24"><a href="group_create.jsp">创建新朋友圈</a></td>
              </tr>
              <tr>
                <form action="group_list.jsp?op=search" method="post">
                  <td height="24"><input name="what" size="20">
                      <input type="submit" value="搜索">                  </td>
                </form>
              </tr>
            </table>
		  <%}%>
		    <table width="100%" class="tableCommon" cellpadding="0" cellspacing="0">
		  	<tr>
		  	  <td align="center">朋友圈分类</td>
		  	  </tr><tr><td height="24" style="padding-left:5px"><a href="group_list.jsp">全部</a></td>
		  	  </tr><%
		  com.redmoon.forum.plugin.group.LeafChildrenCacheMgr lcc = new com.redmoon.forum.plugin.group.LeafChildrenCacheMgr(Leaf.CODE_ROOT);
		  Iterator ir = lcc.getDirList().iterator();
		  while (ir.hasNext()) {
		  	com.redmoon.forum.plugin.group.Leaf lf = (com.redmoon.forum.plugin.group.Leaf)ir.next();
		  %>
		  	<tr><td height="24" style="padding-left:5px"><a href="group_list.jsp?catalogCode=<%=StrUtil.UrlEncode(lf.getCode())%>"><%=lf.getName()%></a></td>
		  	</tr>
		  <%
		  }
		  %></table>
		  </td>
        </tr>
      </table></td>
      <td width="79%" valign="top">
	  <%
		int pagesize = 10;
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();

		GroupDb gd = new GroupDb();
		String sql = GroupSQLBuilder.getListGroupSql(request);
		long total = gd.getQObjectCount(sql, catalogCode);
		paginator.init(total, pagesize);
		String op = ParamUtil.get(request, "op");
		String listType = ParamUtil.get(request, "listType");
		String what = ParamUtil.get(request, "what");
		String querystr = "listType=" + listType + "&op=" + op + "&what=" + StrUtil.UrlEncode(what);
	  %>
	  <table width="100%" border="1" cellspacing="0" cellpadding="3" class="tableCommon">
	  <thead>
        <tr>
          <td width="21%" align="center">LOGO</td>
          <td width="27%" align="center">名称</td>
          <td width="14%" align="center">创建者</td>
          <td width="10%" align="center">话题</td>
          <td width="9%" align="center">相片</td>
          <td width="10%" align="center">成员</td>
          <td width="9%" align="center">状态</td>
        </tr>
		</thead>
		<%		
		QObjectBlockIterator qi = gd.getQObjects(sql, catalogCode, (curpage-1)*pagesize, curpage*pagesize);
		while (qi.hasNext()) {
			gd = (GroupDb)qi.next();
			String logoUrl = gd.getLogoUrl(request);
		%>
		<tr><td height="53" rowspan="2" align="center">
		<%if (!logoUrl.equals("")) {%>
		<img src="<%=logoUrl%>" width=75 height=50>
		<%}%>
		</td>
		  <td><img src="images/group.gif">&nbsp;
		  <%
		  String clrName = StrUtil.toHtml(gd.getString("name"));
		  String color = StrUtil.getNullStr(gd.getString("color"));
		  if (!color.equals(""))
		  	clrName = "<font color=" + color + ">" + clrName + "</font>";
		  if (gd.getInt("is_bold")==1)
		  	clrName = "<strong>" + clrName + "</strong>";			
		  %>		  
		  <a href="group.jsp?id=<%=gd.getLong("id")%>">
		  <%=clrName%>
		  </a></td>
		  <td align="center"><%=um.getUser(gd.getString("creator")).getNick()%></td>
		  <td align="center"><%=gd.getInt("msg_count")%></td>
		  <td align="center"><%=gd.getInt("photo_count")%></td>
		  <td align="center"><%=gd.getInt("user_count")%></td>
		  <td align="center">
		  <%if (gd.getInt("is_open")==1) {%>
		  <a href="group_apply.jsp?id=<%=gd.getLong("id")%>">加入</a>
		  <%}else{%>
		  关闭
		  <%}%>
		  </td>
		</tr>
		<tr>
		  <td colspan="6"><span style="float:right">创建日期：<%=ForumSkin.formatDate(request, gd.getDate("create_date"))%></span>介绍：<%=gd.getString("description")%>&nbsp;</td>
		  </tr>
		<%}%>
      </table>
		  <div align="right" style="padding-top:5px">
            <%
		out.print(paginator.getCurPageBlock(request, "group_list.jsp?" + querystr, "up"));
	  %>
          </div>	  
	  </td>
    </tr>
  </table>
</div>
<%@ include file="../../inc/footer.jsp"%>
</div>
</BODY></HTML>
