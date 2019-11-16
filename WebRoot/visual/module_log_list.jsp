<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.base.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import = "com.redmoon.oa.worklog.WorkLogForModuleMgr"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = privilege.getUser(request);
String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action");

// 日志对应模块的编码
String moduleCode = ParamUtil.get(request, "code");

String code = ParamUtil.get(request, "moduleCodeLog");
if ("".equals(code)) {
	code = "module_log";
}
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(code);
if (msd==null) {
	%>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<%
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "模块不存在！"));
	return;
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
else if (msd.getInt("view_list")==ModuleSetupDb.VIEW_LIST_TREE) {
	boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
	if (!isInFrame) {
		response.sendRedirect(request.getContextPath() + "/" + "visual/module_list_frame.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
		return;
	}
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

request.setAttribute("pageType", "moduleList");

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
<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css" />  
<link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet">

<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>

<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>
<script src="<%=request.getContextPath()%>/inc/flow_js.jsp"></script>
<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCode%>.jsp?code=<%=code%>&pageType=moduleList"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<script src="../js/jquery.raty.min.js"></script>
<script src="../js/BootstrapMenu.min.js"></script>
<style>
input {
	line-height:normal; /*防止查询条件置于工具条时，input受其它样式影响紧贴于上方*/
}
/*
.search-form input {
	width: 80px;
}
*/
.search-form input:not([type="radio"],[type="button"]) {
	width: 80px;
}
</style>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp"%>
<%
int menuItem = 4; // ParamUtil.getInt(request, "menuItem", 1);
%>
<script>
o("menu<%=menuItem%>").className="current";
</script>
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
	return;
}

querystr = "op=" + op + "&moduleCode=" + moduleCode + "&code=" + code + "&orderBy=" + orderBy + "&sort=" + sort + "&unitCode=" + unitCode;

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
	
// 用于传过滤条件
request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
String[] ary = null;
try {
	ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
}
catch (ErrMsgException e) {
	out.print(e.getMessage());			
	return;
}

String sqlUrlStr = ary[1];
if (!sqlUrlStr.equals("")) {
	querystr += "&" + sqlUrlStr;
}

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");
if (fields==null || fields.length==0) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "显示列未配置！"));
	return;
}
String[] fieldsWidth = msd.getColAry(false, "list_field_width");

MacroCtlMgr mm = new MacroCtlMgr();

String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, "#");
String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btnBclass, ",");
String btnRole = StrUtil.getNullStr(msd.getString("btn_role"));
String[] btnRoles = StrUtil.split(btnRole, "#");

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
if (btnNames==null) {
	strSearchTableDis = "display:none";
}

if (!isToolbar) {
%>
<style>
.condSpan {
	display:inline-block;
	float:left;
	width:330px;
	min-height:32px;
}
.condBtnSearch {
	display:inline-block;
	float:left;
	width:50px;
}
</style>
<%
}
%>
<table id="searchTable" style="<%=strSearchTableDis %>" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="left" style="padding-top:5px">
    <form id="searchForm" class="search-form" action="module_log_list.jsp" onsubmit="return searchFormOnSubmit()">&nbsp;
    <%	
	boolean isShowUnitCode = false;
	Vector vtUnit = new Vector();
	DeptDb dd = new DeptDb();
	String myUnitCode = "";
	
    if (msd.getInt("is_unit_show")==1) {
		myUnitCode = privilege.getUserUnitCode(request);
		dd = dd.getDeptDb(myUnitCode);
		
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
			isShowUnitCode = true;
		}
		
		if (isShowUnitCode) {
		%>
		<span class="condSpan">
	    <select id="unitCode" name="unitCode">
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
	    <%}
    }%>
<%
ArrayList<String> list = new ArrayList<String>();

int len = 0;
boolean isQuery = false;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
		if (btnScripts[i].startsWith("{")) {
			Map<String, String> checkboxGroupMap = new HashMap<String, String>();
			FormMgr fm = new FormMgr();
			JSONObject json = new JSONObject(btnScripts[i]);
			if (((String)json.get("btnType")).equals("queryFields")) {
				isQuery = true;
				String condFields = (String)json.get("fields");
				String[] fieldAry = StrUtil.split(condFields, ",");
				Iterator irKey = json.keys();
				for (int j=0; j<fieldAry.length; j++) {
					boolean isSub = false;
					FormDb subFormDb = null;
					String fieldName = fieldAry[j];
					FormField ff = null;
					String title;
					String condType = (String)json.get(fieldName);
					String queryValue = ParamUtil.get(request, fieldName);
					if ("cws_status".equals(fieldName)) {
						title = "状态";
					}
					else if ("cws_flag".equals(fieldName)) {
						title = "冲抵状态";
					}
					else {
						if (fieldName.startsWith("main:")) { // 关联的主表
							 String[] aryField = StrUtil.split(fieldName, ":");			
							 String field = fieldName.substring(5);
							 if (aryField.length==3) {
							  	FormDb mainFormDb = fm.getFormDb(aryField[1]);
							  	ff = mainFormDb.getFormField(aryField[2]);
								if (ff==null) {
									out.print(fieldName + "不存在");
									continue;
								}							  	
							  	title = ff.getTitle();
							 }
							 else {
							  	title = field + " 不存在";
							 }
						}
						else if (fieldName.startsWith("other:")) { // 映射的字段，多重映射不支持
							 String[] aryField = StrUtil.split(fieldName, ":");
							 if (aryField.length<5) {
							 	title = "<font color='red'>格式非法</font>";
							 }
							 else {
								FormDb otherFormDb = fm.getFormDb(aryField[2]);
								ff = otherFormDb.getFormField(aryField[4]);
								if (ff==null) {
									out.print(fieldName + "不存在");
									continue;
								}								
								title = ff.getTitle();
							 }
						}
						else if (fieldName.startsWith("sub:")) { // 关联的子表
							 isSub = true;
							 String[] aryField = StrUtil.split(fieldName, ":");			
							 String field = fieldName.substring(5);
							 if (aryField.length==3) {
							  	subFormDb = fm.getFormDb(aryField[1]);
							  	ff = subFormDb.getFormField(aryField[2]);
								if (ff==null) {
									out.print(fieldName + "不存在");
									continue;
								}						
								if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {			
									String desc = StrUtil.getNullStr(ff.getDescription());	
									if (!"".equals(desc)) {
										title = desc;
									}
									else {		
										title = ff.getTitle();
									}
									String chkGroup = StrUtil.getNullStr(ff.getDescription());
									if (!"".equals(chkGroup)) {
										if (!checkboxGroupMap.containsKey(chkGroup)) {
											checkboxGroupMap.put(chkGroup, "");
										}
										else {
											continue;
										}
									}								
								}
								else {
									title = ff.getTitle();
								}								
							 }
							 else {
							  	title = field + " 不存在";
							 }							
						}
						else {
							ff = fd.getFormField(fieldName);
							if (ff==null) {
								out.print(fieldName + "不存在");
								continue;
							}
							if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {			
								String desc = StrUtil.getNullStr(ff.getDescription());	
								if (!"".equals(desc)) {
									title = desc;
								}
								else {		
									title = ff.getTitle();
								}
								String chkGroup = StrUtil.getNullStr(ff.getDescription());
								if (!"".equals(chkGroup)) {
									if (!checkboxGroupMap.containsKey(chkGroup)) {
										checkboxGroupMap.put(chkGroup, "");
									}
									else {
										continue;
									}
								}								
							}
							else {
								title = ff.getTitle();
							}
						}
						// 用于给convertToHTMLCtlForQuery辅助传值
						ff.setCondType(condType);	
					}
					%>
                    <span class="condSpan">
        			<%=title%>
               		<%
					if ("cws_status".equals(fieldName)) {
                        String nameCond = ParamUtil.get(request, fieldName + "_cond");
						if ("".equals(nameCond)) {
							nameCond = condType;
						}
						int queryValueCwsStatus = ParamUtil.getInt(request, "cws_status", -20000);						
						%>
				          <select name="<%=fieldName%>_cond" style="display:none">
				            <option value="=" selected="selected">等于</option>
				          </select>						
                          <select name='<%=fieldName%>'>
                          <option value='<%=SQLBuilder.CWS_STATUS_NOT_LIMITED%>'>不限</option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DRAFT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DRAFT)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_NOT%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_NOT)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DONE%>' selected><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DONE)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_REFUSED%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_REFUSED)%></option>
                          <option value='<%=com.redmoon.oa.flow.FormDAO.STATUS_DISCARD%>'><%=com.redmoon.oa.flow.FormDAO.getStatusDesc(com.redmoon.oa.flow.FormDAO.STATUS_DISCARD)%></option>
                          </select>                          
						<script>
						$(function() {
							o("<%=fieldName%>_cond").value = "<%=nameCond%>";
							<%if (queryValueCwsStatus!=-20000) {%>
							o("<%=fieldName%>").value = "<%=queryValueCwsStatus%>";
							<%}	else {%>
							o("<%=fieldName%>").value = "<%=msd.getInt("cws_status")%>";
							<%}%>
						});
						</script>							
						<%
					}
					else if ("cws_flag".equals(fieldName)) {
                        String nameCond = ParamUtil.get(request, fieldName + "_cond");
						if ("".equals(nameCond)) {
							nameCond = condType;
						}
						int queryValueCwsFlag = ParamUtil.getInt(request, "cws_flag", -1);						
						%>
				          <select name="<%=fieldName%>_cond" style="display:none">
				            <option value="=" selected="selected">等于</option>
				          </select>						
                          <select name='<%=fieldName%>'>
                          <option value='-1'>不限</option>
                          <option value='0'>否</option>
                          <option value='1'>是</option>
                          </select>
						<script>
						$(function() {
							o("<%=fieldName%>_cond").value = "<%=nameCond%>";
							o("<%=fieldName%>").value = "<%=queryValueCwsFlag%>";
						});
						</script>							
						<%					
					}
					else if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
               			%>
						<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
               			<%
						String idPrefix = fieldName.replaceAll(":", "_");
						if (condType.equals("0")) {
							String fDate = ParamUtil.get(request, fieldName + "FromDate");
							String tDate  = ParamUtil.get(request, fieldName + "ToDate");
							list.add(idPrefix + "FromDate");
							list.add(idPrefix + "ToDate");
							%>
                              	大于
                              <input id="<%=idPrefix%>FromDate" name="<%=fieldName%>FromDate" size="15" style="width:80px" value = "<%=fDate%>" />
                             	 小于
                              <input id="<%=idPrefix%>ToDate" name="<%=fieldName%>ToDate" size="15" style="width:80px" value = "<%=tDate%>" />
	  						<%
						}
						else {
							list.add(idPrefix);
							%>
                            <input id="<%=idPrefix%>" name="<%=fieldName%>" size="15" value = "<%=queryValue%>" />
							<%
						}
					} else if(ff.getType().equals(FormField.TYPE_MACRO)) {
						MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
						if (mu!=null) {
							String queryValueRealShow = ParamUtil.get(request, fieldName + "_realshow");							
							// 用main及other映射字段的描述替换其name，以使得生成的查询控件的id及name中带有main及other
							FormField ffQuery = (FormField)ff.clone();
							ffQuery.setName(fieldName);
							IFormMacroCtl ifmc = mu.getIFormMacroCtl();
							out.print(ifmc.convertToHTMLCtlForQuery(request, ffQuery));
							%>
							<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
							<a id="arrow<%=j %>" href="javascript:;"><i class="fa fa-caret-down"></i></a>		                    							
							<script>
							$(document).ready(function() {
								o("<%=fieldName%>").value = "<%=queryValue%>";
								try {
	                          	  o("<%=fieldName%>_realshow").value = "<%=queryValueRealShow%>";
	                            } catch (e) {}
	                            
	                            <%
	                            if ("text".equals(ifmc.getControlType()) || "img".equals(ifmc.getControlType()) || "textarea".equals(ifmc.getControlType())) {
	                            %>
								// 使=空或者<>空，获得焦点时即为选中状态，以便于修改条件的值
								$("input[name='<%=fieldName%>']").focus(function() {
									if ($(this).val()=='<%=SQLBuilder.IS_EMPTY%>' || $(this).val()=='<%=SQLBuilder.IS_NOT_EMPTY%>') {
										this.select();
									}
								});							
								
								var menu = new BootstrapMenu('#arrow<%=j%>', { 
								  menuEvent: 'click',
								  actions: [{
									  name: '等于空',
									  onClick: function() {
										$('#field<%=j%>').val('<%=SQLBuilder.IS_EMPTY%>');
									  }
									}, {
									  name: '不等于空',
									  onClick: function() {
										$('#field<%=j%>').val('<%=SQLBuilder.IS_NOT_EMPTY%>');
									  }
								  }]
								});		                            
	                            <%}%>
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
				            <option value="=" selected="selected">=</option>
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
						boolean isSpecial = false;
						if (condType.equals(SQLBuilder.COND_TYPE_NORMAL)) {
							if (ff.getType().equals(FormField.TYPE_SELECT)) {
								isSpecial = true;
								%>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
								<select id="<%=fieldName %>" name="<%=fieldName %>">
								<%=FormParser.getOptionsOfSelect(fd, ff) %>
								</select>
								<script>
								$(document).ready(function() {
									o("<%=fieldName%>").value = "<%=queryValue%>";
								});
								</script>							
								<%
							}
							else if (ff.getType().equals(FormField.TYPE_RADIO)) {
								isSpecial = true;
								%>
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />								
								<%
								String[][] aryRadio = FormParser.getOptionsArrayOfRadio(fd, ff);
								for (int k=0; k<aryRadio.length; k++) {
									String val = aryRadio[k][0];
									String text = aryRadio[k][1];
								%>
									<input type="radio" id="<%=fieldName %>" name="<%=fieldName %>" value="<%=val %>"/><%=text %>
								<%									
								}
							}
							else if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
								isSpecial = true;
								%>
								<%
								String[][] aryChk = null;
								if (isSub) {
									aryChk = FormParser.getOptionsArrayOfCheckbox(subFormDb, ff);
								}
								else {
									aryChk = FormParser.getOptionsArrayOfCheckbox(fd, ff);
								}
								for (int k=0; k<aryChk.length; k++) {
									String val = aryChk[k][0];
									String fName = aryChk[k][1];
									if (isSub) {
										fName = "sub:" + subFormDb.getCode() + ":" + fName;
									}
									String text = aryChk[k][2];
									queryValue = ParamUtil.get(request, fName);
								%>
                                    <input name="<%=fName%>_cond" value="<%=condType%>" type="hidden" />								
									<input type="checkbox" id="<%=fName %>" name="<%=fName %>" value="<%=val %>" style="<%=aryChk.length>1?"width:20px":""%>"/>
									<script>
									$(function() {
										o('<%=fName%>').checked = <%=queryValue.equals(val)?"true":"false"%>;
									})
									</script>
									<%if (aryChk.length>1) { %>
									<%=text %>
									<%} %>
								<%									
								}
							}
						}
						if (!isSpecial) {
						%>
							<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />
		                    <input id="field<%=j%>" name="<%=fieldName%>" size="5" />
							<a id="arrow<%=j %>" href="javascript:;"><i class="fa fa-caret-down"></i></a>		                    
							<script>
							$(document).ready(function() {
								o("<%=fieldName%>").value = "<%=queryValue%>";
								
								// 使=空或者<>空，获得焦点时即为选中状态，以便于修改条件的值
								$("#field<%=j%>").focus(function() {
									if ($(this).val()=='<%=SQLBuilder.IS_EMPTY%>' || $(this).val()=='<%=SQLBuilder.IS_NOT_EMPTY%>') {
										this.select();
									}
								});							
								
								var menu = new BootstrapMenu('#arrow<%=j%>', { 
								  menuEvent: 'click',
								  actions: [{
									  name: '等于空',
									  onClick: function() {
										$('#field<%=j%>').val('<%=SQLBuilder.IS_EMPTY%>');
									  }
									}, {
									  name: '不等于空',
									  onClick: function() {
										$('#field<%=j%>').val('<%=SQLBuilder.IS_NOT_EMPTY%>');
									  }
								  }]
								});								
							});
							</script>
						<%
						}
					}
					%>
					</span>
					<%
				}
			}
		}
	}
}
%>
        <input type="hidden" name="op" value="search" />
		<input type="hidden" name="moduleCode" value="<%=moduleCode%>" />
        <input type="hidden" name="code" value="<%=code%>" />
        <%=requestParamInputs%>
        <input type="hidden" name="menuItem" value="<%=menuItem%>" />
        <input type="hidden" name="mainCode" value="<%=ParamUtil.get(request, "mainCode")%>" />
        <input style="<%=strSearchTableDis %>" class="tSearch condBtnSearch" name="submit" type="button" onclick="doQuery()" value=" 搜索 " />

        </form>
    </td>
  </tr>
</table>
<table id="grid" style="display:none"></table>
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
}

var flex;

function changeSort(sortname, sortorder) {
	if (!sortorder)
		sortorder = "desc";
		
	// console.log(sortname + "--" + sortorder);
	var urlStr = "<%=request.getContextPath()%>/visual/moduleList.do?op=<%=op%>&moduleCode=<%=moduleCode%>&code=<%=code%>&unitCode=<%=unitCode%>&formCode=<%=formCode%>&pageSize=" + $("#grid").getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&<%=sqlUrlStr%>";
	$("#grid").flexOptions({url : urlStr});
	$("#grid").flexReload();
}

function onReload() {
	doQuery();
	// window.location.reload();
}

<%
String colProps = "";

String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
boolean isPrompt = false;
if (!promptField.equals("") && !promptIcon.equals("")) {
	isPrompt = true;
}
if (isPrompt) {
	colProps = "{display:'', name:'colPrompt', width:20}";
}

len = fields.length;
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	String title = "";
	boolean sortable = true;
	if (fieldName.startsWith("main:")) {
		String[] subFields = StrUtil.split(fieldName, ":");
		if (subFields.length == 3) {
			FormDb subfd = new FormDb(subFields[1]);
			title = subfd.getFieldTitle(subFields[2]);
			sortable = false;
		}
	} else if (fieldName.startsWith("other:")) {
		String[] otherFields = StrUtil.split(fieldName, ":");
		if (otherFields.length == 5) {
			FormDb otherFormDb = new FormDb(otherFields[2]);
			title = otherFormDb.getFieldTitle(otherFields[4]);
			sortable = false;
		}
	} else if (fieldName.equals("cws_creator")) {
		title = "创建者";
	}
	else if (fieldName.equals("ID")) {
		fieldName = "CWS_MID"; // ModuleController中也作了同样转换
		title = "ID";
	}
	else if (fieldName.equals("cws_progress")) {
		title = "进度";
	}
	else if (fieldName.equals("cws_status")) {
		title = "状态";
	}	
	else if (fieldName.equals("cws_flag")) {
		title = "冲抵状态";
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

	if (colProps.equals(""))
		colProps = "{display: '" + title + "', name : '" + fieldName + "', width : " + wid + ", sortable : " + sortable + ", align: 'center', hide: false}";
	else
		colProps += ",{display: '" + title + "', name : '" + fieldName + "', width : " + wid + ", sortable : " + sortable + ", align: 'center', hide: false}";
}

if (colProps.equals("")) {
	colProps = "{display:'操作', name:'colOperate', width:150}";
}
else {
	colProps += ",{display:'操作', name:'colOperate', width:150}";
}
%>
var requestParams = [];
<%
Enumeration enu = request.getParameterNames();  
while(enu.hasMoreElements()){  
	String paramName = (String)enu.nextElement();  
	String paramVal = ParamUtil.get(request, paramName);
	%>
	requestParams.push({name:'<%=paramName%>', value:'<%=paramVal%>'});
	<%
}

boolean canUserManage = mpd.canUserManage(userName);
%>
var colModel = [<%=colProps%>];
$("#grid").flexigrid({
	url: 'moduleList.do?<%=querystr%>',
	params: requestParams,
	dataType: 'json',
	colModel : colModel,
<%if (isButtonsShow) {%>
	buttons : [
	<%if (msd.getInt("btn_add_show")==1 && mpd.canUserAppend(userName)) {%>
		{name: '添加', bclass: 'add', onpress : action},
	<%}%>
	<%if (msd.getInt("btn_edit_show")==1 && mpd.canUserModify(userName)) {%>
		{name: '修改', bclass: 'edit', onpress : action},	
    <%}%>
	<%
	if (mpd.canUserDel(userName) || canUserManage) {
		if (msd.getInt("btn_del_show")==1) {
		%>
		{name: '删除', bclass: 'delete', onpress : action},
		// {name: '全部导入', bclass: 'import1', onpress : action},
		// {name: '全部导出', bclass: 'export', onpress : action},
		<%
		}
	}
	if (canUserManage && isShowUnitCode) {%>
		{name: '迁移', bclass: 'pass', onpress : action},
	<%}%>
	<%if (mpd.canUserImport(userName)) {%>
		{name: '导入', bclass: 'import1', onpress : action},
	<%}%>    
	<%if (mpd.canUserExport(userName)) {%>
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
			// 检查是否拥有权限
			if (!privilege.isUserPrivValid(request, "admin")) {
				boolean canSeeBtn = false;
				if (btnRoles!=null && btnRoles.length>0) {
					String roles = btnRoles[i];
					String[] codeAry = StrUtil.split(roles, ",");
					if (codeAry!=null) {
						UserDb user = new UserDb();
						user = user.getUserDb(privilege.getUser(request));
						RoleDb[] rdAry = user.getRoles();
						if (rdAry!=null) {
							for (RoleDb rd : rdAry) {
								String roleCode = rd.getCode();
								for (String codeAllowed : codeAry) {
									if (roleCode.equals(codeAllowed)) {
										canSeeBtn = true;
										break;
									}
								}
								if (canSeeBtn) {
								    break;
								}
							}
						}
					}
					else {
						canSeeBtn = true;
					}
				}
				else {
					canSeeBtn = true;
				}
				
				if (!canSeeBtn) {
					continue;
				}
			}
						
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
		<%if (privilege.isUserPrivValid(request, "admin")) {%>
		,{name: '管理', bclass: 'design', onpress : action}
		<%}%>
		<%if (isToolbar) {%>
		,{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
		<%}%>
		],
<%}%>		
	sortname: "<%=orderBy%>",
	sortorder: "<%=sort%>",
	usepager: true,
	checkbox : true,
	useRp: true,
	rp: <%=pagesize%>,
	
	// title: "通知",
	singleSelect: true,
	resizable: false,
	showTableToggleBtn: true,
	showToggleBtn: true,
	
	onChangeSort: changeSort,
	
	// onChangePage: changePage,
	// onRpChange: rpChange,
    onLoad: onLoad,
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
});

function onLoad() {
	try {
	    onFlexiGridLoaded();
	}
	catch(e) {}
}

<%
String queryString = StrUtil.getNullString(request.getQueryString());
String privurl=request.getRequestURL()+"?"+StrUtil.UrlEncode(queryString, "utf-8");
%>

function action(com, grid) {
	if (com=="导出") {
		var cols = "";
		for (i=0; i<colModel.length; i++) {
			if (colModel[i].name=="colOperate")
				continue;
			
			if (cols=="") {
				cols = colModel[i].name;
			}
			else {
				cols += "," + colModel[i].name;
			}
		}

		
		<%
		// 检查是否设置有模板
		Vector v = ModuleExportTemplateMgr.getTempaltes(request, formCode);
		/*
		ModuleExportTemplateDb mid = new ModuleExportTemplateDb();
		String sql = mid.getTable().getSql("listForForm");
		Vector v = mid.list(sql, new Object[]{formCode});
		*/
		if (v.size()>0) {
			%>
			openWin('<%=request.getContextPath()%>/visual/module_excel_sel_templ.jsp?' + $("form").serialize() + '&cols=' + cols, 480, 160);
			<%
		}
		else {
		%>
			window.open('<%=request.getContextPath()%>/visual/module_excel.jsp?' + $("form").serialize() + '&cols=' + cols);
		<%}%>
	}
	else if (com=="全部导出") {
		window.open('<%=request.getContextPath()%>/visual/module_excel.jsp?isAll=true&' + $("form").serialize());
	}	
	else if (com=="全部导入") {
		var url = "<%=request.getContextPath()%>/visual/module_import_excel.jsp?isAll=true&formCode=<%=formCode%>";
		openWin(url,360,50);		
	}	
	else if (com=="添加") {
		window.location.href = "<%=request.getContextPath()%>/visual/module_add.jsp?code=<%=code%>&privurl=<%=privurl%><%=requestParams%>";
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
			id = $(this).val().substring(3);
		});		
		
		<%if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {%>
    	addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/<%=msd.getString("url_edit")%>?parentId=" + id + "&id=" + id + "&code=<%=code%>&formCode=<%=formCode%>");
		<%}else{%>
    	addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_edit.jsp?parentId=" + id + "&id=" + id + "&code=<%=code%>&formCode=<%=formCode%>");
		<%}%>
	}
	else if (com=='删除') {
		var ids = "";
		// value!='on' 过滤掉复选框按钮
		$(".cth input[type='checkbox'][value!='on'][checked='checked']", grid.bDiv).each(function(i) {
			if (ids=="")
				ids = $(this).val().substring(3);
			else
				ids += "," + $(this).val().substring(3);
		});
		del(ids);
	} else if(com=='导入'){
		window.location.href = "<%=request.getContextPath()%>/visual/module_import_excel.jsp?formCode=<%=formCode%>&code=<%=code%>";
	}
	else if (com=="管理") {
		addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=formCode%>&code=<%=code%>");
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
							ids = $(this).val().substring(3);
						else
							ids += "," + $(this).val().substring(3);
					});		
					
					$.ajax({
						type: "post",
						url: "moduleSetUnitCode.do",
						data : {
							code: "<%=code%>",
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
								
								batchOp(ids, "<%=batchField%>", "<%=batchValue%>");
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
		var id = $(this).val().substring(3); // 去掉前面的row		
		if (ids=="")
			ids = id;
		else
			ids += "," + id;
	});
	return ids;
}

$(function() {
	initCalendar();
	
	<%
	// 如果显示单位下拉框
	if (isShowUnitCode) {
		// 如果条件中没有unitCode
		if ("".equals(unitCode)) {
			if (msd.getString("unit_code").equals("-1")) {
			}
			else {
				// 本单位
				%>
				$("#unitCode").val("<%=myUnitCode%>");
				<%
			}
		%>
		<%}else{%>
		$("#unitCode").val("<%=unitCode%>");
		<%}
	}%>
});

function getPageName() {
	var strUrl=window.location.href;
	var arrUrl=strUrl.split("/");
	var strPage=arrUrl[arrUrl.length-1];
	return strPage;
}

function del(ids) {
	if (ids=="") {
		jAlert('请选择记录!','提示');
		return;
	}
	
	jConfirm('您确定要删除么？', '提示', function(r) {	
		if (!r) {
			return;
		}

		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/visual/moduleDel.do",
			data: {
				code: "<%=code%>",
				id: ids
			},
			dataType: "html",
			beforeSend: function(XMLHttpRequest){
				$("body").eq(0).showLoading();
			},
			success: function(data, status){
				data = $.parseJSON(data);
				jAlert(data.msg, "提示");
				if (data.ret=="1") {
					/*
					var ary = ids.split(",");
					for (var i=0; i<ary.length; i++) {
						$('#row' + ary[i]).remove();
					}
					*/
					doQuery();
					// 置全选checkbox为非选中状态
					$(".hDiv input[type='checkbox']").removeAttr("checked");					
				}
			},
			complete: function(XMLHttpRequest, status){
				$("body").eq(0).hideLoading();				
			},
			error: function(XMLHttpRequest, textStatus){
				// 请求出错处理
				alert(XMLHttpRequest.responseText);
			}
		});		
	});
}

function batchOp(ids, batchField, batchValue) {
	$.ajax({
		type: "post",
		url: "<%=request.getContextPath()%>/visual/moduleBatchOp.do",
		data: {
			code: "<%=code%>",
			id: ids,
			batchField: batchField,
			batchValue: batchValue
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$("body").eq(0).showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			jAlert(data.msg, "提示");
			if (data.ret=="1") {
				var ary = ids.split(",");
				for (var i=0; i<ary.length; i++) {
					$('#row' + ary[i]).remove();
				}
			}
		},
		complete: function(XMLHttpRequest, status){
			$("body").eq(0).hideLoading();				
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});	
}

function doQuery() {
	var params = $("form").serialize();
	var urlStr = "<%=request.getContextPath()%>/visual/moduleList.do?" + params;
	$("#grid").flexOptions({url : urlStr});
	$("#grid").flexReload();
}

function searchFormOnSubmit() {
	doQuery();
	return false;
}
</script>
</html>
