<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%
String formCodeTop = ParamUtil.get(request, "code");
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="form_edit.jsp?code=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>表单编辑</span></a></li>
    <li id="menu2"><a href="form_field_m.jsp?code=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>字段管理</span></a></li>
    <li id="menu4"><a href="<%=request.getContextPath()%>/admin/form_remind_list.jsp?code=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>到期提醒</span></a></li>
	<%
	if (com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {
    %>
    <li id="menu5"><a href="<%=request.getContextPath()%>/admin/form_view_setup.jsp?code=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>显示设置</span></a></li>
    <%
	}
	%>
	<%
	//if (com.redmoon.oa.kernel.License.getInstance().isEnterprise() || com.redmoon.oa.kernel.License.getInstance().isGroup() || com.redmoon.oa.kernel.License.getInstance().isPlatform()) {
	if (com.redmoon.oa.kernel.License.getInstance().isPlatformSrc()) {
	%>
    <li id="menu3"><a href="<%=request.getContextPath()%>/visual/module_setup_list.jsp?formCode=<%=StrUtil.UrlEncode(formCodeTop)%>"><span>模块管理</span></a></li>
    <%}%>
</ul>
</div>