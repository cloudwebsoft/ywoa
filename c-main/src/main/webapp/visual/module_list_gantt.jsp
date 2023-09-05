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
<%@ page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
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
int is_workLog = msd.getInt("is_workLog");
if (!msd.getString("code").equals(msd.getString("form_code"))) {
	ModuleSetupDb msdMain = msd.getModuleSetupDb(msd.getString("form_code"));
	is_workLog = msdMain.getInt("is_workLog");
	mainFormCode = msd.getString("form_code");
}

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
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String querystr = "";

int pagesize = ParamUtil.getInt(request, "pageSize", 10);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
String unitCode = ParamUtil.get(request, "unitCode");

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>

<meta http-equiv="X-UA-Compatible" content="IE=Edge;chrome=IE8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />

<link href="../js/jQuery.Gantt/css/gantt.css" rel="stylesheet" type="text/css">
<link href="../js/jQuery.Gantt/css/style.css" type="text/css" rel="stylesheet">

<script src="../js/jQuery.Gantt/js/jquery.min.js"></script>
<script src="../js/jQuery.Gantt/js/jquery.cookie.js"></script>
<script src="../js/jQuery.Gantt/js/jquery.fn.gantt.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />  

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

</head>
<body>
<%@ include file="module_inc_menu_top.jsp"%>
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
	return;
}
if (unitCode.equals("")) {
	unitCode = privilege.getUserUnitCode(request);
}

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

%>
<script>
o("menu100").className="current";
</script>
<div class="spacerH"></div>
<%
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
long total = lr.getTotal();
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
%>
<table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="center">
    <form id="searchForm" action="module_list_gantt.jsp">&nbsp;
    <%
	boolean isShowUnitCode = false;
	
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
    <%}%>
<%
String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, "#");
String btnBclass = StrUtil.getNullStr(msd.getString("btn_bclass"));
String[] btnBclasses = StrUtil.split(btnBclass, ",");

ArrayList<String> list = new ArrayList<String>();
MacroCtlMgr mm = new MacroCtlMgr();

int len = 0;
boolean isQuery = false;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
		if (btnScripts[i].startsWith("{")) {
			// System.out.println(getClass() + " " + btnScripts[i]);
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
					%>
        			<%=ff.getTitle()%>
               		<%if (ff.getType().equals(FormField.TYPE_DATE) || ff.getType().equals(FormField.TYPE_DATE_TIME)) {
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
                              <input id="<%=ff.getName()%>FromDate" name="<%=ff.getName()%>FromDate" size="15" value = "<%=fDate%>" />
                              <!-- <img style="CURSOR: hand" onClick="SelectDate('<%=ff.getName()%>FromDate', 'yyyy-MM-dd')" src="<%=request.getContextPath()%>/images/form/calendar.gif" align="absmiddle" border="0" width="26" height="26" /> -->
                              小于
                              <input id="<%=ff.getName()%>ToDate" name="<%=ff.getName()%>ToDate" size="15" value = "<%=tDate%>" />
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
				}
			}
		}
	}
	
	if (isQuery) {
	%>
        <input type="hidden" name="op" value="search" />
        <input type="hidden" name="code" value="<%=code%>" />
        <%=requestParamInputs%>
        <input class="tSearch" name="submit" type="submit" value="搜索" />
	<%
	}
}
%>
        </form>
    </td>
  </tr>
</table>
<table width="98%" border="0" cellpadding="0" cellspacing="0" class="percent98">
  <tr><td align="right">
&nbsp;找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b>
</td></tr></table>
<div id="ganttBox" class="gantt"></div>
<%
String fieldBeginDate = msd.getString("field_begin_date");
String fieldEndDate = msd.getString("field_end_date");
FormField ffBegin = fd.getFormField(fieldBeginDate);
FormField ffEnd = fd.getFormField(fieldEndDate);
String dateFormatBegin = "yyyy-MM-dd";
String dateFormatEnd = "yyyy-MM-dd";
if (ffBegin.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
	dateFormatBegin = "yyyy-MM-dd HH:mm:ss";
}
if (ffEnd.getFieldType()==FormField.FIELD_TYPE_DATETIME) {
	dateFormatEnd = "yyyy-MM-dd HH:mm:ss";
}
JSONArray jsonAry = new JSONArray();
while (ir.hasNext()) {
	fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
	java.util.Date bd = DateUtil.parse(fdao.getFieldValue(fieldBeginDate), dateFormatBegin);
	java.util.Date ed = DateUtil.parse(fdao.getFieldValue(fieldEndDate), dateFormatEnd);
	JSONObject json = new JSONObject();

String val = "";
	FormField ff = fdao.getFormField(msd.getString("field_name"));
	if (ff.getType().equals(FormField.TYPE_MACRO)) {
		MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		if (mu != null) {
			val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
		}
	}
	else {	
		val = FuncUtil.renderFieldValue(fdao, ff);
	}	
	json.put("name", val);
	
	ff = fdao.getFormField(msd.getString("field_desc"));
	if (ff.getType().equals(FormField.TYPE_MACRO)) {
		MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		if (mu != null) {
			val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
		}
	}
	else {	
		val = FuncUtil.renderFieldValue(fdao, ff);
	}	
	json.put("desc", val);
	
	JSONArray arr = new JSONArray();
	JSONObject jobj = new JSONObject();
	jobj.put("from", "/Date(" + DateUtil.toLong(bd) + ")/");
	jobj.put("to", "/Date(" + DateUtil.toLong(ed) + ")/");

	ff = fdao.getFormField(msd.getString("field_label"));
	if (ff.getType().equals(FormField.TYPE_MACRO)) {
		MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
		if (mu != null) {
			val = mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(ff.getName()));
		}
	}
	else {	
		val = FuncUtil.renderFieldValue(fdao, ff);
	}		
	jobj.put("label", val);
	if (fd.isProgress()) {
		jobj.put("progress", fdao.getCwsProgress());
	}
	jobj.put("dataObj", fdao.getFlowId());
	arr.put(jobj);
	
	json.put("values", arr);
	jsonAry.put(json);
}
%>
<script>
var config={
            divId: "ganttBox",
            navigate: "scroll",
            isProgress: <%=fd.isProgress()%>,
            scale: "<%=msd.getString("scale_default")%>",
            minScale: "<%=msd.getString("scale_min")%>",
            maxScale: "<%=msd.getString("scale_max")%>",
            itemsPerPage: 10,
            onItemClick: function(data) {
            	addTab("<%=fd.getName()%>", "<%=request.getContextPath()%>/flowShowPage.do?flowId=" + data);
            },
            onAddClick: function(dt, rowId) {
            },
            onRender: function() {
                if (window.console && typeof console.log === "function") {
                    //console.log("chart rendered");
                }
            },
            scrollToToday:true
        };

        var source = <%=jsonAry%>;
        config.source = source;
        $("#"+config.divId).empty().gantt(config);
</script>
<table width="98%" class="percent98">
      <tr>
        <td align="right">
		<%
		out.print(paginator.getCurPageBlock("module_list_gantt.jsp?"+querystr));
		%>
        </td>
      </tr>
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

$(function() {
	$("#unitCode").val("<%=unitCode%>");
});

function onChangeUnitCode(unitCode) {
	window.location.href = "<%=request.getContextPath()%>/visual/module_list_gantt.jsp?op=<%=op%>&code=<%=code%>&unitCode=" + unitCode + "&formCode=<%=formCode%>&pageSize=" + flex.getOptions().rp + "&orderBy=<%=orderBy%>&sort=<%=sort%>";
}
</script>
</html>
