<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = StrUtil.getNullString(request.getParameter("op"));
String typeCode = ParamUtil.get(request, "typeCode");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "typeCode", typeCode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();

String title = ParamUtil.get(request, "title");
String flowStatus = ParamUtil.get(request, "flowStatus");
String by = ParamUtil.get(request, "by");

String myname = ParamUtil.get(request, "userName");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", myname, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

if(myname.equals("")){
	myname = privilege.getUser(request);
}
if (!myname.equals(privilege.getUser(request))) {
	if (!(privilege.canAdminUser(request, myname))) {
		out.print(StrUtil.Alert_Back(cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String fromDate = ParamUtil.get(request, "fromDate");
String toDate = ParamUtil.get(request, "toDate");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>我发起的流程列表</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script type="text/javascript" src="inc/common.js"></script>
<script type="text/javascript" src="js/jquery1.7.2.min.js"></script>
<script type="text/javascript" src="js/flexigrid.js"></script>

<link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
<script src="js/datepicker/jquery.datetimepicker.js"></script>

<script>
function onTypeCodeChange(obj) {
	if(obj.options[obj.selectedIndex].value=='not'){alert(obj.options[obj.selectedIndex].text+' <lt:Label res="res.flow.Flow" key="notBeSelect"/>'); return false;}
	window.location.href = "flow_list_mine.jsp?op=search&typeCode=" + obj.options[obj.selectedIndex].value + "&flowStatus=" + o("flowStatus").value;
}
</script>
</head>
<body>
<div class="tabs1Box">
<%@ include file="flow_inc_menu_top.jsp"%>
<script>
o("menu2").className="current";
</script>
</div>
<table id="searchTable" width="100%" border="0" cellspacing="0" cellpadding="0">
    <tr>
      <td align="center">
        <form name="formSearch" action="flow_list_mine.jsp" method="get">
         <lt:Label res="res.flow.Flow" key="type"/>
         <select id="typeCode" name="typeCode" onchange="onTypeCodeChange(this)">
            <option value=""><lt:Label res="res.flow.Flow" key="limited"/></option>
        </select>
        <select id="by" name="by">
          <option value="title"><lt:Label res="res.flow.Flow" key="tit"/></option>
          <option value="flowId"><lt:Label res="res.flow.Flow" key="number"/></option>
        </select>
        <input name="title" value="<%=title%>" />
        <lt:Label res="res.flow.Flow" key="state"/>&nbsp;
        <select id="flowStatus" name="flowStatus">
          <option value="1000" selected="selected"><lt:Label res="res.flow.Flow" key="limited"/></option>
          <option value="<%=WorkflowDb.STATUS_NOT_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_NOT_STARTED)%></option>
          <option value="<%=WorkflowDb.STATUS_STARTED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_STARTED)%></option>
          <option value="<%=WorkflowDb.STATUS_FINISHED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_FINISHED)%></option>
          <option value="<%=WorkflowDb.STATUS_DISCARDED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_DISCARDED)%></option>
          <option value="<%=WorkflowDb.STATUS_REFUSED%>"><%=WorkflowDb.getStatusDesc(WorkflowDb.STATUS_REFUSED)%></option>
        </select>
        <lt:Label res="res.flow.Flow" key="startDate"/>
   	 	<input size="8" id="fromDate" name="fromDate" value="<%=fromDate%>" />
		-
    	<input size="8" id="toDate" name="toDate" value="<%=toDate%>" />        
        <input name="userName" value="<%=myname%>" type="hidden" />
        <input name="op" value="search" type="hidden" />
        <input name="submit" type=submit value='<lt:Label res="res.flow.Flow" key="search"/>' class="tSearch" />
    	</form>
      </td>
    </tr>
</table>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
if (strcurpage.equals(""))
	strcurpage = "1";
if (!StrUtil.isNumeric(strcurpage)) {
	String str = LocalUtil.LoadString(request,"res.flow.Flow","identifyIllegal");
	out.print(StrUtil.makeErrMsg(str));
	return;
}
int pagesize = ParamUtil.getInt(request, "pageSize", 20);
int curpage = Integer.parseInt(strcurpage);

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "mydate";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

WorkflowDb wf = new WorkflowDb();
String sql = "select id from flow where userName=" + StrUtil.sqlstr(myname) + " and status<>" + WorkflowDb.STATUS_NONE;
if (op.equals("search")) {
	if (!typeCode.equals("")) {
		sql += " and type_code=" + StrUtil.sqlstr(typeCode);
	}
	
	if (by.equals("title")) {
		if (!title.equals("")) {
		sql += " and title like " + StrUtil.sqlstr("%" + title + "%");
		}
	}
	else if (by.equals("flowId")) {
		if (!StrUtil.isNumeric(title)) {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","mustNumber");
			out.print(StrUtil.Alert_Back(str));
			return;
		}
		else {
			sql += " and id=" + title;
		}
	}	
	
	if (!fromDate.equals("")) {
		sql += " and mydate>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
	}
	if (!toDate.equals("")) {
		java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
		d = DateUtil.addDate(d, 1);
		String toDate2 = DateUtil.format(d, "yyyy-MM-dd");		
		sql += " and mydate<" + SQLFilter.getDateStr(toDate, "yyyy-MM-dd");
	}	

	if (flowStatus.equals("") || flowStatus.equals("1000")) {
		;
	}
	else {
		sql += " and status=" + flowStatus;
	}	
}

sql += " and status<>" + WorkflowDb.STATUS_DELETED;
sql += " order by " + orderBy + " " + sort;
// out.print(sql);
int total = wf.getWorkflowCount(sql);

Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0)
{
	curpage = 1;
	totalpages = 1;
}

int start = (curpage-1)*pagesize;
int end = curpage*pagesize;

WorkflowBlockIterator ir = wf.getWorkflows(sql, WorkflowCacheMgr.FLOW_GROUP_KEY, start, end);
%>
<table id="grid">
  <thead>
    <tr>
      <th width="50" align="center" abbr="id">ID</th>    
      <%if (cfg.getBooleanProperty("isFlowLevelDisplay")) {%>            
      <th width="27" align="center" abbr="level"><lt:Label res="res.flow.Flow" key="rating"/></th> 
      <%}%>   
      <th width="500" abbr="title"><lt:Label res="res.flow.Flow" key="tit"/></th>
      <th width="90" abbr="begin_date"><lt:Label res="res.flow.Flow" key="startTime"/></th>
      <th width="81" abbr="checkUserName"><lt:Label res="res.flow.Flow" key="finallyApply"/></th>
      <th width="85" abbr="checkUserName"><lt:Label res="res.flow.Flow" key="currentHandle"/></th>
      <th width="90" abbr="status"><lt:Label res="res.flow.Flow" key="state"/></th>
      <th width="64" align="center"><lt:Label res="res.flow.Flow" key="manage"/></th>
    </tr>
  </thead>
  <tbody>
<%
WorkflowPredefineDb wpd = new WorkflowPredefineDb();
com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
MyActionDb mad = new MyActionDb();	
com.redmoon.oa.flow.Directory dir = new com.redmoon.oa.flow.Directory();
while (ir.hasNext()) {
 	WorkflowDb wfd = (WorkflowDb)ir.next(); 
	%>
    <tr>
      <td align="center"><%=wfd.getId()%></td>
      <%if (cfg.getBooleanProperty("isFlowLevelDisplay")) {%>            
      <td align="center"><%=WorkflowMgr.getLevelImg(request, wfd)%></td>
      <%}%>      
      <td align="left">      
		<%
	  	wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
        if (wpd.isLight()) {
		%>
            <a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "")%>', '<%=request.getContextPath()%>/flow_dispose_light_show.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>"><%=wfd.getTitle()%></a>      
	  	<%}else{%>
			<a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "")%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="<lt:Label res='res.flow.Flow' key='viewProcess'/>"><%=wfd.getTitle()%></a>
        <%}%>       
      </td>
      <td align="center"><%=DateUtil.format(wfd.getMydate(), "MM-dd HH:mm")%></td>
      <td><%
		sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " order by receive_date desc";
		java.util.Iterator ir2 = mad.listResult(sql, 1, 1).getResult().iterator();
	  	if (ir2.hasNext()) {
			mad = (MyActionDb)ir2.next();
		%>
        <%=um.getUserDb(mad.getUserName()).getRealName()%>
        <%
		}
	  %></td>
      <td><%
		// CHECK_STATUS_NOT为0
		sql = "select id from flow_my_action where flow_id=" + wfd.getId() + " and is_checked=" + MyActionDb.CHECK_STATUS_NOT + " order by receive_date desc";
		ir2 = mad.listResult(sql, 1, 1).getResult().iterator();
		int k = 0;
		while (ir2.hasNext()) {
			mad = (MyActionDb)ir2.next();
			if (k!=0) {
				out.print("、");
			}
		%>
        <%=um.getUserDb(mad.getUserName()).getRealName()%>
      <%
			k++;
		}
	  %></td>
      <td class="<%=WorkflowDb.getStatusClass(wfd.getStatus())%>"><%=wfd.getStatusDesc()%></td>
      <td align="center">
		<%
        if (wpd.isLight()) {
		%>
            <a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r","")%>', '<%=request.getContextPath()%>/flow_dispose_light_show.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>"><lt:Label res="res.flow.Flow" key="show"/></a>      
	  	<%}else{%>
			<a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r","")%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="<lt:Label res='res.flow.Flow' key='viewProcess'/>"><lt:Label res="res.flow.Flow" key="show"/></a>
        <%}%>      
      
      </td>
    </tr>
<%}%>
  </tbody>
</table>
<%
	String querystr = "op="+op+"&userName=" + StrUtil.UrlEncode(myname) + "&typeCode=" + typeCode + "&by=" + by + "&title=" + StrUtil.UrlEncode(title) + "&flowStatus=" + flowStatus + "&fromDate=" + fromDate + "&toDate=" + toDate;
    //out.print(paginator.getCurPageBlock("flow_list_mine.jsp?"+querystr));
%>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "flow_list_mine.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "flow_list_mine.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "flow_list_mine.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{name: '<lt:Label res="res.flow.Flow" key="condition"/>', bclass: '', type: 'include', id: 'searchTable'}
		],
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
	checkbox : false,
	page: <%=curpage%>,
	total: <%=total%>,
	useRp: true,
	rp: <%=pagesize%>,
	
	//title: "通知",
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
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);

function action(com, grid) {
}

$(function() {
	<%if (!"".equals(by)) {%>
	o("by").value = "<%=by%>";
	<%}%>
	<%if (!"".equals(typeCode)) {%>
	o("typeCode").value = "<%=typeCode%>";
	<%}%>
	<%if (!"".equals(flowStatus)) {%>
	o("flowStatus").value = "<%=flowStatus%>";
	<%}%>
	
	$('#fromDate').datetimepicker({
                	lang:'ch',
                	timepicker:false,
                	format:'Y-m-d'
    });
    $('#toDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d'
    });	
	
	$.ajax({
		type: "post",
		url: "flow/flow_do.jsp",
		data: {
			op: "getTree",
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		success: function(data, status){
			$("#typeCode").empty();				
			
			data = "<option value=''><lt:Label res="res.flow.Flow" key="limited"/></option>" + data;
									
			$("#typeCode").append(data);
			
			o("typeCode").value = "<%=typeCode%>";
		},
		complete: function(XMLHttpRequest, status){
		},
		error: function(XMLHttpRequest, textStatus){
			alert(XMLHttpRequest.responseText);
		}
	});	
	
});
</script>
</html>