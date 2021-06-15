<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.Iterator"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	response.setHeader("X-Content-Type-Options", "nosniff");
	response.setHeader("Content-Security-Policy", "default-src 'self' http: https:; script-src 'self'; frame-ancestors 'self'");
	response.setContentType("text/html;charset=utf-8");

	int queryId = ParamUtil.getInt(request, "queryId");

	// 暂无用
	String formCode = ParamUtil.get(request, "formCode");
	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	
	String op = ParamUtil.get(request, "op");
	// System.out.println(getClass() + " op=" + op);
	Iterator ir = fd.getFields().iterator();
	if (op.equals("getOptions")) {
		while(ir.hasNext()) {
			FormField ff = (FormField)ir.next();
		%>
		<option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
		<%
		}
		return;
	}
%>
<select name="otherField">
  <option value="id">id</option>
<%
		while(ir.hasNext()) {
			FormField ff = (FormField)ir.next();
%>
		<option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
<%
	}
%>
</select>
，显示
<select name="otherShowField">
<%
		ir = fd.getFields().iterator();
		while(ir.hasNext()) {
			FormField ff = (FormField)ir.next();
%>
		<option value="<%=ff.getName()%>"><%=ff.getTitle()%></option>
<%
	}
%>
</select>
