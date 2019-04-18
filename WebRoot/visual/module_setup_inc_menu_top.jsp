<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@page import="com.redmoon.oa.flow.FormDb"%>
<%
String codeTop = ParamUtil.get(request, "code");
String formCodeTop = ParamUtil.get(request, "formCode");
if ("".equals(codeTop)) {
	// module_relate.jsp中添加关联模块时，code因为是关联表单的编码
	codeTop = formCodeTop;
}
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="<%=request.getContextPath()%>/visual/module_field_list.jsp?code=<%=codeTop%>&formCode=<%=formCodeTop%>"><span>列表设置</span></a></li>
    <li id="menu10"><a href="<%=request.getContextPath()%>/visual/module_field_conds.jsp?code=<%=codeTop%>&formCode=<%=formCodeTop%>"><span>查询设置</span></a></li>
    <li id="menu2"><a href="<%=request.getContextPath()%>/visual/module_priv_list.jsp?code=<%=codeTop%>&formCode=<%=formCodeTop%>"><span>权限设置</span></a></li>
    <li id="menu4"><a href="<%=request.getContextPath()%>/visual/module_view_edit.jsp?code=<%=codeTop%>&formCode=<%=formCodeTop%>"><span>选项卡设置</span></a></li>
    <li id="menu6"><a href="<%=request.getContextPath()%>/visual/module_import_list.do?code=<%=codeTop%>&formCode=<%=formCodeTop%>"><span>导入设置</span></a></li>
    <li id="menu9"><a href="<%=request.getContextPath()%>/visual/module_export_list.do?code=<%=codeTop%>&formCode=<%=formCodeTop%>"><span>导出设置</span></a></li>
    <%if (codeTop.equals(formCodeTop)) {%>
	<%
	if (License.getInstance().isSrc()) {
    %>
    <li id="menu7"><a href="<%=request.getContextPath()%>/visual/module_scripts_iframe.jsp?code=<%=codeTop%>&formCode=<%=formCodeTop%>"><span>事件脚本</span></a></li>
    <%
    }
    %>    
    <li id="menu3"><a href="<%=request.getContextPath()%>/visual/module_relate.jsp?code=<%=codeTop%>&formCode=<%=formCodeTop%>"><span>关联模块</span></a></li>
    <%}%>    
    <li id="menu8"><a href="<%=request.getContextPath()%>/admin/form_reports.jsp?code=<%=codeTop%>&formCode=<%=formCodeTop%>"><span>报表关联</span></a></li>
    <%FormDb fedb = new FormDb(formCodeTop);
    if (fedb != null && fedb.isLoaded() && !fedb.isSystem()) {%>
    <li id="menu5"><a href="javascript:;" onclick="addTab('<%=fedb.getName()%>', '<%=request.getContextPath()%>/admin/form_edit.jsp?code=<%=formCodeTop%>')"><span>表单编辑</span></a></li>
    <%}%>
  </ul>
</div>
