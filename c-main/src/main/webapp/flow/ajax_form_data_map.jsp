<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page import = "com.cloudwebsoft.framework.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.base.*"%>
<%@ page import = "com.redmoon.oa.sms.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.ModuleSetupDb"%>
<%@ page import = "org.json.*"%>
<%@ page import="com.redmoon.oa.util.RequestUtil" %>
<%
Privilege pvg = new Privilege();
int flowId = ParamUtil.getInt(request, "flowId", -1);
int sourceFlowId = ParamUtil.getInt(request, "sourceFlowId", -1);
String formCode = ParamUtil.get(request, "formCode"); // destForm
String fieldName = ParamUtil.get(request, "fieldName");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

FormField ff = fd.getFormField(fieldName);
String jsonStr = ff.getDefaultValue();

JSONObject json = new JSONObject(jsonStr);

JSONObject jsonRaw = json;

JSONArray ary = (JSONArray)json.get("maps");
String sourceForm = (String)json.get("sourceForm");
FormDb sourcefd = new FormDb();
sourcefd = sourcefd.getFormDb(sourceForm);

FormDAO fdao = new FormDAO();
fdao = fdao.getFormDAO(sourceFlowId, sourcefd);
// 取出相应的值，置于json字符串中返回
String ret = "";
MacroCtlMgr mam = new MacroCtlMgr();
UserMgr um = new UserMgr();
MacroCtlMgr mm = new MacroCtlMgr();

for (int i=0; i<ary.length(); i++) {
	json = (JSONObject)ary.get(i);
	String destF = (String)json.get("destField");
	String sourceF = (String)json.get("sourceField");
	ff = fd.getFormField(destF);
	if (ff==null) {
		// System.out.println(getClass() + " destF=" + destF + " 不存在！");
		LogUtil.getLog(getClass()).error("destF=" + destF + " 不存在！");
		continue;
	}
	boolean isNest = false;
	String nestFormCode = "";
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
			// 20131123 fgf 添加
			JSONObject jsonDefault = new JSONObject(defaultVal);
			nestFormCode = jsonDefault.getString("destForm");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ModuleSetupDb msd = new ModuleSetupDb();
		msd = msd.getModuleSetupDbOrInit(nestFormCode);
		
		// String listField = StrUtil.getNullStr(msd.getString("list_field"));
		String[] fields = msd.getColAry(false, "list_field");
		
		int len = fields.length;
		
		String sql = "select id from " + FormDb.getTableName(nestFormCode) + " where cws_id=" + sourceFlowId + " order by cws_order";
		// System.out.println(getClass() + " sql=" + sql);
		FormDb nestfd = new FormDb();
		nestfd = nestfd.getFormDb(nestFormCode);
		
		com.redmoon.oa.visual.FormDAO nestfdao = new com.redmoon.oa.visual.FormDAO();
		Vector vt = nestfdao.list(nestFormCode, sql);
		Iterator ir = vt.iterator();
		String nestjsonstr = "";
		while (ir!=null && ir.hasNext()) {
			nestfdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			RequestUtil.setFormDAO(request, nestfdao);

			long id = nestfdao.getId();
			String jstr = "\"id\":\"" + id + "\"";
			for (int j=0; j<len; j++) {
				String fName = fields[j];
				FormField nestff = nestfd.getFormField(fName);
				String ffValue = "", ffAttrValue = ""; // ffAttrValue用于宏控件所在的单元格的属性
				boolean isMacro = false;
				if (nestff.getType().equals(FormField.TYPE_MACRO)) {
					isMacro = true;
					MacroCtlUnit mcu = mam.getMacroCtlUnit(nestff.getMacroType());
					IFormMacroCtl imc = mcu.getIFormMacroCtl();
					ffAttrValue = nestfdao.getFieldValue(fName);
					ffValue = imc.converToHtml(request, nestff, nestfdao.getFieldValue(fName));
				}
				else {
					ffValue = nestfdao.getFieldValue(fName);
				}
				// jstr += "," + "\"fieldName:\":\"" + fName + "\", \"html\":\"" + ffValue + "\", \"value\":\"" + ffAttrValue + "\", \"type\":\"" + nestff.getType() + "\"";
				jstr += "," + "\"" + fName + "\":\"" + fName + "\", \"" + fName + "_html\":\"" + ffValue + "\", \"" + fName + "_value\":\"" + ffAttrValue + "\", \"" + fName + "type\":\"" + nestff.getType() + "\"";
			}
			if (nestjsonstr.equals(""))
				nestjsonstr = "{" + jstr + "}";
			else
				nestjsonstr += ",{" + jstr + "}";
		}
		// value为nest_table，表示是嵌套表字段
		if (ret.equals(""))
			ret = "{\"fieldName\":\"" + destF + "\", \"type\":\"" + ff.getType() + "\", \"value\":\"\",\"macroType\":\"nest_table\", \"editable\":\"" + json.get("editable") + "\", \"appendable\":\"" + json.get("appendable") + "\", \"ary\":[" + nestjsonstr + "]}";
		else
			ret += ",{\"fieldName\":\"" + destF + "\", \"type\":\"" + ff.getType() + "\",\"value\":\"\",\"macroType\":\"nest_table\", \"editable\":\"" + json.get("editable") + "\", \"appendable\":\"" + json.get("appendable") + "\", \"ary\":[" + nestjsonstr + "]}";
	}
	else {
		// System.out.println(getClass() + " destF=" + destF + "=" + fdao.getFieldValue(sourceF));
		if (ret.equals(""))
			ret = "{\"fieldName\":\"" + destF + "\", \"type\":\"" + ff.getType() + "\",\"value\":\"" + StrUtil.getNullStr(fdao.getFieldValue(sourceF)) + "\",\"macroType\":\"\", \"editable\":\"" + json.get("editable") + "\", \"appendable\":\"" + json.get("appendable") + "\", \"ary\":[]}";
		else
			ret += ",{\"fieldName\":\"" + destF + "\", \"type\":\"" + ff.getType() + "\",\"value\":\"" + StrUtil.getNullStr(fdao.getFieldValue(sourceF)) + "\",\"macroType\":\"\", \"editable\":\"" + json.get("editable") + "\", \"appendable\":\"" + json.get("appendable") + "\", \"ary\":[]}";
	}
}

// System.out.println(getClass() + " jsonRaw=" + jsonRaw);

try {
	ary = (JSONArray)jsonRaw.get("mapsNest");
}
catch (JSONException e) {
	ary = null;
	e.printStackTrace();
}
String retNest = "";
// 兼容之前未mapsNest嵌套表时的版本
if (ary!=null && ary.length() > 0) {
	String sourceFormNest = (String)jsonRaw.get("sourceFormNest");
	FormDb sourcefdNest = new FormDb();
	sourcefdNest = sourcefdNest.getFormDb(sourceFormNest);
	
	String destFormNest = (String)jsonRaw.get("destFormNest");
	FormDb destfdNest = new FormDb();
	destfdNest = destfdNest.getFormDb(destFormNest);
	
	FormDAO fdaoNest = new FormDAO();
	fdaoNest = fdao.getFormDAO(sourceFlowId, sourcefdNest);
	// 取出相应的值，置于json字符串中返回
	
	// 取出源嵌套表中的数据
	String sql = "select id from " + FormDb.getTableName(sourceFormNest) + " where cws_id=" + sourceFlowId + " order by cws_order";
	// System.out.println(getClass() + " sql=" + sql);
	
	com.redmoon.oa.visual.FormDAO sourcefdaoNest = new com.redmoon.oa.visual.FormDAO();
	Vector vt = sourcefdaoNest.list(sourceFormNest, sql);
	Iterator ir = vt.iterator();
	while (ir!=null && ir.hasNext()) {
		com.redmoon.oa.visual.FormDAO fdaoSourceNest = (com.redmoon.oa.visual.FormDAO)ir.next();
		// 根据映射关系，取得相应的字段值
		String nestjson = "";
		for (int i=0; i<ary.length(); i++) {
			json = (JSONObject)ary.get(i);
			String destF = (String)json.get("destField");
			FormField destff = destfdNest.getFormField(destF);
			String sourceF = (String)json.get("sourceField");
			FormField sourceff = sourcefdNest.getFormField(sourceF);
				
			String ffValue = "", ffAttrValue = ""; // ffAttrValue用于宏控件所在的单元格的属性
			boolean isMacro = false;
			if (sourceff.getType().equals(FormField.TYPE_MACRO)) {
				isMacro = true;
				MacroCtlUnit mcu = mam.getMacroCtlUnit(sourceff.getMacroType());
				IFormMacroCtl imc = mcu.getIFormMacroCtl();
				ffAttrValue = fdaoSourceNest.getFieldValue(sourceF);
				ffValue = imc.converToHtml(request, sourceff, fdaoSourceNest.getFieldValue(sourceF));
			}
			else {
				ffValue = fdaoSourceNest.getFieldValue(sourceF);
			}
								
			if (nestjson.equals(""))
				nestjson = "\"" + destF + "\":\"" + destF + "\", \"" + destF + "_html\":\"" + ffValue + "\", \"" + destF + "_value\":\"" + ffAttrValue + "\", \"" + destF + "_type\":\"" + destff.getType() + "\", \"" + destF + "_editable\":\"" + json.get("editable") + "\"";
			else
				nestjson += ",\"" + destF + "\":\"" + destF + "\", \"" + destF + "_html\":\"" + ffValue + "\", \"" + destF + "_value\":\"" + ffAttrValue + "\", \"" + destF + "_type\":\"" + destff.getType() + "\", \"" + destF + "_editable\":\"" + json.get("editable") + "\"";
		}
		
		if (retNest.equals(""))
			retNest = "{" + nestjson + "}";
		else
			retNest += ",{" + nestjson + "}";
	}
	
	// out.print("[" + ret + "]");

}
ret = "{\"result\":[" + ret + "], \"resultNest\":[" + retNest + "]}";

out.print(ret);

// System.out.println(getClass() + " " + ret);
%>