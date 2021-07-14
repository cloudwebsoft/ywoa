<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%
String dirCodeTop = ParamUtil.get(request, "dir_code");
if (dirCodeTop.equals("")) {
	dirCodeTop = ParamUtil.get(request, "dirCode");
}
if (dirCodeTop.equals("")) {
	dirCodeTop = Leaf.CODE_WIKI;
}
%>
<div id="tabs1">
  <ul>
      <li id="menu1"><a href="wiki_update_list.jsp?dir_code=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>待审</span></a></li>
      <li id="menu2"><a href="wiki_list.jsp?dir_code=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>词条</span></a></li>
      <li id="menu3"><a href="wiki_statistic_user_list.jsp"><span>发布统计</span></a></li>
      <li id="menu6"><a href="manager.jsp?dir_code=<%=StrUtil.UrlEncode(dirCodeTop)%>"><span>配置</span></a></li>
      <li id="menu7"><a href="wiki_export_rmoffice.jsp?dirCode=<%=StrUtil.UrlEncode(dirCodeTop)%>" title="导出至Word文件"><span>导出</span></a></li>
  </ul>
</div>