<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%
String formCodeTop = ParamUtil.get(request, "formCode");
ModuleSetupDb msdTop = new ModuleSetupDb();
msdTop = msdTop.getModuleSetupDbOrInit(formCodeTop);
String byFieldNameTop = ParamUtil.get(request, "byFieldName");
String showFieldNameTop = ParamUtil.get(request, "showFieldName");
String filterTop = ParamUtil.get(request, "filter");

String openerFormCodeTop = ParamUtil.get(request, "openerFormCode");
String openerFieldNameTop = ParamUtil.get(request, "openerFieldName");
ModulePrivDb mpdTop = new ModulePrivDb(msdTop.getString("form_code"));
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="module_list_sel.jsp?formCode=<%=formCodeTop%>&byFieldName=<%=byFieldNameTop%>&showFieldName=<%=showFieldNameTop%>&filter=<%=StrUtil.UrlEncode(filterTop)%>&openerFormCode=<%=openerFormCodeTop%>&openerFieldName=<%=openerFieldNameTop%>"><span><%=msdTop.getString("name")%>列表</span></a></li>
	<%if (false && mpdTop.canUserSearch(new com.redmoon.oa.pvg.Privilege().getUser(request))) {%>    
	<li id="menu2"><a href="module_search_for_sel.jsp?formCode=<%=formCodeTop%>&byFieldName=<%=byFieldNameTop%>&showFieldName=<%=showFieldNameTop%>&filter=<%=filterTop%>&openerFormCode=<%=openerFormCodeTop%>&openerFieldName=<%=openerFieldNameTop%>"><span>高级查询</span></a></li>
    <%}%>
  </ul>
</div>
