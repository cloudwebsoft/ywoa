<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
if (flowTypeCode.equals(""))
	return;
Leaf lf = new Leaf();
lf = lf.getLeaf(flowTypeCode);
String formCode = lf.getFormCode();
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
Iterator ir = fd.getFields().iterator();
while (ir.hasNext()) {
	FormField ff = (FormField)ir.next();
	%>
	<option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
	<%
}
%>