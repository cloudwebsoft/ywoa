<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>日程列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/jquery.toaster.js"></script>
</head>
<body>
<%@ include file="plan_inc_menu_top.jsp"%>
<%
String userName = ParamUtil.get(request, "userName");
if (userName.equals("")) {
	userName = privilege.getUser(request);
}

if (!userName.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, userName))) {
		out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}
}
String mode = ParamUtil.get(request, "mode");
String menuItem = mode.equals("iMake") ? "menu5" : "menu3";
%>
<script>
o("<%=menuItem%>").className="current";
</script>
<div class="spacerH"></div>
<%
if (!privilege.isUserLogin(request)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();		
String action = ParamUtil.get(request, "action");
String what = ParamUtil.get(request, "what");

String op = ParamUtil.get(request, "op");
String preDate = ParamUtil.get(request, "preDate");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
int isNotepaper = ParamUtil.getInt(request, "isNotepaper", -1);
int isClosed = ParamUtil.getInt(request, "isClosed", -1);
int actionType = ParamUtil.getInt(request, "actionType", -1);

if (op.equals("setClosed")) {
	PlanMgr pm = new PlanMgr();
	try {
		pm.setClosedBatch(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "plan_list.jsp?CPages=" + curpage + "&mode=" + StrUtil.UrlEncode(mode) + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&CPages=" + curpage + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&isClosed=" + isClosed + "&isNotepaper=" + isNotepaper + "&actionType=" + actionType));

	return;
}
else if (op.equals("setNotClosed")) {
	PlanMgr pm = new PlanMgr();
	try {
		pm.setNotClosedBatch(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "plan_list.jsp?CPages=" + curpage + "&mode=" + StrUtil.UrlEncode(mode) + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&CPages=" + curpage + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&isClosed=" + isClosed + "&isNotepaper=" + isNotepaper + "&actionType=" + actionType));
	return;
}
else if (op.equals("setNotepaper")) {
	PlanMgr pm = new PlanMgr();
	try {
		pm.setNotepaperBatch(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "plan_list.jsp?CPages=" + curpage + "&mode=" + StrUtil.UrlEncode(mode) + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&CPages=" + curpage + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&isClosed=" + isClosed + "&isNotepaper=" + isNotepaper + "&actionType=" + actionType));
	return;
}
else if (op.equals("setNotNotepaper")) {
	PlanMgr pm = new PlanMgr();
	try {
		pm.setNotNotepaperBatch(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "plan_list.jsp?CPages=" + curpage + "&mode=" + StrUtil.UrlEncode(mode) + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&CPages=" + curpage + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&isClosed=" + isClosed + "&isNotepaper=" + isNotepaper + "&actionType=" + actionType));
	return;
}
else if (op.equals("setShared")) {
	PlanMgr pm = new PlanMgr();
	try {
		pm.setSharedBatch(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "plan_list.jsp?CPages=" + curpage + "&mode=" + StrUtil.UrlEncode(mode) + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&CPages=" + curpage + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&isClosed=" + isClosed + "&isNotepaper=" + isNotepaper + "&actionType=" + actionType));
	return;
}
else if (op.equals("setNotShared")) {
	PlanMgr pm = new PlanMgr();
	try {
		pm.setNotSharedBatch(request);
	}
	catch (ErrMsgException e) {
		out.print(StrUtil.jAlert_Back(e.getMessage(),"提示"));
	}
	out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "plan_list.jsp?CPages=" + curpage + "&mode=" + StrUtil.UrlEncode(mode) + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&CPages=" + curpage + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&isClosed=" + isClosed + "&isNotepaper=" + isNotepaper + "&actionType=" + actionType));
	return;
}

java.util.Date beginDate = null;
java.util.Date endDate = null;

if (!preDate.equals("") && !preDate.equals("*")) {
	String[] ary = StrUtil.split(preDate, "\\|");
	strBeginDate = ary[0];
	strEndDate = ary[1];
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
}
else {
	if (preDate.equals("*")) {
		beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
		if (endDate!=null)
			endDate = DateUtil.addDate(endDate, 1);
	}
	else {
		strBeginDate = "";
		strEndDate = "";
	}
}

String sql;
String myname = privilege.getUser(request);

sql = "select id from user_plan where username=" + StrUtil.sqlstr(userName);
if (mode.equals("iMake")){
	sql = "select id from user_plan where maker=" + StrUtil.sqlstr(userName);
}

if (isNotepaper != -1) {
	sql += " and is_notepaper=" + isNotepaper;
}

if (isClosed != -1) {
	sql += " and is_closed=" + isClosed;
}

String y = ParamUtil.get(request, "year");
String m = ParamUtil.get(request, "month");
String d = ParamUtil.get(request, "day");
if (!y.equals("")) {
	sql += " and " + SQLFilter.year("myDate") + "=" + y + " and " + SQLFilter.month("myDate") + "=" + m + " and " + SQLFilter.day("myDate") + "=" + d;
}

if (action.equals("search")) {
	if (!what.equals("")) {
		sql += " and title like " + StrUtil.sqlstr("%" + what + "%");
	}
	if (beginDate!=null) {
		sql += " and myDate>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and myDate<" + SQLFilter.getDateStr(DateUtil.format(endDate, "yyyy-MM-dd"), "yyyy-MM-dd");
	}
	if (actionType!=-1) {
		if (actionType==0)
			sql += " and action_type=0";
		else
			sql += " and action_type<>0";
	}
}

sql += " order by myDate desc";

// out.print(sql);
	
PlanDb pd = new PlanDb();

ListResult lr = pd.listResult(sql, curpage, pagesize);
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
<form action="plan_list.jsp" method="get" name="formSearch" id="formSearch">
<table width="98%" border="0" cellpadding="0" cellspacing="0">
  <tr>
	<td align="center">
        <select id="preDate" name="preDate" onchange="if (this.value=='*') o('dateSection').style.display=''; else {o('dateSection').style.display='none';doSub();}">
        <option selected="selected" value="">不限</option>
        <%
        java.util.Date[] ary = DateUtil.getDateSectOfToday();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今天</option>
        <%
        ary = DateUtil.getDateSectOfYestoday();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">昨天</option>
        <%
        ary = DateUtil.getDateSectOfCurWeek();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本周</option>
        <%
        ary = DateUtil.getDateSectOfLastWeek();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上周</option>
        <%
        ary = DateUtil.getDateSectOfCurMonth();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本月</option>
        <%
        ary = DateUtil.getDateSectOfLastMonth();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">上月</option>
        <%
        ary = DateUtil.getDateSectOfQuarter();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">本季度</option>
        <%
        ary = DateUtil.getDateSectOfCurYear();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">今年</option>
        <%
        ary = DateUtil.getDateSectOfLastYear();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">去年</option>
        <%
        ary = DateUtil.getDateSectOfLastLastYear();
        %>
        <option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>">前年</option>
        <option value="*">自定义</option>
        </select>
		<script>
        o("preDate").value = "<%=preDate%>";
        </script>        
        <span id="dateSection" style="display:<%=preDate.equals("*")?"":"none"%>">
        从
        <input id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        至
        <input id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
        </span>
        状态&nbsp;&nbsp;
        <select id="isClosed" name="isClosed" onchange="doSub()">
        <option value="-1">不限</option>
        <option value="0">未完成</option>
        <option value="1">已完成</option>
        </select>
        是否便笺&nbsp;&nbsp;
        <select id="isNotepaper" name="isNotepaper" onchange="doSub()">
        <option value="-1">不限</option>
        <option value="1">是</option>
        <option value="0">否</option>
        </select>
        类型&nbsp;&nbsp;
        <select id="actionType" name="actionType" onchange="doSub()">
        <option value="-1">不限</option>
        <option value="0">非系统生成</option>
        <option value="1">系统生成</option>
        </select>        
        <script>
		o("isClosed").value = "<%=isClosed%>";
		o("isNotepaper").value = "<%=isNotepaper%>";
		o("actionType").value = "<%=actionType%>";
		</script>
		&nbsp;&nbsp;标题&nbsp;&nbsp;
	  	<input type="text" name="what" value="<%=what%>" />           
		<input name="action" value="search" type="hidden" />
		<input name="mode" value="<%=mode%>" type="hidden" />
		<input class="btn" name="search" type="submit" value="搜索" />
		<input name="userName" value="<%=userName%>" type="hidden" />	
	</td>
</tr>
</table>
<div style="height:5px"></div>
</form>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
  <tr>
    <td width="60%" height="26" align="left"><input type="button" class="btn" value="完成" onclick="setClosed()" />
&nbsp;&nbsp;
<input type="button" class="btn" value="未完成" onclick="setNotClosed()" />
&nbsp;&nbsp;
<input type="button" class="btn" value="置为便笺" onclick="setNotepaper()" />
&nbsp;&nbsp;
<input type="button" class="btn" value="取消便笺" onclick="setNotNotepaper()" />
<%if (privilege.isUserPrivValid(request, "plan.share")) {%>
&nbsp;&nbsp;
<input type="button" class="btn" value="共享" onclick="setShared()" />
&nbsp;&nbsp;
<input type="button" class="btn" value="取消共享" onclick="setNotShared()" />
<%}%>
&nbsp;&nbsp;
<input type="button" class="btn" value="删除" onclick="delBatch()" /></td>
    <td align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<div style="height:5px"></div>
<table id="mainTable" width="98%" align="center" class="tabStyle_1 percent98">
  <thead>
  <tr>
    <td width="3%" align="center" class="tabStyle_1_title"><input id="checkbox" name="checkbox" type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids')" /></td>
    <td width="4%" class="tabStyle_1_title">状态</td>
    <td width="22%" class="tabStyle_1_title">日期</td>
    <td width="49%" class="tabStyle_1_title">标题</td>
    <td width="10%" class="tabStyle_1_title">
    <%if (mode.equals("iMake")) {%>
    用户
    <%}else{%>
    制定者
    <%}%>
    </td>
    <td width="10%" class="tabStyle_1_title">操作</td>
  </tr>
  </thead>
<%	
		UserDb user = new UserDb();
	    int id;
		String title, mydate, sEndDate;
		while (ir!=null && ir.hasNext()) {
			pd = (PlanDb)ir.next();
			id = pd.getId();
			title = pd.getTitle();
			mydate = DateUtil.format(pd.getMyDate(), "yy-MM-dd HH:mm");
			sEndDate = DateUtil.format(pd.getEndDate(),"yy-MM-dd HH:mm");		
		%>
  <tr id="plan<%=id%>">
    <td align="center"><input type="checkbox" name="ids" value="<%=id%>" /></td>
    <td align="center"><%=pd.isClosed()?"<img src='../images/task_complete.png' style='width:16px'>":"<img src='../images/task_ongoing.png' style='width:16px'>"%></td>
    <td align="center"><%=mydate%>&nbsp;&nbsp;|&nbsp;&nbsp;<%=sEndDate%></td>
    <td>
	<%=pd.isNotepaper()?"<img title='便笺' src='../images/note.png'>":""%>
	<%=pd.isShared()?"<img title='共享' src='../js/colorsticker/images/share.png'>":""%>
    <a href="javascript:;" onclick="addTab('<%=title%>', '<%=request.getContextPath()%>/plan/plan_show.jsp?id=<%=id%>&menuItem=<%=menuItem %>')">
	<%=title%></a></td>
    <td align="center">
    <%
	if (mode.equals("iMake")) {
		out.print(user.getUserDb(pd.getUserName()).getRealName());
	}
	else {
		if (!pd.getMaker().equals("")) {
			out.print(user.getUserDb(pd.getMaker()).getRealName());
		}
	}
	%>
    </td>
    <td align="center"><a href="javascript:;" onclick="addTab('编辑日程', 'plan/plan_edit.jsp?id=<%=id%>&menuItem=<%=menuItem %>')">编辑</a>&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:;" onclick="isDel(<%=id %>)" style="cursor:pointer">删除</a></td>
  </tr>
<%}%>
</table>
<table width="98%" class="percent98" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="60%" height="23" align="left">
    </td>
    <td width="48%" align="right"><%
		String querystr = "mode=" + mode + "&userName=" + StrUtil.UrlEncode(userName) + "&action=" + action + "&what=" + StrUtil.UrlEncode(what) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&isNotepaper=" + isNotepaper + "&isClosed=" + isClosed + "&actionType=" + actionType;
		out.print(paginator.getCurPageBlock("plan_list.jsp?"+querystr));
		%></td>
  </tr>
</table>
<form id="form1" name="form1" action="">
<input name="op" type="hidden" />
<input name="ids" type="hidden" />
<input name="CPages" value="<%=curpage%>" type="hidden" />
<input name="mode" value="<%=mode%>" type="hidden" />
<input name="isClosed" value="<%=isClosed%>" type="hidden" />
<input name="isNotepaper" value="<%=isNotepaper%>" type="hidden" />
<input name="userName" value="<%=userName%>" type="hidden" />
<input name="action" value="<%=action%>" type="hidden" />
<input name="actionType" value="<%=actionType%>" type="hidden" />
</form>
<br />
</body>
<script>
if(<%=mode.equals("iMake")%>){
	var form12 = document.getElementById("formSearch");
	//form12.submit();
}
function setClosed() {
	var checkedboxs = getCheckboxValue("ids");

	if (checkedboxs==""){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要置为已完成么？","提示",function(r){
		if(!r){return;}
		else{
			o("form1").setAttribute("action", "plan_list.jsp");
		    form1.op.value = "setClosed"; 
			form1.ids.value = checkedboxs;
			form1.submit();
		}
	})
}
function isDel(id){
	jConfirm("确定要删除吗？","提示",function(r){
		if(!r){return;}
		$.ajax({
			type: "post",
			url: "../public/plan/delPlan.do",
			data: {
				id : id
			},
			dataType: "html",
			contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
			beforeSend: function(XMLHttpRequest) {
			},
			success: function(data, status) {
				data = $.parseJSON(data);
				if (data.ret=="1") {
					$('#plan' + id).remove();
				}
				$.toaster({
					"priority" : "info", 
					"message" : data.msg
				});
			},
			error: function(XMLHttpRequest, textStatus) {
				alert(XMLHttpRequest.responseText);
			}
		});

	})
}
function setNotClosed() {
	var checkedboxs = getCheckboxValue("ids");

	if (checkedboxs==""){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要置为未完成么？","提示",function(r){
		if(!r){return;}
		else{
			o("form1").setAttribute("action", "plan_list.jsp");
		    form1.op.value = "setNotClosed"; 
			form1.ids.value = checkedboxs;
			form1.submit();
		}
	})
}

function setNotepaper() {
	var checkedboxs = getCheckboxValue("ids");

	if (checkedboxs==""){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要置为便笺么？","提示",function(r){
		if(!r){return;}
		else{
			o("form1").setAttribute("action", "plan_list.jsp");
		    form1.op.value = "setNotepaper"; 
			form1.ids.value = checkedboxs;
			form1.submit();
		}
	})
}

function setNotNotepaper() {
	var checkedboxs = getCheckboxValue("ids");

	if (checkedboxs==""){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要取消便笺么？","提示",function(r){
		if(!r){return;}
		else{
			o("form1").setAttribute("action", "plan_list.jsp");
		    form1.op.value = "setNotNotepaper"; 
			form1.ids.value = checkedboxs;
			form1.submit();
		}
	})
}

function setShared() {
	var checkedboxs = getCheckboxValue("ids");
	if (checkedboxs==""){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要共享么？","提示",function(r){
		if(!r){return;}
		else{
			o("form1").setAttribute("action", "plan_list.jsp");
		    form1.op.value = "setShared"; 
			form1.ids.value = checkedboxs;
			form1.submit();
		}
	})
}

function setNotShared() {
	var checkedboxs = getCheckboxValue("ids");

	if (checkedboxs==""){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要取消共享么？","提示",function(r){
		if(!r){return;}
		else{
			o("form1").setAttribute("action", "plan_list.jsp");
		    form1.op.value = "setNotShared"; 
			form1.ids.value = checkedboxs;
			form1.submit();
		}
	})
}

function delBatch() {
	var checkedboxs = getCheckboxValue("ids");

	if (checkedboxs==""){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要删除么？","提示",function(r){
		if(!r){return;}
		$.ajax({
			type: "post",
			url: "../public/plan/delPlanBatch.do",
			data: {
				ids : checkedboxs
			},
			dataType: "html",
			contentType: "application/x-www-form-urlencoded; charset=iso8859-1",
			beforeSend: function(XMLHttpRequest) {
			},
			success: function(data, status) {
				data = $.parseJSON(data);
				if (data.ret=="1") {
					var ary = checkedboxs.split(",");
					for (var i in ary) {
						$('#plan' + ary[i]).remove();
					}
				}
				$.toaster({
					"priority" : "info", 
					"message" : data.msg
				});
			},
			error: function(XMLHttpRequest, textStatus) {
				alert(XMLHttpRequest.responseText);
			}
		});

	})
}

function doSub() {
	formSearch.submit();
}

$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
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
});
</script>
</html>