<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%!
// 如果map中存在，则不从request中获取
public String getUrlStr(HttpServletRequest request, Map map) {
    String queryStrTmp = "";
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements()) {
        String paramName = (String) paramNames.nextElement();
        if (!map.containsKey(paramName)) {
	        String[] paramValues = ParamUtil.getParameters(request, paramName);
	        if (paramValues!=null) {
	        	int len = paramValues.length;
	        	/*
	        	if (len==1) {
		            String paramValue = ParamUtil.get(request, paramName);
			        if (queryStrTmp.equals("")) {
		        		queryStrTmp = paramName + "=" + StrUtil.UrlEncode(paramValue);
		        	}
		        	else {
		        	    queryStrTmp += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
		        	}	        		
	        	}
	        	*/
	        	for (int i=0; i<len; i++) {
		            String paramValue = paramValues[i];
			        if (queryStrTmp.equals("")) {
		        		queryStrTmp = paramName + "=" + StrUtil.UrlEncode(paramValue);
		        	}
		        	else {
		        	    queryStrTmp += "&" + paramName + "=" + StrUtil.UrlEncode(paramValue);
		        	}	        	
	        	}
	        }
        }
    }	

    return queryStrTmp;
}
%>
<%
String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));

String op = ParamUtil.get(request, "op");
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式
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
	if (!privilege.isUserPrivValid(request, "sales")) {
		if (!privilege.canAdminUser(request, userName)) {
			out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, "您对该用户没有管理权限！"));
			return;
		}
	}
}

String linkman = ParamUtil.get(request, "linkman");
String strBeginDate = ParamUtil.get(request, "beginDate");
String strEndDate = ParamUtil.get(request, "endDate");
java.util.Date beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
java.util.Date endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");

String orderBy = ParamUtil.get(request, "orderBy");
if (orderBy.equals(""))
	orderBy = "visit_date";
	
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "userName", userName, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "linkman", linkman, getClass().getName());
	
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "linkman", linkman, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strBeginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "strEndDate", strEndDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "userName", userName, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;	
}
	
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>行动记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/flexigrid/flexigrid.css" />
<script src="../inc/common.js"></script>
<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />

<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../js/flexigrid.js"></script>
<script src="<%=request.getContextPath()%>/js/jquery.raty.min.js"></script>

<script>
function openWin(url,width,height) {
	var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}

var curOrderBy = "<%=orderBy%>";
var sort = "<%=sort%>";
function doSort(orderBy) {
	if (orderBy==curOrderBy)
		if (sort=="asc")
			sort = "desc";
		else
			sort = "asc";
			
	window.location.href = "sales_user_action_list.jsp?op=<%=op%>&userName=<%=StrUtil.UrlEncode(userName)%>&linkman=<%=StrUtil.UrlEncode(linkman)%>&beginDate=<%=strBeginDate%>&endDate=<%=strEndDate%>&orderBy=" + orderBy + "&sort=" + sort;
}
</script>
</head>
<body>
<%
String priv = "sales.user";
if (!privilege.isUserPrivValid(request, priv) && !privilege.isUserPrivValid(request, "sales")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

request.setAttribute("isShowVisitTag", "true");
%>
<%@ include file="sales_user_inc_menu_top.jsp"%>
<script>
o("menu3").className="current"; 
</script>
<%
String formCode = "day_lxr";
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
String sql = "select d.id from " + fd.getTableNameByForm() + " d, form_table_sales_linkman l, form_table_sales_customer c where c.id=l.customer and d.lxr=l.id and c.sales_person=" + StrUtil.sqlstr(userName) + " and d.is_visited='是'";

if (op.equals("search")) {
	if (!linkman.equals("")) {
		sql = "select d.id from " + fd.getTableNameByForm() + " d, form_table_sales_linkman l, form_table_sales_customer c where c.id=l.customer and c.sales_person=" + StrUtil.sqlstr(userName) + " and d.lxr=l.id and l.linkmanName like " + StrUtil.sqlstr("%" + linkman + "%");
	}
	if (beginDate!=null) {
		sql += " and d.visit_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and d.visit_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
}

sql += " order by " + orderBy + " " + sort;

// out.print(sql);

int pagesize = ParamUtil.getInt(request, "pagesize", 30);
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
FormDAO fdao = new FormDAO();

ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
int total = lr.getTotal();
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
  <table id="searchTable" width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr>
      <td align="center">
		<form id="formSearch" name="formSearch" action="sales_user_action_list.jsp" method="get">
		<input name="op" value="search" type="hidden" />
      <input name="userName" value="<%=userName%>" type="hidden" />
        &nbsp;联系人
          <input type="text" id="linkman" name="linkman" size="10" value="<%=linkman%>" />
        日期从
        <input type="text" id="beginDate" name="beginDate" size="10" value="<%=strBeginDate%>" />
        至
        <input type="text" id="endDate" name="endDate" size="10" value="<%=strEndDate%>" />
			&nbsp;
			<input class="tSearch" name="submit" type=submit value="搜索">
		</form>
	</td>
    </tr>
  </table>
<table width="93%" border="0" cellpadding="0" cellspacing="0" id="grid">
	<thead>
  <tr>
    <th width="90" style="cursor:pointer">联系人	</th>
    <th width="220" style="cursor:pointer">客户</th>
    <th width="120" style="cursor:pointer" abbr="star">星级</th>
    <th width="120" style="cursor:pointer" abbr="contact_type">方式</th>
    <th width="120" style="cursor:pointer" abbr="pre_date">计划日期</th>
    <th width="110" style="cursor:pointer" abbr="visit_date">联系日期</th>
    <th width="120" style="cursor:pointer">成本</th>
    <th width="360" style="cursor:pointer">联系结果</th>
    <th width="120" style="cursor:pointer">操作</th>
  </tr>
  </thead>
  <%	
	  	int i = 0;
		FormDb fdLinkman = new FormDb();
		fdLinkman = fdLinkman.getFormDb("sales_linkman");
		FormDb fdCustomer = new FormDb();
		fdCustomer = fdCustomer.getFormDb("sales_customer");
		FormDAO fdaoLinkman = new FormDAO();
		FormDAO fdaoCustomer = new FormDAO();
		SelectOptionDb sod = new SelectOptionDb();
		ArrayList<String> list = new ArrayList<String>();		
		while (ir!=null && ir.hasNext()) {
			fdao = (FormDAO)ir.next();
			i++;
			long myid = fdao.getId();
			fdaoLinkman = fdaoLinkman.getFormDAO(StrUtil.toInt(fdao.getFieldValue("lxr")), fdLinkman);
			fdaoCustomer = fdaoCustomer.getFormDAO(StrUtil.toInt(fdaoLinkman.getFieldValue("customer")), fdCustomer);
			String starLevel = RatyCtl.render(request, fdaoCustomer.getFormField("star"), true);
			list.add(starLevel);			
		%>
  <tr align="center">
    <td align="left"><%=fdaoLinkman.getFieldValue("linkmanName")%></td>
    <td align="left"><a href="javascript:;" onclick="addTab('客户', '<%=request.getContextPath()%>/sales/customer_show.jsp?id=<%=fdaoLinkman.getFieldValue("customer")%>&action=<%=StrUtil.UrlEncode(action)%>&formCode=sales_customer')"><%=fdaoCustomer.getFieldValue("customer")%></a></td>
    <td align="center"><%=starLevel%></td>
    <td align="center"><%=fdao.getFieldValue("contact_type")%></td>
    <td align="left"><%=StrUtil.getNullStr(fdao.getFieldValue("pre_date"))%></td>
    <td align="center"><%=StrUtil.getNullStr(fdao.getFieldValue("visit_date"))%></td>
    <td align="left"><%=StrUtil.getNullStr(sod.getOptionName("cost_type", fdao.getFieldValue("cost_type")))%>&nbsp;<%=StrUtil.getNullStr(fdao.getFieldValue("cost_sum"))%></td>
    <td align="left"><%=fdao.getFieldValue("contact_result")%></td>
    <td>
    <a href="javascript:;" onclick="addTab('行动', '<%=request.getContextPath()%>/visual/module_show.jsp?id=<%=myid%>&amp;action=<%=action%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=0')">查看</a>
    &nbsp;&nbsp;
    <a href="javascript:;" onclick="addTab('行动', '<%=request.getContextPath()%>/sales/linkman_visit_edit.jsp?id=<%=myid%>&amp;action=<%=action%>&amp;formCode=<%=StrUtil.UrlEncode(formCode)%>&isShowNav=0')">编辑</a>
    &nbsp;&nbsp;
    <a onclick="jConfirm('您确定要删除么？','提示',function(r){ if(!r){return;}else{window.location.href='../visual_del.jsp?id=<%=myid%>&amp;action=<%=action%>&formCode=<%=formCode%>&amp;privurl=<%=StrUtil.getUrl(request)%>'}})" style="cursor:pointer">删除</a></span> </td>
  </tr>
  <%
		}
%>
</table>
<script>
function initCalendar() {
	$('#beginDate').datetimepicker({
    	lang:'ch',
    	timepicker:false,
    	format:'Y-m-d',
    	formatDate:'Y/m/d',
    });
    $('#endDate').datetimepicker({
        lang:'ch',
        timepicker:false,
        format:'Y-m-d',
        formatDate:'Y/m/d',
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
			showToggleBtn: false,
			
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
		
		<%for (int j = 0; j < list.size(); j++) {
			int index1 = list.get(j).indexOf("star_raty");
			String rand="",scpt="";
			if(index1 > 0){
				rand = list.get(j).substring(index1, index1 + 14);
				index1 = list.get(j).indexOf("<script>");
				int index2 = list.get(j).indexOf("</script>");
				if (index2 > 0)
					scpt = list.get(j).substring(index1 + 8, index2);
			}
		%>
		$('#<%=rand%>').html('');
		<%=scpt%>
		<%}%>		
});

function changeSort(sortname, sortorder) {
<%
Map map = new HashMap();
map.put("pageSize", "");
map.put("orderBy", "");
map.put("sort", "");
String querystr = getUrlStr(request, map);
%>
	window.location.href = "sales_user_action_list.jsp?pagesize=" + flex.getOptions().rp + "&orderBy=" + sortname + "&sort=" + sortorder + "&<%=querystr%>";
}

function changePage(newp) {
<%
map.clear();
map.put("CPages", "");
map.put("pageSize", "");
querystr = getUrlStr(request, map);
%>
	if (newp){
		window.location.href = "sales_user_action_list.jsp?CPages=" + newp + "&pagesize=" + flex.getOptions().rp + "&<%=querystr%>";
	}
}

function rpChange(pageSize) {
<%
map.clear();
map.put("CPages", "");
map.put("pageSize", "");
querystr = getUrlStr(request, map);
%>
	window.location.href = "sales_user_action_list.jsp?CPages=<%=curpage%>&pagesize=" + pageSize + "&<%=querystr%>";
}

function onReload() {
	window.location.reload();
}
</script>
</body>
</html>
