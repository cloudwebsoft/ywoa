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
aqd = aqd.getFormQueryDb(id);

String flowTypeCode = ParamUtil.get(request, "flowTypeCode");
com.redmoon.oa.flow.Leaf lf = new com.redmoon.oa.flow.Leaf();
lf = lf.getLeaf(flowTypeCode);

String formCode = aqd.getTableCode();
FormSQLBuilder fsb = new FormSQLBuilder();
String sql = fsb.getSmartQueryOnFlowChangCondValue(request, aqd, lf);

com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
com.redmoon.oa.flow.FormDAO flowDao = new com.redmoon.oa.flow.FormDAO();

String queryRelated = aqd.getQueryRelated();
FormDb fdRelated = new FormDb();
int queryRelatedId = -1;
if (!queryRelated.equals("")) {
	queryRelatedId = StrUtil.toInt(queryRelated, -1);
	FormQueryDb aqdRelated = aqd.getFormQueryDb(queryRelatedId);
	fdRelated = fdRelated.getFormDb(aqdRelated.getTableCode());
}

String colP = aqd.getColProps();
JSONArray jsonArray = new JSONArray(colP);
%>
<table id="formQueryTable" align="center">
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
int total = 0;
Iterator ir = null;
try {
	// ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);		
	// ir = lr.getResult().iterator();
	// 因为内嵌于页面中，所以不作分页处理，如果数据量太大，不适于放在页面中
	Vector vt = fdao.list(formCode, sql);
	total = vt.size();
	ir = vt.iterator();		
}
catch (Exception e) {
	e.printStackTrace();
	return;
}
	
WorkflowDb wf = new WorkflowDb();
MacroCtlUnit mu = null;
MacroCtlMgr mm = new MacroCtlMgr();
DeptMgr dm = new DeptMgr();
UserDb user = new UserDb();
	
while(ir!=null && ir.hasNext()){
	fdao = (com.redmoon.oa.visual.FormDAO)ir.next();

	JSONObject jo = new JSONObject();
	
	Iterator ffir = fdao.getFields().iterator();
	while (ffir.hasNext()) {
		FormField ff = (FormField)ffir.next();
		
		if (ff.getType().equals(FormField.TYPE_MACRO)) {
			mu = mm.getMacroCtlUnit(ff.getMacroType());
			String macroCode = mu.getCode();
			if (macroCode.equals("macro_dept_select") || macroCode.equals("macro_my_dept_select")) {
				DeptDb dd = dm.getDeptDb(fdao.getFieldValue(ff.getName()));
				jo.put(ff.getName(), dd.getName());
			}
			else {
				// jo.put(ff.getName(), fdao.getFieldValue(ff.getName()));
				jo.put(ff.getName(), mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName())));
			}
		}
		else		
			jo.put(ff.getName(), fdao.getFieldValue(ff.getName()));
	}
	// 写入id是用于flexigrid置于tr的id中，以便于生成checkbox
	jo.put("id", "" + fdao.getId());
	
	// 取得所关联的查询中的数据
	if (queryRelatedId!=-1) {
		int flowId = StrUtil.toInt(fdao.getCwsId());
		flowDao = flowDao.getFormDAO(flowId, fdRelated);
		ffir = flowDao.getFields().iterator();
		while (ffir.hasNext()) {
			FormField ff = (FormField)ffir.next();
			jo.put("rel." + ff.getName(), flowDao.getFieldValue(ff.getName()));
		}
	}
	
	long flowId = fdao.getFlowId();
	if (flowId==-1) {
		flowId = StrUtil.toLong(fdao.getCwsId(), -1);
	}
	String flowCreateDate = "";
	String flowTitle = "";
	String flowStarter = "";
	String flowStatus = "";
	if (flowId!=-1 && flowId!=0) {
		wf = wf.getWorkflowDb((int)flowId);
		flowCreateDate = DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd HH:mm");
		flowTitle = wf.getTitle();
		flowStarter = wf.getUserName();
		flowStarter = user.getUserDb(flowStarter).getRealName();
		flowStatus = wf.getStatusDesc();
	}
	
	jo.put("flowId", "<a href='javascript:;' onclick=\"addTab('" + flowTitle + "', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + flowId + "')\">" + flowId + "</a>");
	jo.put("flowBeginDate", flowCreateDate);
	jo.put("flowTitle", "<a href='javascript:;' onclick=\"addTab('" + flowTitle + "', '" + request.getContextPath() + "/flowShowPage.do?flowId=" + flowId + "')\">" + flowTitle + "</a>");
	jo.put("flowStarter", flowStarter);
	jo.put("flowStatus", flowStatus);
	%>
	<tr>
	<%
	for (int i=0; i<jsonArray.length(); i++) {
		if (((Boolean)jsonArray.getJSONObject(i).get("hide")).booleanValue())
			continue;
		String name = (String)jsonArray.getJSONObject(i).get("name");
		if (name.equalsIgnoreCase("cws_op"))
			continue;
		if (jo.has(name)) {
		%>
		<td><%=jo.get(name)%></td>		
		<%
		}
		else {
		%>
		<td> </td>
		<%
		}
	}
	%>
	</tr>
	<%
}

if (total>0) {
	String statDesc = aqd.getStatDesc();
	if (statDesc.equals(""))
		statDesc = "{}";
	JSONObject json = new JSONObject(statDesc);
	JSONObject jo = new JSONObject();
	Iterator ir3 = json.keys();
	int n = 0;
	while (ir3.hasNext()) {
		String key = (String) ir3.next();
		String modeStat = json.getString(key);
		
		double sumVal = FormSQLBuilder.getSUMOfSQL(sql, key);
		if (modeStat.equals("0")) {
			jo.put(key, NumberUtil.round(sumVal, 2));
		}
		else if (modeStat.equals("1")) {
			jo.put(key, "平均：" + NumberUtil.round(sumVal/total, 2));
		}
		n++;
	}
	if (n>0) {
		// jo.put("flowId", "合计");
		%>
		<tr>
		<%
		for (int i=0; i<jsonArray.length(); i++) {
			if (((Boolean)jsonArray.getJSONObject(i).get("hide")).booleanValue())
				continue;		
			String name = (String)jsonArray.getJSONObject(i).get("name");
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
			<td><%=s%></td>
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