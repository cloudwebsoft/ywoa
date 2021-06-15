<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@page import="org.apache.struts2.components.Else"%>
<%@ include file="../inc/inc.jsp"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	if (!privilege.isUserPrivValid(request, "admin")) {
		out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<title>日志列表</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css"/>
	<link rel="stylesheet" href="<%=request.getContextPath()%>/js/bootstrap/css/bootstrap.min.css"/>
	<style>
		.search-form input, select {
			vertical-align: middle;
		}
	</style>
	<script src="../inc/common.js"></script>
	<script src="../js/jquery-1.9.1.min.js"></script>
	<script src="../js/jquery-migrate-1.2.1.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen"/>
	<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
	<script src="../js/datepicker/jquery.datetimepicker.js"></script>
	<script type="text/javascript" src="../js/flexigrid.js"></script>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css"/>
</head>
<body>
<%
	String userName = ParamUtil.get(request, "userName");
	String beginDate = ParamUtil.get(request, "beginDate");
	String endDate = ParamUtil.get(request, "endDate");
	String userAction = ParamUtil.get(request, "userAction");
	String logType = ParamUtil.get(request, "log_type");
	String dept = ParamUtil.get(request, "dept");
	int device = ParamUtil.getInt(request, "device", -1);

	int pageSize = ParamUtil.getInt(request, "pageSize", 20);
	Paginator paginator = new Paginator(request);
	int curpage = paginator.getCurPage();

	LogDb ld = new LogDb();
	String op = ParamUtil.get(request, "op");
	if (op.equals("del")) {
		int delid = ParamUtil.getInt(request, "id");
		LogDb ldb = ld.getLogDb(delid);
		if (ldb.del())
			out.print(StrUtil.jAlert_Redirect("操作成功","提示",
					"log_list.jsp?pageSize=" + pageSize + "&CPages=" + curpage));
		else
			out.print(StrUtil.jAlert_Back("操作失败","提示"));
		return;
	} else if (op.equals("delBatch")) {
		String[] ids = ParamUtil.getParameters(request, "ids");
		if (ids != null) {
			int len = ids.length;
			for (int i = 0; i < len; i++) {
				LogDb ldb = ld.getLogDb(StrUtil.toInt(ids[i]));
				ldb.del();
			}
			out.print(StrUtil.jAlert_Redirect("操作成功","提示", "log_list.jsp?pageSize=" + pageSize + "&CPages=" + curpage));
			return;
		}
	}
%>
<table id="searchTable" width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td align="center">
			<form name="formSearch" class="search-form" action="log_list.jsp" method="get">
				用户&nbsp;
				<input id="userName" name="userName" size="10" value="<%=userName%>">
				&nbsp;设备
				<select id="device" name="device">
					<option value="-1">不限</option>
					<option value="<%=LogDb.DEVICE_PC%>">电脑</option>
					<option value="<%=LogDb.DEVICE_MOBILE%>">手机</option>
				</select>
				<script>
					o("device").value = "<%=device%>";
				</script>
				&nbsp;开始时间
				<input id="beginDate" name="beginDate" size="10" value="<%=beginDate%>"/>
				结束时间
				<input id="endDate" name="endDate" size="10" value="<%=endDate%>"/>
				动作&nbsp;
				<input id="userAction" name="userAction" size="10" value="<%=userAction%>"/>
				<input name="pageSize" type="hidden" value="<%=pageSize%>"/>
				<input name="action" value="search" type="hidden"/>
				&nbsp;
				部门&nbsp;<select name="dept">
				<option value="">请选择</option>
				<%
					DeptMgr dm = new DeptMgr();
					DeptDb lf = dm.getDeptDb(DeptDb.ROOTCODE);
					DeptView dv = new DeptView(lf);
					dv.ShowDeptAsOptions(out, lf, lf.getLayer());
				%>
			</select>&nbsp;
				类别&nbsp;<select id="log_type" name="log_type">
				<option value="">请选择</option>
				<option value="0">登陆系统</option>
				<option value="1">退出登陆</option>
				<option value="2">操作</option>
				<option value="3">警告</option>
				<option value="4">出错</option>
				<option value="5">权限</option>
				<option value="<%=LogDb.TYPE_HACK%>">攻击</option>
			</select>&nbsp;
				<input type="submit" value="搜索" class="tSearch"/>
				<script>
					o("dept").value = "<%=dept%>";
					o("log_type").value = "<%=logType%>";
				</script>
			</form>
		</td>
	</tr>
</table>
<%
	String sql;
	String myname = privilege.getUser(request);
	sql = "select l.ID from log l where 1=1";

	String action = ParamUtil.get(request, "action");
	if (action.equals("search")) {
		if (!dept.equals("")) {
			sql = "select l.ID from log l, dept_user du where l.USER_NAME=du.user_name";
		}
		String cond = "";
		if (!userName.equals("")) {
			cond += " and l.user_name=" + StrUtil.sqlstr(userName);
		}
		if (!beginDate.equals("")) {
			cond += " and log_date>="
					+ StrUtil.sqlstr(DateUtil.toLongString(DateUtil
					.parse(beginDate, "yyyy-MM-dd")));
		}
		if (!endDate.equals("")) {
			cond += " and log_date<="
					+ StrUtil.sqlstr(DateUtil.toLongString(DateUtil
					.parse(endDate, "yyyy-MM-dd")));
		}
		if (!userAction.equals("")) {
			cond += " and l.action like "
					+ StrUtil.sqlstr("%" + userAction + "%");
		}

		if (!logType.equals("")) {
			cond += " and log_type =" + StrUtil.sqlstr(logType);
		}
		if (!dept.equals("")) {
			cond += " and du.dept_code = "
					+ StrUtil.sqlstr(dept);
		}
		if (device!=-1) {
			cond += " and device=" + device;
		}

		if (!cond.equals("")) {
			sql += " " + cond;
		}
	}

	sql += " order by log_date desc";

	// out.print(sql);

	ListResult lr = ld.listResult(sql, curpage, pageSize);
	long total = lr.getTotal();
	Vector v = lr.getResult();
	Iterator ir = null;
	if (v != null)
		ir = v.iterator();
	paginator.init(total, pageSize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages == 0) {
		curpage = 1;
		totalpages = 1;
	}
%>
<form name="form1" action="log_list.jsp?op=delBatch&pageSize=<%=pageSize%>&CPages=<%=curpage%>" method="post">
	<table width="93%" border="0" cellpadding="0" cellspacing="0" id="mainTable">
		<thead>
		<tr>
			<th width="50" align="center"><input type="checkbox" onclick="if (this.checked) selAllCheckBox('ids'); else deSelAllCheckBox('ids');" /></th>
			<th width="120" align="center" >日期</th>
			<th width="120" align="center" >用户</th>
			<th width="110" align="center" >部门</th>
			<th width="220" align="center" >动作</th>
			<th width="100" align="center" >IP</th>
			<th width="100" align="center" >设备</th>
			<th width="90" align="center" >类型</th>
			<th width="110" align="center" >操作</th>
		</tr>
		</thead>
		<%
			DeptUserDb dud = new DeptUserDb();
			UserMgr um = new UserMgr();
			while (ir.hasNext()) {
				ld = (LogDb) ir.next();
				ld = ld.getLogDb(ld.getId());
		%>
		<tr class="highlight">
			<td width="5%" align="center"><input type="checkbox" id="ids" name="ids" value="<%=ld.getId()%>"></td>
			<td width="12%" align="center"><%=DateUtil.format(ld.getDate(), "yy-MM-dd HH:mm")%></td>
			<td width="12%" align="center">
				<%
					UserDb ud = null;
					String realName = ld.getUserName();
					if (!"".equals(ld.getUserName())) {
						ud = um.getUserDb(ld.getUserName());
						if (ud.isLoaded()) {
							realName = ud.getRealName();
						}
				%>
				<a href="javascript:;" onclick="addTab('<%=realName%>', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>')"><%=realName%></a>
				<%}%>
			</td>
			<td width="11%">
				<%
					if (ud!=null) {
						Iterator ir2 = dud.getDeptsOfUser(ud.getName()).iterator();
						int k = 0;
						while (ir2.hasNext()) {
							DeptDb dd = (DeptDb) ir2.next();
							String deptName = "";
							if (!dd.getParentCode().equals(DeptDb.ROOTCODE)
									&& !dd.getCode().equals(DeptDb.ROOTCODE)) {
								deptName = dm.getDeptDb(dd.getParentCode()).getName()
										+ "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>"
										+ dd.getName();
							} else
								deptName = dd.getName();
							if (k == 0) {
								out.print("<a href='javascript:;' onClick=\"addTab('" + deptName + "', '" + request.getContextPath() + "/admin/dept_user.jsp?deptCode="
										+ StrUtil.UrlEncode(dd.getCode())
										+ "')\">" + deptName + "</a>");
							} else {
								out.print("，&nbsp;<a href='javascript:;' onClick=\"addTab('" + deptName + "', '" + request.getContextPath() + "/admin/dept_user.jsp?deptCode="
										+ StrUtil.UrlEncode(dd.getCode())
										+ "')\">" + deptName + "</a>");
							}
							k++;
						}
					}
				%>
			</td>
			<td width="22%"><%=StrUtil.toHtml(ld.getAction())%></td>
			<td width="9%" align="center"><%=ld.getIp()%></td>
			<td width="9%" align="center"><%
				if (ld.getDevice()==LogDb.DEVICE_PC) {
					out.print("电脑");
				}
				else {
					out.print("手机");
				}
			%></td>
			<td width="9%" align="center"><%=LogUtil.getTypeDesc(request, ld.getType())%></td>
			<td width="11%" align="center"><a onclick="jConfirm('您确定要删除么？','提示',function(r){if(!r){event.returnValue=false;}else{window.location.href='log_list.jsp?op=del&id=<%=ld.getId()%>&action=<%=action%>&userName=<%=StrUtil.UrlEncode(userName)%>&beginDate=<%=beginDate%>&endDate=<%=endDate%>&pageSize=<%=pageSize%>&CPages=<%=curpage%>&userAction=<%=StrUtil.UrlEncode(userAction)%>&dept=<%=StrUtil.UrlEncode(dept)%>&device=<%=device%>&log_type=<%=StrUtil.UrlEncode(logType)%>'}}) " style="cursor:pointer">删除</a></td>
		</tr>
		<%
			}
		%>
	</table>
	<input name="CPages" value="<%=curpage%>" type="hidden"/>
</form>
</body>
<%
	String querystr = "action=" + action
			+ "&userName=" + StrUtil.UrlEncode(userName)
			+ "&beginDate=" + beginDate + "&endDate=" + endDate
			+ "&userAction=" + StrUtil.UrlEncode(userAction)
			+ "&dept=" + StrUtil.UrlEncode(dept)
			+ "&device=" + device
			+ "&log_type=" + StrUtil.UrlEncode(logType);
	//out.print(paginator.getCurPageBlock("log_list.jsp?" + querystr));
%>
<script>

function openExcel() {
<%
com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
String sql3des = cn.js.fan.security.ThreeDesUtil.encrypt2hex(cfg.getKey(), sql);
%>	
	var sql = "<%=sql3des%>";
	window.open("log_excel.jsp?sql=" + sql); 
}

function del(){
    var checkedboxs = 0;
	var checkboxboxs = document.getElementsByName("ids");
	if (checkboxboxs!=null)
	{
		// 如果只有一个元素
		if (checkboxboxs.length==null) {
			if (checkboxboxs.checked){
			   checkedboxs = 1;
			}
		}
		for (i=0; i<checkboxboxs.length; i++)
		{
			if (checkboxboxs[i].checked){
			   checkedboxs = 1;
			}
		}
	}
	if (checkedboxs==0){
	    jAlert("请先选择记录！","提示");
		return;
	}
	jConfirm("您确定要删除吗？","提示",function(r){
		if(!r){return;}
		else{
			o("form1").submit();
		}
	})
}

$(document).ready( function() {
	$('#endDate').datetimepicker({
                	lang:'ch',
                	timepicker:false,
                	format:'Y-m-d'
     });
    $('#beginDate').datetimepicker({
                	lang:'ch',
                	timepicker:false,
                	format:'Y-m-d'
     });
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
	flex = $("#mainTable").flexigrid
	(
		{
		buttons : [
			{name: '导出', bclass: 'export', onpress : action},
			{name: '删除', bclass: 'delete', onpress : action},
			{separator: true},
			{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
			],
		/*
		searchitems : [
			{display: 'ISO', name : 'iso'},
			{display: 'Name', name : 'name', isdefault: true}
			],
		sortname: "iso",
		sortorder: "asc",
		*/
		url: false,
		usepager: true,
		checkbox: false,
		page: <%=curpage%>,
		total: <%=total%>,
		useRp: true,
		rp: <%=pageSize%>,
		
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
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	); 
});
function action(com, grid) {
	if (com=='导出')	{
		openExcel();
	}
	else if (com=='删除') {
		del();
	}
}
function changeSort(sortname, sortorder) {
	window.location.href = "log_list.jsp?<%=querystr%>&pageSize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "log_list.jsp?<%=querystr%>&CPages=" + newp + "&pageSize=" + flex.getOptions().rp;
}

function rpChange(pageSize) {
	window.location.href = "log_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pageSize=" + pageSize;
}

function onReload() {
	window.location.reload();
}
</script>
</html>
