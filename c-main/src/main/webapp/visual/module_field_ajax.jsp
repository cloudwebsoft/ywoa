
<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String formCode = ParamUtil.get(request, "formCode");
	if ("".equals(formCode)) {
		String code = ParamUtil.get(request, "code");
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDb(code);
		formCode = msd.getString("form_code");
	}
	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	
	String op = ParamUtil.get(request, "op");
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
<span style="display:">
<select name="otherField" id="otherField">
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
，
</span>
显示
<select name="otherShowField" id="otherShowField">
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
