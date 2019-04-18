<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="org.json.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.flow.macroctl.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.flow.query.*"%>
<%@page import="com.redmoon.oa.pvg.Privilege"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	out.print(StrUtil.makeErrMsg("标识非法！"));
	return;
}
int pagesize = ParamUtil.getInt(request, "pageSize", 300);
int curpage = Integer.parseInt(strcurpage);

FormQueryDb aqd = new FormQueryDb();
int id = ParamUtil.getInt(request, "id", -1);
		
if (id==-1) {
	out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"" + SkinMgr.getSkinPath(request) + "/css.css\" />");
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "id不能为空！"));
	return;
}

String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
lf = lf.getLeaf(flowTypeCode);

// System.out.println(getClass() + " flowTypeCode=" + flowTypeCode);

aqd = aqd.getFormQueryDb(id);

QueryScriptUtil	qsu = new QueryScriptUtil();

ResultIterator ri = qsu.executeQueryOnQueryInFlowChangCondValue(request, aqd, lf);
if (ri==null)
	return;
	
String colP = aqd.getColProps();
JSONArray jsonArray = new JSONArray(colP);
%>
<table id="formQueryTable">
<thead>
	<tr>
<%
for (int i=0; i<jsonArray.length(); i++) {
	JSONObject json = jsonArray.getJSONObject(i);
	if (((Boolean)json.get("hide")).booleanValue())
		continue;
	String name = (String)json.get("name");
	if (name.equalsIgnoreCase("cws_op"))
		continue;
	%>
	<th width="<%=json.get("width")%>"><%=json.get("display")%></th>
	<%
}
%>
	</tr>
</thead>
<tbody>
<%
MacroCtlUnit mu = null;
MacroCtlMgr mm = new MacroCtlMgr();
DeptMgr dm = new DeptMgr();
UserDb user = new UserDb();

Vector fields = new Vector();
String formCode = qsu.getFormCode();
if (formCode != null) {
	FormDb fd = new FormDb(formCode);
	if (fd != null && fd.isLoaded()) {
		fields = fd.getFields();
	}
}
	
while(ri.hasNext()){
	ResultRecord rr = (ResultRecord)ri.next();
			
	JSONObject jo = new JSONObject();
	HashMap mapIndex = qsu.getMapIndex();
	Iterator irMap = mapIndex.keySet().iterator();
	int k = 0;
	while (irMap.hasNext()) {
		String keyName = (String) irMap.next();

		jo.put(keyName, StrUtil.getNullStr(rr.getString(keyName)));
		
		// System.out.println(getClass() + " keyName=" + keyName);
	}
	%>
	<tr>
	<%
	for (int i=0; i<jsonArray.length(); i++) {
		String name = (String)jsonArray.getJSONObject(i).get("name");
		if (((Boolean)jsonArray.getJSONObject(i).get("hide")).booleanValue())
			continue;		
		if (name.equalsIgnoreCase("cws_op"))
			continue;
		if (!jo.has(name.toUpperCase())) {
			continue;
		}
		// System.out.println(getClass() + "--" + name);
		%>
		<td width="<%=jsonArray.getJSONObject(i).get("width")%>">
		<%
		Iterator it = fields.iterator();
		String val = jo.get(name.toUpperCase()).toString();
		while (it.hasNext()) {
			FormField ff = (FormField) it.next();
			if (ff.getName().equalsIgnoreCase(name) && ff.getType().equals(FormField.TYPE_MACRO)) {
				val = mm.getMacroCtlUnit(ff.getMacroType()).getIFormMacroCtl().converToHtml(request, ff, val);
				break;
			}
		}
		out.print(val);
		/*
		try {
			String userName = jo.get(name.toUpperCase()).toString();
			if (userName != null && userName.equals(new Privilege().getUser(request))) {
				user = user.getUserDb(userName);
				userName = user.getRealName();
			}
			out.print(userName);
		}
		catch(Exception e) {
		}
		*/
		%>
		</td>
		<%
	}
	%>
	</tr>
	<%
}
		
if (ri.size()>0) {
	String statDesc = aqd.getStatDesc();
	if (statDesc.equals(""))
		statDesc = "{}";
	JSONObject json = new JSONObject(statDesc);
	JSONObject jo = new JSONObject();
	Map mapFieldType = ri.getMapType();
	
	Iterator ir3 = json.keys();
	int n = 0;
	while (ir3.hasNext()) {
		String key = (String) ir3.next();
		String modeStat = json.getString(key);
		
		Integer iType = (Integer)mapFieldType.get(key.toUpperCase());
		int fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());
		
		double sumVal = FormSQLBuilder.getSUMOfSQL(qsu.getSql(), key);
		if (modeStat.equals("0")) {
			if (fieldType==FormField.FIELD_TYPE_INT
			  || fieldType==FormField.FIELD_TYPE_LONG) {
				jo.put(key, (long)sumVal);
			}
			else {
				jo.put(key, NumberUtil.round(sumVal, 2));
			}
		}
		else if (modeStat.equals("1")) {
			jo.put(key, "平均：" + NumberUtil.round(sumVal/ri.size(), 2));
		}
		n++;
	}
	if (n>0) {
	%>
	<tr>
	<%
		for (int i=0; i<jsonArray.length(); i++) {
			String name = (String)jsonArray.getJSONObject(i).get("name");
			if (((Boolean)jsonArray.getJSONObject(i).get("hide")).booleanValue())
				continue;					
			if (name.equalsIgnoreCase("cws_op"))
				continue;
			if (jo.has(name)) {
			%>
			<td><%=jo.get(name)%></td>
			<%
			}
			else {
				String s = "&nbsp;";
				if (i==0)
					s = "合计";
			%>
			<td width="<%=jsonArray.getJSONObject(i).get("width")%>"><%=s%></td>
			<%
			}
		}
	%>
	</tr>
	<%
	}
}		
%>
	</tbody>
    </table>
</body>
</html>