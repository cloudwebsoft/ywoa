<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.worklog.*"%>
<%@ page import="com.redmoon.oa.oacalendar.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%
// 翻月
int showyear = ParamUtil.getInt(request, "showyear", -1);
Calendar cal = Calendar.getInstance();
if (showyear==-1)
	showyear = cal.get(cal.YEAR);

int curyear = cal.get(cal.YEAR);	
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>部门工作-日报</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>

<link href="../js/qTip2/jquery.qtip.css" rel="stylesheet" />
<script src="../js/qTip2/jquery.qtip.js"></script>

<style>
.none {
	background-color:#FBC4C4;
}
.normal {
	background-color:#AAF49F;
}
</style>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@ include file="../admin/admin_dept_user_inc_menu_top.jsp"%>
<script>
o("menu6").className="current";
</script>
<div class="spacerH"></div>
<%
String deptCode = ParamUtil.get(request, "deptCode");

if (deptCode.equals("")) {
	out.print(SkinUtil.makeInfo(request, "请选择某个部门！"));
	return;
}
if (!privilege.canUserAdminDept(request, deptCode)) {
	out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

com.redmoon.oa.dept.DeptDb dd = new com.redmoon.oa.dept.DeptDb();
dd = dd.getDeptDb(deptCode);
if (dd==null || !dd.isLoaded()) {
	out.print(StrUtil.Alert("部门" + deptCode + "不存在！"));
	return;
}

UserDb user = new UserDb();
	
DeptMgr dm = new DeptMgr();
DeptUserDb du = new DeptUserDb();

DeptDb deptDb = new DeptDb();
deptDb = deptDb.getDeptDb(deptCode);
Vector dv = new Vector();
deptDb.getAllChild(dv, deptDb);
String depts = StrUtil.sqlstr(deptCode);
Iterator ird = dv.iterator();
while (ird.hasNext()) {
	deptDb = (DeptDb)ird.next();
	depts += "," + StrUtil.sqlstr(deptDb.getCode());
}
%>
<table class="percent98" width="98%" align="center">
  <tr><td align="center">
<select name="showyear" onChange="var y=this.options[this.selectedIndex].value; window.location.href='mywork_dept_week.jsp?deptCode=<%=StrUtil.UrlEncode(deptCode)%>&showyear=' + y;">
  <%for (int y=curyear-60; y<=curyear; y++) {%>
  <option value="<%=y%>"><%=y%></option>
  <%}%>
</select>
<script>
o("showyear").value = "<%=showyear%>";
</script>
<%
int week = 0;
int days = 365;
int day = 0;
if (showyear % 400 == 0 || (showyear % 4 == 0 && showyear % 100 != 0)) 
{//判断是否闰年，闰年366天
	days = 366;
}
//得到一年所有天数然后除以7
day = days % 7;//得到余下几天
week = days / 7;//得到多少周
%>
<span class="none">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;色表示未写报告
</td></tr></table>
<form action="" method="post" name="form1" id="form1">
  <table id="mainTable" class="tabStyle_1" border="0" align="center" cellpadding="0" cellspacing="0">
  <thead>
    <tr>
      <td class="tabStyle_1_title" nowrap width="70" height="24" align="center">姓名</td>
      <%
	  int allNoCount = 0;
	  for (int i=1; i<=week; i++) {
	  %>
      <td class="tabStyle_1_title" width="40" align="center"><%=i%></td>
      <%}%>
      <td class="tabStyle_1_title" nowrap width="50">未写次数</td>
    </tr>
  </thead>
<%
	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
	String orderField = showByDeptSort ? "du.orders" : "u.orders";

	String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ") order by du.DEPT_CODE asc, " + orderField + " asc";
	Iterator ir = du.listBySQL(sql).iterator();
	int row = 0;
	while (ir.hasNext()) {
		DeptUserDb pu = (DeptUserDb)ir.next();
		if (!pu.getUserName().equals(""))
			user = user.getUserDb(pu.getUserName());
		else
			continue;
		int noCount = 0;
		row ++;
%>
    <tr>
      <td height="22" align="left"><%=user.getRealName()%></td>
      <%
	  WorkLogDb wld = new WorkLogDb();
	  for (int i=1; i<=week; i++) {
		  // 判断是否为非工作日
		  if (wld==null)
			wld = new WorkLogDb();
		  wld = wld.getWorkLogDb(user.getName(), WorkLogDb.TYPE_WEEK, showyear, i);
		  String cls = "";
		  if (wld==null) {
			cls = "class='none'";
			noCount ++;
		  }
		  else
			cls = "class='normal'";	
	  %>
      <td id="td<%=row%>_<%=i%>" align="center" <%=cls%>>
      <%
	  // out.print("N");
	  %>
      <script>
	  $("#td<%=row%>_<%=i%>").qtip({
		position: {
			my: 'top left', 
      		at: 'bottom right',
			viewport: false
		},
		content: {
			text: "加载中...",
			ajax: {
			  url: "<%=request.getContextPath()%>/mywork/mywork_tip.jsp?userName=<%=StrUtil.UrlEncode(user.getName())%>&logType=<%=WorkLogDb.TYPE_WEEK%>&year=<%=showyear%>&item=<%=i%>"
			},
			title: {
				text: '<%=i%>周',
				button: "关闭"
			}	
		},
		show: 'click',
		hide: 'click' // false
	  });
	  </script>
      </td>
      <%}%>
      <td align="center"><%=noCount%></td>
    </tr>
    <%
		allNoCount += noCount;
    }
%>
    <tr>
      <td height="22" align="center">合计</td>
      <td colspan="<%=week%>">&nbsp;</td>
      <td align="center"><%=allNoCount%></td>
    </tr>
  </table>
</form>
</body>
<script>
$(document).ready( function() {
	$("#mainTable td").mouseout( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).removeClass("tdOver"); });
	});  
	
	$("#mainTable td").mouseover( function() {
		if ($(this).parent().parent().get(0).tagName!="THEAD")
			$(this).parent().find("td").each(function(i){ $(this).addClass("tdOver"); });  
	});  
});
</script>
</html>
