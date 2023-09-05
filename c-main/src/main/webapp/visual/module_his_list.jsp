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
<%@ page import="org.json.*"%>
<%@ page import="com.redmoon.oa.util.RequestUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "read")) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(StrUtil.Alert_Back("表单不存在！"));
	return;
}

ModulePrivDb mpd = new ModulePrivDb(formCode);
if (!mpd.canUserManage(privilege.getUser(request))) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");

long fdaoId = ParamUtil.getLong(request, "fdaoId", -1);
if (fdaoId==-1) {
	// out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "err_id")));
	// return;
}

String orderBy = ParamUtil.get(request, "orderBy");
if ("".equals(orderBy)) {
	orderBy = "id";
}
String sort = ParamUtil.get(request, "sort");
if ("".equals(sort)) {
	sort = "desc";
}

String querystr = "";

int pagesize = ParamUtil.getInt(request, "pageSize", 20);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

String userName = ParamUtil.get(request, "userName");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
int logType = ParamUtil.getInt(request, "logType", -1);
java.util.Date beginDate=null, endDate=null;

if (!strBeginDate.equals("")) {
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
}

if (!strEndDate.equals("")) {
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	strEndDate = DateUtil.format(DateUtil.addDate(endDate, 1), "yyyy-MM-dd");
}

String sql = "select id from " + FormDb.getTableNameForLog(formCode);

if (op.equals("search")) {
	sql = "select id from " + FormDb.getTableNameForLog(formCode) + " where 1=1";
	
	if (fdaoId!=-1) {
		sql += " and cws_log_id=" + fdaoId;
	}
	if (logType!=-1) {
		sql += " and cws_log_type=" + logType;
	}
	if (!userName.equals("")) {
		sql += " and (cws_log_user like " + StrUtil.sqlstr("%" + userName + "%") + " or cws_log_user in (select name from users where realname like " + StrUtil.sqlstr("%" + userName + "%") + "))";
	}
	if (beginDate!=null) {
		sql += " and cws_log_date >=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and cws_log_date <" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
}

sql += " order by " + orderBy + " " + sort;
// out.print(sql);

querystr = "op=" + op + "&formCode=" + formCode + "&orderBy=" + orderBy + "&sort=" + sort + "&fdaoId=" + fdaoId + "&userName=" + StrUtil.UrlEncode(userName) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate + "&logType=" + logType;
%>
<!doctype html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title><%=fd.getName()%>列表</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.search-form input,select {
			vertical-align:middle;
		}
		.search-form input:not([type="radio"]):not([type="button"]):not([type="checkbox"]) {
			width: 80px;
			line-height: 20px; /*否则输入框的文字会偏下*/
		}
	</style>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui-1.10.4.css"/>
	<script src="../js/jquery-ui/jquery-ui-1.10.4.min.js"></script>
	<script type="text/javascript" src="../js/flexigrid.js"></script>

	<script src="../js/jquery.bgiframe.js"></script>

	<script src="<%=request.getContextPath()%>/inc/flow_dispose_js.jsp"></script>

	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>

	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link href="../js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen"/>
	<script type="text/javascript" src="../js/jquery-showLoading/jquery.showLoading.js"></script>

	<script src="../js/BootstrapMenu.min.js"></script>
</head>
<body>
<%
String action = ParamUtil.get(request, "action");
if (action.equals("del")) {
	if (!mpd.canUserManage(privilege.getUser(request))) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}

	com.redmoon.oa.visual.FormDAOLog fdaoLogDel = new com.redmoon.oa.visual.FormDAOLog(fd);
	long id = ParamUtil.getLong(request, "id");
	fdaoLogDel = fdaoLogDel.getFormDAOLog(id);
	if (fdaoLogDel.del()) {
		out.print(StrUtil.jAlert_Redirect("删除成功！", "提示", "module_his_list.jsp?" + querystr + "&CPages=" + curpage));
	} else {
		out.print(StrUtil.jAlert_Back("操作失败!", "提示"));
	}
	return;
}
com.redmoon.oa.visual.FormDAOLog fdaoLog = new com.redmoon.oa.visual.FormDAOLog(fd);

ListResult lr = fdaoLog.listResult(sql, curpage, pagesize);
long total = lr.getTotal();
Vector v = lr.getResult();
Iterator ir = null;
if (v!=null) {
	ir = v.iterator();
}
	
FormDAOLog fdaoLogFirst = null;
if (fdaoId!=-1 && v.size()>0) {
	fdaoLogFirst = (FormDAOLog)v.elementAt(v.size()-1);
}
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
		
MacroCtlMgr mm = new MacroCtlMgr();
%>
<table id="searchTable" width="98%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="left">
    <form id="searchForm" class="search-form" action="module_his_list.jsp">&nbsp;
    	类型&nbsp;
        <select id="logType" name="logType">
        <option value="-1">不限</option>
        <option value="<%=FormDAOLog.LOG_TYPE_CREATE%>">创建</option>
        <option value="<%=FormDAOLog.LOG_TYPE_EDIT%>">修改</option>
        <option value="<%=FormDAOLog.LOG_TYPE_DEL%>">删除</option>
        </select>
    	用户&nbsp;
    	<input id="userName" name="userName" size="10" value="<%=userName%>" />
        ID&nbsp;
        <input id="fdaoId" name="fdaoId" size="5" value="<%=fdaoId==-1?"":fdaoId%>" />
        开始时间&nbsp;
        <input id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        结束时间&nbsp;
        <input id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
      	<input type="hidden" name="op" value="search" />
        <input type="hidden" name="formCode" value="<%=formCode%>" />
        <input type="hidden" name="fdaoId" value="<%=fdaoId%>" />
        <input class="tSearch" name="submit" type="submit" value="搜索">
        </form>
    </td>
  </tr>
</table>
<table id="grid" border="0" cellpadding="2" cellspacing="0">
  <thead>
  <tr>
    <th width="150" style="cursor:hand">时间</th>
    <th width="60" style="cursor:hand">用户</th>
    <th width="40" style="cursor:hand">类型</th>
    <th width="40" style="cursor:hand">原始ID</th>
<%
Vector fields = fd.getFields();
Iterator irff = fields.iterator();
while (irff.hasNext()) {
	FormField ff = (FormField)irff.next();
%>
    <th width="105" style="cursor:hand" abbr="<%=ff.getName()%>">
	<%=ff.getTitle()%>	
	</th>
<%}%>
    <th width="130" style="cursor:hand">操作</th>
  </tr>
  </thead>
  <tbody>
  <%	
  int k = 0;
  UserMgr um = new UserMgr();
  while (ir!=null && ir.hasNext()) {
	  fdaoLog = (com.redmoon.oa.visual.FormDAOLog)ir.next();
	
	  // 置SQL宏控件中需要用到的fdao
	  RequestUtil.setFormDAO(request, fdaoLog);
	  
	  k++;
	  long id = fdaoLog.getId();
  %>
  <tr align="center" id="<%=id%>">
  		<td><%=DateUtil.format(fdaoLog.getLogDate(), "yyyy-MM-dd HH:mm:ss")%>
        <td><%=um.getUserDb(fdaoLog.getLogUser()).getRealName()%></td>
        <td>
        <%if (fdaoLog.getLogType()==FormDAOLog.LOG_TYPE_CREATE) {%>
        创建
        <%}else if (fdaoLog.getLogType()==FormDAOLog.LOG_TYPE_EDIT) {%>
        修改
        <%}else{%>
        删除
        <%}%>
        </td>
        <td><a href="javascript:;" onclick="addTab('原始记录<%=fdaoLog.getLogId()%>', '<%=request.getContextPath()%>/visual/moduleShowPage.do?id=<%=fdaoLog.getLogId()%>&formCode=<%=StrUtil.UrlEncode(fdaoLog.getFormDb().getCode())%>')"><%=fdaoLog.getLogId()%></a></td>
<%
	irff = fields.iterator();
	while (irff.hasNext()) {
		FormField ff = (FormField)irff.next();
		String fieldName = ff.getName();
		String val = "";
		String valFirst = "";
		boolean isSame = true;
%>
		<%if (!ff.getName().equals("cws_creator")) {
			if (ff.getType().equals(FormField.TYPE_MACRO)) {
				MacroCtlUnit mu = mm.getMacroCtlUnit(ff.getMacroType());
				if (mu != null) {
					val = StrUtil.getNullStr(mu.getIFormMacroCtl().converToHtml(request, ff, fdaoLog.getFieldValue(fieldName)));
					if (fdaoId!=-1) {
						valFirst = mu.getIFormMacroCtl().converToHtml(request, ff, fdaoLogFirst.getFieldValue(fieldName));
					}
				}
			}
			else {		
				val = StrUtil.getNullStr(fdaoLog.getFieldValue(fieldName));
				if (fdaoId!=-1) {
					valFirst = fdaoLogFirst.getFieldValue(fieldName);
				}
			}
			if (fdaoId!=-1) {
				isSame = val.equals(valFirst);
			}
		}else{
			if (fdaoLog.getCreator()!=null) {
				UserDb user = um.getUserDb(fdaoLog.getCreator());
				if (user!=null)
					val = user.getRealName();
			}
		}%>
        <td align="left" style="background-color:<%=(!isSame)?"#FFFF66":""%>">
        <%=val%>
        </td>
	<%}%>
	<td><%if (privilege.isUserPrivValid(request, "admin")) {%>
    	&nbsp;&nbsp;<a onclick="if (!confirm('您确定要删除么？')) event.returnValue=false;" href="<%=request.getContextPath()%>/visual/module_his_list.jsp?<%=querystr%>&action=del&op=<%=op%>&id=<%=id%>&CPages=<%=curpage%>">删除</a>
		<%if (mpd.canUserManage(privilege.getUser(request)) && fdaoLog.getLogType() == FormDAOLog.LOG_TYPE_DEL) {%>
		<a href="javascript:;" onclick="restore('<%=fdaoLog.getId()%>')">恢复</a>
		<%}%>
    <%}%></td>
  </tr>
  <%
  }
%>
  </tbody>
</table>
</body>
<script>
	function restore(id) {
		jConfirm('您确定要恢复么？', '提示', function(r) {
			if (!r) {
				return;
			}
			
			$.ajax({
				type: "post",
				url: "<%=request.getContextPath()%>/visual/module_log_list_old.jsp",
				data: {
					code: '<%=formCode%>',
					formCode: '<%=formCode%>',
					action: 'restore',
					id: id
				},
				dataType: "html",
				beforeSend: function(XMLHttpRequest){
					$("body").eq(0).showLoading();
				},
				success: function(data, status){
					data = $.parseJSON(data);
					jAlert(data.msg, "提示");
					if (data.ret=="1") {
						$('tr[id=' + id + ']').remove();
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
		});
	}
	
function initCalendar() {
	$('#beginDate').datetimepicker({
     	lang:'ch',
     	timepicker:false,
     	format:'Y-m-d',
     	formatDate:'Y/m/d'
	});
	$('#endDate').datetimepicker({
		lang:'ch',
		timepicker:false,
        format:'Y-m-d',
		formatDate:'Y/m/d'
	});
}

function doOnToolbarInited() {
	initCalendar();
}

var flex;

function changeSort(sortname, sortorder) {
	window.location.href = "<%=request.getContextPath()%>/visual/module_his_list.jsp?op=<%=op%>&formCode=<%=formCode%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "<%=request.getContextPath()%>/visual/module_his_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "<%=request.getContextPath()%>/visual/module_his_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}

flex = $("#grid").flexigrid
(
	{
	buttons : [
		{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
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
	
	// title: "通知",
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
	onToolbarInited: doOnToolbarInited,
	autoHeight: true,
	width: document.documentElement.clientWidth,
	height: document.documentElement.clientHeight - 84
	}
);

function action(com, grid) {

}

$(document).ready(function() {
	o("logType").value = "<%=logType%>";
});
</script>
</html>
