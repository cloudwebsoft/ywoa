<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="org.json.*"%>
<%
String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
if (flowTypeCode.equals(""))
	return;
Leaf lf = new Leaf();
lf = lf.getLeaf(flowTypeCode);
String formCode = lf.getFormCode();
String fields = ParamUtil.get(request, "fields");
String[] fieldAry = null;
String fieldText = "";
MacroCtlMgr mm = new MacroCtlMgr();		

// System.out.println(getClass() + " fields=" + fields);

if (fields!=null && !fields.equals("")) {
 	fieldAry = fields.split(",");
  	FormDb fd = new FormDb();
	fd = fd.getFormDb(formCode);
	// 找出嵌套表
	FormDb nestfd = new FormDb();
	Vector vfd = new Vector();
	Vector v = fd.getFields();
	Iterator ir = v.iterator();
	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());		
			if (mu!=null && mu.getNestType() != MacroCtlUnit.NEST_TYPE_NONE) {
				String nestFormCode = ff.getDefaultValue();
				try {
					String defaultVal;
					if (mu.getNestType()==MacroCtlUnit.NEST_DETAIL_LIST) {
						defaultVal = StrUtil.decodeJSON(ff.getDescription());				
					}
					else {
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
				vfd.addElement(nestFormDb);
				// break;
			}
		}
	}
	
	int len = fieldAry.length;
	for (int i=0; i<len; i++) {
		if (fieldText.equals("")) {
			if (fieldAry[i].startsWith("nest.")) {
				ir = vfd.iterator();
				while (ir.hasNext()) {
					nestfd = (FormDb)ir.next();
					String nestFieldName = nestfd.getFieldTitle(fieldAry[i].substring("nest.".length()));
					if (!nestFieldName.equals("")) {
						fieldText = nestFieldName + "(嵌套表)";
						break;
					}
				}
			}
			else {
				fieldText = fd.getFieldTitle(fieldAry[i]);
			}
		}
		else {
			if (fieldAry[i].startsWith("nest.")) {
				ir = vfd.iterator();
				while (ir.hasNext()) {
					nestfd = (FormDb)ir.next();
					String nestFieldName = nestfd.getFieldTitle(fieldAry[i].substring("nest.".length()));
					if (!nestFieldName.equals("")) {
						fieldText += "," + nestFieldName + "(嵌套表)";
						break;
					}
				}				
			}
			else {
				fieldText += "," + fd.getFieldTitle(fieldAry[i]);
			}
		}
	}
	out.print(fieldText);
	
	// System.out.println(getClass() + " fieldText=" + fieldText);
}
%>