<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));
String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "id"; // "find_date";
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";

String customer = ParamUtil.get(request, "customer");
String customerType = ParamUtil.get(request, "customer_type");
String find_dateFromDate = ParamUtil.get(request, "find_dateFromDate");
String find_dateToDate = ParamUtil.get(request, "find_dateToDate");

String enterType = ParamUtil.get(request, "enterType");
String tel = ParamUtil.get(request, "tel");
String category = ParamUtil.get(request, "category");

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式

String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String formCode = "sales_customer";
if (action.equals("manage")) {
	if (!privilege.isUserPrivValid(request, "sales")) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
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

String query = ParamUtil.get(request, "query");
	
String querystr = "";

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

String[] categories = ParamUtil.getParameters(request, "categories");
if (categories==null) {
	SelectMgr sm = new SelectMgr();
    SelectDb sd = sm.getSelect("sales_customer_category");
    Vector vsd = sd.getOptions();
    Iterator irsd = vsd.iterator();
    categories = new String[vsd.size()];
    int i=0;
    while (irsd.hasNext()) {
        SelectOptionDb sod = (SelectOptionDb)irsd.next();
        categories[i] = sod.getValue();
        i++;
    }
	//categories = new String[]{"5", "4"};
}
String preDate = ParamUtil.get(request, "preDate");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");

try {	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "preDate", preDate, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

java.util.Date beginDate = null;
java.util.Date endDate = null;

if (!preDate.equals("-") && !preDate.equals("*")) {
	if (preDate.equals("")) {
		java.util.Date[] ary = DateUtil.getDateSectOfCurMonth();
     	preDate = DateUtil.format(ary[0], "yyyy-MM-dd") + "|" + DateUtil.format(ary[1], "yyyy-MM-dd");
		
     	beginDate = ary[0];
		endDate = ary[1];
	}
	else {
		String[] ary = StrUtil.split(preDate, "\\|");
		strBeginDate = ary[0];
		strEndDate = ary[1];
		beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
		endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
	}
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
<title>客户汇总</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery.raty.min.js"></script>
<script src="../inc/flow_dispose_js.jsp"></script>

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
</head>
<%@ include file="../inc/nocache.jsp"%>
<body>
<table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td align="left">
    <form id="searchForm" action="customer_summary.jsp" method="get">
    &nbsp;
    <select id="preDate" name="preDate" onchange="if (this.value=='*') o('dateSection').style.display=''; else o('dateSection').style.display='none'">
      <option selected="selected" value="-">不限</option>
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

      <span id="dateSection" style="display:<%=preDate.equals("*")?"":"none"%>"> 从
        <input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        至
        <input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
      </span>
      客户种类：
      <%
      SelectMgr sm = new SelectMgr();
      SelectDb sd = sm.getSelect("sales_customer_category");
      Vector vsd = sd.getOptions();
      Iterator irsd = vsd.iterator();
      while (irsd.hasNext()) {
          SelectOptionDb sod = (SelectOptionDb)irsd.next();
          %>
        <input id="categories" name="categories" value="<%=sod.getValue()%>" type="checkbox" onclick="$('#searchForm').submit()" /><%=sod.getName()%>
          <%
      }
      %>
      &nbsp;<input type="submit" class="tSearch" value="查询" />
      </form>
      </td>
  </tr>
</table>

<%
querystr = "op="+op + "&action=" + StrUtil.UrlEncode(action) + "&preDate=" + preDate;

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();

FormDAO fdao = new FormDAO();

String sql;
String unitCode = privilege.getUserUnitCode(request);

// 取得本单位内有销售人员权限的人
Vector vUser = privilege.getUsersHavePriv("sales.user", unitCode);
UserDb user = new UserDb();
String cats = "";
if (categories!=null) {
	for (int i=0; i<categories.length; i++) {
		try {	
			com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "categories", categories[i], getClass().getName());
		}
		catch (ErrMsgException e) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
			return;
		}
		
		if (cats.equals(""))
			cats = StrUtil.sqlstr(categories[i]);
		else
			cats += "," + StrUtil.sqlstr(categories[i]);
		querystr += "&categories=" + categories[i];
	}
	cats = "(" + cats + ")";
}
%>
    <table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
    	<thead>
      <tr align="center">
        <th width="80" style="cursor:pointer">销售员</th>
        <th width="320" style="cursor:pointer">客户名称</th>
        <th width="120" style="cursor:pointer">种类</th>
        <th width="120" style="cursor:pointer">类型</th>
        <th width="120" style="cursor:pointer">性质</th>
        <th width="120" style="cursor:pointer">星级</th>
        <th width="120" style="cursor:pointer">类别</th>
        <th width="120" style="cursor:pointer">创建日期</th>
        <th width="120" style="cursor:pointer">最后行动</th>
        <th width="120" style="cursor:pointer">行动日期</th>
      </tr>
      </thead>
<%
Iterator irUser = vUser.iterator();
while (irUser.hasNext()) {
	user = (UserDb)irUser.next();

	sql = "select id from " + fd.getTableNameByForm() + " where sales_person=" + StrUtil.sqlstr(user.getName());
	if (beginDate!=null) {
		sql += " and find_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and find_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
	if (!cats.equals("")) {
		sql += " and category in " + cats;
	}
    sql += " order by category desc";
	
	// System.out.println(getClass() + " " + sql);
	
	Iterator ir = fdao.list(formCode, sql).iterator();

	  int i = 0;
      SelectOptionDb sod = new SelectOptionDb();
	  
	  while (ir!=null && ir.hasNext()) {
		  fdao = (FormDAO)ir.next();
		  i++;
		  long id = fdao.getId();
	  %>
      <tr align="center">
        <td width="6%" align="center"><a href="javascript:;" onclick="addTab('<%=user.getRealName()%>', '<%=request.getContextPath()%>/user_info.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>')"><%=user.getRealName()%></a></td>
        <td width="18%" align="left"><a href="javascript:;" onclick="addTab('<%=fdao.getFieldValue("customer")%>', '<%=request.getContextPath()%>/sales/customer_show.jsp?id=<%=id%>&amp;action=<%=action%>&formCode=<%=formCode%>')"><%=fdao.getFieldValue("customer")%></a></td>
        <td width="4%" align="center"><%=sod.getOptionName("sales_customer_category", fdao.getFieldValue("category"))%></td>
        <td width="6%" align="left"><%=sod.getOptionName("sales_customer_type", fdao.getFieldValue("customer_type"))%></td>
        <td width="7%" align="left"><%=sod.getOptionName("qyxz", fdao.getFieldValue("enterType"))%></td>
        <td width="10%" align="center"><%=RatyCtl.render(request, fdao.getFormField("star"), true)%></td>
        <td width="7%" align="center"><%=sod.getOptionName("khlb", fdao.getFieldValue("kind"))%></td>
        <td width="8%" align="center"><%=fdao.getFieldValue("find_date")%></td>
        <td width="26%" align="left">
        <%
		sql = "select d.id from form_table_day_lxr d, form_table_sales_linkman l where d.lxr=l.id and d.is_visited='是' and l.customer=" + fdao.getId(); //  + " and d.is_visited='是'";
		sql += " order by visit_date desc";
		String visit_date = "";
		ListResult lr = fdao.listResult("day_lxr", sql, 1, 1);
		Iterator irLxr = lr.getResult().iterator();
		if (irLxr.hasNext()) {
			FormDAO fdaoLxr = (FormDAO)irLxr.next();
			%>
			<%=fdaoLxr.getFieldValue("contact_result")%>
			<%
			visit_date = fdaoLxr.getFieldValue("visit_date");
		}
		%>
        </td>
        <td width="8%"><%=visit_date%></td>
      </tr>
      <%
    }
}%>
    </table>	
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
	$(function(){
		flex = $("#grid").flexigrid
		(
			{
			buttons : [
			{name: '条件', bclass: '', type: 'include', id: 'searchTable'}
			],
			url: false,
			usepager: false,
			checkbox : false,
			
			// title: "通知",
			singleSelect: true,
			resizable: false,
			showTableToggleBtn: true,
			showToggleBtn: false,
			
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
		
		o("preDate").value = "<%=preDate%>";
		
		<%
		if (categories!=null) {
			for (int i=0; i<categories.length; i++) {
				%>
				setCheckboxChecked("categories", '<%=categories[i]%>');
				<%
			}
		}		
		%>		
});

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>
