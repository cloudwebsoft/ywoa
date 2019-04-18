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
String skincode = UserSet.getSkin(request);
if (skincode.equals(""))
	skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
if (skin==null)
	skin = skm.getSkin(UserSet.defaultSkin);
String skinPath = skin.getPath();

String listType = ParamUtil.get(request, "listType");
if (listType.equals(""))
	listType = "listmine";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
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
  <TABLE cellSpacing=0 cellPadding=1 rules=rows width="98%" align=center class="tableCommon">
  <TBODY>
  <TR>
        <TD height=25>&nbsp;<img src="../../images/userinfo.gif" width="9" height="9">&nbsp;<a>
          <lt:Label res="res.label.forum.inc.position" key="cur_position"/>
        </a>&nbsp;<a href="<%=request.getContextPath()%>/forum/index.jsp"><lt:Label res="res.label.forum.inc.position" key="forum_home"/>
        </a>&nbsp;<B>&raquo;</B>
        <a href="group_list.jsp">朋友圈</a>&nbsp;<B>&raquo;</B>
		<%if (listType.equals("listmine")) {%>
		我创建的朋友圈
		<%}else{%>
		我加入的朋友圈
		<%}%>
		</TD>
        </TR>
  </TBODY></TABLE>
  <br>
  <table class="per100" width="98%" border="0" cellspacing="0" cellpadding="0">
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
		    <table class="tableCommon" width="100%" border="1" cellpadding="0" cellspacing="0">
              <tr>
                <td height="22" class="td_title"><%=user.getNick()%>&nbsp;朋友圈欢迎您</td>
              </tr>
              <tr>
                <td height="24"><a href="group_mine.jsp?listType=listmine">我创建的朋友圈</a></td>
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
		    <table width="100%" border="1" cellpadding="0" cellspacing="0" class="tableCommon">
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
		  %>
		  </table>
		  </td>
        </tr>
      </table></td>
      <td width="79%" valign="top">
	  <%
	  Privilege pvg = new Privilege();
	  GroupDb gd = new GroupDb();
	  String sql;
	  if (listType.equals("listmine"))
	  	sql = gd.getTable().getSql("listmine").replaceFirst("\\?", StrUtil.sqlstr(pvg.getUser(request)));
	  else
		sql = gd.getTable().getSql("listattend").replaceFirst("\\?", StrUtil.sqlstr(pvg.getUser(request)));
	  
	  Vector v = gd.list(sql);
	  %>
	  <table width="100%" border="1" cellspacing="0" cellpadding="3" class="tableCommon">
        <thead>
          <td width="21%" align="center">LOGO</td>
          <td width="27%" align="center">名称</td>
          <td width="14%" align="center">类别</td>
          <td width="10%" align="center">话题</td>
          <td width="9%" align="center">相片</td>
          <td width="10%" align="center">成员</td>
          <td width="9%" align="center">状态</td>
        </thead>
		<%		
		com.redmoon.forum.plugin.group.Directory dir = new com.redmoon.forum.plugin.group.Directory();
		ir = v.iterator();
		while (ir.hasNext()) {
			gd = (GroupDb)ir.next();
		%>
		<tr><td height="53" rowspan="2" align="center"><img src="<%=gd.getLogoUrl(request)%>" width=75 height=50></td>
		  <td><img src="images/group.gif">&nbsp;<a href="group.jsp?id=<%=gd.getLong("id")%>" target="_blank"><%=StrUtil.toHtml(gd.getString("name"))%></a></td>
		  <td align="center">
		  <%
			com.redmoon.forum.plugin.group.Leaf lf = dir.getLeaf(gd.getString("catalog_code"));
			if (lf!=null) {%>
				<a href="group.jsp?id=<%=gd.getLong("id")%>"><%=lf.getName()%></a>
			<%}
		  %>
		  </td>
		  <td align="center"><%=gd.getInt("msg_count")%></td>
		  <td align="center"><%=gd.getInt("photo_count")%></td>
		  <td align="center"><%=gd.getInt("user_count")%></td>
		  <td align="center"><%if (gd.getInt("is_open")==1) {%>
            开启
            <%}else{%>
			关闭
			<%}%>		</td>
		</tr>
		<tr>
		  <td colspan="6"><span style="float:right">创建日期：<%=ForumSkin.formatDate(request, gd.getDate("create_date"))%></span>介绍：<%=gd.getString("description")%></td>
		  </tr>
		<%}%>
      </table>
	  </td>
    </tr>
  </table>
<br />
</div>
<%@ include file="../../inc/footer.jsp"%>
</div>
</BODY></HTML>
