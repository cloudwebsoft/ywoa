<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.base.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.pvg.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormMgr"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.worklog.WorkLogForModuleMgr"%>
<%@ page import = "com.redmoon.oa.util.RequestUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");
String moduleCodeRelated = ParamUtil.get(request, "moduleCodeRelated");
String menuItem = ParamUtil.get(request, "menuItem");
String moduleCode = ParamUtil.get(request, "code");

ModuleSetupDb parentMsd = new ModuleSetupDb();
parentMsd = parentMsd.getModuleSetupDbOrInit(moduleCode);
formCode = parentMsd.getString("form_code");

String mode = ParamUtil.get(request, "mode");
String tagName = ParamUtil.get(request, "tagName");
int parentId = ParamUtil.getInt(request, "parentId", -1);

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
String formCodeRelated = msd.getString("form_code");

// 通过选项卡标签关联
boolean isSubTagRelated = "subTagRelated".equals(mode);

if (isSubTagRelated) {
   	String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, tagName);
	try {
		JSONObject json = new JSONObject(tagUrl);
		if (json.has("viewList")) {
			int viewList = StrUtil.toInt(json.getString("viewList"), ModuleSetupDb.VIEW_DEFAULT);
			if (viewList==ModuleSetupDb.VIEW_LIST_GANTT) {
				response.sendRedirect("module_list_relate_gantt.jsp?mode=subTagRelated&tagName=" + StrUtil.UrlEncode(tagName) + "&parentId=" + parentId + "&code=" + formCode + "&menuItem=" + menuItem);
				return;
			}
		}
		if (!json.isNull("formRelated")) {
			// formCodeRelated = json.getString("formRelated");
			moduleCodeRelated = json.getString("formRelated");
			msd = msd.getModuleSetupDb(moduleCodeRelated);
			formCodeRelated = msd.getString("form_code");		
		}
		else {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "选项卡关联配置不正确！"));
			return;
		}
	} catch (JSONException e) {
		e.printStackTrace();
	}
}

// 置嵌套表及关联查询选项卡生成链接需要用到的cwsId
request.setAttribute("cwsId", "" + parentId);
// 用于传过滤条件
request.setAttribute(ModuleUtil.MODULE_SETUP, msd);

FormDb fd = new FormDb();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=msd.getString("name")%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css" />  
<link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet">
    <style>
        input {
            line-height: normal; /*防止查询条件置于工具条时，input受其它样式影响紧贴于上方*/
        }
        .search-form input:not([type="radio"],[type="button"]) {
            width: 80px;
        }
    </style>
<script src="../inc/common.js"></script>
<script src="../js/jquery1.7.2.min.js"></script>
<script src="../js/flexigrid.js"></script>
<script src="../js/jquery.raty.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCodeRelated%>.jsp?parentId=<%=parentId%>&formCode=<%=formCode%>&formCodeRelated=<%=formCodeRelated%>"></script>

<script src="../js/jquery-ui/jquery-ui.js"></script>
<script src="../js/jquery.bgiframe.js"></script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />

<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" /> 
<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script src="../js/BootstrapMenu.min.js"></script>
<%
fd = fd.getFormDb(formCodeRelated);
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
	return;
}

// 置页面类型
// request.setAttribute("pageType", "list");

String relateFieldValue = "";
if (parentId==-1) {
	out.print(SkinUtil.makeErrMsg(request, "缺少父模块记录的ID！"));
	return;
}
else {
	if (!isSubTagRelated) {
		com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(formCode);
		relateFieldValue = fdm.getRelateFieldValue(parentId, msd.getString("code"));
		if (relateFieldValue==null) {
			// 20171016 fgf 如果取得的为null，则说明可能未设置两个模块相关联，但是为了能够使简单选项卡能链接至关联模块，此处应允许不关联
			relateFieldValue = SQLBuilder.IS_NOT_RELATED;
			// out.print(StrUtil.jAlert_Back("请检查模块是否相关联！","提示"));
			// return;
		}
	}
}

String op = ParamUtil.get(request, "op");
ModulePrivDb mpd = new ModulePrivDb(moduleCodeRelated);
if (!mpd.canUserSee(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

ModuleRelateDb mrd = new ModuleRelateDb();
Iterator ir = mrd.getModulesRelated(moduleCode).iterator();
while (ir.hasNext()) {
	mrd = (ModuleRelateDb)ir.next();
	String code = mrd.getString("relate_code");
	if (code.equals(formCodeRelated)) {
		if (mrd.getInt("relate_type") == ModuleRelateDb.TYPE_SINGLE) {
			// 获取与formCode关联的表单型（单条记录）的ID
			com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
			fdao = fdao.getFormDAOOfRelate(fd, relateFieldValue);
			if (fdao!=null) {
				long id = fdao.getId();			
				response.sendRedirect("module_show_relate.jsp?id=" + id + "&parentId=" + parentId + "&formCodeRelated=" + formCodeRelated + "&formCode=" + formCode);
				return;
			}
		}
	}
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

//int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

String[] arySQL = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);
String sql = arySQL[0];
String sqlUrlStr = arySQL[1];
// System.out.println(getClass() + " " + sql);

querystr = "op=" + op + "&mode=" + mode + "&tagName=" + StrUtil.UrlEncode(tagName) + "&code=" + moduleCode + "&menuItem=" + menuItem + "&formCode=" + formCode + "&moduleCodeRelated=" + moduleCodeRelated + "&formCodeRelated=" + moduleCodeRelated + "&parentId=" + parentId + "&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=" + isShowNav;
if (!sqlUrlStr.equals("")) {
    if (!sqlUrlStr.startsWith("&")) {
		querystr += "&" + sqlUrlStr;
	}
	else {
	    querystr += sqlUrlStr;
	}
}
// System.out.println("querystr = " + querystr);

String action = ParamUtil.get(request, "action");
if (action.equals("del")) {
	FormMgr fm = new FormMgr();
	FormDb fdRelated = fm.getFormDb(formCodeRelated);
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fdRelated);
	try {
		if (fdm.del(request)) {
			String privurl = ParamUtil.get(request, "privurl");
			if (!privurl.equals(""))
				out.print(StrUtil.jAlert_Redirect("删除成功！","提示", privurl));
			else
				out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "module_list_relate.jsp?" + querystr + "&CPages=" + curpage + "&isShowNav=" + isShowNav));
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		return;
	}
}
else if (action.equals("batchOp")) {
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
%>
<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "module_list_relate.jsp?menuItem=<%=menuItem%>&code=<%=moduleCode%>&parentId=<%=parentId%>&formCode=<%=formCode%>&menuItem=<%=menuItem%>&formCodeRelated=<%=formCodeRelated%>&moduleCodeRelated=<%=moduleCodeRelated%>&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=<%=isShowNav%>";
}
</script>
</head>
<body>
<%
if (isShowNav==1) {
%>
<%@ include file="module_inc_menu_top.jsp"%>
<script>
o("menu<%=menuItem%>").className="current"; 
</script>
<%}%>
<%	
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
ListResult lr = fdao.listResult(formCodeRelated, sql, curpage, pagesize);
int total = lr.getTotal();
Vector v = lr.getResult();
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

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

String privurl=request.getRequestURL()+"?"+StrUtil.UrlEncode(querystr, "utf-8");

int is_workLog = msd.getInt("is_workLog");
		
String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = StrUtil.split(listField, ",");
String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = StrUtil.split(listFieldWidth, ",");
String listFieldOrder = StrUtil.getNullStr(msd.getString("list_field_order"));
String[] fieldsOrder = StrUtil.split(listFieldOrder, ",");

String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, "#");
String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btnBclass, ",");	
String btnRole = StrUtil.getNullStr(msd.getString("btn_role"));
String[] btnRoles = StrUtil.split(btnRole, "#");

boolean isButtonsShow = false;
isButtonsShow = mpd.canUserAppend(privilege.getUser(request)) ||
				mpd.canUserModify(privilege.getUser(request)) ||
				mpd.canUserManage(privilege.getUser(request)) ||
				mpd.canUserImport(privilege.getUser(request)) ||
				mpd.canUserExport(privilege.getUser(request)) ||
				btnNames!=null;
String strSearchTableDis = "";
if (!isButtonsShow) {
	strSearchTableDis = "display:none";
}

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
<table id="searchTable" class="percent98" style="<%=strSearchTableDis%>" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="80%" height="28" align="left" style="padding-top:5px">
    <form id="searchForm" class="search-form" action="module_list_relate.jsp">
	<%
	ArrayList<String> list = new ArrayList<String>();
	MacroCtlMgr mm = new MacroCtlMgr();

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
							 if (aryField.length==3) {
							  	FormDb mainFormDb = fm.getFormDb(aryField[1]);
							  	ff = mainFormDb.getFormField(aryField[2]);
								if (ff==null) {
									out.print(fieldName + "不存在");
									continue;
								}							  	
							  	// title = mainFormDb.getName() + "：" + ff.getTitle();
							  	title = ff.getTitle();
							 }
							 else {
							 	out.print(fieldName + " 不存在");
							 	continue;							  	
							 }
						}
						else if (fieldName.startsWith("sub:")) { // 关联的子表
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
							 	out.print(fieldName + "格式非法");
							 	continue;
							 }
							 else {
								FormDb otherFormDb = fm.getFormDb(aryField[2]);
								ff = otherFormDb.getFormField(aryField[4]);
								if (ff==null) {
									out.print(fieldName + "不存在");
									continue;
								}								
								// title = otherFormDb.getName() + "：" + ff.getTitle();
								title = ff.getTitle();
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
						if (condType.equals("0")) {
							String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
							String tDate  = ParamUtil.get(request, ff.getName() + "ToDate");
							list.add(ff.getName() + "FromDate");
							list.add(ff.getName() + "ToDate");
							%>
                              大于
                              <input id="<%=ff.getName()%>FromDate" name="<%=ff.getName()%>FromDate" size="15" style="width:80px" value = "<%=fDate%>" />
                              <!-- <img style="CURSOR: hand" onClick="SelectDate('<%=ff.getName()%>FromDate', 'yyyy-MM-dd')" src="<%=request.getContextPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
                              小于
                              <input id="<%=ff.getName()%>ToDate" name="<%=ff.getName()%>ToDate" size="15" style="width:80px" value = "<%=tDate%>" />
                              <!-- <img style="CURSOR: hand" onClick="SelectDate('<%=ff.getName()%>ToDate', 'yyyy-MM-dd')" src="<%=request.getContextPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
							<!-- <script>
							$(document).ready(function() {
							o("<%=ff.getName()%>FromDate").value = "<%=fDate%>";
							o("<%=ff.getName()%>ToDate").value = "<%=tDate%>";
							});
							</script> -->
	  <%
						}
						else {
							list.add(ff.getName());
							%>
                              <input id="<%=ff.getName()%>" name="<%=ff.getName()%>" size="15" value = "<%=queryValue%>" />
                              <!-- <img style="CURSOR: hand" onClick="SelectDate('<%=ff.getName()%>', 'yyyy-MM-dd')" src="<%=request.getContextPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
							  <!-- <script>
							  $(document).ready(function() {							  
                              o("<%=fieldName%>").value = "<%=queryValue%>";
							  });
                              </script> -->						
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
							<%
								if ("text".equals(ifmc.getControlType()) || "img".equals(ifmc.getControlType()) || "textarea".equals(ifmc.getControlType())) {
							%>
							<a id="arrow<%=j %>" href="javascript:;"><i class="fa fa-caret-down"></i></a>
							<%
								}
							%>
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
										$("input[name='<%=fieldName%>']").val('<%=SQLBuilder.IS_EMPTY%>');
									  }
									}, {
									  name: '不等于空',
									  onClick: function() {
										$("input[name='<%=fieldName%>']").val('<%=SQLBuilder.IS_NOT_EMPTY%>');
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
								<input name="<%=fieldName%>_cond" value="<%=condType%>" type="hidden" />								
								<%
								String[][] aryChk = FormParser.getOptionsArrayOfCheckbox(fd, ff);
								for (int k=0; k<aryChk.length; k++) {
									String val = aryChk[k][0];
									String fName = aryChk[k][1];
									String text = aryChk[k][2];
									queryValue = ParamUtil.get(request, fName);
								%>
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
		if (isQuery) {
		%>
		    <input type="hidden" name="code" value="<%=moduleCode%>" />
		    <input type="hidden" name="formCodeRelated" value="<%=moduleCodeRelated%>" />
		    <input type="hidden" name="formCode" value="<%=formCode%>" />
		    <input type="hidden" name="parentId" value="<%=parentId%>" />
	        <input type="hidden" name="op" value="search" />
	        <input type="hidden" name="menuItem" value="<%=menuItem%>" />
			<input type="hidden" name="moduleCodeRelated" value="<%=moduleCodeRelated%>"/>
	        <input type="hidden" name="mode" value="<%=mode%>" />
	        <input type="hidden" name="tagName" value="<%=tagName%>" />
	        <%=requestParamInputs%>
	        <input class="tSearch" name="submit" type="submit" value=" 搜索 " />
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
com.redmoon.oa.flow.FormMgr fm = new com.redmoon.oa.flow.FormMgr();
len = 0;
if (fields!=null)
	len = fields.length;
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	if (fieldName.equals("colOperate")) {
		continue;
	}
	String title = "";
	String doSort = "doSort('" + fieldName + "')";
	if (isSubTagRelated) {
		doSort = "";
	}
	
	boolean isFieldExist = true;
	if (fieldName.equals("cws_creator")) {
		title = "创建者";
	}
	else if (fieldName.equals("cws_progress")) {
		title = "进度";
	}
	else if (fieldName.equals("ID")) {
		title = "ID";
	}
	else if (fieldName.equals("flowId")) {
		title = "流程ID";
	}
	else if (fieldName.equals("cws_status")) {
		title = "状态";
	}	
	else if (fieldName.equals("cws_flag")) {
		title = "冲抵状态";
	}	
	else {
		if (fieldName.startsWith("main")) {
			String[] ary = StrUtil.split(fieldName, ":");
			FormDb mainFormDb = fm.getFormDb(ary[1]);
			title = mainFormDb.getFieldTitle(ary[2]);
			doSort = "";	
		}
		else if (fieldName.startsWith("other")) {
			String[] ary = StrUtil.split(fieldName, ":");
			FormDb otherFormDb = fm.getFormDb(ary[2]);
			title = otherFormDb.getFieldTitle(ary[4]);
		}
		else {
			title = fd.getFieldTitle(fieldName);
		}
		if ("".equals(title)) {
			title = fieldName + "不存在";
			isFieldExist = false;
			doSort = "";
		}
	}
	
%>
    <th width="<%=fieldsWidth[i]%>" <%if (!fieldName.startsWith("main:") && !fieldName.startsWith("other:")) {%> abbr="<%=fieldName%>"<%}%>>
	<%=title%>
	<%if (isFieldExist && !isSubTagRelated && orderBy.equals(fieldName)) {
		if (sort.equals("asc")) 
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		else
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
	}%>	
	</th>
<%}%>
    <th width="170">操作</th>
  </tr>
  </thead>  
  <tbody>  
  <%
  com.redmoon.oa.sso.Config ssoCfg = new com.redmoon.oa.sso.Config();
  String desKey = ssoCfg.get("key");

  String userName = privilege.getUser(request);
  boolean canView = mpd.canUserView(userName);
  boolean canLog = mpd.canUserLog(userName);
  boolean canManage = mpd.canUserManage(userName);
  boolean canModify = mpd.canUserModify(userName);
  boolean canDel = mpd.canUserDel(userName);
  
  int k = 0;
  UserMgr um = new UserMgr();
  while (ir!=null && ir.hasNext()) {
	  fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
	  RequestUtil.setFormDAO(request, fdao);
	  k++;
	  long id = fdao.getId();
  %>
  <tr align="center" id="<%=id%>">
<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		if (fieldName.equals("colOperate")) {
			continue;
		}
	%>	
		<td align="left">
		<%
		if (fieldName.equals("cws_creator")) {%>
			<%=StrUtil.getNullStr(um.getUserDb(fdao.getCreator()).getRealName())%>			
		<%
		} else if (fieldName.equals("cws_progress")) {
			out.print(fdao.getCwsProgress());
		} 
		else if (fieldName.equals("ID")) {
			out.print(fdao.getId());
		}
		else if (fieldName.equals("flowId")) {
			out.print("<a href=\"javascript:;\" onclick=\"addTab('流程', '" + request.getContextPath() + "/flow_modify.jsp?flowId=" + fdao.getFlowId() + "')\">" + fdao.getFlowId() + "</a>");
		}
		else if (fieldName.equals("cws_status")) {
			out.print(com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus()));
		}				
		else if (fieldName.equals("cws_flag")) {
			out.print(fdao.getCwsFlag());
		}
		else {
			if (i==0 && canView) {
			%>
			<a href="javascript:;" onclick="addTab('<%=fd.getName() %>', '<%=request.getContextPath() %>/visual/module_show.jsp?code=<%=moduleCodeRelated%>&parentId=<%=id%>&id=<%=id%>')">			
			<%
			}
            if (fieldName.startsWith("main:")) {
				String[] ary = StrUtil.split(fieldName, ":");
				FormDb mainFormDb = fm.getFormDb(ary[1]);
				com.redmoon.oa.visual.FormDAOMgr fdmMain = new com.redmoon.oa.visual.FormDAOMgr(mainFormDb);
				com.redmoon.oa.visual.FormDAO fdaoMain = fdmMain.getFormDAO(parentId);
				FormField ff = mainFormDb.getFormField(ary[2]);
				if (ff != null && ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdaoMain.getFieldValue(ary[2])));
					}
				} else {
					out.print(fdmMain.getFieldValueOfMain(parentId, ary[2]));
				}
			}
			else if (fieldName.startsWith("other:")) {
				// String[] ary = StrUtil.split(fieldName, ":");
				
				// FormDb otherFormDb = fm.getFormDb(ary[2]);
				// com.redmoon.oa.visual.FormDAOMgr fdmOther = new com.redmoon.oa.visual.FormDAOMgr(otherFormDb);
				// out.print(fdmOther.getFieldValueOfOther(fdao.getFieldValue(ary[1]), ary[3], ary[4]));
				
				out.print(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName));
			}
			else{
				FormField ff = fd.getFormField(fieldName);
				// ff!=null 防止列被删除
				if (ff!=null && ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						IFormMacroCtl imc = mu.getIFormMacroCtl();
						if (imc!=null) {
							out.print(imc.converToHtml(request, ff, fdao.getFieldValue(fieldName)));
						}
						else {
							out.print("宏控件：" + ff.getMacroType() + " 不存在");
						}
					}
				}
				else {%>
					<%=StrUtil.getNullStr(fdao.getFieldValue(fieldName))%>
				<%}
			}%>
		<%}%>
        <%if (i==0 && canView) {%>
        </a>
        <%}%>
		</td>
	<%}%>
	<td>
		<%if (msd.getInt("btn_display_show")==1 && canView) {%>
			<!--<a href="javascript:;" onclick="addTab('<%=fd.getName() %>', '<%=request.getContextPath() %>/visual/module_show_relate.jsp?code=<%=moduleCode%>&parentId=<%=parentId%>&id=<%=id%>&menuItem=<%=menuItem%>&formCodeRelated=<%=formCodeRelated%>&formCode=<%=formCode%>&isShowNav=1')">查看</a>-->
			<a href="javascript:;" onclick="addTab('<%=fd.getName() %>', '<%=request.getContextPath() %>/visual/module_show.jsp?code=<%=moduleCodeRelated%>&parentId=<%=id%>&id=<%=id%>')">查看</a>
			<%
			if (msd.getInt("btn_flow_show")==1 && fdao.getFlowId()!=-1) {
		        String visitKey = cn.js.fan.security.ThreeDesUtil.encrypt2hex(desKey, String.valueOf(fdao.getFlowId()));    		
				%>
		    	&nbsp;&nbsp;<a href="javascript:;" onclick="addTab('查看流程', '<%=request.getContextPath() %>/flow_modify.jsp?flowId=<%=fdao.getFlowId() %>&visitKey=<%=visitKey %>')">流程</a>
			<%
			}		
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
		String op_link_role = StrUtil.getNullStr(msd.getString("op_link_role"));
		// 为兼容以前的版本，初始化tRole
		if (!op_link_name.equals("") && op_link_role.equals("")) {
			if (linkNames!=null) {
				for (int m=0; m<linkNames.length-1; m++) {
					op_link_role += "#";
				}
			}				
		}				
		String[] linkRoles = StrUtil.split(op_link_role, "#");        
        if (linkNames!=null) {
            for (int i=0; i<linkNames.length; i++) {
                String linkName = linkNames[i];
                
                String linkField = linkFields[i];
                String linkCond = linkConds[i];
                String linkValue = linkValues[i];
                String linkEvent = linkEvents[i];
				String linkRole = linkRoles[i];
				
				// 检查是否拥有权限
				if (!privilege.isUserPrivValid(request, "admin")) {
					boolean canSeeLink = false;
					if (!linkRole.equals("")) {
						String[] codeAry = StrUtil.split(linkRole, ",");
						if (codeAry!=null) {
							UserDb user = new UserDb();
							user = user.getUserDb(privilege.getUser(request));
							RoleDb[] rdAry = user.getRoles();
							if (rdAry!=null) {
								for (RoleDb rd : rdAry) {
									String roleCode = rd.getCode();
									for (String codeAllowed : codeAry) {
										if (roleCode.equals(codeAllowed)) {
											canSeeLink = true;
											break;
										}
									}
								}
							}
						}
						else {
							canSeeLink = true;
						}
					}
					else {
						canSeeLink = true;
					}
					
					if (!canSeeLink) {
						continue;
					}
				}						
				                
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
                    &nbsp;&nbsp;<a href="javascript:;" onclick="<%=ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, moduleCodeRelated)%>"><%=linkName%></a>
                <%				
                    }
                    else {
                %>
                    &nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=linkName%>', '<%=request.getContextPath()%>/<%=ModuleUtil.renderLinkUrl(request, fdao, linkHrefs[i], linkName, moduleCodeRelated)%>')"><%=linkName%></a>
                <%
                    }
                }
            }
        }			

		if (msd.getInt("btn_log_show")==1) {
			if (canLog || canManage) {
				if (fd.isLog()) {
				%>
				&nbsp;&nbsp;<a href="javascript:;" onclick="addTab('修改日志', '<%=request.getContextPath()%>/visual/module_log_list.jsp?op=search&code=<%=formCodeRelated%>&fdaoId=<%=id%>')">日志</a>
				<%}
			}
		}
	    if(is_workLog==1){
		%> 
	    	&nbsp;&nbsp;<a href="javascript:;" onclick="addTab('<%=msd.getString("name")%>汇报', '<%=request.getContextPath()%>/ymoa/queryMyWork.action?code=prj_task&id=<%=id%>')">汇报</a>
	    <%
	    }
	    %>		
		</td>
  </tr>
  <%
  }
%>
  </tbody>
</table>
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
	try {
   		onFlexiGridLoaded();
    }
    catch (e) {}
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "<%=request.getContextPath()%>/visual/module_list_relate.jsp?op=<%=op%>&parentId=<%=parentId%>&menuItem=<%=menuItem%>&code=<%=moduleCode%>&formCode=<%=formCode%>&formCodeRelated=<%=formCodeRelated%>&moduleCodeRelated=<%=moduleCodeRelated%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&<%=sqlUrlStr%>";
}

function changePage(newp) {
	if (newp)
		window.location.href = "<%=request.getContextPath()%>/visual/module_list_relate.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	var curPage =  parseInt(<%=curpage%>);
	var total = parseInt(<%=total%>);
	var curTotal = curPage*parseInt(pageSize);
	if(curTotal>total){
		curPage = 1;
	}
	window.location.href = "<%=request.getContextPath()%>/visual/module_list_relate.jsp?<%=querystr%>&CPages="+curPage+"&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
<%
if (isButtonsShow) {%>		
	buttons : [
	<%if (msd.getInt("btn_add_show")==1 && mpd.canUserAppend(userName)) {%>
		{name: '添加', bclass: 'add', onpress : action},
	<%}%>
	<%if (msd.getInt("btn_edit_show")==1 && mpd.canUserModify(userName)) {%>
		{name: '修改', bclass: 'edit', onpress : action},	
    <%}%>
	<%if (msd.getInt("btn_edit_show")==1 && (mpd.canUserDel(userName) || canManage)) {%>
		{name: '删除', bclass: 'delete', onpress : action},
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
					// 如果codeAry为null，则表示所有人都能看到
					if (codeAry == null){
					    canSeeBtn = true;
					}
					else{
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
		<%
		// 检查是否设置有模板
		Vector vt = ModuleExportTemplateMgr.getTempaltes(request, msd.getString("form_code"));
		if (vt.size()>0) {
			%>
			openWin('<%=request.getContextPath()%>/visual/module_excel_sel_templ.jsp?isRelate=true&<%=querystr%>&cols=' + cols, 480, 160);
			<%
		}
		else {
			%>		
			window.open('module_excel_relate.jsp?<%=querystr%>&cols=' + cols);
			// window.open('module_excel_relate.jsp?' + $("form").serialize() + '&cols=' + cols);
			<%
		}%>
	}
	else if (com=="添加") {
		window.location.href = "module_add_relate.jsp?code=<%=StrUtil.UrlEncode(moduleCode)%>&parentId=<%=parentId%>&menuItem=<%=menuItem%>&formCode=<%=formCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&isShowNav=<%=isShowNav%>";
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
		
    	addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_edit_relate.jsp?code=<%=moduleCode%>&parentId=<%=parentId%>&id=" + id + "&menuItem=<%=menuItem%>&moduleCodeRelated=<%=moduleCodeRelated%>&formCode=<%=formCode%>");
	} else if (com == '导入'){
		// var url = "module_import_excel.jsp?formCode=<%=formCodeRelated%>&parentId=<%=parentId%>";
		// openWin(url,360,50);
		window.location.href = "module_import_excel.jsp?formCode=<%=formCodeRelated%>&code=<%=moduleCode%>&parentId=<%=parentId%>&menuItem=<%=menuItem%>";		
	}
	else if (com=="管理") {
		addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=msd.getString("form_code")%>&code=<%=msd.getString("code")%>");
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
				// alert(ids);
				window.location.href = "module_list_relate.jsp?action=del&<%=querystr%>&CPages=<%=curpage%>&id=" + ids + "&isShowNav=<%=isShowNav%>&pageSize=" + flex.getOptions().rp;
			}
		})
		
	}
	
	<%
	if (btnNames!=null) {
		len = btnNames.length;
		for (int i=0; i<len; i++) {
			if (!btnScripts[i].startsWith("{")) {
			%>
				if (com=='<%=btnNames[i]%>') {
				<%=btnScripts[i]%>
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
								window.location.href = "module_list_relate.jsp?action=batchOp&batchField=<%=StrUtil.UrlEncode(batchField)%>&batchValue=<%=StrUtil.UrlEncode(batchValue)%>&<%=querystr%>&CPages=<%=curpage%>&privurl=<%=privurl%>&id=" + ids + "&pageSize=" + flex.getOptions().rp;
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
	initCalendar();
});
</script>
</html>
