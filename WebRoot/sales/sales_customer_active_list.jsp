<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.flow.FormDb"%>
<%@ page import = "com.redmoon.oa.flow.FormField"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="com.redmoon.oa.pvg.RoleDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%

if (!privilege.isUserPrivValid(request, "sales.manager")) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String strcurpage = StrUtil.getNullString(request.getParameter("CPages"));

String op = ParamUtil.get(request, "op");
String userName1 = privilege.getUser(request);
String action = ParamUtil.get(request, "action"); // action为manage时表示为销售总管理员方式
if (action.equals("manage")) {
	if (!privilege.isUserPrivValid(request, "sales")) {
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

DeptDb ddb1 = new DeptDb();
UserDb udb1 = new UserDb();
udb1 = udb1.getUserDb(userName1);
String[] deptArr1 = udb1.getAdminDepts();
String dept = ParamUtil.get(request,"dept");
if("".equals(dept)){
if (!privilege.isUserPrivValid(request, "sales")&&privilege.isUserPrivValid(request, "sales.manager")&&deptArr1.length==0) {
	dept = getDeptCode(userName1);
}
if (privilege.isUserPrivValid(request, "sales.manager")&&!privilege.isUserPrivValid(request, "sales")) {
	if(deptArr1.length>0){
		for(int s=0;s<deptArr1.length;s++){
			if("".equals(dept)){
				dept = "'"+deptArr1[s]+"'";
			}else{
				dept += ",'"+deptArr1[s]+"'";
			}
		}
	}
}
}
String preDate = ParamUtil.get(request, "preDate");
Date[] ary1 = DateUtil.getDateSectOfCurMonth();
String dateAry = DateUtil.format(ary1[0], "yyyy-MM-dd") + "|" + DateUtil.format(ary1[1], "yyyy-MM-dd");
if("".equals(preDate)){
	preDate = dateAry;
}

String linkman = ParamUtil.get(request, "linkman");

String strBeginDate = "";
String strEndDate = "";

java.util.Date beginDate = null;
java.util.Date endDate = null;

if (!preDate.equals("")) {
	String[] ary = StrUtil.split(preDate, "\\|");
	strBeginDate = ary[0];
	strEndDate = ary[1];
	beginDate = DateUtil.parse(strBeginDate, "yyyy-MM-dd");
	endDate = DateUtil.parse(strEndDate, "yyyy-MM-dd");
}

String orderBy = ParamUtil.get(request, "orderBy");
try {
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "orderBy", orderBy, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, privilege, "linkman", linkman, getClass().getName());

	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "op", op, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "linkman", linkman, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "beginDate", strBeginDate, getClass().getName());
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "endDate", strEndDate, getClass().getName());
	
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
if (orderBy.equals(""))
	orderBy = "visit_date";
	
String sort = ParamUtil.get(request, "sort");
if (sort.equals(""))
	sort = "desc";
	
String unitCode = privilege.getUserUnitCode(request);

%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>行动记录</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>

<script type="text/javascript" src="../util/jscalendar/calendar.js"></script>
<script type="text/javascript" src="../util/jscalendar/lang/calendar-zh.js"></script>
<script type="text/javascript" src="../util/jscalendar/calendar-setup.js"></script>
<style type="text/css"> @import url("../util/jscalendar/calendar-win2k-2.css"); </style>

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
			
	window.location.href = "sales_customer_active_list.jsp?op=<%=op%>&orderBy=" + orderBy + "&sort=" + sort + "&linkman=<%=StrUtil.UrlEncode(linkman)%>&beginDate=<%=strBeginDate%>&endDate=<%=strEndDate%>";
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

<div class="spacerH"></div>
<%
/**
String formCode = "day_lxr";
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
String sql = "select id from " + fd.getTableNameByForm() + " d where d.is_visited='是' and d.unit_code=" + StrUtil.sqlstr(unitCode);

if (op.equals("search")) {
	if (!linkman.equals("")) {
		sql = "select d.id from " + fd.getTableNameByForm() + " d, form_table_sales_linkman l where d.lxr=l.id and l.unit_code=" + StrUtil.sqlstr(unitCode) + " and l.linkmanName like " + StrUtil.sqlstr("%" + linkman + "%");
	}
	if (beginDate!=null) {
		sql += " and d.visit_date>=" + SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd");
	}
	if (endDate!=null) {
		sql += " and d.visit_date<" + SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
	}
}*/
String roleCode = "sales_user";
String sql = "select userName from user_of_role where roleCode = "+StrUtil.sqlstr(roleCode);
if(!"".equals(dept)){
	sql += " and userName in (select user_name from dept_user where dept_code in("+ dept+"))";;
}

//sql += " order by " + orderBy + " " + sort;

// out.print(sql);

int pagesize = 20;
Paginator paginator = new Paginator(request);
int curpage = paginator.getCurPage();
	
//FormDAO fdao = new FormDAO();

//ListResult lr = fdao.listResult(formCode, sql, curpage, pagesize);
JdbcTemplate jt1 = new JdbcTemplate();
ResultIterator ri1 = jt1.executeQuery(sql,curpage,pagesize);
ResultRecord rd1 = null;
long total = ri1.getTotal();

//int total = lr.getTotal();
//Vector v = lr.getResult();
//Iterator ir = null;
//if (v!=null)
	//ir = v.iterator();
paginator.init(total, pagesize);
// 设置当前页数和总页数
int totalpages = paginator.getTotalPages();
if (totalpages==0) {
	curpage = 1;
	totalpages = 1;
}
%>
<form action="sales_customer_active_list.jsp">
<table width="98%" align="center" class="percent98">
<tr><td align="center">
&nbsp;&nbsp;时 间&nbsp;&nbsp;&nbsp;<select id="preDate" name="preDate" style="height:24px">
<option value="">不限</option>
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
<option value="<%=DateUtil.format(ary[0], "yyyy-MM-dd")%>|<%=DateUtil.format(ary[1], "yyyy-MM-dd")%>" selected="selected">本月</option>
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
</select>
<%if (privilege.isUserPrivValid(request, "sales")) {%>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;部 门&nbsp;&nbsp;&nbsp;<select id="dept" name="dept" style="height:24px"><option value="" selected="selected">不限</option>

<%
	String sqldept = "select name,code from department where rootCode='root' and parentCode<>'-1'";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sqldept);
	ResultRecord rd = null;
	while(ri.hasNext()){
		rd = (ResultRecord)ri.next();
	
%>
	<option value="<%=rd.getString(2) %>"><%=rd.getString(1) %></option>
	<%} %>
</select>&nbsp;&nbsp;&nbsp;&nbsp;
<script>
o("preDate").value = "<%=preDate%>";
o("dept").value = "<%=dept %>";
</script>
<%} %>
<%
        if (privilege.isUserPrivValid(request, "sales.manager")&&!privilege.isUserPrivValid(request, "sales")) {
        	//String deptCodes = getManageDepts(userName);
        	//String deptArr[] = deptCodes.split(",");
        	DeptDb ddb = new DeptDb();
        	UserDb udb = new UserDb();
			udb = udb.getUserDb(userName1);
			String[] deptArr = udb.getAdminDepts();
        	if(deptArr.length>1){
        %>
        &nbsp;所属部门：<select id="dept" name="dept" style="height:24px"><option value="" selected="selected">不限</option>

        <%
        	for(int t = 0 ;t<deptArr.length;t++){
        		ddb = ddb.getDeptDb(deptArr[t]);
        %>
        	<option value="<%=deptArr[t] %>"><%=ddb.getName() %></option>
        	<%} %>
        </select>&nbsp;&nbsp;
        <script>
		o("dept").value="<%=dept%>";
		</script>
        <%}} %>

<input type="submit" class="btn" value="查询" />
</td></tr></table>
</form>
<table class="percent98" width="95%" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="47" height="28">&nbsp;</td>
    <td align="right" backgroun="images/title1-back.gif">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=curpage %>/<%=totalpages %></b></td>
  </tr>
</table>
<table class="tabStyle_1 percent98" width="98%" border="0" align="center" cellpadding="2" cellspacing="0">
  <tr align="center">
  	<td class="tabStyle_1_title" width="4%">序号</td>
    <td class="tabStyle_1_title" width="7%">客户经理</td>
    <td class="tabStyle_1_title" width="8%">部门	</td>
    <td class="tabStyle_1_title" width="9%">新增客户数</td>
    <td class="tabStyle_1_title" width="9%">新增商机数</td>
    <td class="tabStyle_1_title" width="9%">新增行动数</td>
    <td class="tabStyle_1_title" width="9%">有变更的商机信息数</td>
  </tr>
  <%	
  		String sqlcus = "select count(id) from form_table_sales_customer where sales_person=? and find_date>="+ SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd")+" and find_date<"+ SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
  		String sqlcha = "select count(id) from form_table_sales_chance where cws_creator=? and find_date>="+ SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd")+" and find_date<"+ SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
  		String sqlact = "select count(id) from form_table_day_lxr where cws_creator=? and visit_date>="+ SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd")+" and visit_date<"+ SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
  		String sqlhis = "select distinct chanceId from sales_chance_history where creator=? and action='更改信息' and update_date>="+ SQLFilter.getDateStr(strBeginDate, "yyyy-MM-dd")+" and update_date<"+ SQLFilter.getDateStr(strEndDate, "yyyy-MM-dd");
  		JdbcTemplate jtc = new JdbcTemplate();
  		JdbcTemplate jtch = new JdbcTemplate();
  		JdbcTemplate jta = new JdbcTemplate();
  		JdbcTemplate jth = new JdbcTemplate();
  		ResultIterator ric = null;
  		ResultIterator rich = null;
  		ResultIterator ria = null;
  		ResultIterator rih = null;
  		ResultRecord rdc = null;
  		ResultRecord rdch = null;
  		ResultRecord rda = null;
  		int sumCus = 0;
  		int sumCha = 0;
  		int sumAct = 0;
  		int num = 0;
	  	while(ri1.hasNext()){
	  		num++;
	  		rd1 = (ResultRecord)ri1.next();
	  		String userName = rd1.getString(1);
	  		ric = jtc.executeQuery(sqlcus,new Object[]{userName});
	  		if(ric.hasNext()){
	  			rdc = (ResultRecord)ric.next();
	  			sumCus = rdc.getInt(1);
	  		}
	  		rich = jtch.executeQuery(sqlcha,new Object[]{userName});
	  		if(rich.hasNext()){
	  			rdch = (ResultRecord)rich.next();
	  			sumCha = rdch.getInt(1);
	  		}
	  		ria = jta.executeQuery(sqlact,new Object[]{userName});
	  		if(ria.hasNext()){
	  			rda = (ResultRecord)ria.next();
	  			sumAct = rda.getInt(1);
	  		}
	  		rih = jth.executeQuery(sqlhis,new Object[]{userName});
	%>
  <tr align="center">
  	<td  align="center"><%=(curpage-1)*pagesize+num %></td>
    <td  align="center"><%=userName %></td>
    <td  align="center"><%=getDeptNames(getDeptCode(userName)) %></td>
    <td  align="center"><%=sumCus %></td>
    <td  align="center"><%=sumCha %></td>
    <td  align="center"><%=sumAct %></td>
   <td  align="center"><%=rih.size() %></td>
  </tr>
<%} %>
</table>
<table class="percent98" width="98%"  border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td width="48%" height="30" align="left">&nbsp;</td>
    <td width="52%" align="right"><%
			out.print(paginator.getCurPageBlock("sales_customer_active_list.jsp?action=" + action + "&orderBy=" + orderBy + "&sort=" + sort + "&op=" + op + "&linkman=" + StrUtil.UrlEncode(linkman) + "&beginDate=" + strBeginDate + "&endDate=" + strEndDate+"&preDate="+preDate+"&dept="+dept));
			%></td>
  </tr>
</table>
<br />

</body>
<%!

	public String getDeptCode(String uName){
		String sql = "select dept_code from dept_user where user_name=?";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		String deptCode = "";
		String deptCodes = "";
		try{
			ri = jt.executeQuery(sql,new Object[]{uName});
			if(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				deptCodes = rd.getString(1);
				String codeAry[] = deptCodes.split(",");
				if(codeAry.length>0){
					deptCode = codeAry[0];
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return deptCode;
	}

	public String getDeptNames(String deptCode){
		String deptNames = "";
		String deptName = "";
		String sql = "select name from department where code in(?)";
		JdbcTemplate jt = new JdbcTemplate();
		ResultIterator ri = null;
		ResultRecord rd = null;
		try{
			ri = jt.executeQuery(sql,new Object[]{deptCode});
			while(ri.hasNext()){
				rd = (ResultRecord)ri.next();
				deptName = rd.getString(1);
				if("".equals(deptNames)){
					deptNames += deptName;
				}else{
					deptNames += ","+deptName;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return deptNames;
	}
%>
</html>
