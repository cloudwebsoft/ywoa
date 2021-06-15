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
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css" />  
<link href="../lte/css/font-awesome.min.css?v=4.4.0" rel="stylesheet">
	<style>
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
		.cond-title {
			margin: 0 5px;
		}
	</style>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/flexigrid.js"></script>
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
if (sort.equals("")) {
	sort = "desc";
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
int defaultPageSize = cfg.getInt("modulePageSize");
int pagesize = ParamUtil.getInt(request, "pageSize", defaultPageSize);

String querystr = "";
int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

String[] arySQL = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);
String sqlUrlStr = arySQL[1];

querystr = "op=" + op + "&mode=" + mode + "&tagName=" + StrUtil.UrlEncode(tagName) + "&code=" + moduleCode + "&menuItem=" + menuItem + "&formCode=" + formCode + "&moduleCodeRelated=" + moduleCodeRelated + "&formCodeRelated=" + moduleCodeRelated + "&parentId=" + parentId + "&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=" + isShowNav;
if (!sqlUrlStr.equals("")) {
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
String userName = privilege.getUser(request);
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
							JSONArray arr = new JSONArray(opts);
							for (int i=0; i<arr.length(); i++) {
								JSONObject json = arr.getJSONObject(i);
								// 不能用getString，因为有些可能为int型
								StrUtil.concat(sb, ",", json.get("name") + ":" + json.get("value"));
							}
						}
						catch(JSONException e) {
							DebugUtil.e("module_list_relate.jsp", "选项json解析错误，字段：", ff.getTitle() + " " + ff.getName() + " 中选项为：" + opts);
							e.printStackTrace();
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
<style>
	<%=msd.getCss(ConstUtil.PAGE_TYPE_LIST)%>
</style>
<table id="searchTable" class="percent98" style="<%=strSearchTableDis%>" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="80%" height="28" align="left" style="padding-top:5px">
    <form id="searchForm" class="search-form" action="module_list_relate.jsp" onsubmit="return false">
	<%
	MacroCtlMgr mm = new MacroCtlMgr();
	ArrayList<String> dateFieldNamelist = new ArrayList<String>();

	int len = 0;
	boolean isQuery = false;
	
	if (btnNames!=null) {
		len = btnNames.length;
		for (int i=0; i<len; i++) {
		  if (btnScripts[i].startsWith("{")) {
			Map<String, String> checkboxGroupMap = new HashMap<String, String>();		  
		  	FormMgr fm = new FormMgr();
			JSONObject json = new JSONObject(btnScripts[i]);
			if (json.get("btnType").equals("queryFields")) {
				String condFields = (String) json.get("fields");
				String condTitles = "";
				if (json.has("titles")) {
					condTitles = (String) json.get("titles");
				}
				String[] fieldAry = StrUtil.split(condFields, ",");
				String[] titleAry = StrUtil.split(condTitles, ",");
				if (fieldAry.length > 0) {
					isQuery = true;
				}
				for (int j = 0; j < fieldAry.length; j++) {
					String fieldName = fieldAry[j];
					String fieldTitle = "#";
					if (titleAry!=null) {
						fieldTitle = titleAry[j];
						if ("".equals(fieldTitle)) {
							fieldTitle = "#";
						}
					}

					String condType = (String) json.get(fieldName);
					CondUnit condUnit = CondUtil.getCondUnit(request, fd, fieldName, fieldTitle, condType, checkboxGroupMap, dateFieldNamelist);
					out.print("<span class=\"cond-span\">");
					out.print("<span class=\"cond-title\">");
					out.print(condUnit.getFieldTitle());
					out.print("</span>");
					out.print(condUnit.getHtml());
					out.print("</span>");
					out.print("<script>");
					out.print(condUnit.getScript());
					out.print("</script>");
				}
			}
		  }		
		}

		// 当doQuery时，需要取相关的数据，所以上面的隐藏输入框必须得有
		if (isQuery) {
		%>
	        <input class="tSearch" type="submit" onclick="doQuery()" value="搜索" />
		<%
		}
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
<%
	boolean canView = mpd.canUserView(userName);
	boolean canLog = mpd.canUserLog(userName);
	boolean canManage = mpd.canUserManage(userName);

	StringBuffer colProps = new StringBuffer();

	String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
	String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
	String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
	boolean isPrompt = false;
	if (!promptField.equals("") && !promptIcon.equals("")) {
		isPrompt = true;
	}
	if (isPrompt) {
		colProps.append("{display:'', name:'colPrompt', width:20}");
	}

	boolean isColOperateShow = true;

	len = fields.length;
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		String fieldTitle = fieldsTitle[i];

		String title = "";
		boolean sortable = true;

		if ("#".equals(fieldTitle)) {
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
			else if (fieldName.equals("flowId")) {
				title = "流程号";
			}
			else if (fieldName.equals("cws_flag")) {
				title = "冲抵状态";
			}
			else if (fieldName.equals("colOperate")) {
				title = "操作";
			}
			else if (fieldName.equals("cws_create_date")) {
				title = "创建时间";
			}
			else if (fieldName.equals("flow_begin_date")) {
				title = "流程开始时间";
			}
			else if (fieldName.equals("flow_end_date")) {
				title = "流程结束时间";
			}
			else if (fieldName.equals("cws_id")) {
				title = "关联ID";
			}
			else {
				title = fd.getFieldTitle(fieldName);
			}
		}
		else {
			title = fieldTitle;
		}

		String w = fieldsWidth[i];
		int wid = StrUtil.toInt(w, 100);
		if (w.indexOf("%")==w.length()-1) {
			w = w.substring(0, w.length()-1);
			wid = 800*StrUtil.toInt(w, 20)/100;
		}

		if ("0".equals(fieldsShow[i])) {
			if ("colOperate".equals(fieldName)) {
				isColOperateShow = false;
			}
			continue;
		}

		String props;
		if ("colOperate".equals(fieldName)) {
			props = "{display:'操作', name:'colOperate', width:" + wid + "}";
		}
		else {
			props = "{display: '" + title + "', name : '" + fieldName + "', width : " + wid + ", sortable : " + sortable + ", align: '" + fieldsAlign[i] + "', hide: false, process:editCol}";
		}

		StrUtil.concat(colProps, ",", props);
	}

	// 如果允许显示操作列，且未定义colOperate，则将其加入，宽度默认为150
	if (isColOperateShow && colProps.lastIndexOf("colOperate")==-1) {
		StrUtil.concat(colProps, ",", "{display:'操作', name:'colOperate', width:150}");
	}
%>
<table id="grid" style="display:none"></table>
<%
	if (!isToolbar) {
%>
<span id="switcher" style="cursor:pointer; position: absolute">
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

var colModel = [<%=colProps.toString()%>];
flex = $("#grid").flexigrid({
	<%
	if (!"search".equals(op)) {
	%>
	url: 'moduleListRelate.do?<%=querystr%>',
	<%
	}
	%>
	params: requestParams,
	dataType: 'json',
	colModel : colModel,
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
			String btnType = json.getString("btnType");
			if ("batchBtn".equals(btnType) || "flowBtn".equals(btnType)) {
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
		,{name: '管理', bclass: 'manage', onpress : action}
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
	onReload: onReload,
	onToolbarInited: doOnToolbarInited,
	autoHeight: <%=msd.getInt("is_auto_height")==1 ? true:false%>,
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
	}
	else if (com=="添加") {
		window.location.href = "module_add_relate.jsp?parentPageType=<%=parentPageType%>&code=<%=StrUtil.UrlEncode(moduleCode)%>&parentId=<%=parentId%>&menuItem=<%=menuItem%>&formCode=<%=formCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&isShowNav=<%=isShowNav%>";
	}
	else if (com=="修改") {
		var id = getIdsSelected(true);
		if (id=='') {
			jAlert('请选择一条记录!', '提示');
			return;
		}

		var tabId = getActiveTabId();
    	addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_edit_relate.jsp?mode=<%=mode%>&code=<%=moduleCode%>&parentId=<%=parentId%>&id=" + id + "&menuItem=<%=menuItem%>&moduleCodeRelated=<%=moduleCodeRelated%>&formCode=<%=formCode%>&tabIdOpener=" + tabId);
	} else if (com == '导入'){
		window.location.href = "module_import_excel.jsp?formCode=<%=formCodeRelated%>&code=<%=moduleCode%>&moduleCodeRelated=<%=moduleCodeRelated%>&parentId=<%=parentId%>&menuItem=<%=menuItem%>";
	}
	else if (com=="管理") {
		addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=msd.getString("form_code")%>&code=<%=msd.getString("code")%>");
	}	
	else if (com=='删除') {
		var ids = getIdsSelected();
		if (ids=="") {
			jAlert('请选择记录!','提示');
			return;
		}
		jConfirm("您确定要删除么？","提示",function(r){
			if (!r) {
				return;
			} else {
				try {
					onBeforeModuleDel<%=moduleCodeRelated%>(ids);
				}
				catch (e) {}

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
				String btnType = json.getString("btnType");
				if ("batchBtn".equals(btnType)) {
					String batchField = json.getString("batchField");
					String batchValue = json.getString("batchValue");
				%>
					if (com=='<%=btnNames[i]%>') {
						var ids = getIdsSelected();
						if (ids=="") {
							jAlert('请选择记录!','提示');
							return;
						}
						jConfirm("您确定要" + com + "么？","提示",function(r){
							if(!r){return;}
							else{
								batchOp(ids, "<%=batchField%>", "<%=batchValue%>");
								// window.location.href = "module_list_relate.jsp?action=batchOp&batchField=<%=StrUtil.UrlEncode(batchField)%>&batchValue=<%=StrUtil.UrlEncode(batchValue)%>&<%=querystr%>&privurl=<%=privurl%>&id=" + ids + "&pageSize=" + flex.getOptions().rp;
							}
						})					
					}
				<%
				}
				else if ("flowBtn".equals(btnType)) {
					String flowTypeCode = json.getString("flowTypeCode");
					Leaf lf = new Leaf();
					lf = lf.getLeaf(flowTypeCode);
				%>
					if (com == '<%=btnNames[i]%>') {
						addTab('<%=lf.getName()%>', '<%=request.getContextPath()%>/flow_initiate1_do.jsp?typeCode=<%=flowTypeCode%>');
					}
					<%
				}
			}
		}
	}
%>
}

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
		onFlexiGridLoaded();
	}
	catch(e) {}
}

function doQuery() {
	var params = $("form").serialize();
	var urlStr = "<%=request.getContextPath()%>/visual/moduleListRelate.do?" + params;
	$("#grid").flexOptions({url : urlStr});
	$("#grid").flexReload();

	// 置全选checkbox为非选中状态
	$(".hDiv input[type='checkbox']").removeAttr("checked");
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
	
	$( celDiv ).click(function() {
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
