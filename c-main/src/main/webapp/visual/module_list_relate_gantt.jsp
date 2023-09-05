<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.db.ListResult"%>
<%@ page import = "cn.js.fan.db.Paginator"%>
<%@ page import = "cn.js.fan.util.DateUtil"%>
<%@ page import = "cn.js.fan.util.ParamUtil"%>
<%@ page import = "cn.js.fan.util.StrUtil"%>
<%@ page import = "cn.js.fan.web.SkinUtil"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.MacroCtlMgr"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.MacroCtlUnit"%>
<%@ page import="com.redmoon.oa.ui.SkinMgr"%>
<%@ page import="org.json.JSONArray"%>
<%@ page import="org.json.JSONException"%>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Vector" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");
String formCodeRelated = ParamUtil.get(request, "formCodeRelated");
String menuItem = ParamUtil.get(request, "menuItem");
String moduleCode = ParamUtil.get(request, "code");

ModuleSetupDb parentMsd = new ModuleSetupDb();
parentMsd = parentMsd.getModuleSetupDbOrInit(moduleCode);
formCode = parentMsd.getString("form_code");

String mode = ParamUtil.get(request, "mode");
String tagName = ParamUtil.get(request, "tagName");
int parentId = ParamUtil.getInt(request, "parentId", -1);

// 通过选项卡标签关联
boolean isSubTagRelated = "subTagRelated".equals(mode);

if (isSubTagRelated) {
   	String tagUrl = ModuleUtil.getModuleSubTagUrl(moduleCode, tagName);
	try {
		JSONObject json = new JSONObject(tagUrl);
		if (!json.isNull("formRelated")) {
			formCodeRelated = json.getString("formRelated");
		}
		else {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "选项卡关联配置不正确！"));
			return;
		}
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

FormDb fd = new FormDb();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=fd.getName()%>任务看板</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />

<meta http-equiv="X-UA-Compatible" content="IE=Edge;chrome=IE8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />

<link href="../js/jQuery.Gantt/css/gantt.css" rel="stylesheet" type="text/css" />
<link href="../js/jQuery.Gantt/css/style.css" type="text/css" rel="stylesheet" />
<script src="../inc/common.js"></script>

<script src="../js/jQuery.Gantt/js/jquery.min.js"></script>
<script src="../js/jQuery.Gantt/js/jquery.cookie.js"></script>
<script src="../js/jQuery.Gantt/js/jquery.fn.gantt.js"></script>

<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
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
		relateFieldValue = fdm.getRelateFieldValue(parentId, formCodeRelated);
		if (relateFieldValue==null) {
			out.print(StrUtil.jAlert_Back("请检查模块是否相关联！","提示"));
			return;
		}
	}
}

// 置嵌套表及关联查询选项卡生成链接需要用到的cwsId
request.setAttribute("cwsId", "" + parentId);

String op = ParamUtil.get(request, "op");
ModulePrivDb mpd = new ModulePrivDb(formCodeRelated);
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
				response.sendRedirect("moduleShowRelatePage.do?id=" + id + "&parentId=" + parentId + "&moduleCodeRelated=" + formCodeRelated + "&code=" + formCode);
				return;
			}
		}
	}
}

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String querystr = "";
int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

int isShowNav = ParamUtil.getInt(request, "isShowNav", 1);

String[] arySQL = SQLBuilder.getModuleListRelateSqlAndUrlStr(request, fd, op, orderBy, sort, relateFieldValue);
String sql = arySQL[0];
String sqlUrlStr = arySQL[1];
// out.print(sql);

querystr = "op=" + op + "&mode=" + mode + "&tagName=" + StrUtil.UrlEncode(tagName) + "&code=" + moduleCode + "&menuItem=" + menuItem + "&formCode=" + formCode + "&formCodeRelated=" + formCodeRelated + "&parentId=" + parentId + "&orderBy=" + orderBy + "&sort=" + sort + "&isShowNav=" + isShowNav;
if (!sqlUrlStr.equals(""))
	querystr += "&" + sqlUrlStr;
%>
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
<div class="spacerH"></div>
<%	
com.redmoon.oa.visual.FormDAO fdao = new com.redmoon.oa.visual.FormDAO();
ListResult lr = fdao.listResult(formCodeRelated, sql, curpage, pagesize);
long total = lr.getTotal();
Vector v = lr.getResult();
if (v!=null)
	ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCodeRelated);

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
%>
<table class="percent98" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td height="28" align="center">
      <form id="searchForm" action="module_list_relate_gantt.jsp">
  <%
	ArrayList<String> list = new ArrayList<String>();
	MacroCtlMgr mm = new MacroCtlMgr();
	
	String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
	String[] btnNames = StrUtil.split(btnName, ",");
	String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
	String[] btnScripts = StrUtil.split(btnScript, "#");
	int len = 0;
	boolean isQuery = false;
	if (btnNames!=null) {
		len = btnNames.length;
		for (int i=0; i<len; i++) {
		
		  if (btnScripts[i].startsWith("{")) {
			// System.out.println(getClass() + " " + btnScripts[i]);
			// if (true) continue;
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
		else if (!btnScripts[i].startsWith("{")) {
		%>
        &nbsp;<input class="btn" type="button" value="<%=btnNames[i]%>" onclick="<%=StrUtil.HtmlEncode(btnScripts[i])%>" />
        <%
		  }
		}
		if (isQuery) {
		%>
        <input type="hidden" name="code" value="<%=moduleCode%>" />
        <input type="hidden" name="formCodeRelated" value="<%=formCodeRelated%>" />
        <input type="hidden" name="formCode" value="<%=formCode%>" />
        <input type="hidden" name="parentId" value="<%=parentId%>" />
        <input type="hidden" name="op" value="search" />
        <input type="hidden" name="menuItem" value="<%=menuItem%>" />
        <input type="hidden" name="mode" value="<%=mode%>" />
        <input type="hidden" name="tagName" value="<%=tagName%>" />
        <%=requestParamInputs%>
        <input class="tSearch" name="submit" type="submit" value="搜索" />
        <%
		}		
	}
	%>
      </form>   
    </td>
  </tr>
  <tr>
    <td height="28" align="right">共 <b><%=paginator.getTotal() %></b> 条　每页 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<div id="ganttBox" class="gantt"></div>
<%
String fieldBeginDate = StrUtil.getNullStr(msd.getString("field_begin_date"));
String fieldEndDate = StrUtil.getNullStr(msd.getString("field_end_date"));

if (fieldBeginDate.equals("") || fieldEndDate.equals("")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "请配置模块：" + msd.getString("name") + "的任务看板视图"));
	return;
}

FormField ffBegin = fd.getFormField(fieldBeginDate);
FormField ffEnd = fd.getFormField(fieldEndDate);

if (ffBegin==null || ffEnd==null) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "开始或结束时间不存在！"));
	return;
}

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
<table width="98%" border="0" cellspacing="0" cellpadding="0" align="center" class="percent98">
  <tr>
    <td height="23" align="right">
    <%
		out.print(paginator.getCurPageBlock("module_list_relate_gantt.jsp?"+querystr));
	%></td>
  </tr>
</table>
</body>
</html>
