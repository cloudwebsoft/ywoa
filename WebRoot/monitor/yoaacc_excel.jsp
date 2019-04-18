<%@ page contentType="text/html; charset=gb2312"%>
<%@ page import="java.io.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="jxl.*"%>
<%@ page import="jxl.write.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)){
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, "表单不存在！"));
	return;
}
String op = ParamUtil.get(request, "op");
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
if (!cn.js.fan.db.SQLFilter.isValidSqlParam(orderBy)) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ monitor/yoaacc_excel.jsp orderBy=" + orderBy);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "param_invalid")));
	return;
}	
	
String[] ary = null;
boolean isMine = ParamUtil.get(request, "isMine").equals("true");
if (isMine) {
	ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort, privilege.getUser(request), "user_name");
}
else
	ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
String sql = ary[0];
String sqlUrlStr = ary[1];

FormDAO fdao = new FormDAO();
Vector v = fdao.list(formCode, sql);

// out.print(sql);
// if (true) return;
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");

MacroCtlMgr mm = new MacroCtlMgr();
			
response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(fd.getName()) + ".xls");  
            
OutputStream os = response.getOutputStream();

try {
	
	File file = new File(Global.realPath + "monitor/template/blank.xls");
	Workbook wb = Workbook.getWorkbook(file);
	UserMgr um = new UserMgr();

	// 打开一个文件的副本，并且指定数据写回到原文件
	WritableWorkbook wwb = Workbook.createWorkbook(os, wb);
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

	Iterator ir = v.iterator();

	int j = 1;
	int k = 0;
	
	while (ir.hasNext()) {
		fdao = (FormDAO)ir.next();
		for (int i=0; i<len; i++) {
			String fieldName = fields[i];
			String fieldValue = "";
			if (!fieldName.equals("cws_creator")) {
				
				FormField ff = fd.getFormField(fieldName);
				if (ff==null) {
					fieldValue = "不存在！";
				}
				else {
					if (ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu != null) {
							fieldValue = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName));
						}
					}
					else {
						fieldValue = fdao.getFieldValue(fieldName);
					}
				}
				
			}else{
				fieldValue =StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName());
			}
			
			Label a = new Label(i, j, fieldValue);
			ws.addCell(a);
		}
			
		j++;
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
%>
