<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String moduleCode = "";	
String formCode = ParamUtil.get(request, "sourceFormCode");
if (formCode.equals(""))
	return;
	
String mode = ParamUtil.get(request, "mode");
ModuleSetupDb msd = new ModuleSetupDb();
if ("module".equals(mode)) {
	moduleCode = formCode;
	msd = msd.getModuleSetupDb(moduleCode);
	formCode = msd.getString("form_code");
}	

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
Vector v = fd.getFields();
Iterator ir = v.iterator();
String json = "";

MacroCtlMgr mm = new MacroCtlMgr();

String op = ParamUtil.get(request, "op");
if (op.equals("getNestTableFields")) {
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();

		boolean isNest = false;
		String nestFormCode = ""; // 目标表单中嵌套表宏控件对应的表单编码
		MacroCtlUnit mu = null;
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			mu = mm.getMacroCtlUnit(ff.getMacroType());
			if (mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
				nestFormCode = ff.getDefaultValue();
				isNest = true;
			}
		}

		if (isNest) {
			try {
				String defaultVal = "";
				if (mu.getNestType()==MacroCtlUnit.NEST_DETAIL_LIST) {
					defaultVal = StrUtil.decodeJSON(ff.getDescription());
				}
				else {
					defaultVal = ff.getDescription();
					if ("".equals(defaultVal)) {
						defaultVal = ff.getDefaultValueRaw();
					}
					defaultVal = StrUtil.decodeJSON(defaultVal); // ff.getDefaultValueRaw()
				}
				JSONObject jsonDefault = new JSONObject(defaultVal);
				nestFormCode = jsonDefault.getString("destForm");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// 取出目标表单中的嵌套表的字段
			FormDb fdNest = new FormDb();
			fdNest = fdNest.getFormDb(nestFormCode);
			
			Vector vNest = fdNest.getFields();
			Iterator irNest = vNest.iterator();
			while (irNest.hasNext()) {
				ff = (FormField)irNest.next();
				if (json.equals(""))
				  json = "{\"id\":\"" + ff.getName() + "\", \"name\":\"" + ff.getTitle() + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue().replaceAll("\\r\\n", "") + "\"}";
				else
				  json += ",{\"id\":\"" + ff.getName() + "\", \"name\":\"" + ff.getTitle() + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue().replaceAll("\\r\\n", "") + "\"}";
			}
			json = "{\"result\":[" + json + "], \"total\":" + v.size() + ", \"formNest\":\"" + nestFormCode + "\"}";
			LogUtil.getLog(getClass()).info(json);
			out.print(json);
			break;
		}
	}
}
else {
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		// 注意getDefaultValue要过滤掉\r\n
		if (json.equals(""))
			json = "{\"id\":\"" + ff.getName() + "\", \"name\":\"" + ff.getTitle() + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue().replaceAll("\\r\\n", "") + "\"}";
		else
			json += ",{\"id\":\"" + ff.getName() + "\", \"name\":\"" + ff.getTitle() + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue().replaceAll("\\r\\n", "") + "\"}";
	}
	json += ",{\"id\":\"" + com.redmoon.oa.visual.FormDAO.FormDAO_NEW_ID + "\", \"name\":\"ID\", \"type\":\"" + FormField.TYPE_TEXTFIELD + "\", \"macroType\":\"" + FormField.MACRO_NOT + "\", \"defaultValue\":\"\"}"; // 记录的ID
	// 加入映射字段
	if ("module".equals(mode)) {
		// String listField = StrUtil.getNullStr(msd.getString("list_field"));
		String[] fields = msd.getColAry(false, "list_field");
		
		int len = 0;
		if (fields!=null)
			len = fields.length;		
		for (int i=0; i<len; i++) {
			String fieldName = fields[i];
			String title = "";
			boolean isMap = false;
			FormField ff = null;
			if (fieldName.startsWith("main:")) {
				String[] subFields = StrUtil.split(fieldName, ":");
				if (subFields.length == 3) {
					FormDb subfd = new FormDb(subFields[1]);
					ff = subfd.getFormField(subFields[2]);
					title = ff.getTitle();
					isMap = true;
				}
			} else if (fieldName.startsWith("other:")) {
				String[] otherFields = StrUtil.split(fieldName, ":");
				if (otherFields.length == 5) {
					FormDb otherFormDb = new FormDb(otherFields[2]);
					ff = otherFormDb.getFormField(otherFields[4]);					
					title = ff.getTitle();
					isMap = true;
				}
			}	
			
			if (isMap) {
				if (json.equals(""))
					json = "{\"id\":\"" + fieldName + "\", \"name\":\"" + title + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue().replaceAll("\\r\\n", "") + "\"}";
				else
					json += ",{\"id\":\"" + fieldName + "\", \"name\":\"" + title + "\", \"type\":\"" + ff.getType() + "\", \"macroType\":\"" + ff.getMacroType() + "\", \"defaultValue\":\"" + ff.getDefaultValue().replaceAll("\\r\\n", "") + "\"}";
			}
		}
	}	
	json = "{\"result\":[" + json + "], \"total\":" + v.size() + "}";
	out.print(json);
}
%>