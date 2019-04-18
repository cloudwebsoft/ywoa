<%@ page contentType="text/html; charset=utf-8"%><%@ page import = "java.util.*"%><%@ page import = "com.redmoon.oa.ui.*"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "cn.js.fan.web.*"%><%@ page import = "com.redmoon.oa.flow.*"%><%@ page import = "com.redmoon.oa.visual.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
	if (!privilege.isUserPrivValid(request, "admin.flow.query")) {
		out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String formCode = ParamUtil.get(request, "formCode");
	if (formCode.equals(""))
		return;

   	ModulePrivDb mpd = new ModulePrivDb(formCode);
	if (!mpd.canUserSee(formCode)) {
		// return;
	}
	
	// response.setContentType("application/x-json");
	
	response.setContentType("text/plain");
	
	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);

	Vector vt = fd.getFields();
	Iterator ir = vt.iterator();
	String data = "";
	while (ir!=null && ir.hasNext()) {
	   	FormField ff = (FormField)ir.next();
		if (!ff.isCanQuery())
			continue;
		
	   	if (data.equals(""))
		   	data = "{\"tableShortCode\":\"" + formCode + "\", \"name\":\"" + ff.getTitle() + "\", \"code\":\"" + ff.getName() + "\"}";
		else
		   	data += ",{\"tableShortCode\":\"" + formCode + "\", \"name\":\"" + ff.getTitle() + "\", \"code\":\"" + ff.getName() + "\"}";
	}

	data = "[" + data + "]";
	out.print(data);
%>