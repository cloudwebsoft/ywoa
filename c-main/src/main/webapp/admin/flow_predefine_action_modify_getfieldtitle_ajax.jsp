<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.flow.Leaf" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit"%>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.Vector" %>
<%@ page import="com.cloudweb.oa.api.IWorkflowHelper" %>
<%@ page import="com.cloudweb.oa.utils.SpringUtil" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
if ("".equals(flowTypeCode)) {
	return;
}
Leaf lf = new Leaf();
lf = lf.getLeaf(flowTypeCode);
String formCode = lf.getFormCode();
String fields = ParamUtil.get(request, "fields");
String[] fieldAry;
String fieldText = "";
MacroCtlMgr mm = new MacroCtlMgr();		

if (fields!=null && !"".equals(fields)) {
	// 老版迁移过来的系统中原有流程套用流程后，fields中会含有comma
	fields = fields.replaceAll("comma", ",");
 	fieldAry = fields.split(",");
  	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	// 找出嵌套表
	FormDb nestfd = new FormDb();
	Vector<FormDb> vfd = new Vector<>();
	Vector<FormField> v = fd.getFields();
	for (FormField ff : v) {
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
			if (mu != null && mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
				String nestFormCode = ff.getDefaultValue();
				try {
					String defaultVal;
					if (mu.getNestType() == MacroCtlUnit.NEST_DETAIL_LIST) {
						defaultVal = StrUtil.decodeJSON(ff.getDescription());
					} else {
						String desc = ff.getDescription();
						if ("".equals(desc)) {
							desc = ff.getDefaultValueRaw();
						}
						defaultVal = StrUtil.decodeJSON(desc); // ff.getDefaultValueRaw()		
					}
					JSONObject json = new JSONObject(defaultVal);
					nestFormCode = json.getString("destForm");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				FormDb nestFormDb = nestfd.getFormDb(nestFormCode);
				if (nestFormDb.isLoaded()) {
					vfd.addElement(nestFormDb);
				}
				else {
					DebugUtil.e(getClass(), "表单：", nestFormCode + " 不存在");
				}
			}
		}
	}
	
	for (String s : fieldAry) {
		if ("".equals(fieldText)) {
			if (s.startsWith("nest.")) {
				for (FormDb formDb : vfd) {
					String nestFieldName = formDb.getFieldTitle(s.substring("nest.".length()));
					if (nestFieldName != null && !"".equals(nestFieldName)) {
						fieldText = nestFieldName + "(嵌套表)";
						break;
					}
				}
			} else {
				fieldText = fd.getFieldTitle(s);
			}
		} else {
			if (s.startsWith("nest.")) {
				for (FormDb formDb : vfd) {
					String nestFieldName = formDb.getFieldTitle(s.substring("nest.".length()));
					if (nestFieldName != null && !"".equals(nestFieldName)) {
						fieldText += "," + nestFieldName + "(嵌套表)";
						break;
					}
				}
			} else {
				fieldText += "," + fd.getFieldTitle(s);
			}
		}
	}
	out.print(fieldText);
}
%>