<%@ page contentType="text/html; charset=gb2312"%><%@ page import="java.io.*"%><%@ page import="cn.js.fan.db.*"%><%@ page import="java.util.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.security.*"%><%@ page import="com.redmoon.oa.*"%><%@ page import = "com.redmoon.oa.person.*"%><%@ page import = "com.redmoon.oa.visual.*"%><%@ page import = "com.redmoon.oa.flow.FormDb"%><%@ page import = "com.redmoon.oa.flow.FormField"%><%@ page import = "com.redmoon.oa.flow.macroctl.*"%><%@ page import="jxl.*"%><%@ page import="jxl.write.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String code = ParamUtil.get(request, "formCode");
String moduleCode = ParamUtil.get(request, "moduleCode");
if ("".equals(moduleCode)) {
	moduleCode = code;
}
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(moduleCode);
if (msd==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
}

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");

String formCode = msd.getString("form_code");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "表单不存在！"));
	return;
}

response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(fd.getName()) + ".xls");  
            
OutputStream os = response.getOutputStream();

try {
	
	File file = new File(Global.realPath + "visual/template/blank.xls");
	Workbook wb = Workbook.getWorkbook(file);
	UserMgr um = new UserMgr();

	WorkbookSettings settings = new WorkbookSettings ();  
	settings.setWriteAccess(null);  
	
	// 打开一个文件的副本，并且指定数据写回到原文件
	WritableWorkbook wwb = Workbook.createWorkbook(os, wb, settings);
	WritableSheet ws = wwb.getSheet(0);

	int len = 0;
	if (fields!=null)
		len = fields.length;
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		String title = "创建者";
		if (!fieldName.equals("cws_creator"))
			title = fd.getFieldTitle(fieldName);
		
		Label a = new Label(i, 0, title);
		ws.addCell(a);
	}

	wwb.write();
	wwb.close();
	wb.close();
} catch (Exception e) {
	out.println(e.toString());
}
finally {
	os.close();
}

out.clear();
out = pageContext.pushBody();
%>