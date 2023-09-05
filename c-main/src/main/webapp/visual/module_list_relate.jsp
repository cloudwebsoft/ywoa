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
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.cloudweb.oa.cond.CondUnit" %>
<%@ page import="com.cloudweb.oa.cond.CondUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
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
long parentId = ParamUtil.getLong(request, "parentId", -1);

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCodeRelated);
String formCodeRelated = msd.getString("form_code");
boolean isEditInplace = msd.getInt("is_edit_inplace")==1;
boolean isAutoHeight = msd.getInt("is_auto_height")==1;

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
			else if (viewList==ModuleSetupDb.VIEW_LIST_CALENDAR) {
				response.sendRedirect("module_list_relate_calendar.jsp?mode=subTagRelated&tagName=" + StrUtil.UrlEncode(tagName) + "&parentId=" + parentId + "&code=" + formCode + "&menuItem=" + menuItem);
				return;
			}
		}
		if (!json.isNull("formRelated")) {
			// formCodeRelated = json.getString("formRelated");
			moduleCodeRelated = json.getString("formRelated");
			msd = msd.getModuleSetupDb(moduleCodeRelated);
			formCodeRelated = msd.getString("form_code");
			isEditInplace = msd.getInt("is_edit_inplace")==1;
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
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=msd.getString("name")%></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css" />
<link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet">
	<style>
		i {
			margin-right: 3px;
		}
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form select {
			width: 80px;
		}
		.search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
		.cond-title {
			margin: 0 5px;
		}

		<%=StrUtil.getNullStr(msd.getCss(ConstUtil.PAGE_TYPE_LIST))%>
	</style>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../inc/flow_js.jsp"></script>
	<script src="../js/jquery.raty.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
	<script>
		var requestParams = [];
		<%
			StringBuffer params = new StringBuffer();
            Enumeration enu = request.getParameterNames();
            while(enu.hasMoreElements()) {
				String paramName = (String)enu.nextElement();
				if ("code".equals(paramName) || "formCode".equals(paramName)) {
					continue;
				}
				String paramVal = ParamUtil.get(request, paramName);
				StrUtil.concat(params, "&", paramName + "=" + StrUtil.UrlEncode(paramVal));
        %>
		requestParams.push({name: '<%=paramName%>', value: '<%=paramVal%>'});
		<%
            }
        %>
	</script>
	<script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCodeRelated%>.jsp?parentId=<%=parentId%>&formCode=<%=formCode%>&formCodeRelated=<%=formCodeRelated%>&moduleCodeRelated=<%=moduleCodeRelated%>&pageType=<%=ConstUtil.PAGE_TYPE_LIST_RELATE%>&time=<%=Math.random()%>"></script>

	<script src="../js/jquery.bgiframe.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>

	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<script src="../js/BootstrapMenu.min.js"></script>

	<script type="text/javascript" src="../js/jquery.editinplace.js"></script>
	<script src="../inc/map.js"></script>
	<script type="text/javascript" src="../js/jquery.toaster.js"></script>

	<link rel="stylesheet" href="../js/layui/css/layui.css" media="all">
	<link rel="stylesheet" href="../js/soul-table/soulTable.css" media="all">
	<script src="../js/layui/layui.js" charset="utf-8"></script>
<%
fd = fd.getFormDb(formCodeRelated);
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
	return;
}

// 置页面类型
request.setAttribute("pageType", ConstUtil.PAGE_TYPE_LIST_RELATE);

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

String parentPageType = ParamUtil.get(request, "parentPageType");
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
			if (fdao==null) {
				fdao = new com.redmoon.oa.visual.FormDAO(fd);
				fdao.setFlowTypeCode(String.valueOf(System.currentTimeMillis()));
				fdao.setCwsId(relateFieldValue); // 关联的模块的ID
				fdao.setCreator(privilege.getUser(request)); // 参数为用户名（创建记录者），必填
				fdao.setUnitCode(privilege.getUserUnitCode(request)); // 置单位编码，必填
				fdao.setCwsParentForm(formCode); // 如为嵌套表，则置主表单的编码，且必填，否则选填
				fdao.create();
			}
			long id = fdao.getId();
			if ("edit".equals(parentPageType)) {
				response.sendRedirect("module_edit_relate.jsp?menuItem=" + menuItem + "&id=" + id + "&parentId=" + parentId + "&moduleCodeRelated=" + moduleCodeRelated + "&code=" + moduleCode + "&isShowNav=1");
			}
			else {
				response.sendRedirect("module_show_relate.jsp?menuItem=" + menuItem + "&id=" + id + "&parentId=" + parentId + "&moduleCodeRelated=" + moduleCodeRelated + "&code=" + moduleCode);
			}
			return;
		}
	}
}

String userName = privilege.getUser(request);
String orderBy = ParamUtil.get(request, "orderBy");
String sort = ParamUtil.get(request, "sort");
if ("".equals(orderBy)) {
	String filter = StrUtil.getNullStr(msd.getFilter(userName)).trim();
	boolean isComb = filter.startsWith("<items>") || "".equals(filter);
	// 如果是组合条件，则赋予后台设置的排序字段
	if (isComb) {
		orderBy = StrUtil.getNullStr(msd.getString("orderby"));
		sort = StrUtil.getNullStr(msd.getString("sort"));
	}
	if ("".equals(orderBy)) {
		orderBy = "id";
	}
}
if ("".equals(sort)) {
	sort = "desc";
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
int defaultPageSize = cfg.getInt("modulePageSize");
int pagesize = ParamUtil.getInt(request, "pageSize", defaultPageSize);

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

String[] arySQL = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);
String sqlUrlStr = arySQL[1];

String querystr = "op=" + op + "&mode=" + mode + "&tagName=" + StrUtil.UrlEncode(tagName) + "&code=" + moduleCode + "&menuItem=" + menuItem + "&formCode=" + formCode + "&moduleCodeRelated=" + moduleCodeRelated + "&formCodeRelated=" + moduleCodeRelated + "&parentId=" + parentId + "&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=" + isShowNav;
if (!"".equals(sqlUrlStr)) {
    if (!sqlUrlStr.startsWith("&")) {
		querystr += "&" + sqlUrlStr;
	}
	else {
	    querystr += sqlUrlStr;
	}
}
%>
<script>
var mapEditable = new Map();
var mapEditableOptions = new Map();
var mapCheckboxPresent = new Map;
<%
if (isEditInplace) {
	// 取得当前用户的可写字段，对在位编辑的字段进行初始化
	String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
	if (fieldWrite!=null && !"".equals(fieldWrite)) {
		String[] fds = StrUtil.split(fieldWrite, ",");
		if (fds != null) {
			for (String fieldName : fds) {
				FormField ff = fd.getFormField(fieldName);
				if (ff==null) {
					continue;
				}
				if (ff.getType().equals(FormField.TYPE_TEXTAREA) || ff.getType().equals(FormField.TYPE_TEXTFIELD)
					|| ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME) || ff.getType().equals(FormField.TYPE_CHECKBOX)) {
				%>
					mapEditable.put("<%=fieldName%>", "<%=ff.getType()%>");
				<%
					if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
						ff.setValue("1");
						// 取得present
				%>
					mapCheckboxPresent.put("<%=fieldName%>", "<%=ff.convertToHtml()%>")
                <%
					}
				}
			}
		}
	}
	else {
		MacroCtlMgr mm = new MacroCtlMgr();
		ir = fd.getFields().iterator();
		while (ir.hasNext()) {
			FormField ff = (FormField)ir.next();
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu!=null) {
					IFormMacroCtl ifmc = mu.getIFormMacroCtl();
					String type = ifmc.getControlType();
					if (type.equals(FormField.TYPE_TEXTFIELD)) {
						%>
						mapEditable.put("<%=ff.getName()%>", "<%=FormField.TYPE_TEXTFIELD%>");
						<%
					}
					else if (type.equals(FormField.TYPE_SELECT)) {
						StringBuffer sb = new StringBuffer();
						String opts = ifmc.getControlOptions(userName, ff);
						try {
							org.json.JSONArray arr = new org.json.JSONArray(opts);
							for (int i=0; i<arr.length(); i++) {
								org.json.JSONObject json = arr.getJSONObject(i);
								// 不能用getString，因为有些可能为int型
								StrUtil.concat(sb, ",", json.get("name") + ":" + json.get("value"));
							}
						}
						catch(JSONException e) {
							DebugUtil.e("module_list_relate.jsp", "选项json解析错误，字段：", ff.getTitle() + " " + ff.getName() + " 中选项为：" + opts);
							// e.printStackTrace();
						}
						%>
						mapEditable.put("<%=ff.getName()%>", "<%=FormField.TYPE_SELECT%>");
						mapEditableOptions.put("<%=ff.getName()%>", "<%=sb.toString()%>");
						<%
					}
				}
			}
			else if (ff.getType().equals(FormField.TYPE_SELECT)) {
				String[][] aryOpt = FormParser.getOptionsArrayOfSelect(fd, ff);
				StringBuffer sb = new StringBuffer();
				for (int i=0; i<aryOpt.length; i++) {
					StrUtil.concat(sb, ",", aryOpt[i][0] + ":" + aryOpt[i][1]);
				}
				%>
				mapEditable.put("<%=ff.getName()%>", "<%=FormField.TYPE_SELECT%>");
				mapEditableOptions.put("<%=ff.getName()%>", "<%=sb.toString()%>");
				<%
			}
			else if (ff.getType().equals(FormField.TYPE_TEXTAREA) || ff.getType().equals(FormField.TYPE_TEXTFIELD)
				|| ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME) || ff.getType().equals(FormField.TYPE_CHECKBOX)) {
			%>
				mapEditable.put("<%=ff.getName()%>", "<%=ff.getType()%>");
				<%
				if (ff.getType().equals(FormField.TYPE_CHECKBOX)) {
					ff.setValue("1");
					// 取得present
				%>
				mapCheckboxPresent.put("<%=ff.getName()%>", "<%=ff.convertToHtml()%>")
				<%
				}
			}
		}
	}
}
%>
	
	var curOrderBy = "<%=orderBy%>";
	var sort = "<%=sort%>";
	function doSort(orderBy) {
		if (orderBy==curOrderBy)
			if (sort=="asc")
				sort = "desc";
			else
				sort = "asc";
				
		window.location.href = "module_list_relate.jsp?menuItem=<%=menuItem%>&code=<%=moduleCode%>&parentId=<%=parentId%>&formCode=<%=formCode%>&formCodeRelated=<%=formCodeRelated%>&moduleCodeRelated=<%=moduleCodeRelated%>&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=<%=isShowNav%>";
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

// 加上二开传入的参数
querystr += "&" + params.toString();

String[] fields = msd.getColAry(false, "list_field");
String[] fieldsWidth = msd.getColAry(false, "list_field_width");
String[] fieldsShow = msd.getColAry(false, "list_field_show");
String[] fieldsTitle = msd.getColAry(false, "list_field_title");
String[] fieldsAlign = msd.getColAry(false, "list_field_align");

String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, "#");
String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btnBclass, ",");	
String btnRole = StrUtil.getNullStr(msd.getString("btn_role"));
String[] btnRoles = StrUtil.split(btnRole, "#");

boolean canView = mpd.canUserView(userName);
boolean canLog = mpd.canUserLog(userName);
boolean canManage = mpd.canUserManage(userName);

com.alibaba.fastjson.JSONArray colProps = ModuleUtil.getColProps(msd, false);

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
.cond-span {
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
    <form id="searchForm" class="search-form" action="module_list_relate.jsp" onsubmit="return false">
	<%
	MacroCtlMgr mm = new MacroCtlMgr();
	ArrayList<String> dateFieldNamelist = new ArrayList<String>();

	int len = 0;

	String condsHtml = ModuleUtil.getConditionHtml(request, msd, dateFieldNamelist);
	boolean isQuery = !"".equals(condsHtml);
	out.print(condsHtml);

	// 当doQuery时，需要取相关的数据，所以上面的隐藏输入框必须得有
	if (isQuery) {
	%>
	<button class="layui-btn layui-btn-primary layui-btn-sm" type="submit" onclick="doQuery()" data-type="reload"><i class="fa fa-search"></i>搜索</button>
	<%
	}
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
    </form>   
    </td>
  </tr>
</table>
<script>
	<%
    if (!isQuery) {
    %>
	$('#searchTable').hide();
	<%
    }
    %>
</script>
<table class="layui-hide" id="table_list" lay-filter="<%=moduleCodeRelated%>"></table>
<script type="text/html" id="toolbar_list">
	<div class="layui-btn-container">
		<%if (msd.getInt("btn_add_show") == 1 && mpd.canUserAppend(userName)) {%>
		<button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="add" title="增加"><i class="fa fa-plus-circle"></i></i></i>增加</button>
		<%}%>
		<%if (msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(userName)) {%>
		<button class="layui-btn layui-btn-sm layui-btn-primary layui-border-orange" lay-event="edit" title="修改"><i class="fa fa-pencil"></i>修改</button>
		<%}%>
		<%if (msd.getInt("btn_del_show") == 1 && (mpd.canUserDel(userName) || canManage)) {%>
		<button class="layui-btn layui-btn-sm layui-btn-primary layui-border-red" lay-event="delRows" title="删除"><i class="layui-icon layui-icon-delete"></i>删除</button>
		<%}%>
		<%if (mpd.canUserImport(userName)) {%>
		<button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="importXls" title="导入Excel文件"><i class="fa fa-arrow-circle-o-down"></i>导入</button>
		<%}%>
		<%if (mpd.canUserExport(userName)) {%>
		<button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="exportXls" title="导出Excel文件"><i class="fa fa-file-excel-o"></i>导出</button>
		<%}%>
		<%
			if (btnNames != null && btnBclasses != null) {
				len = btnNames.length;
				for (int i = 0; i < len; i++) {
					boolean isToolBtn = false;
					if (!btnScripts[i].startsWith("{")) {
						isToolBtn = true;
					} else {
						JSONObject json = new JSONObject(btnScripts[i]);
						String btnType = json.getString("btnType");
						if ("batchBtn".equals(btnType) || "flowBtn".equals(btnType)) {
							isToolBtn = true;
						}
					}
					if (isToolBtn) {
						// 检查是否拥有权限
						if (!privilege.isUserPrivValid(request, "admin")) {
							boolean canSeeBtn = false;
							if (btnRoles != null && btnRoles.length > 0) {
								String roles = btnRoles[i];
								String[] codeAry = StrUtil.split(roles, ",");
								// 如果codeAry为null，则表示所有人都能看到
								if (codeAry == null) {
									canSeeBtn = true;
								} else {
									UserDb user = new UserDb();
									user = user.getUserDb(privilege.getUser(request));
									RoleDb[] rdAry = user.getRoles();
									if (rdAry != null) {
										for (RoleDb roleDb : rdAry) {
											String roleCode = roleDb.getCode();
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
							} else {
								canSeeBtn = true;
							}

							if (!canSeeBtn) {
								continue;
							}
						}
		%>
		<button class="layui-btn layui-btn-sm layui-btn-primary layui-border-green" lay-event="event<%=i%>">
			<i class="fa <%=btnBclasses[i]%>"></i>
			<%=btnNames[i]%>
		</button>
		<%
					}
				}
			}
		%>
		<%if (privilege.isUserPrivValid(request, "admin")) {%>
		<button class="layui-btn layui-btn-sm layui-btn-primary layui-border-blue" lay-event="manage"><i class="layui-icon layui-icon-set"></i>管理</button>
		<%}%>
	</div>
</script>
<%
	if (!isToolbar) {
%>
<span id="switcher" style="cursor:pointer; position: absolute; display: none">
	<img id="switchBtn" src="../images/hide.png" title="显示/隐藏 查询区域"/>
</span>
<script>
	$(function() {
		var $box = $('#searchTable');
		var l = $box.offset().left + $box.width();
		var t = $box.offset().top;
		$('#switcher').css({'top': t + 'px', 'left': l + 'px'});

		var $btn = $('#switchBtn');
		var $form = $('#searchForm');
		$('#switcher').click(function() {
			if ($btn.attr('src').indexOf("show.png") != -1) {
				$form.show();
				$btn.attr('src', '../images/hide.png');
			}
			else {
				$form.hide();
				$btn.attr('src', '../images/show.png');
			}
		});
	});
</script>
<%
	}
%>
</body>
<script>
function initCalendar() {
	<%for (String ffname : dateFieldNamelist) {%>
	$('#<%=ffname%>').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d'
    });

	$('#<%=ffname%>').attr('autocomplete', 'off');
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
	if (!sortorder)
		sortorder = "desc";
	var urlStr = "<%=request.getContextPath()%>/visual/moduleListRelate.do?op=<%=op%>&mode=<%=mode%>&tagName=<%=StrUtil.UrlEncode(tagName)%>&parentId=<%=parentId%>&code=<%=moduleCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&formCode=<%=formCode%>&pageSize=" + $("#grid").getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&<%=sqlUrlStr%>&" + $("form").serialize();
	$("#grid").flexOptions({url : urlStr});
	$("#grid").flexReload();
}

function onReload() {
	doQuery();
}
var tableData;
layui.config({
	base: '../js/',   // 第三方模块所在目录
	// version: 'v1.6.2' // 插件版本号
}).extend({
	soulTable: 'soul-table/soulTable',
	tableChild: 'soul-table/tableChild',
	tableMerge: 'soul-table/tableMerge',
	tableFilter: 'soul-table/tableFilter',
	excel: 'soul-table/excel',
});

layui.use(['table', 'soulTable'], function () {
	var table = layui.table;
	var soulTable = layui.soulTable;

	table.render({
		elem: '#table_list'
		, toolbar: '#toolbar_list'
		, defaultToolbar: ['filter', 'print'/*, 'exports', {
				title: '提示'
				,layEvent: 'LAYTABLE_TIPS'
				,icon: 'layui-icon-tips'
			}*/]
		, drag: {toolbar: true}
		, method: 'post'
		, url: 'moduleListRelate.do?<%=querystr%>'
		, cols: [
			<%=colProps.toString()%>
		]
		, id: 'tableList'
		, page: true
		, unresize: false
		, limit: <%=pagesize%>
		<%if (isAutoHeight) {%>
		, height: 'full-98'
		<%}%>
		, parseData: function (res) { //将原始数据解析成 table 组件所规定的数据
			return {
				"code": res.errCode, //解析接口状态
				"msg": res.msg, //解析提示文本
				"count": res.total, //解析数据长度
				"data": res.rows //解析数据列表
			};
		}
		,done: function(res, curr, count){
			tableData = res.data;
			soulTable.render(this);
		}
	});

	//头工具栏事件
	table.on('toolbar()', function (obj) {
		var checkStatus = table.checkStatus(obj.config.id);
		switch (obj.event) {
			case 'add':
				window.location.href = "module_add_relate.jsp?parentPageType=<%=parentPageType%>&code=<%=StrUtil.UrlEncode(moduleCode)%>&parentId=<%=parentId%>&menuItem=<%=menuItem%>&formCode=<%=formCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&isShowNav=<%=isShowNav%>";
				break;
			case 'edit':
				var data = checkStatus.data;
				if (data.length == 0) {
					layer.msg('请选择记录');
					return;
				} else if (data.length > 1) {
					layer.msg('只能选择一条记录');
					return;
				}
				var id = data[0].id;

				var tabId = getActiveTabId();
				addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_edit_relate.jsp?mode=<%=mode%>&code=<%=moduleCode%>&parentId=<%=parentId%>&id=" + id + "&menuItem=<%=menuItem%>&moduleCodeRelated=<%=moduleCodeRelated%>&formCode=<%=formCode%>&tabIdOpener=" + tabId);
				break;
			case 'delRows':
				var data = checkStatus.data;
				if (data.length == 0) {
					layer.msg('请选择记录');
					return;
				}

				var ids = '';
				for (var i in data) {
					var json = data[i];
					if (ids == '') {
						ids = json.id;
					} else {
						ids += ',' + json.id;
					}
				}

				layer.confirm('您确定要删除么？', {icon: 3, title: '提示'}, function (index) {
					//do something
					try {
						onBeforeModuleDel(ids);
					} catch (e) {
					}

					$.ajax({
						type: "post",
						url: "<%=request.getContextPath()%>/visual/moduleDelRelate.do",
						data: {
							code: "<%=moduleCodeRelated%>",
							mode: "<%=mode%>",
							parentId: "<%=parentId%>",
							parentModuleCode: "<%=moduleCode%>",
							ids: ids
						},
						dataType: "html",
						beforeSend: function(XMLHttpRequest){
							$("body").showLoading();
						},
						success: function(data, status){
							data = $.parseJSON(data);
							jAlert(data.msg, "提示");
							if (data.ret=="1") {
								doQuery();
								try {
									onModuleDel<%=moduleCodeRelated%>(ids);
								}
								catch (e) {}
							}
						},
						complete: function(XMLHttpRequest, status){
							$("body").hideLoading();
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert(XMLHttpRequest.responseText);
						}
					});
					layer.close(index);
				});
				// layer.msg(checkStatus.isAll ? '全选': '未全选');
				break;
			case 'importXls':
				window.location.href = "module_import_excel.jsp?formCode=<%=formCodeRelated%>&code=<%=moduleCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&parentId=<%=parentId%>&menuItem=<%=menuItem%>";
				break;
			case 'exportXls':
				var cols = "";
				// 找出未隐藏的表头
				$("div[lay-id='" + obj.config.id + "']").find('.layui-table th').each(function () {
					if ($(this).data("field") && $(this).data("field") != "0" && $(this).data("field") != "colOperate") {
						if (!$(this).hasClass('layui-hide')) {
							if (cols == "") {
								cols = $(this).data("field");
							} else {
								cols += "," + $(this).data("field");
							}
						}
					}
				});
			<%
            // 检查是否设置有模板
			Vector vt = ModuleExportTemplateMgr.getTempaltes(request, msd.getString("form_code"));
			String expUrl = "";
			// 检查是否设置有模板
			if (vt.size()>0) {
				expUrl = request.getContextPath() + "/visual/module_excel_sel_templ.jsp?mode=" + mode + "&isRelate=true";
			}
			else {
				expUrl = request.getContextPath() + "/visual/exportExcelRelate.do";
			}
			%>
				// 生成表单，以post方式，否则IE11下，某些参数可能会有问题
				// 如果用window.open方式，则IE11中当含有coo_address、coo_address_cond时，接收到coo_address的值为?_address_cond=0?_address=，而chrome中不会
				var expForm = o("exportForm");
				if (expForm != null) {
					expForm.parentNode.removeChild(expForm);
				}
				expForm = document.createElement("FORM");
				document.body.appendChild(expForm);

				expForm.style.display = "none";
				expForm.target = "_blank";
				expForm.method = "post";
				expForm.action = "<%=expUrl%>";
				var fields = $(".search-form").serializeArray();
				jQuery.each( fields, function(i, field) {
					expForm.innerHTML += "<input name='" + field.name + "' value='" + field.value + "'/>";
				});
				expForm.innerHTML += "<input name='cols' value='" + cols + "'/>";
				expForm.submit();
				break;
			case 'manage':
				addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=msd.getString("form_code")%>&code=<%=msd.getString("code")%>");
				break;
				//自定义头工具栏右侧图标 - 提示
			case 'LAYTABLE_TIPS':
				layer.alert('这是工具栏右侧自定义的一个图标按钮');
				break;
				<%
                if (btnNames!=null) {
                    len = btnNames.length;
                    for (int i=0; i<len; i++) {
                        if (!btnScripts[i].startsWith("{")) {
                        %>
			case 'event<%=i%>':
				<%=ModuleUtil.renderScript(request, btnScripts[i])%>
				break;
				<%
                }
                else {
                    JSONObject json = new JSONObject(btnScripts[i]);
                    if ((json.get("btnType")).equals("batchBtn")) {
                        String batchField = json.getString("batchField");
                        String batchValue = json.getString("batchValue");
                    %>
			case 'event<%=i%>':
				var data = checkStatus.data;
				if (data.length == 0) {
					layer.msg('请选择记录');
					return;
				}

				var ids = '';
				for (var i in data) {
					var json = data[i];
					if (ids == '') {
						ids = json.id;
					} else {
						ids += ',' + json.id;
					}
				}
				jConfirm("您确定要<%=btnNames[i]%>么？", "提示", function (r) {
					if (!r) {
						return;
					} else {
						batchOp(ids, "<%=batchField%>", "<%=batchValue%>");
					}
				})
				break;
				<%
                    }
                    else if ("flowBtn".equals(json.get("btnType"))) {
						String flowTypeCode = json.getString("flowTypeCode");
						Leaf lf = new Leaf();
						lf = lf.getLeaf(flowTypeCode);
						if (lf == null) {
							DebugUtil.e(getClass(), "流程型按钮 flowTypeCode", flowTypeCode + " 不存在");
						}
						else {
                        %>
			case 'event<%=i%>':
				addTab('<%=lf.getName()%>', '<%=request.getContextPath()%>/flow_initiate1_do.jsp?typeCode=<%=flowTypeCode%>');
				break;
				<%
                    }
                }
    	}
	}
}
%>
		}
	});

	$(document).on('click','.layui-table-cell',function(){
		var $parent = $(this).parent();
		var dataIndex = $parent.parent().attr('data-index');
		// 如果所点的是数据行
		if (dataIndex >= 0) {
			var id = tableData[dataIndex].id;
			var fieldName = $parent.attr('data-field');
			editCol(this, id, fieldName);
		}
	})

	$('.search-form .layui-btn').on('click', function (e) {
		e.preventDefault();
		table.reload('tableList', {
			page: {
				curr: 1 //重新从第 1 页开始
			}
			, where: $('.search-form').serializeJsonObject()
		}, 'data');
	});

	//监听表格排序问题
	table.on('sort()', function (obj) { //注：sort lay-filter="对应的值"
		table.reload('tableList', { //testTable是表格容器id
			initSort: obj // 记录初始排序，如果不设的话，将无法标记表头的排序状态。 layui 2.1.1 新增参数
			, where: {
				orderBy: obj.field //排序字段
				, sort: obj.type //排序方式
			}
		});
	});
});

function batchOp(ids, batchField, batchValue) {
	$.ajax({
		type: "post",
		url: "<%=request.getContextPath()%>/visual/moduleBatchOp.do",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
		data: {
			code: "<%=moduleCodeRelated%>",
			id: ids,
			batchField: batchField,
			batchValue: batchValue
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
			$("body").showLoading();
		},
		success: function(data, status){
			data = $.parseJSON(data);
			jAlert(data.msg, "提示");
			if (data.ret=="1") {
				doQuery();
			}
		},
		complete: function(XMLHttpRequest, status){
			$("body").hideLoading();
		},
		error: function(XMLHttpRequest, textStatus){
			// 请求出错处理
			alert(XMLHttpRequest.responseText);
		}
	});
}

// 用于工具条自定义按钮的调用
function getIdsSelected(onlyOne) {
	var ids = "";
	$(".cth input[type='checkbox'][value!='on']", grid.bDiv).each(function(i) {
		if($(this).is(":checked")) {
			if (ids=="")
				ids = $(this).val().substring(3);
			else
				ids += "," + $(this).val().substring(3);
		}
	});

	var selectedCount = 0;
	var ary = ids.split(",");
	if (ids!="") {
		selectedCount = ary.length;
	}
	if (selectedCount == 0) {
		return "";
	}

	if (selectedCount > 1 && onlyOne) {
		return "";
	}
	return ids;
}

function onLoad() {
	try {
		onModuleListLoaded();
	}
	catch(e) {}
}

function doQuery() {
	layui.table.reload('tableList');
}

function editCol(celDiv, id, colName) {
	if (!mapEditable.containsKey(colName)) {
		return;
	}

	var fieldType = mapEditable.get(colName).value.toLowerCase();
	var selectOptions = "";
	var opts = mapEditableOptions.get(colName);
	if (opts!=null) {
		selectOptions = opts.value;
	}
	
	// 该插件会上传值：original_value、update_value
	$(celDiv).editInPlace({
		field_type: fieldType,
		url: "moduleEditInPlace.do",
		saving_text: "保存中...",
		saving_image: "../images/loading.gif",
		select_text: "请选择",
		select_options: selectOptions,
		checkbox_present: mapCheckboxPresent.get(colName) != null ? mapCheckboxPresent.get(colName).value : "",
		params: "colName=" + colName + "&id=" + id + "&code=<%=StrUtil.UrlEncode(moduleCodeRelated)%>",
		error:function(obj) {
			alert(JSON.stringify(obj));
		},
		success:function(data) {
			data = $.parseJSON(data);
			if (data.ret==-1) { // 值未更改
				return;
			}
			else {
				$.toaster({
					"priority" : "info",
					"message" : data.msg
				});
				// $("#grid").flexReload();
			}
		}
	});
}

var fieldArr = [];

<%
for (int i=0; i<fields.length; i++) {
	String fieldName = fields[i];
	if (fieldName.equals("colOperate")) {
		continue;
	}
%>
	fieldArr.push('<%=fieldName%>');
<%
}
%>
function initEditInPlace() {
	$('#grid tr').each(function() {
		var id = $(this).attr('id');
		$(this).children('td').each(function(i) {
			if (i==0) {
				return;
			}
			var k = i-1; // 第0列为checkbox
			for (var n=0; n<fieldArr.length; n++) {
				if (n==k) {
					var field = fieldArr[n];
					initEditCol($(this).children('div')[0], id, field);
				}
			}
		});
	});
}

$(function() {
	initCalendar();
	
	if (<%=isEditInplace%>) {
		if (typeof(canEditInplace)=="function") {
			if (canEditInplace()) {
				initEditInPlace();
			}
		}
		else {
			initEditInPlace();
		}
	}

	<%
	if ("search".equals(op)) {
	%>
	// 必须得用setTimeout，否则因为jquery的document.ready赋值顺序问题，表单serial后所取得的参数为空
	setTimeout(function() {
		doQuery();
	}, 0)
	<%
	}
	%>
});
</script>
</html>
