<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.db.ListResult" %>
<%@ page import="cn.js.fan.db.Paginator" %>
<%@ page import="cn.js.fan.db.ResultIterator" %>
<%@ page import="cn.js.fan.db.ResultRecord" %>
<%@ page import="cn.js.fan.util.DateUtil" %>
<%@ page import="cn.js.fan.util.ParamUtil" %>
<%@ page import="cn.js.fan.util.StrUtil" %>
<%@ page import="cn.js.fan.web.SkinUtil" %>
<%@ page import="com.cloudweb.oa.cond.CondUnit" %>
<%@ page import="com.cloudweb.oa.cond.CondUtil" %>
<%@ page import="com.cloudweb.oa.utils.ConstUtil" %>
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate" %>
<%@ page import="com.redmoon.oa.dept.DeptChildrenCache" %>
<%@ page import="com.redmoon.oa.dept.DeptDb" %>
<%@ page import="com.redmoon.oa.flow.FormDb" %>
<%@ page import="com.redmoon.oa.flow.FormField" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlMgr" %>
<%@ page import="com.redmoon.oa.flow.macroctl.MacroCtlUnit" %>
<%@ page import="com.redmoon.oa.person.UserDb" %>
<%@ page import="com.redmoon.oa.person.UserMgr" %>
<%@ page import="com.redmoon.oa.sys.DebugUtil" %>
<%@ page import="com.redmoon.oa.ui.SkinMgr" %>
<%@ page import="com.redmoon.oa.util.RequestUtil" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="com.redmoon.oa.flow.FormDAO" %>
<%@ page import="com.redmoon.oa.flow.FormDAOMgr" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
- 功能描述：从嵌套表格点击“选择”按钮出的模块选择窗体，拉单
- 访问规则：
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：20131124
==================
- 修改者：
- 修改时间：
- 修改原因：
- 修改点：
*/
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String nestFormCode = ParamUtil.get(request, "nestFormCode");
String nestType = ParamUtil.get(request, "nestType");
String parentFormCode = ParamUtil.get(request, "parentFormCode");
String nestFieldName = ParamUtil.get(request, "nestFieldName");
long parentId = ParamUtil.getLong(request, "parentId", com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID);
int flowId = com.redmoon.oa.visual.FormDAO.NONEFLOWID;

FormDb pForm = new FormDb();
pForm = pForm.getFormDb(parentFormCode);
FormField nestField = pForm.getFormField(nestFieldName);

if (parentId!=com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID) {
	com.redmoon.oa.visual.FormDAO fdaoPar = new com.redmoon.oa.visual.FormDAO();
	fdaoPar = fdaoPar.getFormDAO(parentId, pForm);
	flowId = fdaoPar.getFlowId();
}

JSONObject json = null;
JSONArray mapAry = new JSONArray();
String filter = "";
try {
	// 20131123 fgf 添加
	String defaultVal = StrUtil.decodeJSON(nestField.getDescription());
	json = new JSONObject(defaultVal);
	nestFormCode = json.getString("destForm");
	filter = json.getString("filter");
	mapAry = (JSONArray)json.get("maps");
} catch (JSONException e) {
	e.printStackTrace();
	out.print(SkinUtil.makeErrMsg(request, "JSON解析失败！"));
	return;
}

String moduleCode = json.getString("sourceForm");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(moduleCode);
String formCode = msd.getString("form_code");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.Alert_Back("表单不存在！"));
	return;
}

com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
boolean isNestSheetCheckPrivilege = cfg.getBooleanProperty("isNestSheetCheckPrivilege");

ModulePrivDb mpd = new ModulePrivDb(moduleCode);
if (isNestSheetCheckPrivilege) {
	if (!mpd.canUserSee(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

if ("selBatch".equals(op)) {
	FormDb nestFd = new FormDb();
	nestFd = nestFd.getFormDb(nestFormCode);
	
	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO(fd);
	com.redmoon.oa.visual.FormDAO fdaoNest = new com.redmoon.oa.visual.FormDAO(nestFd);
	String ids = ParamUtil.get(request, "ids");
	String[] ary = StrUtil.split(ids, ",");
	if (ary!=null) {
		// 检查嵌套表中是否有重复记录
		if (!FormDAOMgr.checkFieldIsUniqueNestOnPull(request, parentId, parentFormCode, fd, nestFd, ary)) {
			StringBuilder sbFields = new StringBuilder();
			for (FormField ff : nestFd.getFields()) {
				if (ff.isUniqueNest()) {
					StrUtil.concat(sbFields, "+", ff.getTitle());
				}
			}
			out.print(StrUtil.Alert_Back(String.format(SkinUtil.LoadString(request, "res.module.flow", "err_is_not_unique"), sbFields)));
			return;
		}

		if ("detaillist".equals(nestType)) {
		%>
			<script>
			if (window.opener) {
				window.opener.clearDetailList("<%=nestFieldName%>");
			}
			</script>
		<%
		}
	
		// 取出待插入的数据
		for (int i = 0; i < ary.length; i++) {
			long id = StrUtil.toLong(ary[i]);
			fdao = fdao.getFormDAO(id, fd);
			
			ModuleSetupDb msdNest = new ModuleSetupDb();
			msdNest = msdNest.getModuleSetupDbOrInit(nestFormCode);
			// String listField = StrUtil.getNullStr(msdNest.getString("list_field"));
			// System.out.println(getClass() + " listField=" + listField);
			String[] fields = msdNest.getColAry(false, "list_field");
			
			int len = 0;
			if (fields!=null) {
				len = fields.length;
			}
			// System.out.println(getClass() + " nestType=" + nestType);
			
			// 根据映射关系赋值
			JSONObject jsonObj2 = new JSONObject();
			for (int k=0; k<mapAry.length(); k++) {
				JSONObject jsonObj = null;
				try {
					jsonObj = mapAry.getJSONObject(k);
					String sfield = (String) jsonObj.get("sourceField");
					String dfield = (String) jsonObj.get("destField");
					String fieldValue = "";
					if (sfield.startsWith("main:")) {
						String[] subFields = sfield.split(":");
						if (subFields.length == 3) {
							FormDb subfd = new FormDb(subFields[1]);
							com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
							// FormField subff = subfd.getFormField(subFields[2]);
							String subsql = "select id from " + subfdao.getTableName() + " where cws_id=" + id + " order by cws_order";
							JdbcTemplate jt = new JdbcTemplate();
							try {
								ResultIterator ri = jt.executeQuery(subsql);
								if (ri.hasNext()) {
									ResultRecord rr = (ResultRecord) ri.next();
									int subid = rr.getInt(1);
									subfdao = new com.redmoon.oa.visual.FormDAO(subid, subfd);
									fieldValue = subfdao.getFieldValue(subFields[2]);
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
					else if (sfield.startsWith("other:")) {
						fieldValue = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, sfield);
					}
					else {
						fieldValue = fdao.getFieldValue(sfield);
					}

					if (sfield.equals(com.redmoon.oa.visual.FormDAO.FormDAO_NEW_ID) || "FormDAO_ID".equals(sfield)) {
						fieldValue = String.valueOf(fdao.getId());
					}

					jsonObj2.put(dfield, fieldValue);
					
					fdaoNest.setFieldValue(dfield, fieldValue);
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
			
			if ("detaillist".equals(nestType)) {
				JSONArray jsonAry = new JSONArray();
				jsonAry.put(jsonObj2);
				// System.out.println(getClass() + " jsonAry=" + jsonAry);
				%>
				<script>				
				// 如果有父窗口
				if (window.opener) {
				  try {
					window.opener.insertRow("<%=nestFormCode%>", '<%=jsonAry%>', "<%=nestFieldName%>");
				  }
				  catch (e) {
				  }
				}
				</script>						
				<%
				continue;
			}					
			
            fdaoNest.setFlowId(flowId);
			fdaoNest.setCwsId(String.valueOf(parentId));
			fdaoNest.setCreator(privilege.getUser(request));
			fdaoNest.setUnitCode(privilege.getUserUnitCode(request));
			fdaoNest.setCwsQuoteId((int)id);
			fdaoNest.setCwsParentForm(parentFormCode);
			fdaoNest.setCwsQuoteForm(formCode);
			boolean re = fdaoNest.create();
			if (re) {
				RequestUtil.setFormDAO(request, fdaoNest);
						
				long fdaoId = fdaoNest.getId();
				// 如果是嵌套表格2
				if (nestType.equals(ConstUtil.NEST_SHEET)) {
					MacroCtlMgr mm = new MacroCtlMgr();
					// System.out.println(getClass() + " nestFormCode=" + nestFormCode);					
					String tds = "";
					String token = "#@#";
					for (int n=0; n<len; n++) {
						String fieldName = fields[n];
						String v = "";
						if (fieldName.startsWith("main:")) {
							String[] subFields = fieldName.split(":");
							if (subFields.length == 3) {
								FormDb subfd = new FormDb(subFields[1]);
								com.redmoon.oa.visual.FormDAO subfdao = new com.redmoon.oa.visual.FormDAO(subfd);
								FormField subff = subfd.getFormField(subFields[2]);
								String subsql = "select id from " + subfdao.getTableName() + " where cws_id=" + id + " order by cws_order";
								JdbcTemplate jt = new JdbcTemplate();
								try {
									ResultIterator ri = jt.executeQuery(subsql);
									if (ri.hasNext()) {
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
										v = subFieldValue;
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						} else if (fieldName.startsWith("other:")) {
							v = com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName);
						} 
						else {
							v = StrUtil.getNullStr(fdaoNest.getFieldHtml(request, fieldName)); // fdao.getFieldValue(fieldName);
						}
						if (n==0) {
							tds = v;
						} else {
							tds += token + v;
						}
					}
			
					%>
						<script>
                        // 如果有父窗口
                        if (window.opener) {
                          try {
							<%if (parentId==com.redmoon.oa.visual.FormDAO.TEMP_CWS_ID) {%>
							window.opener.addTempCwsId("<%=nestFormCode%>", <%=fdaoId%>);
							<%}%>
                            window.opener.insertRow_<%=nestFormCode%>("<%=nestFormCode%>", <%=fdaoId%>, "<%=tds.replaceAll("\"", "\\\\\"")%>", "<%=token%>", true);
                          }
                          catch (e) {
                          	// console.log(e);
                          }
                        }
                        </script>
					<%
				}
			}
		}
		
		if (nestType.equals(ConstUtil.NEST_SHEET) || nestType.equals(ConstUtil.NEST_DETAIL_LIST)) {
		%>
		<script>
		window.close();
		</script>
		<%
		}
		else if (ConstUtil.NEST_TABLE.equals(nestType)) {
		%>
		<script>
			window.opener.refreshNestTableCtl<%=nestFieldName%>();
			window.close();
		</script>
		<%
		}
		else {
		%>
		<script>
			window.opener.location.reload();
			window.close();
		</script>
		<%
		}
		return;
	}
}

String orderBy = ParamUtil.get(request, "orderBy");
String sort = ParamUtil.get(request, "sort");
if ("".equals(orderBy)) {
	String filterOfModule = StrUtil.getNullStr(msd.getString("filter")).trim();
	boolean isComb = filterOfModule.startsWith("<items>") || "".equals(filterOfModule);
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

String querystr = "";

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

// 过滤条件
// System.out.println(getClass() + " filter=" + filter);
String conds = filter; // ParamUtil.get(request, "filter");
if ("none".equals(conds)) {
	conds = "";
}

String action = ParamUtil.get(request, "action");

long mainId = ParamUtil.getLong(request, "mainId", -1);
querystr = "op=" + op + "&action=" + action + "&formCode=" + formCode + "&orderBy=" + orderBy + "&sort=" + sort + "&filter=" + StrUtil.UrlEncode(conds) + "&parentFormCode=" + parentFormCode + "&nestFieldName=" + nestFieldName + "&nestType=" + nestType + "&parentId=" + parentId + "&mainId=" + mainId;

String sql = "select t1.id from " + fd.getTableNameByForm() + " t1";
String urlStrFilter = "";

MacroCtlMgr mm = new MacroCtlMgr();            	

// 如果在宏控件中定义了条件conds，则解析条件，并从父窗口中取表单域的值，再作为参数重定向回本页面传入sql条件参数中
if (!"".equals(conds)) {
	// 如果尚未取值，则从其父窗口中取值，然后重定向回来赋值给条件中的{$fieldName}
	if (!"afterGetClientValue".equals(action)) {
		StringBuffer urlStrBuffer = new StringBuffer();
		if ("search".equals(op)) {
				Iterator ir = fd.getFields().iterator();
				while (ir.hasNext()) {
					FormField ff = (FormField)ir.next();
					String value = ParamUtil.get(request, ff.getName());
					String name_cond = ParamUtil.get(request, ff.getName() + "_cond");
					if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
						urlStrBuffer = StrUtil.concat(urlStrBuffer, "&", ff.getName() + "_cond=" + name_cond);
						if ("0".equals(name_cond)) {
							// 时间段
							String fDate = ParamUtil.get(request, ff.getName() + "FromDate");
							String tDate = ParamUtil.get(request, ff.getName() + "ToDate");
							if (!"".equals(fDate)) {
								urlStrBuffer = StrUtil.concat(urlStrBuffer, "&", ff.getName() + "FromDate=" + fDate);
							}
							if (!"".equals(tDate)) {
								urlStrBuffer = StrUtil.concat(urlStrBuffer, "&", ff.getName() + "ToDate=" + fDate);
							}
						}
						else {
							// 时间点
							String d = ParamUtil.get(request, ff.getName());
							if (!"".equals(d)) {
								urlStrBuffer = StrUtil.concat(urlStrBuffer, "&", ff.getName() + "=" + StrUtil.UrlEncode(d));
							}
						}
					}			
					else if (ff.getType().equals(FormField.TYPE_SELECT)) {
						String[] ary = ParamUtil.getParameters(request, ff.getName());
						if (ary!=null) {
							int len = ary.length;
							for (int n=0; n<len; n++) {
								if (!"".equals(ary[n])) {
									urlStrBuffer = StrUtil.concat(urlStrBuffer, "&", ff.getName() + "=" + StrUtil.UrlEncode(ary[n]));
								}
							}
						}
					}			
					else {
						if (!"".equals(value)) {
							urlStrBuffer = StrUtil.concat(urlStrBuffer, "&", ff.getName() + "=" + StrUtil.UrlEncode(value));
							urlStrBuffer = StrUtil.concat(urlStrBuffer, "&", ff.getName() + "_cond=" + StrUtil.UrlEncode(name_cond));
						}
					}
				}
				
				int cws_flag = ParamUtil.getInt(request, "cws_flag", -1);			
				String cws_flag_cond = ParamUtil.get(request, "cws_flag_cond");
				StrUtil.concat(urlStrBuffer, "&", "cws_flag_cond=" + cws_flag_cond);
				StrUtil.concat(urlStrBuffer, "&", "cws_flag=" + cws_flag);
				int cws_status = ParamUtil.getInt(request, "cws_status", -20000);						
				String cws_status_cond = ParamUtil.get(request, "cws_status_cond");			
				StrUtil.concat(urlStrBuffer, "&", "cws_status=" + cws_status);
				StrUtil.concat(urlStrBuffer, "&", "cws_status_cond=" + cws_status_cond);
			}		
	%>
		<script>
        var condStr = "";
        </script>
        <%
        // 从父窗口取值
		boolean isFound = false;
		Pattern p = Pattern.compile(
			"\\{\\$([A-Z0-9a-z-_@\\u4e00-\\u9fa5\\xa1-\\xff]+)\\}", // 前为utf8中文范围，后为gb2312中文范围
			Pattern.DOTALL | Pattern.CASE_INSENSITIVE);        
		Matcher m = p.matcher(conds);
        while (m.find()) {
            String fieldName = m.group(1);
            if ("cwsCurUser".equals(fieldName) || "curUser".equals(fieldName)
            	|| "curUserDept".equals(fieldName) || "curUserRole".equals(fieldName) || "admin.dept".equals(fieldName)) {
				isFound = true;
            	continue;
            }
			// 当条件为包含时，fieldName以@开头
			if (fieldName.startsWith("@")) {
				fieldName = fieldName.substring(1);
			}
            %>
            <script>
            if (condStr=="")
                condStr = "<%=fieldName%>=" + encodeURI(window.opener.o("<%=fieldName%>").value);
            else
                condStr += "&<%=fieldName%>=" + encodeURI(window.opener.o("<%=fieldName%>").value);
            </script>
            <%
            isFound = true;
        }
        
        if (isFound) {
            %>
            <script>
            window.location.href = "module_list_nest_sel.jsp?action=afterGetClientValue&<%=querystr%>&<%=urlStrBuffer%>&" + condStr;
            </script>
            <%
            return;
        }
	}
	else {
		String[] ary = ModuleUtil.parseFilter(request, formCode, conds);
		if (ary[0]!=null) {
			// conds = ary[0];
			urlStrFilter = ary[1];
		}
	}	
}

// 用于传相应模块的msd，因为模块中的main:...，other:...字段需解析，此时仅根据拉单时指定的filter过滤，而不根据模块中的过滤条件
request.setAttribute(ModuleUtil.MODULE_SETUP, msd);
request.setAttribute(ModuleUtil.NEST_SHEET_FILTER, filter);

String query = ParamUtil.get(request, "query");
String keywords = "";
String urlStr = "";

if (!"".equals(query)) {
	sql = query;
}
else if ("insearch".equals(op)) {
	Iterator ir = fd.getFields().iterator();
	String cond = "";
	keywords = ParamUtil.get(request, "keywords");

	while (ir.hasNext()) {
		FormField ff = (FormField)ir.next();
		if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
			continue;
		}			
		else if (ff.getType().equals(FormField.TYPE_SELECT)) {
			continue;
		}			
		else {
			if (!"".equals(keywords)) {
				if ("".equals(cond)) {
					cond += ff.getName() + " like " + StrUtil.sqlstr("%" + keywords + "%");
				} else {
					cond += " or " + ff.getName() + " like " + StrUtil.sqlstr("%" + keywords + "%");
				}
			}
		}
	}
	if (!"".equals(cond)) {
		sql = sql + (sql.contains(" where ") ? " and " : " where ") + "(" + cond + ")";
	}
		
	if (!sql.toLowerCase().contains("cws_status")) {
		if (!sql.contains(" where ")) {
			sql += " where cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
		}
		else {
			sql += " and cws_status=" + com.redmoon.oa.flow.FormDAO.STATUS_DONE;
		}
	}
	
	// System.out.print(getClass() + " sql=" + sql);
	sql += " order by " + orderBy + " " + sort;		
}
else {
	op = "search";
	String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
	sql = ary[0];
	urlStr = ary[1];
} 

DebugUtil.i(getClass(), "sql", sql);

if ("".equals(urlStr)) {
	urlStr = urlStrFilter;
}
else {
	urlStr += "&" + urlStrFilter;
}

if (!"".equals(urlStr)) {
	querystr += "&" + urlStr;
}
	
String[] fields = msd.getColAry(false, "list_field");
String[] fieldsWidth = msd.getColAry(false, "list_field_width");
String[] fieldsTitle = msd.getColAry(false, "list_field_title");
%>
<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title><%=fd.getName()%>列表</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<style>
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]):not([type="submit"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
		.cond-span {
			display: inline-block;
			float: left;
			text-align: left;
			width: 300px;
			height: 32px;
			margin: 3px 0;
		}
		.condBtnSearch {
			display: inline-block;
			float: left;
		}
		.cond-title {
			margin: 0 5px;
		}

		<%=msd.getCss(ConstUtil.PAGE_TYPE_LIST)%>
	</style>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<script src="../js/jquery.bgiframe.js"></script>
	<script src="../js/BootstrapMenu.min.js"></script>
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>

	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>

	<script>
		var curOrderBy = "<%=orderBy%>";
		var sort = "<%=sort%>";

		function doSort(orderBy) {
			if (orderBy == curOrderBy)
				if (sort == "asc")
					sort = "desc";
				else
					sort = "asc";

			window.location.href = "module_list_nest_sel.jsp?op=<%=op%>&formCode=<%=formCode%>&orderBy=" + orderBy + "&sort=" + sort + "&<%=querystr%>&keywords=<%=keywords%>";
		}

		function sel(id, sth) {
			window.opener.setIntpuObjValue(id, sth);
			window.close();
		}
	</script>
</head>
<body>
<%@ include file="module_nest_sel_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<div class="spacerH"></div>
<table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="center">
    <form id="searchForm" class="search-form" action="module_list_nest_sel.jsp" method="get">
    &nbsp;
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

String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, "#");
String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btnBclass, ",");

ArrayList<String> dateFieldNamelist = new ArrayList<String>();

FormMgr fm = new FormMgr();

int len = 0;
boolean isQuery = false;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
		if (btnScripts[i].startsWith("{")) {
			// System.out.println(getClass() + " " + btnScripts[i]);
			Map<String, String> checkboxGroupMap = new HashMap<String, String>();
			JSONObject jsonBtn = new JSONObject(btnScripts[i]);
			if (((String)jsonBtn.get("btnType")).equals("queryFields")) {
				String condFields = (String)jsonBtn.get("fields");
				String condTitles = "";
				if (jsonBtn.has("titles")) {
					condTitles = (String) jsonBtn.get("titles");
				}
				String[] fieldAry = StrUtil.split(condFields, ",");
				if (fieldAry.length > 0) {
					isQuery = true;
				}
				String[] titleAry = StrUtil.split(condTitles, ",");
				for (int j = 0; j < fieldAry.length; j++) {
					String fieldName = fieldAry[j];
					String fieldTitle = "#";
					if (titleAry != null) {
						fieldTitle = titleAry[j];
						if ("".equals(fieldTitle)) {
							fieldTitle = "#";
						}
					}

					if (!jsonBtn.has(fieldName)) {
						continue;
					}
					String condType = (String) jsonBtn.get(fieldName);
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
	
	if (isQuery) {
	%>
        <input type="hidden" name="op" value="search" />
        <input type="hidden" name="nestFormCode" value="<%=nestFormCode%>" />
        <input type="hidden" name="parentFormCode" value="<%=parentFormCode%>" />
        <input type="hidden" name="nestFieldName" value="<%=nestFieldName%>" />
        <input type="hidden" name="parentId" value="<%=parentId%>" />
        <input type="hidden" name="orderBy" value="<%=orderBy %>" />
        <input type="hidden" name="sort" value="<%=sort %>" />
        <input class="btn btn-default" type="submit" value="搜索" />
		<input type="hidden" name="nestType" value="<%=nestType%>" />        
	<%
	}
}
%>
        </form>
    </td>
  </tr>
</table>
<%
	com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
	ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
	long total = lr.getTotal();
	Vector v = lr.getResult();
	Iterator ir = null;
	if (v != null) {
		ir = v.iterator();
	}
	paginator.init(total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages == 0) {
		curpage = 1;
		totalpages = 1;
	}
%>
<table class="percent98 p9" width="98%" border="0" align="center" cellpadding="3" cellspacing="1">
  <tr>
    <td align="left" width="30%" height="23">
  	<span style="display:none">  
          关键字：
	<form name="form1" method="post" action="module_list_nest_sel.jsp?op=insearch&<%=querystr%>" id="form1">
		<input name="keywords" id="keywords" size=20 value="<%=keywords %>" />
		<input type="submit" name="Submit" value="搜索" class="btn"/>
	</form>
	</span>
	<input type="button" class="btn" value="选择" onClick="selBatch()" /></td>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
  <tr align="center">
    <td class="tabStyle_1_title" width="30px" style="cursor:hand">
      <input name="checkbox" type="checkbox" onClick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" />
    </td>
<%
// 取得当前用户的隐藏字段
String fieldHide = mpd.getUserFieldsHasPriv(privilege.getUser(request), "hide");
String[] fdsHide = StrUtil.split(fieldHide, ",");      
len = 0;
if (fields!=null) {
	len = fields.length;
}
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	
	if (fdsHide!=null) {
		boolean isHide = false;
		for (String hideField : fdsHide) {
			if (hideField.equals(fieldName)) {
				isHide = true;
				break;
			}
		}
		if (isHide) {
			continue;
		}
	}
	
	String fieldTitle = fieldsTitle[i];
	Object[] aryTitle = CondUtil.getFieldTitle(fd, fieldName, fieldTitle);
	String title = (String)aryTitle[0];
	boolean sortable = (Boolean)aryTitle[1];

	String doSort;
	if (!sortable) {
		doSort = "";
	}
	else {
		doSort = "doSort('" + fieldName + "')";
	}
%>
    <td class="tabStyle_1_title" width="<%=fieldsWidth[i]%>" style="cursor:hand" onclick="doSort('<%=fieldName%>')">
	<%=title%>
	<%
	if (orderBy.equals(fieldName)) {
		if (sort.equals("asc")) {
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		} else {
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
		}
	}%>	
	</td>
<%}%>
  </tr>
  <%
	  	WorkflowDb wf = new WorkflowDb();
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			RequestUtil.setFormDAO(request, fdao);
			k++;
			long id = fdao.getId();
		%>
  <tr align="center" class="highlight">
    <td align="center"><input type="checkbox" id="ids" name="ids" value="<%=id%>" /></td>
<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
		if (fdsHide!=null) {
			boolean isHide = false;
			for (String hideField : fdsHide) {
				if (hideField.equals(fieldName)) {
					isHide = true;
					break;
				}
			}
			if (isHide) {
				continue;
			}
		}		
	%>	
		<td align="left">
        <%if (i==0) {%>
		<a href="module_show.jsp?parentId=<%=id%>&id=<%=id%>&code=<%=moduleCode%>&isShowNav=0" target="_blank">
		<%
		}
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
			out.print(StrUtil.getNullStr(com.redmoon.oa.visual.FormDAOMgr.getFieldValueOfOther(request, fdao, fieldName)));
		} 		
		else if (fieldName.equals("cws_creator")) {
			String realName = "";
			if (fdao.getCreator() != null) {
				UserDb user = um.getUserDb(fdao.getCreator());
				if (user != null) {
					realName = user.getRealName();
				}
			}
			out.print(realName);	
		}
		else if (fieldName.equals("cws_progress")) {
			out.print(fdao.getCwsProgress());
		}
		else if (fieldName.equals("cws_status")) {
			out.print(com.redmoon.oa.flow.FormDAO.getStatusDesc(fdao.getCwsStatus()));
		}
		else if (fieldName.equals("cws_flag")) {
			out.print(fdao.getCwsFlag());
		}
		else if (fieldName.equalsIgnoreCase("ID")) {
			out.print(fdao.getId());
		}
		else if (fieldName.equals("cws_id")) {
			out.print(StrUtil.getNullStr(fdao.getCwsId()));
		}
		else if (fieldName.equals("cws_create_date")) {
			out.print(DateUtil.format(fdao.getCwsCreateDate(), "yyyy-MM-dd"));
		}
		else if (fieldName.equals("flow_begin_date")) {
			int fdaoFlowId = fdao.getFlowId();
			if (fdaoFlowId!=-1) {
				wf = wf.getWorkflowDb(fdaoFlowId);
				out.print(DateUtil.format(wf.getBeginDate(), "yyyy-MM-dd"));
			}
		}
		else if (fieldName.equals("flow_end_date")) {
			int fdaoFlowId = fdao.getFlowId();
			if (fdaoFlowId!=-1) {
				wf = wf.getWorkflowDb(fdaoFlowId);
				out.print(DateUtil.format(wf.getEndDate(), "yyyy-MM-dd"));
			}
		}
		else{
			FormField ff = fdao.getFormField(fieldName);
			if (ff!=null) {
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
					}
				}
				else {%>		
					<%=FuncUtil.renderFieldValue(fdao, ff)%>                    
				<%}
			}else {
				%>
				<%=fieldName%>不存在
				<%
			}%>
		<%}
		if (i==0) {
		%>
		</a>
        <%}%>
        </td>
	<%}%>
  </tr>
  <%
  }
%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent98">
  <tr>
    <td width="50%" height="23" align="left">
    <input type="button" class="btn" value="选择" onClick="selBatch()" />
    </td>
    <td width="50%" align="right"><%
		out.print(paginator.getCurPageBlock("module_list_nest_sel.jsp?"+querystr + "&keywords=" + keywords));
	%></td>
  </tr>
</table>
<form name="hidForm" action="" method="post">
<input name="op" type="hidden" />
<input name="ids" type="hidden" />
<input type="hidden" name="nestType" value="<%=nestType%>" />
<input type="hidden" name="parentFormCode" value="<%=parentFormCode%>" />
<input type="hidden" name="nestFieldName" value="<%=nestFieldName%>" />
<input type="hidden" name="parentId" value="<%=parentId%>" />
</form>
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

$(function() {
	initCalendar();
});

function selAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs!=null)	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++) {
			checkboxboxs[i].checked = true;
		}
	}
}

function deSelAllCheckBox(checkboxname) {
	var checkboxboxs = document.getElementsByName(checkboxname);
	if (checkboxboxs != null) {
		if (checkboxboxs.length == null) {
			checkboxboxs.checked = false;
		}
		for (i = 0; i < checkboxboxs.length; i++) {
			checkboxboxs[i].checked = false;
		}
	}
}

function selBatch() {
	var ids = getCheckboxValue("ids");
	if (ids=="") {
		jAlert("请先选择记录！", "提示");
		return;
	}

	jConfirm("您确定要选择么？", "提示", function (r) {
		if (!r) {
			return;
		} else {
			hidForm.action = "module_list_nest_sel.jsp";
			hidForm.op.value = "selBatch";
			hidForm.ids.value = ids;
			hidForm.submit();
		}
	});
}
</script>
</html>
