<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/i18nTag.tld" prefix="lt"%>
<jsp:useBean id="docmanager" scope="page" class="com.redmoon.oa.fileark.DocumentMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
 
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

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();

String op = ParamUtil.get(request, "op");
String by = ParamUtil.get(request, "by");
String what = ParamUtil.get(request, "what");
String typeCode = ParamUtil.get(request, "typeCode");

String fromDate = ParamUtil.get(request, "fromDate");
String toDate = ParamUtil.get(request, "toDate");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "m.check_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String sql = "select m.id from flow_my_action m, flow f where f.id=m.flow_id and f.status<>" + WorkflowDb.STATUS_NONE + " and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and (is_checked=0 or is_checked=1 or is_checked=" + MyActionDb.CHECK_STATUS_TRANSFER + " or is_checked=" + MyActionDb.CHECK_STATUS_RETURN + ")";
if (op.equals("search")) {
	if (by.equals("title")) {
		if (!typeCode.equals("")) {
			sql = "select m.id from flow f,flow_my_action m where f.id=m.flow_id and f.status<>" + WorkflowDb.STATUS_NONE + " and f.type_code=" + StrUtil.sqlstr(typeCode) + " and f.title like " + StrUtil.sqlstr("%" + what + "%") +  " and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and (m.is_checked=0 or m.is_checked=1 or m.is_checked=" + MyActionDb.CHECK_STATUS_TRANSFER + " or m.is_checked=" + MyActionDb.CHECK_STATUS_RETURN + ")";
		}
		else
			sql = "select m.id from flow f,flow_my_action m where f.id=m.flow_id and f.status<>" + WorkflowDb.STATUS_NONE + " and f.title like " + StrUtil.sqlstr("%" + what + "%") +  " and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and (m.is_checked=0 or m.is_checked=1 or m.is_checked=" + MyActionDb.CHECK_STATUS_TRANSFER + " or m.is_checked=" + MyActionDb.CHECK_STATUS_RETURN + ")";
	}
	if (by.equals("flowId")) {
		if (!StrUtil.isNumeric(what)) {
			String str = LocalUtil.LoadString(request,"res.flow.Flow","mustNumber");
			out.print(StrUtil.Alert_Back(str));
			return;
		}
		else {
			sql = "select m.id from flow f,flow_my_action m where f.id=m.flow_id and f.id=" + what + " and (m.user_name=" + StrUtil.sqlstr(myname) + " or m.proxy=" + StrUtil.sqlstr(myname) + ") and (m.is_checked=0 or m.is_checked=1 or m.is_checked=" + MyActionDb.CHECK_STATUS_TRANSFER + " or m.is_checked=" + MyActionDb.CHECK_STATUS_RETURN + ")";
		}
	}
	
	if (!fromDate.equals("")) {
		sql += " and m.receive_date>=" + SQLFilter.getDateStr(fromDate, "yyyy-MM-dd");
	}
	if (!toDate.equals("")) {
		java.util.Date d = DateUtil.parse(toDate, "yyyy-MM-dd");
		d = DateUtil.addDate(d, 1);
		String toDate2 = DateUtil.format(d, "yyyy-MM-dd");		
		sql += " and m.receive_date<" + SQLFilter.getDateStr(toDate2, "yyyy-MM-dd");
	}	
}
sql += " and f.status<>" + WorkflowDb.STATUS_DELETED;
sql += " order by " + orderBy + " " + sort;
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>待办流程列表</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<style>
		.search-form input, select {
			vertical-align: middle;
		}
	</style>
	<script type="text/javascript" src="inc/common.js"></script>
	<script src="js/jquery-1.9.1.min.js"></script>
	<script src="js/jquery-migrate-1.2.1.min.js"></script>
	<script type="text/javascript" src="js/flexigrid.js"></script>
	<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link rel="stylesheet" type="text/css" href="js/datepicker/jquery.datetimepicker.css"/>
	<script src="js/datepicker/jquery.datetimepicker.js"></script>
	<script>
		function onTypeCodeChange(obj) {
			if (obj.options[obj.selectedIndex].value == 'not') {
				alert(obj.options[obj.selectedIndex].text + ' <lt:Label res="res.flow.Flow" key="notBeSelect"/>');
				return false;
			}
			window.location.href = "flow_list_done.jsp?op=search&by=" + o("by").value + "&typeCode=" + obj.options[obj.selectedIndex].value;
		}
	</script>
</head>
<body>
<%@ include file="flow_inc_menu_top.jsp"%>
<script>
if (o("menu3"))
	o("menu3").className="current"; 
</script>
<table id="searchTable" width="80%" border="0" align="center" cellspacing="0" cellpadding="0">
    <tr>
      <td align="center">
  <form name="form1" class="search-form" action="flow_list_done.jsp?op=search" method=post>
      <lt:Label res="res.flow.Flow" key="type"/>
        <select id="typeCode" name="typeCode" onchange="onTypeCodeChange(this)">
          <option value=""><lt:Label res="res.flow.Flow" key="limited"/></option>
        </select>	
        &nbsp;
        <select id="by" name="by">
          <option value="title"><lt:Label res="res.flow.Flow" key="tit"/></option>
          <option value="flowId"><lt:Label res="res.flow.Flow" key="number"/></option>
        </select>
        <input name="what" value="<%=what%>" />
        <lt:Label res="res.flow.Flow" key="arrivalTime"/>
   	 	<input size="8" id="fromDate" name="fromDate" value="<%=fromDate%>" />
		-
    	<input size="8" id="toDate" name="toDate" value="<%=toDate%>" />

        <input name="userName" value="<%=myname%>" type="hidden" />
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
MyActionDb mad = new MyActionDb();
ListResult lr = mad.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Paginator paginator = new Paginator(request, total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}

int start = (curpage-1)*pagesize;
int end = curpage*pagesize;
%>
<table width="954" height="48" id="grid">
  <thead>
    <tr>
      <th width="50" align="center" abbr="id">ID</th>
      <%if (cfg.getBooleanProperty("isFlowLevelDisplay")) {%>                  
      <th width="26" align="center"><lt:Label res="res.flow.Flow" key="rating"/></th>
      <%}%>
      <th width="350"><lt:Label res="res.flow.Flow" key="tit"/></th>
      <th width="81"><lt:Label res="res.flow.Flow" key="organ"/></th>      
      <th width="90" abbr="receive_date"><lt:Label res="res.flow.Flow" key="arrivalTime"/></th>
      <th width="90" abbr="m.check_date"><lt:Label res="res.flow.Flow" key="handleTime"/></th>
      <th width="90" abbr="expire_date"><lt:Label res="res.flow.Flow" key="expirationDate"/></th>
      <th width="90" abbr="expire_date"><lt:Label res="res.flow.Flow" key="remainTime"/></th>
      <th width="56" abbr="performance"><lt:Label res="res.flow.Flow" key="achievements"/></th>
      <th width="65" align="center" abbr="action_status"><lt:Label res="res.flow.Flow" key="reachState"/></th>
      <th width="79" align="center"><lt:Label res="res.flow.Flow" key="operate"/></th>
    </tr>
  </thead>
  <tbody>
<%
WorkflowPredefineDb wpd = new WorkflowPredefineDb();
java.util.Iterator ir = lr.getResult().iterator();	
com.redmoon.oa.person.UserMgr um = new com.redmoon.oa.person.UserMgr();
Directory dir = new Directory();	
WorkflowDb wfd2 = new WorkflowDb();
int m = 0;
while (ir.hasNext()) {
 	mad = (MyActionDb)ir.next();
	WorkflowDb wfd = wfd2.getWorkflowDb((int)mad.getFlowId());	
	String userName = wfd.getUserName();
	String userRealName = "";
	if (userName!=null) {
		UserDb user = um.getUserDb(wfd.getUserName());
		userRealName = user.getRealName();
	}
	
  	Leaf ft = dir.getLeaf(wfd.getTypeCode());
	
	boolean isRecall = false;
	if (ft!=null) {
		WorkflowPredefineDb wfp = new WorkflowPredefineDb();
		wfp = wfp.getPredefineFlowOfFree(wfd.getTypeCode());
		isRecall = wfp.isRecall();
	}
	m++;
	%>
    <tr id=<%=m %>>
      <td align="center"><%=mad.getFlowId()%></td>
      <%if (cfg.getBooleanProperty("isFlowLevelDisplay")) {%>                  
      <td align="center"><%=WorkflowMgr.getLevelImg(request, wfd)%></td> 
      <%}%>     
      <td>
      <%
	  boolean isExpired = false;
	  java.util.Date chkDate = mad.getCheckDate();
	  if (chkDate==null)
		chkDate = new java.util.Date();
	  if (DateUtil.compare(chkDate, mad.getExpireDate())==1) {
		isExpired = true;
	  }
	  if (isExpired) {%>
		  <img src="images/flow/expired.png" align="absmiddle" alt="<lt:Label res='res.flow.Flow' key='timeOut'/>" />
	  <%}%>      
		<%
	  	wpd = wpd.getPredefineFlowOfFree(wfd.getTypeCode());
        if (wpd.isLight()) {
		%>
            <a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "")%>', '<%=request.getContextPath()%>/flow_dispose_light_show.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>"><%=wfd.getTitle()%></a>      
	  	<%}else{%>
			<a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r", "")%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="<lt:Label res='res.flow.Flow' key='viewProcess'/>"><%=wfd.getTitle()%></a>
        <%}%>      
	  </td>      
      <td align="left"><%=userRealName%></td>                 
      <td align="center"><%=DateUtil.format(mad.getReceiveDate(), "MM-dd HH:mm")%> </td>
      <td align="center"><%=DateUtil.format(mad.getCheckDate(), "MM-dd HH:mm")%> </td>
      <td align="center"><%=DateUtil.format(mad.getExpireDate(), "MM-dd HH:mm")%></td>
      <td align="center"><%
						String remainDateStr = "";
						if (mad.getExpireDate()!=null && DateUtil.compare(new java.util.Date(), mad.getExpireDate())==2) {
							int[] ary = DateUtil.dateDiffDHMS(mad.getExpireDate(), new java.util.Date());
							String str_day = LocalUtil.LoadString(request,"res.flow.Flow","day");
							String str_hour = LocalUtil.LoadString(request,"res.flow.Flow","h_hour");
							String str_minute = LocalUtil.LoadString(request,"res.flow.Flow","minute");
							remainDateStr = ary[0] + str_day + ary[1] + str_hour + ary[2] + str_minute;
							out.print(remainDateStr);
						}%></td>
      <td align="center"><%=NumberUtil.round(mad.getPerformance(), 2)%></td>
      <td align="center" class="<%=WorkflowActionDb.getStatusClass(mad.getActionStatus())%>"><%=WorkflowActionDb.getStatusName(mad.getActionStatus())%>	  </td>
      <td align="center">
		<%
        if (wpd.isLight()) {
		%>
            <a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r","")%>', '<%=request.getContextPath()%>/flow_dispose_light_show.jsp?flowId=<%=wfd.getId()%>')" title="<%=wfd.getTitle()%>"><lt:Label res="res.flow.Flow" key="show"/></a>      
	  	<%}else{%>
			<a href="javascript:;" onclick="addTab('<%=StrUtil.toHtml(wfd.getTitle()).replaceAll("\r","")%>', '<%=request.getContextPath()%>/flow_modify.jsp?flowId=<%=wfd.getId()%>')" title="<lt:Label res='res.flow.Flow' key='viewProcess'/>"><lt:Label res="res.flow.Flow" key="show"/></a>
        <%}%>      
		<%
        boolean isFree = ft.getType()!=Leaf.TYPE_LIST;
        if (isRecall && mad.canRecall(privilege.getUser(request))) {
          if (isFree) {%>
              &nbsp;&nbsp;<a href='javascript:;' onClick="recallFree(<%=m %>,<%=wfd.getId()%>,<%=mad.getId()%>)"><lt:Label res="res.flow.Flow" key="withdraw"/></a>
          <%}else{%>
              &nbsp;&nbsp;<a href='javascript:;' onClick="recall(<%=m %>,<%=wfd.getId()%>,<%=mad.getId()%>)"><lt:Label res="res.flow.Flow" key="withdraw"/></a>
          <%}
        }%>
      </td>
    </tr>
    <%}%>
  </tbody>
</table>
<%
	String querystr = "op=" + op + "&userName=" + StrUtil.UrlEncode(myname) + "&by=" + by + "&what=" + StrUtil.UrlEncode(what) + "&fromDate=" + fromDate + "&toDate=" + toDate;
    // out.print(paginator.getCurPageBlock("?"+querystr));
%>
</body>
<script>
var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "flow_list_done.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "flow_list_done.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "flow_list_done.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
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
		url: "flow/getTree.do",
		data: {
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		success: function(data, status){
			$("#typeCode").empty();				
			
			data = "<option value=''><lt:Label res='res.flow.Flow' key='limited'/></option>" + data;
									
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

function recall(id,flow_id,action_id){
	jConfirm('<lt:Label res="res.flow.Flow" key="toForcedWithdraw"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type:"get",
				url:"flow/recall.do",
				data:{"flow_id":flow_id,"action_id":action_id},
		 		success:function(data,status){
		 			$("#"+id).remove();
		 			data = $.parseJSON(data);
		 			
		 			jAlert(data.msg,'<lt:Label res="res.flow.Flow" key="prompt"/>');
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			//alert(XMLHttpRequest.responseText);
		 		}
			})
		}
	})
}

function recallFree(id,flow_id,action_id){
	jConfirm('<lt:Label res="res.flow.Flow" key="toForcedWithdraw"/>','<lt:Label res="res.flow.Flow" key="prompt"/>',function(r){
		if(!r){
			return;
		}else{
			$.ajax({
				type:"get",
				url:"flow/recall.do",
				data:{"flow_id":flow_id,"action_id":action_id},
		 		success:function(data,status){
		 			$("#"+id).remove();
		 			data = $.parseJSON(data);
		 			
		 			jAlert(data.msg,'<lt:Label res="res.flow.Flow" key="prompt"/>');
		 		},
		 		error:function(XMLHttpRequest, textStatus){
		 			//alert(XMLHttpRequest.responseText);
		 		}
			})
		}
	})
}

</script>
</html>