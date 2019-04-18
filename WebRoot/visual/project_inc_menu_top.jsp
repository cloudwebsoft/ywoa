<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%
String formCodeTop = ParamUtil.get(request, "formCode");
ModuleSetupDb msdTop = new ModuleSetupDb();
msdTop = msdTop.getModuleSetupDbOrInit(formCodeTop);
// String pageTypeTop = StrUtil.getNullStr((String)request.getAttribute("pageType"));

int parentIdTop = ParamUtil.getInt(request, "parentId", -1);
%>
<div id="tabs1">
  <ul>
    <li id="menu1"><a href="project_list_m.jsp?formCode=<%=formCodeTop%>"><span><%=msdTop.getString("name")%>列表</span></a></li>
	<li id="menu1"><a href="project_add.jsp?formCode=<%=formCodeTop%>"><span>创建<%=msdTop.getString("name")%></span></a></li>
  </ul>
</div>
