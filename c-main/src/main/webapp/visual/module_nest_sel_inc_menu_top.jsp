<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "org.json.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%
String nestTypeTop = ParamUtil.get(request, "nestType");
String parentFormCodeTop = ParamUtil.get(request, "parentFormCode");
String nestFieldNameTop = ParamUtil.get(request, "nestFieldName");
long parentIdTop = ParamUtil.getLong(request, "parentId", -1);

FormDb pFormTop = new FormDb();
pFormTop = pFormTop.getFormDb(parentFormCodeTop);
FormField nestFieldTop = pFormTop.getFormField(nestFieldNameTop);

JSONObject jsonTop = null;
String filterTop = "";
try {
	// 20131123 fgf 添加
	String defaultVal = StrUtil.decodeJSON(nestFieldTop.getDescription());
	jsonTop = new JSONObject(defaultVal);
	filterTop = jsonTop.getString("filter");
} catch (JSONException e) {
	// TODO Auto-generated catch block
	// e.printStackTrace();
	out.print(SkinUtil.makeErrMsg(request, "JSON解析失败！"));
	return;
}

String formCodeTop = jsonTop.getString("sourceForm");

ModuleSetupDb msdTop = new ModuleSetupDb();
msdTop = msdTop.getModuleSetupDbOrInit(formCodeTop);
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="module_list_nest_sel.jsp?parentFormCode=<%=parentFormCodeTop%>&nestFieldName=<%=nestFieldNameTop%>&nestType=<%=nestTypeTop%>&parentId=<%=parentIdTop%>" title="<%=msdTop.getString("name")%>"><span><%=msdTop.getString("name")%>列表</span></a></li>
    <%
	// 如果已经存在条件，则不出现高级查询选项卡
	if (filterTop.equals("none")) {
	%>
	<li id="menu2"><a href="module_search_for_nest_sel.jsp?parentFormCode=<%=parentFormCodeTop%>&nestFieldName=<%=nestFieldNameTop%>&nestType=<%=nestTypeTop%>&parentId=<%=parentIdTop%>"><span>高级查询</span></a></li>
    <%}%>
  </ul>
</div>
