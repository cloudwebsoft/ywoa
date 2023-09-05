<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "java.sql.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "com.redmoon.oa.flow.query.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.BasicDataMgr"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.sql.SqlNode"%>
<%@ page import = "org.json.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String op = ParamUtil.get(request, "op");
if (op.equals("")) {
	// response.setContentType("application/x-json"); 
	
	int id = ParamUtil.getInt(request, "id", -1);
	if (id==-1) {
		out.print("id不能为空！");
		return;
	}
	
	FormQueryDb aqd = new FormQueryDb();
	aqd = aqd.getFormQueryDb(id);
	
	QueryScriptUtil	qsu = new QueryScriptUtil();
	try {
		qsu.getCondFields(request, aqd);
	}
	catch(ErrMsgException e) {
		out.print(e.getMessage());
		return;
	}
	
	// qsu.getCondNodes();
	
	Map map = qsu.getCondTitleMap();
	
	// action为sel时表示拉单，否则为changeCondValue
	String action = ParamUtil.get(request, "action");
	
	// 用于查询字段选择宏控件
	String openerFormCode = ParamUtil.get(request, "openerFormCode");
	String openerFieldName = ParamUtil.get(request, "openerFieldName");
	
	%>
	<form id="formConds" name="formConds" action="form_query_script_list_do.jsp" method="post">
        <input id="id" name="id" value="<%=id%>" type="hidden" />
    <%if (action.equals("sel")) {
		String nestFormCode = ParamUtil.get(request, "nestFormCode");
		String nestType = ParamUtil.get(request, "nestType");
		String parentFormCode = ParamUtil.get(request, "parentFormCode");
		String nestFieldName = ParamUtil.get(request, "nestFieldName");
		long parentId = ParamUtil.getLong(request, "parentId", com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
		%>
        <input id="nestFormCode" name="nestFormCode" value="<%=nestFormCode%>" type="hidden" />
        <input id="nestType" name="nestType" value="<%=nestType%>" type="hidden" />
        <input id="parentFormCode" name="parentFormCode" value="<%=parentFormCode%>" type="hidden" />
        <input id="nestFieldName" name="nestFieldName" value="<%=nestFieldName%>" type="hidden" />
        <input id="parentId" name="parentId" value="<%=parentId%>" type="hidden" />
        <input id="action" name="action" value="searchInResult" type="hidden" />
        <input id="mode" name="mode" value="<%=action%>" type="hidden" />
    <%}else if (action.equals("selField")) {%>
        <input id="mode" name="mode" value="<%=action%>" type="hidden" />
        <input id="openerFormCode" name="openerFormCode" value="<%=openerFormCode%>" type="hidden" />
        <input id="openerFieldName" name="openerFieldName" value="<%=openerFieldName%>" type="hidden" />
        <input id="action" name="action" value="searchInResult" type="hidden" />
    <%}else if (action.equals("filter")) {%>
        <input id="mode" name="mode" value="<%=action%>" type="hidden" />
        <input id="action" name="action" value="searchInResult" type="hidden" />
	<%}else{%>
        <input id="mode" name="mode" value="changeCondValue" type="hidden" />
    <%}%>
	<%
	// 如果action为sel，则说明是拉单，如果不是，则说明是在查询结果页中重新配置搜索条件
	if (!action.equals("sel") && !action.equals("selField") && !action.equals("filter")) {
		Iterator ir = qsu.getCondNodes().iterator();
		while (ir.hasNext()) {
			SqlNode sn = (SqlNode)ir.next();
			String fieldName = sn.getLeft();
			fieldName = fieldName.replaceAll("#", "");
			String fieldTitle = (String)map.get(fieldName);
			
			String cond = sn.getCondition();
			String right = sn.getRight();

			if (right.startsWith("'")) {
				right = right.substring(1, right.length()-1);
			}
			
			if ("like".equals(cond)) {
				cond = "包含";
				if (right.startsWith("%")) {
					right = right.substring(1);
				}
				if (right.endsWith("%")) {
					right = right.substring(0, right.length() - 1);
				}
			}
			right = right.replaceAll("#", "");
			%>
			<div>
			<%=fieldTitle%>&nbsp;<%=cond%>&nbsp;
			<input id="<%=fieldName%>" name="<%=fieldName%>" value="<%=right%>" />
			</div>
			<%
		}
	}
	else { // 如果是拉单或者是查询字段选择窗体宏控件
		map = qsu.getCols(request, aqd);
		if (map==null) {
			out.print("查询结果不存在！");
			return;
		}
		Iterator irMap = map.keySet().iterator();

		Map mapFieldType = qsu.getMapFieldType();
		Map mapFieldTitle = qsu.getMapFieldTitle();
%>
<table width="100%">
<%
	while (irMap.hasNext()) {
		String keyName = (String) irMap.next();
		
		if (keyName.equals("CWS_CREATOR") || keyName.equals("CWS_ID") || keyName.equals("CWS_ORDER") || keyName.equals("FLOWTYPECODE")
				|| keyName.equals("CWS_STATUS") || keyName.equals("ID") || keyName.equals("FLOWID") || keyName.equals("UNIT_CODE")) {
			continue;
		}
		
		keyName = keyName.toUpperCase();
		Integer iType = (Integer)mapFieldType.get(keyName.toUpperCase());
		
		int fieldType = QueryScriptUtil.getFieldTypeOfDBType(iType.intValue());
		String fieldTitle = (String)mapFieldTitle.get(keyName.toUpperCase());
%>
		<tr>
            <td><%=fieldTitle%></td>
		    <td>
			  <%if (fieldType==FormField.FIELD_TYPE_DATE || fieldType==FormField.FIELD_TYPE_DATETIME) {%>
				从 <input size="12" id="<%=keyName%>FromDate" name="<%=keyName%>FromDate" kind="date">
		  	  <%}else{%>
				<select name="<%=keyName%>_cond">
				  <option value="1">等于</option>
				  <%if (fieldType==FormField.FIELD_TYPE_VARCHAR || fieldType==FormField.FIELD_TYPE_TEXT) {%>
				  <option value="0" selected>包含</option>
				  <%}
				  else if (fieldType!=FormField.FIELD_TYPE_BOOLEAN) {
				  %>
				  <option value=">=" selected>>=</option>
				  <option value=">" selected>></option>
				  <option value="<=" selected><=</option>
				  <option value="&lt;" selected><</option>
				  <%}%>
				</select>
			 <%}%></td>
		    <td>
			<%if (fieldType==FormField.FIELD_TYPE_DATE || fieldType==FormField.FIELD_TYPE_DATETIME) {%>
          		至
                <input size="12" id="<%=keyName%>ToDate" name="<%=keyName%>ToDate" kind="date">
			<%}else{%>
				<input id="keyName" name="<%=keyName%>" />
			<%}%>
		    </td>
	    </tr>
		<%}
	}
	%>
</table>    
	</form>
	<%
	
	// JSONObject jobject = new JSONObject();
	// jobject.put("rows", rows);
	

}
%>