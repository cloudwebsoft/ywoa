<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="org.json.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.worklog.WorkLogForModuleMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = privilege.getUser(request);
String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action");

String code = ParamUtil.get(request, "code");
String mainFormCode = code;
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(code);
if (msd==null) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
}

int is_workLog = msd.getInt("is_workLog");
if (!msd.getString("code").equals(msd.getString("form_code"))) {
	ModuleSetupDb msdMain = msd.getModuleSetupDb(msd.getString("form_code"));
	is_workLog = msdMain.getInt("is_workLog");
	mainFormCode = msd.getString("form_code");
}

if (msd.getInt("is_use") != 1) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块未启用！"));
	return;
}

String formCode = msd.getString("form_code");

if (msd.getInt("view_list")==ModuleSetupDb.VIEW_LIST_GANTT) {
	response.sendRedirect(request.getContextPath() + "/" + "visual/module_list_gantt.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
	return;
}

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

ModulePrivDb mpd = new ModulePrivDb(code);
if (!mpd.canUserSee(privilege.getUser(request))) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />	
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String orderBy = ParamUtil.get(request, "orderBy");
String sort = ParamUtil.get(request, "sort");

if (orderBy.equals("")) {
	String filter = StrUtil.getNullStr(msd.getString("filter")).trim();
	boolean isComb = filter.startsWith("<items>") || filter.equals("");
	// 如果是组合条件，则赋予后台设置的排序字段
	if (isComb) {
		orderBy = StrUtil.getNullStr(msd.getString("orderby"));
		sort = StrUtil.getNullStr(msd.getString("sort"));
	}
	if ("".equals(orderBy)) {
		orderBy = "id";
	}
}

if (sort.equals(""))
	sort = "desc";

String querystr = "";

int pagesize = ParamUtil.getInt(request, "pageSize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
String unitCode = ParamUtil.get(request, "unitCode");

%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>

<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCode%>.jsp"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

</head>
<body>
<%@ include file="module_inc_menu_top.jsp"%>
<%
int menuItem = ParamUtil.getInt(request, "menuItem", 1);
%>
<script>
o("menu<%=menuItem%>").className="current";
</script>
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
	return;
}
if (unitCode.equals("")) {
	unitCode = privilege.getUserUnitCode(request);
}

// 用于传过滤条件
request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
String[] ary = null;
try {
	ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "SQL：" + e.getMessage()));
	return;
}

String sql = ary[0];
String sqlUrlStr = ary[1];

// out.print(sql);
querystr = "op=" + op + "&code=" + code + "&orderBy=" + orderBy + "&sort=" + sort + "&unitCode=" + unitCode;

// 将过滤配置中request中其它参数也传至url中，这样分页时可传入参数
String requestParams = "";
String requestParamInputs = "";

Map map = ModuleUtil.getFilterParams(request, msd);
Iterator irMap = map.keySet().iterator();
while (irMap.hasNext()) {
	String key = (String)irMap.next();
	String val = (String)map.get(key);
	requestParams += "&" + key + "=" + val;
	requestParamInputs += "<input type='hidden' name='" + key + "' value='" + val + "' />";	
}
querystr += requestParams;
	
if (!sqlUrlStr.equals(""))
	querystr += "&" + sqlUrlStr;

if (action.equals("del")) {
	if (!mpd.canUserManage(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
		
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (fdm.del(request)) {
			String privurl = ParamUtil.get(request, "privurl");
			// out.print(StrUtil.Alert_Redirect("删除成功！", "module_list.jsp?" + querystr + "&CPages=" + curpage));
			out.print(StrUtil.jAlert_Redirect("删除成功！","提示", privurl));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
}
else if (action.equals("batchOp")) {
	if (!mpd.canUserManage(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
		
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (fdm.batchOperate(request)) {
			String privurl = ParamUtil.get(request, "privurl");
			out.print(StrUtil.jAlert_Redirect("操作成功！","提示", privurl));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}	
}
else if (action.equals("setUnitCode")) {
	if (!mpd.canUserManage(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String strIds = ParamUtil.get(request, "ids");
	String[] ids = StrUtil.split(strIds, ",");
	if (ids==null) {
		out.print(StrUtil.jAlert_Back("请选择记录！","提示"));
		return;
	}
	
	JSONObject json = new JSONObject();
	String toUnitCode = ParamUtil.get(request, "toUnitCode");
	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
	try {
		int len = ids.length;
		for (int i=0; i<len; i++) {
			int id = StrUtil.toInt(ids[i]);
			fdao = fdao.getFormDAO(id, fd);
			fdao.setUnitCode(toUnitCode);
			fdao.save();
		}
		json.put("ret", "1");
		json.put("msg", "操作成功！");
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
	}
	out.print(json);
	return;
}

com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
int total = lr.getTotal();
Vector v = lr.getResult();

Iterator ir = null;
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");
String[] fieldsWidth = msd.getColAry(false, "listFieldWidth");
String[] fieldsOrder = msd.getColAry(false, "list_field_order");
String[] fieldsLink = msd.getColAry(false, "list_field_link");

MacroCtlMgr mm = new MacroCtlMgr();

String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, "#");
String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btnBclass, ",");

boolean isToolbar = true;
if (btnNames!=null) {
	int len = btnNames.length;
	for (int i=0; i<len; i++) {
		if (btnScripts[i].startsWith("{")) {
			JSONObject json = new JSONObject(btnScripts[i]);
			if (((String)json.get("btnType")).equals("queryFields")) {	
				if (json.has("isToolbar")) {
					isToolbar = json.getInt("isToolbar")==1;
				}			
				break;
			}
		}
	}
}

boolean isButtonsShow = false;
isButtonsShow = (msd.getInt("btn_add_show")==1 && mpd.canUserAppend(privilege.getUser(request))) ||
				(msd.getInt("btn_edit_show")==1 && mpd.canUserModify(privilege.getUser(request))) ||
				mpd.canUserManage(privilege.getUser(request)) ||
				mpd.canUserImport(privilege.getUser(request)) ||
				mpd.canUserExport(privilege.getUser(request)) ||
				(btnNames!=null && isToolbar);
String strSearchTableDis = "";
if (!isButtonsShow) {
	strSearchTableDis = "display:none";
}

if (!isToolbar) {
%>
<style>
.condSpan {
	display:inline-block;
	float:left;
	width:330px;
	height:32px;
}
.condBtnSearch {
	display:inline-block;
	float:left;
}
</style>
<%
}
%>
<table id="searchTable" style="<%=strSearchTableDis %>" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="left">
    <form id="searchForm" action="module_list.jsp">&nbsp;
    <%	
	boolean isShowUnitCode = false;
	/*
    Vector vt = privilege.getUserAdminUnits(request);
	if (vt.size()>0) {
		isShowUnitCode = true;
		if (vt.size()==1) {
			DeptDb dd = (DeptDb)vt.elementAt(0);
			// 只有一个单位，且是本单位节点
			if (dd.getCode().equals(privilege.getUserUnitCode(request))) {
				isShowUnitCode = false;
			}
		}
	}
	*/
	
	String myUnitCode = privilege.getUserUnitCode(request);
	DeptDb dd = new DeptDb();
	dd = dd.getDeptDb(myUnitCode);
	
	Vector vtUnit = new Vector();
	vtUnit.addElement(dd);
		
	// 向下找两级单位
    DeptChildrenCache dl = new DeptChildrenCache(dd.getCode());
    java.util.Vector vt = dl.getDirList();	
	Iterator irDept = vt.iterator();
	while (irDept.hasNext()) {
		dd = (DeptDb)irDept.next();
		if (dd.getType()==DeptDb.TYPE_UNIT) {
			vtUnit.addElement(dd);
			DeptChildrenCache dl2 = new DeptChildrenCache(dd.getCode());
			Iterator ir2 = dl2.getDirList().iterator();
			while (ir2.hasNext()) {
				dd = (DeptDb)ir2.next();
				if (dd.getType()==DeptDb.TYPE_UNIT) {
					vtUnit.addElement(dd);
				}
				
				DeptChildrenCache dl3 = new DeptChildrenCache(dd.getCode());
				Iterator ir3 = dl3.getDirList().iterator();
				while (ir3.hasNext()) {
					dd = (DeptDb)ir3.next();
					if (dd.getType()==DeptDb.TYPE_UNIT) {
						vtUnit.addElement(dd);
					}
				}				
			}
			
		}
	}
	
	// 如果是集团单位，且能够管理模块
	if (vtUnit.size()>1 && mpd.canUserManage(privilege.getUser(request))) {
		// 如果是总部用户
		// if (myUnitCode.equals(DeptDb.ROOTCODE)) {
			isShowUnitCode = true;
		// }
	}
	
	if (isShowUnitCode) {
	%>
	<span class="condSpan">
    <select id="unitCode" name="unitCode" onChange="onChangeUnitCode(this.value);">
    <%if (privilege.getUserUnitCode(request).equals(DeptDb.ROOTCODE)) {%>    
    <option value="-1">不限</option>
    <%}%>
    <%
	Iterator irUnit = vtUnit.iterator();
    while (irUnit.hasNext()) {
    	dd = (DeptDb)irUnit.next();
    	int layer = dd.getLayer();
    	String layerStr = "";
    	for (int i=2; i<layer; i++) {
    		layerStr += "&nbsp;&nbsp;";
    	}
    	if (layer>1) {
    		layerStr += "├";
    	}
    %>
    <option value="<%=dd.getCode()%>"><%=layerStr%><%=dd.getName()%></option>
    <%}%>
    </select>
    </span>
    <%}%>
<%
ArrayList<String> list = new ArrayList<String>();

int len = 0;
boolean isQuery = false;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
		if (btnScripts[i].startsWith("{")) {
			JSONObject json = new JSONObject(btnScripts[i]);
			if (((String)json.get("btnType")).equals("queryFields")) {			
				isQuery = true;
				String condFields = (String)json.get("fields");
				String[] fieldAry = StrUtil.split(condFields, ",");
				Iterator irKey = json.keys();
				for (int j=0; j<fieldAry.length; j++) {
					String fieldName = fieldAry[j];
					FormField ff = fd.getFormField(fieldName);
					if (ff==null) {
						out.print(fieldName + "不存在");
						continue;
					}
					
					String condType = (String)json.get(fieldName);
					String queryValue = ParamUtil.get(request, fieldName);
					// 用于给convertToHTMLCtlForQuery辅助传值
					ff.setCondType(condType);
					%>
                    <span class="condSpan">
        			<%=ff.getTitle()%>
               		<%if (ff.getType().equals(ff.TYPE_DATE) || ff.getType().equals(ff.TYPE_DATE_TIME)) {
               			%>
						<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
               			<%
						if (condType.equals("0")) {
							String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
							String tDate  = ParamUtil.get(request, ff.getName() + "ToDate");
							list.add(ff.getName() + "FromDate");
							list.add(ff.getName() + "ToDate");
							%>
                              大于
                              <input id="<%=ff.getName()%>FromDate" name="<%=ff.getName()%>FromDate" size="15" style="width:80px" value = "<%=fDate%>" />
                              小于
                              <input id="<%=ff.getName()%>ToDate" name="<%=ff.getName()%>ToDate" size="15" style="width:80px" value = "<%=tDate%>" />
	  						<%
						}
						else {
							list.add(ff.getName());
							%>
                              <input id="<%=ff.getName()%>" name="<%=ff.getName()%>" size="15" value = "<%=queryValue%>" />
							<%
						}
					} else if(ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu!=null) {
							String queryValueRealShow = ParamUtil.get(request, fieldName + "_realshow");
							out.print(mu.getIFormMacroCtl().convertToHTMLCtlForQuery(request, ff));
							%>
							<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
							<script>
							$(document).ready(function() {
							o("<%=fieldName%>").value = "<%=queryValue%>";
							try {
                          	  o("<%=fieldName%>_realshow").value = "<%=queryValueRealShow%>";
                            } catch (e) {}
							});
							</script>						
						<%
						}
					}
					else if (ff.getFieldType()==FormField.FIELD_TYPE_INT || ff.getFieldType()==FormField.FIELD_TYPE_DOUBLE || ff.getFieldType()==FormField.FIELD_TYPE_FLOAT || ff.getFieldType()==FormField.FIELD_TYPE_LONG || ff.getFieldType()==FormField.FIELD_TYPE_PRICE) {
						String nameCond = ParamUtil.get(request, fieldName + "_cond");
						if ("".equals(nameCond)) {
							nameCond = condType;
						}
						%>
				          <select name="<%=fieldName%>_cond">
				            <option value="=" selected="selected">等于</option>
				            <option value=">">></option>
				            <option value="&lt;"><</option>
				            <option value=">=">>=</option></option>
				            <option value="&lt;="><=</option>
				          </select>						
	                      <input name="<%=fieldName%>" size="5" />
						<script>
						$(document).ready(function() {
							o("<%=fieldName%>_cond").value = "<%=nameCond%>";
							o("<%=fieldName%>").value = "<%=queryValue%>";
						});
						</script>						
						<%
					}
					else {
						%>
						<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
	                    <input name="<%=fieldName%>" size="5" />
						<script>
						$(document).ready(function() {
						o("<%=fieldName%>").value = "<%=queryValue%>";
						});
						</script>
						<%
					}
					%>
					</span>
					<%
				}
			}
		}
	}
	
	if (isQuery) {
	%>
        <input type="hidden" name="op" value="search" />
        <input type="hidden" name="code" value="<%=code%>" />
        <%=requestParamInputs%>
        <input type="hidden" name="menuItem" value="<%=menuItem%>" />
        <input type="hidden" name="mainCode" value="<%=ParamUtil.get(request, "mainCode")%>" />
        <input class="tSearch condBtnSearch" name="submit" type="submit" value=" 搜索 " />
	<%
	}
}
%>
        </form>
    </td>
  </tr>
</table>
<table id="grid" border="0" cellpadding="2" cellspacing="0">
  <thead>
  <tr>
<%
String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
boolean isPrompt = false;
if (!promptField.equals("") && !promptIcon.equals("")) {
	isPrompt = true;
}

if (isPrompt) {
%>
<th width="20"></th>
<%
}

len = 0;
if (fields!=null)
	len = fields.length;
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	String title = "";
	if (fieldName.startsWith("main:")) {
		String[] subFields = StrUtil.split(fieldName, ":");
		if (subFields.length == 3) {
			FormDb subfd = new FormDb(subFields[1]);
			title = subfd.getFieldTitle(subFields[2]);
		}
	} else if (fieldName.startsWith("other:")) {
		String[] otherFields = StrUtil.split(fieldName, ":");
		if (otherFields.length == 5) {
			FormDb otherFormDb = new FormDb(otherFields[2]);
			title = otherFormDb.getFieldTitle(otherFields[4]);
		}
	} else if (fieldName.equals("cws_creator")) {
		title = "创建者";
	}
	else if (fieldName.equals("ID")) {
		title = "ID";
	}
	else if (fieldName.equals("cws_progress")) {
		title = "进度";
	}
	else {
		title = fd.getFieldTitle(fieldName);
	}
	String w = fieldsWidth[i];
	int wid = StrUtil.toInt(w, 50);
	if (w.indexOf("%")==w.length()-1) {
		w = w.substring(0, w.length()-1);
		wid = 800*StrUtil.toInt(w, 20)/100;
	}
%>
    <th width="<%=wid%>" style="cursor:hand" abbr="<%=fieldName%>">
	<%=title%>	
	</th>
<%}%>
    <th width="150" style="cursor:hand">操作</th>
  </tr>
  </thead>
  <tbody>
  <%	
		com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
		String desKey = ssoCfg.get("key");
		  
  		boolean canView = mpd.canUserView(privilege.getUser(request));
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long id = fdao.getId();
		%>
  <tr align="center" id="<%=id%>">
  	<%
	if (isPrompt) {
	%>
		<td>
		<%
		// 判断条件
		if (ModuleUtil.isPrompt(request, msd, fdao)) {
		%>
		<img src="<%=SkinMgr.getSkinPath(request)%>/icons/prompt/<%=promptIcon%>" style="width:16px;" align="absmiddle" />
		<%
		}
		%>
		</td>
		<%
	}
	%>
	<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
	%>	
		<td align="left">
        <%		
		if (!fieldsLink[i].equals("#") && !fieldsLink[i].startsWith("$")) {
			String link = FormUtil.parseAndSetFieldValue(fieldsLink[i], fdao);	
			if (!link.startsWith("http:"))
				link = request.getContextPath() + "/" + link;
		%>
			<a href="javascript:;" onClick="addTab('<%=msd.getString("name")%>', '<%=link%>')">
        <%}
        else if (i==0 && "#".equals(fieldsLink[i]) && canView) {
			if (msd.getInt("view_show")==ModuleSetupDb.VIEW_SHOW_CUSTOM) {			
        %>
			<a href="javascript:;" onclick="addTab('<%=msd.getString("name")%>', '<%=request.getContextPath()%>/<%=msd.getString("url_show")%>?parentId=<%=id%>&id=<%=id%>&code=<%=code%>')">
	    <%	}
			else {
			%>
			<a href="javascript:;" onclick="addTab('<%=msd.getString("name")%>', '<%=request.getContextPath()%>/visual/module_show.jsp?parentId=<%=id%>&id=<%=id%>&code=<%=code%>')">
			<%
			}
		}%>
		<%
		if (fieldName.startsWith("main:")) {
			String[] subFields = fieldName.split(":");
			if (subFields.length == 3) {
				FormDb subfd = new FormDb(subFields[1]);
				com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
				FormField subff = subfd.getFormField(subFields[2]);
				String subsql = "select id from " + subfdao.getTableName() + " where cws_id=" + id + " order by cws_order";
				JdbcTemplate jt = new JdbcTemplate();
				StringBuilder sb = new StringBuilder();
				try {
					ResultIterator ri = jt.executeQuery(subsql);
					while (ri.hasNext()) {
						ResultRecord rr = (ResultRecord) ri.next();
						int subid = rr.getInt(1);
						subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
						String subFieldValue = subfdao.getFieldValue(subFields[2]);
						if (subff != null && subff.getType().equals(FormField.TYPE_MACRO)) {
							MacroCtlUnit mu = mm.getMacroCtlUnit(subff.getMacroType());
							if (mu != null) {
								subFieldValue = mu.getIFormMacroCtl().converToHtml(request, subff, subFieldValue);
							}
						}
						sb.append("<span>").append(subFieldValue).append("</span>").append(ri.hasNext() ? "</br>" : "");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				out.print(sb.toString());
			}
		} else if (fieldName.startsWith("other:")) {
			out.print(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName));
		} 
		else if (fieldName.equals("ID")) {
			out.print(fdao.getId());
		}
		else if (fieldName.equals("cws_progress")) {
			out.print(fdao.getCwsProgress());
		}
		else if (fieldName.equals("cws_creator")) {
			String realName = "";
			if (fdao.getCreator()!=null) {
			UserDb user = um.getUserDb(fdao.getCreator());
			if (user!=null)
				realName = user.getRealName();
			}
		%>
			<%=realName%>
		<%
		}
		else {
			FormField ff = fdao.getFormField(fieldName);
			if (ff==null) {
				out.print(fieldName + " 已不存在！");
			}
			else {
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						// request.setAttribute("fdao", fdao);
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
					}
				}
				else {%>		
					<%=FuncUtil.renderFieldValue(fdao, ff)%>
				<%}
			}%>
		<%}
		if (!fieldsLink[i].equals("") && !fieldsLink[i].startsWith("$")) {%>		
        </a>
        <%}
        else if (i==0 && "#".equals(fieldsLink[i]) && canView) {
        %>
        </a>
        <%} %>
        </td>
	<%}%>
	<td>
	<%if (msd.getInt("btn_display_show")==1 && canView) {
		if (msd.getInt("view_show")==ModuleSetupDb.VIEW_SHOW_CUSTOM) {
			%>
	    	<a href="javascript:;" onClick="addTab('<%=msd.getString("name")%>', '<%=request.getContextPath()%>/<%=msd.getString("url_show")%>?parentId=<%=id%>&id=<%=id%>&code=<%=code%>')">查看</a>
			<%
		}
		else {%>    
	    	<a href="javascript:;" onClick="addTab('<%=msd.getString("name")%>', '<%=request.getContextPath()%>/visual/module_show.jsp?parentId=<%=id%>&id=<%=id%>&code=<%=code%>')">查看</a>
	    <%
		}
		if (fd.isFlow() && fdao.getFlowId()!=-1) { 
	        String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(fdao.getFlowId()));    
	    %>
	    	&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('查看流程', '<%=request.getContextPath() %>/flow_modify.jsp?flowId=<%=fdao.getFlowId() %>&visitKey=<%=visitKey %>')">流程</a>
	    <%} 
    }%>
	<%if (msd.getInt("btn_edit_show")==1 && mpd.canUserModify(privilege.getUser(request))) {
		if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {
			%>
    		&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('<%=msd.getString("name")%>', '<%=request.getContextPath()%>/<%=msd.getString("url_edit")%>?parentId=<%=id%>&id=<%=id%>&code=<%=code%>&formCode=<%=formCode%>')">修改</a>
			<%
		}
		else {
		%>
    		&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('<%=msd.getString("name")%>', '<%=request.getContextPath()%>/visual/module_edit.jsp?parentId=<%=id%>&id=<%=id%>&code=<%=code%>&formCode=<%=formCode%>')">修改</a>
    <%	}
	}
	String op_link_name = StrUtil.getNullStr(msd.getString("op_link_name"));
	String[] linkNames = StrUtil.split(op_link_name, ",");
	String op_link_href = StrUtil.getNullStr(msd.getString("op_link_url"));
	String[] linkHrefs = StrUtil.split(op_link_href, ",");
	
	String op_link_field = StrUtil.getNullStr(msd.getString("op_link_field"));
	String[] linkFields = StrUtil.split(op_link_field, ",");
	String op_link_cond = StrUtil.getNullStr(msd.getString("op_link_cond"));
	String[] linkConds = StrUtil.split(op_link_cond, ",");
	String op_link_value = StrUtil.getNullStr(msd.getString("op_link_value"));
	String[] linkValues = StrUtil.split(op_link_value, ",");
	String op_link_event = StrUtil.getNullStr(msd.getString("op_link_event"));
	String[] linkEvents = StrUtil.split(op_link_event, ",");		
	if (linkNames!=null) {
		for (int i=0; i<linkNames.length; i++) {
			String linkName = linkNames[i];
			
			String linkField = linkFields[i];
			String linkCond = linkConds[i];
			String linkValue = linkValues[i];
			String linkEvent = linkEvents[i];
			
			if (linkField.equals("#")) {
				linkField = "";
			}
			if (linkCond.equals("#")) {
				linkCond = "";
			}
			if (linkValue.equals("#")) {
				linkValue = "";
			}
			if (linkEvent.equals("#")) {
				linkEvent = "";
			}			
			if (linkField.equals("") || ModuleUtil.isLinkShow(request, msd, fdao, linkField, linkCond, linkValue)) {
				if ("click".equals(linkEvent)) {
			%>
				&nbsp;&nbsp;<a href="javascript:;" onClick="<%=ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code)%>"><%=linkName%></a>
			<%				
				}
				else {
			%>
				&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('<%=linkName%>', '<%=request.getContextPath()%>/<%=ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, code)%>')"><%=linkName%></a>
			<%
				}
			}
		}
	}	
	
	%>
	<%if (mpd.canUserManage(privilege.getUser(request))) {%>
    <!--
    &nbsp;&nbsp;<a onclick="if (!confirm('您确定要删除么？')) event.returnValue=false;" href="<%=request.getContextPath()%>/visual/module_list.jsp?action=del&op=<%=op%>&id=<%=id%>&code=<%=code%>&formCode=<%=formCode%>&CPages=<%=curpage%>&orderBy=<%=orderBy%>&sort=<%=sort%>&<%=sqlUrlStr%><%=requestParams%>">删除</a>
    -->
    <%if (fd.isLog()) {%>
    &nbsp;&nbsp;<a href="javascript:;" onClick="addTab('修改日志', '<%=request.getContextPath()%>/visual/module_log_list.jsp?op=search&code=<%=code%>&fdaoId=<%=id%>&formCode=<%=formCode%>')">日志</a>
    <%}%>
    <%}%>
    <% WorkLogForModuleMgr wlfm = new WorkLogForModuleMgr();
    	if(is_workLog==1){ %>
    		&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('<%=msd.getString("name")%>汇报', '<%=request.getContextPath()%>/queryMyWork.action?code=<%=mainFormCode%>&id=<%=id%>')">汇报</a>
    <%
    	}
    %>
    <%if (mpd.canUserReActive(privilege.getUser(request))) {
    	MyActionDb mad = new MyActionDb();
    	long flowId = fdao.getFlowId();
    	if (flowId!=0 && flowId!=-1) {
			WorkflowDb wf = new WorkflowDb();
			wf = wf.getWorkflowDb((int)flowId);
			WorkflowPredefineDb wpd = new WorkflowPredefineDb();
			wpd = wpd.getDefaultPredefineFlow(wf.getTypeCode());
			boolean isReactive = false;
			if (wpd!=null) {
				isReactive = wpd.isReactive();
			}

			if (isReactive) {
    			mad = mad.getMyActionDbFirstChecked(flowId, privilege.getUser(request));
				if (mad!=null) {
		%>
					&nbsp;&nbsp;<a href="javascript:;" onClick="addTab('<%=msd.getString("name")%>变更', '<%=request.getContextPath()%>/flow_dispose.jsp?myActionId=<%=mad.getId() %>')">变更</a>
		<%		}
			}
    	}
    }%>
    </td>
  </tr>
  <%
  }
%>
  </tbody>
</table>
<div id="dlg" style="display:none">
	<%if (isShowUnitCode) {%>
    将数据迁移至：<select id="toUnitCode" name="toUnitCode">
    <%
	Iterator irUnit = vtUnit.iterator();
    while (irUnit.hasNext()) {
    	dd = (DeptDb)irUnit.next();
    %>
    <option value="<%=dd.getCode()%>"><%=dd.getName()%></option>
    <%}%>
    </select>
    <%}%>
</div>
</body>
<script>
function initCalendar() {
	<%for (String ffname : list) {%>
	$('#<%=ffname%>').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d'
    });
    <%}%>
}

function doOnToolbarInited() {
	initCalendar();
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "<%=request.getContextPath()%>/visual/module_list.jsp?op=<%=op%>&code=<%=code%>&unitCode=<%=unitCode%>&formCode=<%=formCode%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&<%=sqlUrlStr%>";
}

function changePage(newp) {
	if (newp)
		window.location.href = "<%=request.getContextPath()%>/visual/module_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	var curPage =  parseInt(<%=curpage%>);
	var total = parseInt(<%=total%>);
	var curTotal = curPage*parseInt(pageSize);
	if(curTotal>total){
		curPage = 1;
	}
	window.location.href = "<%=request.getContextPath()%>/visual/module_list8.jsp?<%=querystr%>&CPages="+curPage+"&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
<%if (isButtonsShow) {%>	
	buttons : [
	<%if (msd.getInt("btn_add_show")==1 && mpd.canUserAppend(privilege.getUser(request))) {%>
		{name: '添加', bclass: 'add', onpress : action},
	<%}%>
	<%if (msd.getInt("btn_edit_show")==1 && mpd.canUserModify(privilege.getUser(request))) {%>
		{name: '修改', bclass: 'edit', onpress : action},	
    <%}%>
	<%if (mpd.canUserManage(privilege.getUser(request))) {%>
		{name: '删除', bclass: 'delete', onpress : action},
		// {name: '全部导入', bclass: 'import1', onpress : action},
		// {name: '全部导出', bclass: 'export', onpress : action},
		<%if (isShowUnitCode) {%>
		{name: '迁移', bclass: 'pass', onpress : action},
		<%}%>
	<%}%>    
	<%if (mpd.canUserImport(privilege.getUser(request))) {%>
		{name: '导入', bclass: 'import1', onpress : action},
	<%}%>    
	<%if (mpd.canUserExport(privilege.getUser(request))) {%>
		{name: '导出', bclass: 'export', onpress : action},
    <%}%>	
<%
if (btnNames!=null && btnBclasses!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
		boolean isToolBtn = false;
		if (!btnScripts[i].startsWith("{")) {
			isToolBtn = true;
		}
		else {
			JSONObject json = new JSONObject(btnScripts[i]);			
			if (((String)json.get("btnType")).equals("batchBtn")) {
				isToolBtn = true;
			}		
		}
		if (isToolBtn) {
			// 产品基本信息模块中btnBclasses为null
			String btnBCls = "";
			if (btnBclasses!=null) {
				btnBCls = btnBclasses[i];
			}
			%>
			{name: '<%=btnNames[i]%>', bclass: '<%=btnBCls%>', onpress : action},
			<%		
		}
	}
}
%>		
		{separator: true}
		<%if (isToolbar) {%>
		,{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		<%}%>
		],
<%}%>		
	/*
	searchitems : [
		{display: 'ISO', name : 'iso'},
		{display: 'Name', name : 'name', isdefault: true}
		],
	*/
	sortname: "<%=orderBy%>",
	sortorder: "<%=sort%>",
	url: false,
	usepager: true,
	checkbox : true,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	
	// title: "通知",
	singleSelect: true,
	resizable: false,
	showTableToggleBtn: true,
	showToggleBtn: true,
	
	onChangeSort: changeSort,
	
	onChangePage: changePage,
	onRpChange: rpChange,
	onReload: onReload,
	/*
	onRowDblclick: rowDbClick,
	onColSwitch: colSwitch,
	onColResize: colResize,
	onToggleCol: toggleCol,
	*/
	onToolbarInited: doOnToolbarInited,
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);

<%
String queryString = StrUtil.getNullString(request.getQueryString());
String privurl=request.getRequestURL()+"?"+StrUtil.UrlEncode(queryString, "utf-8");
%>

function action(com, grid) {
	if (com=="导出") {
		var cols = "";
		$("th[axis*='col']", this.hDiv).each(function() {
			if (!this.hide) {
				if(typeof($(this).attr("abbr"))!="undefined") {
					if (cols=="") {
						cols = $(this).attr("abbr");
					}
					else {
						cols += "," + $(this).attr("abbr");
					}
				}
			}
		});
		
		window.open('module_excel.jsp?<%=querystr%>&cols=' + cols);
	}
	else if (com=="全部导出") {
		window.open('module_excel.jsp?isAll=true&<%=querystr%>');
	}	
	else if (com=="全部导入") {
		var url = "module_import_excel.jsp?isAll=true&formCode=<%=formCode%>";
		openWin(url,360,50);		
	}	
	else if (com=="添加") {
		window.location.href = "module_add.jsp?code=<%=code%>&privurl=<%=privurl%><%=requestParams%>";
	}
	else if (com=="修改") {
		selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('请选择记录!', '提示');
			return;
		}
		else if (selectedCount>1) {
			jAlert('请选择一条记录!', '提示');
			return;
		}
		var id = "";
		// value!='on' 过滤掉复选框按钮
		$(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function(i) {
			id = $(this).val();
		});		
		
		<%if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {%>
    	addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/<%=msd.getString("url_edit")%>?parentId=" + id + "&id=" + id + "&code=<%=code%>&formCode=<%=formCode%>");
		<%}else{%>
    	addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_edit.jsp?parentId=" + id + "&id=" + id + "&code=<%=code%>&formCode=<%=formCode%>");
		<%}%>
	}
	else if (com=='删除') {
		selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('请选择记录!','提示');
			return;
		}
		jConfirm("您确定要删除么？","提示",function(r){
			if(!r){return;}
			else{
				var ids = "";
				// value!='on' 过滤掉复选框按钮
				$(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function(i) {
					if (ids=="")
						ids = $(this).val();
					else
						ids += "," + $(this).val();
				});
				window.location.href = "module_list8.jsp?action=del&<%=querystr%>&CPages=<%=curpage%>&privurl=<%=privurl%>&id=" + ids + "&pageSize=" + flex.getOptions().rp;
			}
		})
	} else if(com=='导入'){
		// var url = "module_import_excel.jsp?formCode=<%=formCode%>";
		// openWin(url,530,40);
		window.location.href = "module_import_excel.jsp?formCode=<%=formCode%>&code=<%=code%>";
	}
	else if (com=="迁移") {
		selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
		if (selectedCount == 0) {
			jAlert('请选择记录!','提示');
			return;
		}
		jQuery("#dlg").dialog({
			title: "迁移",
			modal: true,
			// bgiframe:true,
			buttons: {
				"取消": function() {
					jQuery(this).dialog("close");
				},
				"确定": function() {
					
					var ids = "";
					// value!='on' 过滤掉复选框按钮
					$(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function(i) {
						if (ids=="")
							ids = $(this).val();
						else
							ids += "," + $(this).val();
					});		

					jQuery.ajax({
						type: "post",
						url: "module_list8.jsp",
						data : {
							code: "<%=code%>",
							action: "setUnitCode",
							ids: ids,
							toUnitCode: $("#toUnitCode").val()
						},
						dataType: "html",
						beforeSend: function(XMLHttpRequest){
							//ShowLoading();
						},
						success: function(data, status){
							data = $.parseJSON(data);
							if (data.ret=="0") {
								jAlert(data.msg, "提示");
							}
							else {
								jAlert(data.msg, "提示");
								window.location.href = "<%=request.getRequestURL()+"?"+request.getQueryString()%>";
							}							
						},
						complete: function(XMLHttpRequest, status){
							// HideLoading();
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert("error:" + XMLHttpRequest.responseText);
						}
					});	
	
					jQuery(this).dialog("close");						
				}
			},
			closeOnEscape: true,
			draggable: true,
			resizable:true,
			width:300					
			});
			
	}
	
	<%
	if (btnNames!=null) {
		len = btnNames.length;
		for (int i=0; i<len; i++) {
			if (!btnScripts[i].startsWith("{")) {
			%>
				if (com=='<%=btnNames[i]%>') {
				<%=ModuleUtil.renderScript(request, btnScripts[i])%>
				}
			<%
			}
			else {
				JSONObject json = new JSONObject(btnScripts[i]);			
				if (((String)json.get("btnType")).equals("batchBtn")) {
					String batchField = json.getString("batchField");
					String batchValue = json.getString("batchValue");
				%>
					if (com=='<%=btnNames[i]%>') {
						selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
						if (selectedCount == 0) {
							jAlert('请选择记录!','提示');
							return;
						}
						jConfirm("您确定要" + com + "么？","提示",function(r){
							if(!r){return;}
							else{
								var ids = "";
								// value!='on' 过滤掉复选框按钮
								$(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function(i) {
									if (ids=="")
										ids = $(this).val();
									else
										ids += "," + $(this).val();
								});
								window.location.href = "module_list8.jsp?action=batchOp&batchField=<%=StrUtil.UrlEncode(batchField)%>&batchValue=<%=StrUtil.UrlEncode(batchValue)%>&<%=querystr%>&CPages=<%=curpage%>&privurl=<%=privurl%>&id=" + ids + "&pageSize=" + flex.getOptions().rp;
							}
						})					
					}
				<%
				}		
			}			
		}
	}
	%>		
	
}

// 用于工具条自定义按钮的调用
function getIdsSelected(onlyOne) {
	var selectedCount = $(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).length;
	if (selectedCount == 0) {
		return "";
	}
	
	if (selectedCount > 1 && onlyOne) {
		return "";
	}
		
	var ids = "";
	// value!='on' 过滤掉复选框按钮
	$(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function(i) {
		if (ids=="")
			ids = $(this).val();
		else
			ids += "," + $(this).val();
	});
	return ids;
}

$(function() {
	$("#unitCode").val("<%=unitCode%>");
});

function onChangeUnitCode(unitCode) {
	window.location.href = "<%=request.getContextPath()%>/visual/module_list.jsp?op=<%=op%>&code=<%=code%>&unitCode=" + unitCode + "&formCode=<%=formCode%>&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>&sort=<%=sort%>";
}
</script>
</html>
