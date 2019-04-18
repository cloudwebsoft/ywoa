<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.forum.person.*" %>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ page import="cn.js.fan.module.cms.plugin.wiki.*" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
int id = ParamUtil.getInt(request, "id");
UserDesktopSetupDb udsd = new UserDesktopSetupDb();
udsd = udsd.getUserDesktopSetupDb(id);

String userName = privilege.getUser(request);
%>
<div id="drag_<%=id%>" dragTitle="<%=udsd.getTitle()%>" count="<%=udsd.getCount()%>" wordCount="<%=udsd.getWordCount()%>" class="portlet drag_div bor" >
<div class="portlet_content" style="margin:0px; padding:0px">

    <div id="drag_<%=id%>_h" class="box">
    	<span class="titletxt"><img src="<%=SkinMgr.getSkinPath(request)%>/images/titletype.png" width="8" height="12" /><a href="cms/plugin/wiki/admin/wiki_score_rank.jsp">wiki得分排行</a></span>
        <div class="opbut-1"><img onclick="mini('<%=udsd.getId()%>')" title="最小化" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/minimization.png" align="absmiddle" /></div>
        <div class="opbut-2"><img onclick="mod('<%=udsd.getId()%>')" title="修改显示方式" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/configure.png" align="absmiddle" /></div>
        <div class="opbut-3"><img onclick="clo('<%=udsd.getId()%>')" title="关闭" class="btnIcon" src="<%=SkinMgr.getSkinPath(request)%>/images/close.png" align="absmiddle" /></div>
        </div>
<%
UserPropDb upd = new UserPropDb();
String sql = "select name from sq_user_prop order by wiki_score desc";
ListResult lr = upd.listResult(sql, 1, udsd.getCount());
Iterator ir = lr.getResult().iterator();
%>
<table id="drag_<%=udsd.getId()%>_c" class="tabStyle_1" style="width:100%" cellSpacing="0" cellPadding="3" width="95%" align="center">
  <thead>
    <tr>
      <td noWrap width="49%">用户</td>
      <td noWrap width="51%">得分</td>
    </tr>
  </thead>
  <tbody>
<%
com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
while (ir.hasNext()) {
	upd = (UserPropDb)ir.next();
	com.redmoon.oa.person.UserDb user = um.getUserDb(upd.getString("name"));
%>
    <tr onMouseOver="this.className='tbg1sel'" onMouseOut="this.className='tbg1'" class="tbg1">
      <td>
      <a href="<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(upd.getString("name"))%>" target="_blank"><%=user.getRealName()%></a></td>
      <td align="center"><%=upd.getDouble("wiki_score")%></td>
    </tr>
<%}%>
  </tbody>
</table>

</div>
</div>
