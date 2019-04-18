<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.sale.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));

String preDate = ParamUtil.get(request, "preDate");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");

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
	}
	else {
		strBeginDate = "";
		strEndDate = "";
	}
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>分配记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script>
function openWin(url,width,height) {
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
</script>
</head>
<body>
<%@ include file="customer_distribute_inc_menu_top.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<%
String priv = "sales";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
String formCode = "sales_customer";
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式

if (op.equals("del")) {
	long id = ParamUtil.getLong(request, "id");
	CustomerDistributeDb cdd = new CustomerDistributeDb();
	cdd = (CustomerDistributeDb)cdd.getQObjectDb(new Long(id));
	if (cdd.del()) {
		out.print(StrUtil.jAlert_Redirect("操作成功！","提示", "customer_distribute_list.jsp"));
	}
	else
		out.print(StrUtil.jAlert_Back("操作失败！","提示"));
	return;
}

String userName = ParamUtil.get(request, "userName");
if (userName.equals(""))
	userName = privilege.getUser(request);

if (!userName.equals(privilege.getUser(request))) {
	// 检查是否有管理用户所在部门的权限
	DeptUserDb dud = new DeptUserDb();
	Vector vDept = dud.getDeptsOfUser(userName);
	Iterator vIr = vDept.iterator();
	boolean canAdminUser = false;
	while (vIr.hasNext()) {
		DeptDb dd = (DeptDb)vIr.next();
		if (com.redmoon.oa.pvg.Privilege.canUserAdminDept(request, dd.getCode())) {
			canAdminUser = true;
			break;
		}
	}
	if (!canAdminUser) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "您对该用户没有管理权限！"));
		return;
	}
}

String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String unitCode = privilege.getUserUnitCode(request);

String sql = "select d.id from oa_sales_customer_distr d, form_table_sales_customer c where d.customer_id=c.id and c.unit_code=" + StrUtil.sqlstr(unitCode);		

String customer = ParamUtil.get(request, "customer");
String searchUserName = ParamUtil.get(request, "searchUserName");

try {	
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "customer", customer, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "searchUserName", searchUserName, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "customer", customer, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "searchUserName", searchUserName, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}


String query = ParamUtil.get(request, "query");
if (!query.equals(""))
	sql = query;
else
	if (op.equals("search")) {
		if (!searchUserName.equals("")) {
			sql = "select d.id from oa_sales_customer_distr d, form_table_sales_customer c, users u where d.customer_id=c.id and d.user_name=u.name and u.unit_code=" + StrUtil.sqlstr(unitCode) + " and u.realname like " + StrUtil.sqlstr("%" + searchUserName + "%");		
		}
		if (!customer.equals(""))
			sql += " and c.customer like " + StrUtil.sqlstr("%" + customer + "%");
			
		if (beginDate!=null) {
			sql += " and d.create_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
		}
		if (endDate!=null) {
			sql += " and d.create_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
		}
			
	}
	
	querystr = "op=" + op + "&customer=" + StrUtil.UrlEncode(customer) + "&searchUserName=" + StrUtil.UrlEncode(searchUserName) + "&query=" + StrUtil.UrlEncode(sql) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate;

	sql += " order by d.create_date desc";
	// out.print(sql);

		int pagesize = ParamUtil.getInt(request, "pagesize", 10);
		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
			
		FormDAO fdao = new FormDAO();
		
		CustomerDistributeDb cdd = new CustomerDistributeDb();
		
		ListResult lr = cdd.listResult(sql, curpage, pagesize);
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
%>

  <table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td align="center">
        <form id="form2" name="form2" action="?op=search" method="post">
        &nbsp;客户
          <input type="text" name="customer" size="10" value="<%=customer%>" />
          业务员
          <input type="text" name="searchUserName" size="10" value="<%=searchUserName%>" />
          分配日期
<select id="preDate" name="preDate" onchange="if (this.value=='*') o('dateSection').style.display=''; else o('dateSection').style.display='none'">
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
<input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
至
<input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
</span>           
          &nbsp;<input class="tSearch" type="submit" value="查  询" name="submit" />
          <input type="hidden" name="action" value="<%=action%>" />
          </form>
          </td>
      </tr>
    </tbody>
  </table>

<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr align="center">
    <th width="200">客户名称</th>
    <th width="120">电话</th>
    <th width="120">业务员</th>
    <th width="120">类别</th>
    <th width="120">分配者</th>
    <th width="120">时间</th>
    <th width="100">操作</th>
  </tr>
  </thead>
  <%	
	  	int i = 0;
		String selCode = "khlb";
		SelectMgr sm = new SelectMgr();
		UserMgr um = new UserMgr();			
		while (ir.hasNext()) {
			cdd = (CustomerDistributeDb)ir.next();
			i++;
			long customerId = cdd.getLong("customer_id");
			fdao = fdao.getFormDAO(customerId, fd);
		%>
  <tr align="center">
    <td width="25%" align="left"><a href="customer_show.jsp?id=<%=customerId%>&amp;action=<%=action%>&formCode=<%=formCode%>" target="_blank"><%=fdao.getFieldValue("customer")%></a></td>
    <td width="12%" align="left"><a href="customer_show.jsp?id=<%=customerId%>&amp;action=<%=action%>&formCode=<%=formCode%>" target="_blank"><%=fdao.getFieldValue("tel")%></a></td>
    <td width="13%" align="left">
	<a href="customer_distribute_list.jsp?op=search&searchUserName=<%=StrUtil.UrlEncode(cdd.getString("user_name"))%>">
	<%
	UserDb user = um.getUserDb(cdd.getString("user_name"));
	out.print(user.getRealName());
	%>
	</a></td>
    <td width="13%" align="left">
	<%
			String optName = "";
			SelectDb sd = sm.getSelect(selCode);
			if (sd.getType() == SelectDb.TYPE_LIST) {
				SelectOptionDb sod = new SelectOptionDb();
				optName = sod.getOptionName(selCode, fdao.getFieldValue("kind"));
			} else {
				TreeSelectDb tsd = new TreeSelectDb();
				tsd = tsd.getTreeSelectDb(fdao.getFieldValue("kind"));
				optName = tsd.getName();
			}
			out.print(optName);	
	%>	</td>
    <td width="13%" align="left">
	<%
	user = um.getUserDb(cdd.getString("distributer"));
	out.print(user.getRealName());
	%>	</td>
    <td width="14%" align="left"><%=DateUtil.format(cdd.getDate("create_date"), "yy-MM-dd HH:mm")%></td>
    <td width="10%" align="center"><a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{ window.location.href='customer_distribute_list.jsp?op=del&amp;id=<%=cdd.getLong("id")%>'}})" style="cursor:pointer">删除</a></td>
  </tr>
  <%
		}
%>
</table>
<%
			//out.print(paginator.getCurPageBlock("?action=" + action + "&" +querystr));
	%>
<script>
	function initCalendar() {
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
	}
	function doOnToolbarInited() {
		initCalendar();
	}
	$(document).ready(function() {
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
		onToolbarInited: doOnToolbarInited,
		autoHeight: true,
		width: document.documentElement.clientWidth,
		height: document.documentElement.clientHeight - 84
		}
	);
});

function changeSort(sortname, sortorder) {
	window.location.href = "customer_distribute_list.jsp?<%=querystr%>&pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder;
}

function changePage(newp) {
	if (newp)
		window.location.href = "customer_distribute_list.jsp?<%=querystr%>&CPages=" + newp + "&pagesize=" + flex.getOptions().rp;
}

function rpChange(pagesize) {
	window.location.href = "customer_distribute_list.jsp?<%=querystr%>&CPages=<%=curpage%>&pagesize=" + pagesize;
}

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>
