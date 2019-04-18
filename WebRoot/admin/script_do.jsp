<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.redmoon.oa.shell.*"%>
<%
// 节点在控件中的内部名称
String op = ParamUtil.get(request, "op");
if ("getMethods".equals(op)) {
	String pkgName = ParamUtil.get(request, "pkgName");
    out.print(CompleterUtil.getMethods(pkgName));
}
else if ("getAllClasses".equals(op)) {
	String importSection = ParamUtil.get(request, "importSection");
	out.print(CompleterUtil.getAllClasses(importSection));
}
%>