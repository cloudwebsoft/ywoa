<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.base.*" %>
<%@ page import="com.redmoon.oa.visual.*" %>
<%@ page import="com.redmoon.oa.flow.macroctl.*" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.pvg.*" %>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="org.json.*" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.cloudweb.oa.cond.CondUtil" %>
<%@ page import="com.cloudweb.oa.cond.CondUnit" %>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String userName = privilege.getUser(request);
String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action");

String code = ParamUtil.get(request, "moduleCode");
if ("".equals(code)) {
	code = ParamUtil.get(request, "code");
	if ("".equals(code)) {
		code = ParamUtil.get(request, "formCode");
	}
}
if ("".equals(code)) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, SkinUtil.ERR_ID)));
	return;
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

boolean isEditInplace = msd.getInt("is_edit_inplace")==1;
String formCode = msd.getString("form_code");

if (msd.getInt("view_list")==ModuleSetupDb.VIEW_LIST_GANTT) {
	response.sendRedirect(request.getContextPath() + "/visual/module_list_gantt.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
	return;
}
else if (msd.getInt("view_list")==ModuleSetupDb.VIEW_LIST_CALENDAR) {
	response.sendRedirect(request.getContextPath() + "/visual/module_list_calendar.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
	return;
}
else if (msd.getInt("view_list")==ModuleSetupDb.VIEW_LIST_TREE) {
	boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
	if (!isInFrame) {
		response.sendRedirect(request.getContextPath() + "/visual/module_list_frame.jsp?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
		return;
	}
}
else if (msd.getInt("view_list")==ModuleSetupDb.VIEW_LIST_MODULE_TREE) {
	boolean isInFrame = ParamUtil.getBoolean(request, "isInFrame", false);
	if (!isInFrame) {
		response.sendRedirect(request.getContextPath() + "/visual/module_basic_tree_frame.jsp" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
		return;
	}
}
else if (msd.getInt("view_list")==ModuleSetupDb.VIEW_LIST_CUSTOM) {
	String moduleUrlList = StrUtil.getNullStr(msd.getString("url_list"));
	if (!"".equals(moduleUrlList)) {
		response.sendRedirect(request.getContextPath() + "/" + moduleUrlList + "?code=" + code + "&formCode=" + StrUtil.UrlEncode(formCode));
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

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
int defaultPageSize = cfg.getInt("modulePageSize");
int pagesize = ParamUtil.getInt(request, "pageSize", defaultPageSize);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
String unitCode = ParamUtil.get(request, "unitCode");
%>
<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title><%=fd.getName()%>列表</title>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
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
    <script type="text/javascript" src="../js/flexigrid.js"></script>

    <script src="../js/jquery.bgiframe.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css" />
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>

    <script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>
    <script src="<%=request.getContextPath()%>/inc/flow_js.jsp"></script>
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
    <script src="<%=request.getContextPath()%>/flow/form_js/form_js_<%=formCode%>.jsp?op=<%=op%>&code=<%=code%>&pageType=moduleList&<%=params.toString()%>&time=<%=Math.random()%>"></script>
    <script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
    <script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
    <link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>

    <link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
    <script src="../js/datepicker/jquery.datetimepicker.js"></script>

    <link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
    <script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

    <script src="../js/jquery.raty.min.js"></script>
    <script src="../js/BootstrapMenu.min.js"></script>

    <script type="text/javascript" src="../js/jquery.editinplace.js"></script>
    <script src="../inc/map.js"></script>
    <script type="text/javascript" src="../js/jquery.toaster.js"></script>
</head>
<body>
<%@ include file="module_inc_menu_top.jsp" %>
<%
    int menuItem = ParamUtil.getInt(request, "menuItem", 1);
%>
<script>
    o("menu<%=menuItem%>").className = "current";
</script>
<%
    if (!fd.isLoaded()) {
        out.print(StrUtil.jAlert_Back("表单不存在！", "提示"));
        return;
    }

    querystr = "op=" + op + "&code=" + code + "&orderBy=" + orderBy + "&sort=" + sort + "&unitCode=" + unitCode;

	// 将过滤配置中request中其它参数也传至url中，这样分页时可传入参数
    String requestParams = "";
    String requestParamInputs = "";

    Map map = ModuleUtil.getFilterParams(request, msd);
    Iterator irMap = map.keySet().iterator();
    while (irMap.hasNext()) {
        String key = (String) irMap.next();
        String val = (String) map.get(key);
        requestParams += "&" + key + "=" + StrUtil.UrlEncode(val);
        requestParamInputs += "<input type='hidden' name='" + key + "' value='" + val + "' />";
    }
    querystr += requestParams;

    // 用于传过滤条件
    request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
    String[] ary = null;
    try {
        ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
    } catch (ErrMsgException e) {
        out.print(e.getMessage());
        return;
    }

    String sqlUrlStr = ary[1];
    if (!sqlUrlStr.equals("")) {
        querystr += "&" + sqlUrlStr;
    }

	String[] fields = msd.getColAry(false, "list_field");
    if (fields == null || fields.length==0) {
        out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "显示列未配置！"));
        return;
    }
	String[] fieldsWidth = msd.getColAry(false, "list_field_width");
	String[] fieldsShow = msd.getColAry(false, "list_field_show");
	String[] fieldsTitle = msd.getColAry(false, "list_field_title");
	String[] fieldsAlign = msd.getColAry(false, "list_field_align");

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
    if (btnNames != null) {
        int len = btnNames.length;
        for (int i = 0; i < len; i++) {
            if (btnScripts[i].startsWith("{")) {
                JSONObject json = new JSONObject(btnScripts[i]);
                if (((String) json.get("btnType")).equals("queryFields")) {
                    if (json.has("isToolbar")) {
                        isToolbar = json.getInt("isToolbar") == 1;
                    }
                    break;
                }
            }
        }
    }

    boolean isButtonsShow = false;
    isButtonsShow = (msd.getInt("btn_add_show") == 1 && mpd.canUserAppend(privilege.getUser(request))) ||
            (msd.getInt("btn_edit_show") == 1 && mpd.canUserModify(privilege.getUser(request))) ||
			mpd.canUserDel(privilege.getUser(request)) ||
			mpd.canUserManage(privilege.getUser(request)) ||
            mpd.canUserImport(privilege.getUser(request)) ||
            mpd.canUserExport(privilege.getUser(request)) ||
            (btnNames != null && isToolbar);

    Map btnCanShowMap = new HashMap();
	if (btnNames!=null && btnBclasses!=null) {
		int len = btnNames.length;
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
					if (canSeeBtn) {
						btnCanShowMap.put(btnNames[i], "");
					}
				}
			}
		}

		if (btnCanShowMap.size()>0) {
			isButtonsShow = true;
		}
	}

    String strSearchTableDis = "";
    if (btnNames == null) {
        strSearchTableDis = "display:none";
    }

    if (!isToolbar) {
%>
<style>
    .cond-span {
        display: inline-block;
        float: left;
        width: 300px;
        min-height: 32px;
    }

    .condBtnSearch {
        display: inline-block;
        float: left;
        width: 50px;
    }
</style>
<%
    }
%>
<style>
	<%=msd.getCss(ConstUtil.PAGE_TYPE_LIST)%>
</style>
<table id="searchTable" style="<%=strSearchTableDis %>" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="left" style="padding-top:5px">
    <form id="searchForm" class="search-form" action="module_list.jsp" onsubmit="return false">&nbsp;
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
		<span class="cond-span">
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
    }

    // 显示查询条件
	ArrayList<String> dateFieldNamelist = new ArrayList<String>();
	int len = 0;
	boolean isQuery = false;
	if (btnNames != null) {
		len = btnNames.length;
		for (int i = 0; i < len; i++) {
			if (btnScripts[i].startsWith("{")) {
				Map<String, String> checkboxGroupMap = new HashMap<String, String>();
				JSONObject json = new JSONObject(btnScripts[i]);
				if (((String) json.get("btnType")).equals("queryFields")) {
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
	}
%>
		<span class="cond-span">
        <input type="hidden" name="op" value="search" />
        <input type="hidden" name="code" value="<%=code%>" />
        <%=requestParamInputs%>
        <input type="hidden" name="menuItem" value="<%=menuItem%>" />
        <input type="hidden" name="mainCode" value="<%=ParamUtil.get(request, "mainCode")%>" />
        <%if (isQuery || isShowUnitCode) { %>
        <input style="<%=strSearchTableDis %>" class="tSearch" type="submit" onclick="doQuery()" value="搜索" />
		</span>
		<%} %>
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
<%
	FormViewDb formViewDb = new FormViewDb();
	Vector vtView = formViewDb.getViews(formCode);
	if (vtView.size() > 0) {
		Iterator irView = vtView.iterator();
%>
<div id="dlgView" style="display:none">
    <select id="formViewId" name="formViewId">
		<option value="<%=ConstUtil.MODULE_EXPORT_WORD_VIEW_FORM%>">根据表单</option>
		<%
			while (irView.hasNext()) {
				formViewDb = (FormViewDb)irView.next();
		%>
			<option value="<%=formViewDb.getLong("id")%>"><%=formViewDb.getString("name")%></option>
		<%
			}
		%>
    </select>
</div>
<%
	}
%>
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
}

function changeSort(sortname, sortorder) {
	if (!sortorder) {
		sortorder = "desc";
	}
	var urlStr = "<%=request.getContextPath()%>/visual/moduleList.do?op=<%=op%>&code=<%=code%>&unitCode=<%=unitCode%>&formCode=<%=formCode%>&pageSize=" + $("#grid").getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&<%=sqlUrlStr%>&" + $("form").serialize();
	$("#grid").flexOptions({url : urlStr});
	$("#grid").flexReload();
}

function onReload() {
	doQuery();
}

var mapEditable = new Map();
var mapEditableOptions = new Map();
var mapCheckboxPresent = new Map;
<%
boolean canUserManage = mpd.canUserManage(userName);

if (isEditInplace) {
	// 取得当前用户的可写字段，对在位编辑的字段进行初始化
	String fieldWrite = mpd.getUserFieldsHasPriv(userName, "write");
	if (fieldWrite!=null && !"".equals(fieldWrite)) {
		String[] fds = StrUtil.split(fieldWrite, ",");
		if (fds != null) {
			for (String fieldName : fds) {
				FormField ff = fd.getFormField(fieldName);
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
		Iterator ir = fd.getFields().iterator();
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

StringBuffer colProps = new StringBuffer();

String promptField = StrUtil.getNullStr(msd.getString("prompt_field"));
String promptValue = StrUtil.getNullStr(msd.getString("prompt_value"));
String promptIcon = StrUtil.getNullStr(msd.getString("prompt_icon"));
boolean isPrompt = false;
if (!"".equals(promptField) && !"".equals(promptIcon)) {
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

	Object[] aryTitle = CondUtil.getFieldTitle(fd, fieldName, fieldTitle);
	String title = (String)aryTitle[0];
	boolean sortable = (Boolean)aryTitle[1];

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
var colModel = [<%=colProps.toString()%>];
$("#grid").flexigrid({
	<%
	if (!"search".equals(op)) {
	%>
	url: 'moduleList.do?<%=querystr%>',
	<%
	}
	%>
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
	<%if (mpd.canUserExport(userName)) {%>
		{name: '生成', bclass: 'word', onpress : action},
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
				if (btnCanShowMap.get(btnNames[i])==null) {
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
	autoHeight: <%=msd.getInt("is_auto_height")==1 ? true:false%>,
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
		String expUrl = "";
		// 检查是否设置有模板
		Vector v = ModuleExportTemplateMgr.getTempaltes(request, formCode);
		if (v.size()>0) {
			expUrl = request.getContextPath() + "/visual/module_excel_sel_templ.jsp";
			%>
			// openWin('<%=request.getContextPath()%>/visual/module_excel_sel_templ.jsp?' + $("#searchForm").serialize() + '&cols=' + encodeURI(cols), 480, 160);
			<%
		}
		else {
			expUrl = request.getContextPath() + "/visual/exportExcel.do";
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
		var fields  = $("#searchForm").serializeArray();
		jQuery.each( fields, function(i, field) {
			expForm.innerHTML += "<input name='" + field.name + "' value='" + field.value + "'/>";
		});
		expForm.innerHTML += "<input name='cols' value='" + cols + "'/>";
		expForm.submit();
	}
	else if (com=="生成") {
		var ids = getIdsSelected();

		<%
		int exportWordView = msd.getInt("export_word_view");
		if (exportWordView == ConstUtil.MODULE_EXPORT_WORD_VIEW_SELECT) {
		%>
        $("#dlgView").dialog({
            title:'请选择',
            modal: true,
            width: 350,
            height: 160,
            // bgiframe:true,
            buttons: {
                '<lt:Label res="res.flow.Flow" key="cancel"/>': function() {
                    $(this).dialog("close");
                },
                '<lt:Label res="res.flow.Flow" key="sure"/>': function() {
					window.open('<%=request.getContextPath()%>/visual/exportWord?formViewId=' + $('#formViewId').val() + '&ids=' + ids + "&code=<%=code%>&" + $("form").serialize());
					$(this).dialog("close");
                }
            },
            closeOnEscape: true,
            draggable: true,
            resizable:true
        });
        <%
        }
		else {
		%>
			window.open('<%=request.getContextPath()%>/visual/exportWord?formViewId=<%=exportWordView%>&ids=' + ids + "&code=<%=code%>&" + $("form").serialize());
			$(this).dialog("close");
		<%
		}
        %>
	}
	else if (com=="全部导出") {
		window.open('<%=request.getContextPath()%>/visual/exportExcel.do?isAll=true&' + $("#searchForm").serialize());
	}
	else if (com=="全部导入") {
		var url = "<%=request.getContextPath()%>/visual/module_import_excel.jsp?isAll=true&formCode=<%=formCode%>";
		openWin(url,360,50);
	}
	else if (com=="添加") {
		window.location.href = "<%=request.getContextPath()%>/visual/module_add.jsp?code=<%=code%>&<%=params%>&privurl=<%=privurl%>";
	}
	else if (com=="修改") {
        var id = getIdsSelected(true);
        if (id=='') {
			jAlert('请选择一条记录!', '提示');
			return;
		}

		var tabId = getActiveTabId();

		<%if (msd.getInt("view_edit")==ModuleSetupDb.VIEW_EDIT_CUSTOM) {%>
    	addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/<%=msd.getString("url_edit")%>?parentId=" + id + "&id=" + id + "&code=<%=code%>&formCode=<%=formCode%>" + "&tabIdOpener=" + tabId);
		<%}else{%>
    	addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_edit.jsp?parentId=" + id + "&id=" + id + "&code=<%=code%>&formCode=<%=formCode%>" + "&tabIdOpener=" + tabId);
		<%}%>
	}
	else if (com=='删除') {
		var ids = getIdsSelected();
		del(ids);
	} else if(com=='导入') {
		window.location.href = "<%=request.getContextPath()%>/visual/module_import_excel.jsp?formCode=<%=formCode%>&code=<%=code%>&<%=params%>";
	}
	else if (com=="管理") {
		addTab("<%=msd.getString("name")%>", "<%=request.getContextPath()%>/visual/module_field_list.jsp?formCode=<%=formCode%>&code=<%=code%>&tabIdOpener=" + getActiveTabId());
	}
	else if (com=="迁移") {
	    var ids = getIdsSelected();
		if (ids=='') {
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
					var ids = getIdsSelected();
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
				String btnType = json.getString("btnType");
				if ("batchBtn".equals(btnType)) {
					String batchField = json.getString("batchField");
					String batchValue = json.getString("batchValue");
				%>
					if (com=='<%=btnNames[i]%>') {
					    var ids = getIdsSelected();
						if (ids=='') {
							jAlert('请选择记录!','提示');
							return;
						}
						jConfirm("您确定要" + com + "么？","提示",function(r){
							if(!r){return;}
							else{
								batchOp(ids, "<%=batchField%>", "<%=batchValue%>");
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

		try {
			onBeforeModuleDel<%=code%>(ids);
		}
		catch (e) {}

		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/visual/moduleDel.do",
			data: {
				code: "<%=code%>",
				id: ids
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
						onModuleDel(ids);
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
	});
}

function batchOp(ids, batchField, batchValue) {
	$.ajax({
		type: "post",
		url: "<%=request.getContextPath()%>/visual/moduleBatchOp.do",
		contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
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
				doQuery();
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
	// console.log(urlStr);
	$("#grid").flexOptions({url : urlStr});
	$("#grid").flexReload();

	// 置全选checkbox为非选中状态
	$(".hDiv input[type='checkbox']").removeAttr("checked");
}

function editCol(celDiv, id, colName) {
	if (!<%=isEditInplace%>) {
		return;
	}
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
			params: "colName=" + colName + "&id=" + id + "&code=<%=StrUtil.UrlEncode(code)%>",
			error:function(obj){
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
					$("#grid").flexReload();
				}
			}
		});
	});
}

$(document).ready(function() {
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
		}else{
		%>
			$("#unitCode").val("<%=unitCode%>");
			<%
		}
	}

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
