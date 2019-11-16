<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "project.admin")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);


ModulePrivDb mpd = new ModulePrivDb(formCode);
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

if (!cn.js.fan.db.SQLFilter.isValidSqlParam(orderBy)) {
	com.redmoon.oa.LogUtil.log(privilege.getUser(request), StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "SQL_INJ monitor/yoaacc_excel.jsp orderBy=" + orderBy);
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "param_invalid")));
	return;
}

String querystr = "";

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDbOrInit(formCode);

request.setAttribute("MODULE_SETUP", msd);
String[] ary = SQLBuilder.getModuleListSqlAndUrlStr(request, fd, op, orderBy, sort);
String sql = ary[0];
String sqlUrlStr = ary[1];
String kind = ParamUtil.get(request, "kind");
String what = ParamUtil.get(request, "what");
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
String prj_type = ParamUtil.get(request, "prj_type");
int status = ParamUtil.getInt(request, "status", 0);

String action = ParamUtil.get(request, "action");

try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "prj_type", prj_type, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "what", what, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "kind", kind, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "beginDate", beginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "endDate", endDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "action", action, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}

querystr = "op=" + op + "&action=" + action + "&formCode=" + formCode + "&orderBy=" + orderBy + "&sort=" + sort + "&kind=" + kind + "&what=" + StrUtil.UrlEncode(what) + "&beginDate=" + beginDate + "&endDate=" + endDate + "&prj_type=" + prj_type + "&status=" + status;
if (!sqlUrlStr.equals(""))
	querystr += "&" + sqlUrlStr;
// out.print(sql);


%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><%=fd.getName()%>列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>

<!-- 
<style type="text/css"> 
@import url("<%=request.getContextPath()%>/util/jscalendar/calendar-win2k-2.css"); 
</style>

<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/util/jscalendar/calendar-setup.js"></script>
 -->

<script>
var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "project_list_m.jsp?op=<%=op%>&formCode=<%=formCode%>&orderBy=" + orderBy + "&sort=" + sort + "&<%=querystr%>";
}

function selAllCheckBox(checkboxname){
	var checkboxboxs = document.all.item(checkboxname);
	if (checkboxboxs!=null){
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			checkboxboxs.checked = true;
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			checkboxboxs[i].checked = true;
		}
	}
}

function getIds() {
    var checkedboxs = 0;
	var checkboxboxs = document.all.item("ids");
	var id = "";
	if (checkboxboxs!=null){
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked){
			   checkedboxs = 1;
			   id = checkboxboxs.value;
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].checked){
			   checkedboxs = 1;
			   if (id=="")
				   id = checkboxboxs[i].value;
			   else
				   id += "," + checkboxboxs[i].value;
			}
		}
	}
	return id;
}

function del(){
	var id = getIds();
	if (id==""){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要删除吗？","提示",function(r){
		if(!r){return;}
		else{
			window.location.href = "project_list_m.jsp?action=del&<%=querystr%>&CPages=<%=curpage%>&id=" + id;
		}
	})
}
</script>
</head>
<body>
<%
if (!fd.isLoaded()) {
	out.print(StrUtil.jAlert_Back("表单不存在！","提示"));
	return;
}
if (action.equals("del")) {
	if (!mpd.canUserManage(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
		
	com.redmoon.oa.visual.FormDAOMgr fdm = new com.redmoon.oa.visual.FormDAOMgr(fd);
	try {
		if (fdm.del(request)) {
			out.print(StrUtil.jAlert_Redirect("删除成功！","提示", "project_list_m.jsp?action=search&" + querystr + "&CPages=" + curpage));
			out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
			return;
		}
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
		out.print("<script type='text/javascript'>$('#popup_overlay').hide();</script>");
		return;
	}
}
 %>
<%@ include file="project_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<div class="spacerH"></div>
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right"><form name="formSearch" action="project_list_m.jsp" method="get">
      <table width="98%"  border="0" align="center" cellpadding="0" cellspacing="0" class="p9">
        <tr>
            <td height="30" align="center">
			<input name="action" value="search" type="hidden" />
	  <%
	  Iterator irType = SelectMgr.getOptions("project_type").iterator();
	  String opts = "";
	  while (irType.hasNext()) {
      	SelectOptionDb sod = (SelectOptionDb) irType.next();
		opts += "<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>";
	  }
	  %>
              类型
              <select name="prj_type" id="prj_type">
			  	<option value="">不限</option>
                <%=opts%>
              </select>            
            状态
              <select id="status" name="status">
              <option value="-1">不限</option>
              <%
			  Iterator ir2 = SelectMgr.getOptions("project_status").iterator();
			  opts = "";
			  while (ir2.hasNext()) {
				SelectOptionDb sod = (SelectOptionDb) ir2.next();
				opts += "<option value='" + sod.getValue() + "'>" + sod.getName() + "</option>";
			  }
			  out.print(opts);
			  %>
              </select>
			  <script>
			  formSearch.prj_type.value = "<%=prj_type%>";
			  formSearch.status.value = "<%=status%>";
			  </script>
            <select id="kind" name="kind">
              <option value="name">标题</option>
              <option value="content">内容</option>
            </select>
            <input id="what" name="what" size=20 value="<%=what%>">
            <input name="formCode" value="<%=formCode%>" type="hidden" />
           	  开始日期
             	<input id="beginDate" name="beginDate" value="<%=beginDate%>" size=15>
             	结束日期 
             	<input id="endDate" name="endDate" value="<%=endDate%>" size=15>
             	
              &nbsp;
              <input class="btn" name="submit" type=submit value="搜索">
		    </td>
        </tr>

      </table>
          </form></td>
  </tr>
</table>
<%
if (action.equals("search")) {
	sql = "select id from form_table_project where 1=1 ";
	if (kind.equals("name"))
		sql += " and name like " + StrUtil.sqlstr("%" + what + "%");
	else
		sql += " and content like " + StrUtil.sqlstr("%" + what + "%");
	if (!beginDate.equals(""))
		sql += " and begin_Date>=" + StrUtil.sqlstr(beginDate);
	if (!endDate.equals(""))
		sql += " and end_Date<=" + StrUtil.sqlstr(endDate);
		
	if (!prj_type.equals("all") && !prj_type.equals(""))
		sql += " and prj_type=" + StrUtil.sqlstr(prj_type);

	if (status!=-1) {
		sql += " and status=" + status;
	}
	
	sql += " and unit_code=" + StrUtil.sqlstr(privilege.getUserUnitCode(request));
						
	sql += " order by " + orderBy + " " + sort;
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
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

// String listField = StrUtil.getNullStr(msd.getString("list_field"));
String[] fields = msd.getColAry(false, "list_field");
// String listFieldWidth = StrUtil.getNullStr(msd.getString("list_field_width"));
String[] fieldsWidth = msd.getColAry(false, "list_field_width");

MacroCtlMgr mm = new MacroCtlMgr();
%>
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
  <tr align="center">
<%
int len = 0;
if (fields!=null)
	len = fields.length;
for (int i=0; i<len; i++) {
	String fieldName = fields[i];
	String title = "创建者";
	if (!fieldName.equals("cws_creator"))
		title = fd.getFieldTitle(fieldName);
%>
    <td class="tabStyle_1_title" width="<%=fieldsWidth[i]%>" style="cursor:hand" onClick="doSort('<%=fieldName%>')">
	<%=title%>
	<%if (orderBy.equals(fieldName)) {
		if (sort.equals("asc")) 
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		else
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
	}%>	
	</td>
<%}%>
    <td width="150px" class="tabStyle_1_title" title="按时间排序" style="cursor:hand">操作
      <%if (orderBy.equals("id")) {
		if (sort.equals("asc")) 
			out.print("<img src='../netdisk/images/arrow_up.gif' width=8px height=7px align=absMiddle>");
		else
			out.print("<img src='../netdisk/images/arrow_down.gif' width=8px height=7px align=absMiddle>");
	}%>
    </td>
  </tr>
  <%	
	  	int k = 0;
		UserMgr um = new UserMgr();
		while (ir!=null && ir.hasNext()) {
			fdao = (com.redmoon.oa.visual.FormDAO)ir.next();
			k++;
			long id = fdao.getId();
		%>
  <tr align="center" class="highlight">
<%
	for (int i=0; i<len; i++) {
		String fieldName = fields[i];
	%>	
		<td align="left">
		<%if (i==0) {%>
		<input type="checkbox" name="ids" value="<%=id%>" />
		<%}%>
        <%if (fieldName.equals("progress")) {%>
		  <div class="progressBar" style="height:30px">
              <div class="progressBarFore" style="width:<%=fdao.getFieldValue("progress")%>%;">
              </div>
              <div class="progressText">
              <%=fdao.getFieldValue("progress")%>%
              </div>
          </div>        
        <%} else if (!fieldName.equals("cws_creator")) {
			FormField ff = fd.getFormField(fieldName);
			if (ff==null) {
				out.print(fieldName + " 已不存在！");
			}
			else {
				if (ff.getType().equals(FormField.TYPE_MACRO)) {
					MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
					if (mu != null) {
						out.print(mu.getIFormMacroCtl().converToHtml(request, ff, fdao.getFieldValue(fieldName)));
					}
				}
				else {
					if (fieldName.equals("name")) {
					%>
                    <a href="javascript:;" onClick="addTab('<%=fdao.getFieldValue("name")%>', '<%=request.getContextPath()%>/project/project_show.jsp?parentId=<%=id%>&projectId=<%=id%>&formCode=project')"><%=fdao.getFieldValue(fieldName)%></a>
					<%}else{%>
					<%=fdao.getFieldValue(fieldName)%>
                    <%}%>
				<%}
			}%>
		<%}else{
			String realName = "";
			if (fdao.getCreator()!=null) {
			UserDb user = um.getUserDb(fdao.getCreator());
			if (user!=null)
				realName = user.getRealName();
			}
		%>
		<%=realName%>
		<%}%>
		</td>
	<%}%>
	<td>
		<a href="javascript:;" onClick="addTab('<%=fdao.getFieldValue("name")%>', '<%=request.getContextPath()%>/project/project_show.jsp?parentId=<%=id%>&projectId=<%=id%>&formCode=project')">查看</a>&nbsp;&nbsp;
<%if (mpd.canUserManage(privilege.getUser(request))) {%>
		<a href="javascript:;" onClick="addTab('项目编辑', '<%=request.getContextPath()%>/visual/project_edit.jsp?parentId=<%=id%>&id=<%=id%>&formCode=<%=formCode%>')">编辑</a>&nbsp;&nbsp;&nbsp;<a onClick="jConfirm('您确定要删除么','提示',function(r){if(!r){return;}else{window.location.href='project_list_m.jsp?action=del&op=<%=op%>&id=<%=id%>&formCode=<%=formCode%>&CPages=<%=curpage%>&orderBy=<%=orderBy%>&sort=<%=sort%>&<%=sqlUrlStr%>&kind=<%=kind%>&what=<%=StrUtil.UrlEncode(what)%>&beginDate=<%=beginDate%>&endDate=<%=endDate%>'}})" style="cursor:pointer">删除</a>
	<%}%></td>
  </tr>
  <%
  }
%>
</table>
<table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="percent98">
  <tr>
    <td width="50%" height="23" align="left"><input class="btn" onClick="selAllCheckBox('ids')" value="全选" type="button" />&nbsp;&nbsp;
<input class="btn" type="button" onClick="javascript:del()" value="删除" />
&nbsp;
<input name="button" class="btn" type="button" onClick="javascript:window.open('module_excel.jsp?<%=querystr%>')" value="导出" />
<%
String btnName = StrUtil.getNullStr(msd.getString("btn_name"));
String[] btnNames = StrUtil.split(btnName, ",");
String btnScript = StrUtil.getNullStr(msd.getString("btn_script"));
String[] btnScripts = StrUtil.split(btnScript, ",");
len = 0;
if (btnNames!=null) {
	len = btnNames.length;
	for (int i=0; i<len; i++) {
	%>
	&nbsp;&nbsp;<input type="button" class="btn" value="<%=btnNames[i]%>" onClick="<%=StrUtil.HtmlEncode(btnScripts[i])%>" />
	<%
	}
}
%>	  
	  </td>
    <td width="50%" align="right"><%
		out.print(paginator.getCurPageBlock("project_list_m.jsp?"+querystr));
	%></td>
  </tr>
</table>
<script>
$(function(){
	$('#beginDate').datetimepicker({
		lang:'ch',
		datepicker:true,
		timepicker:false,
		format:'Y-m-d'
	});
    $('#endDate').datetimepicker({
		lang:'ch',
		datepicker:true,
		timepicker:false,
		format:'Y-m-d'
	});
})
</script>
</body>
</html>
