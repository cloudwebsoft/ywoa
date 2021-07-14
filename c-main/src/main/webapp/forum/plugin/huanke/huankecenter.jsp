<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.cloudwebsoft.framework.base.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.huanke.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
String rootPath = request.getContextPath();
%>
<table width="100%" border="0" cellpadding="5" cellspacing="1">
  <tr>
    <td colspan="2" align="left" valign="top"><strong>换客管理</strong>：
	<a href="<%=rootPath%>/forum/plugin/huanke/huanke_list.jsp?huankeType=exchange" target="_blank">待换物品</a>&nbsp;&nbsp;
	<a href="<%=rootPath%>/forum/plugin/huanke/huanke_list.jsp?huankeType=stocks" target="_blank">库存换品</a>&nbsp;&nbsp;
	<a href="<%=rootPath%>/forum/plugin/huanke/huanke_list.jsp?huankeType=exchanged" target="_blank">换出的换品</a>&nbsp;&nbsp;
	<a href="<%=rootPath%>/forum/plugin/huanke/huanke_list.jsp?huankeType=all" target="_blank">所有换品</a>
	</td>
  </tr>
</table>
