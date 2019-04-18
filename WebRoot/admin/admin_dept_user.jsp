<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.redmoon.oa.visual.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.worklog.*"%>
<%@ page import="cn.js.fan.db.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>部门工作</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script>
function preview(userName,deptCode){
	window.parent.location.href='archive_user_modify.jsp?userName='+userName+'&deptCode='+deptCode;
}

var isLeftMenuShow = true;
function closeLeftMenu() {
	if (isLeftMenuShow) {
		window.parent.setCols("0,*");
		isLeftMenuShow = false;
		btnName.innerHTML = "+&nbsp;打开菜单";
	}
	else {
		window.parent.setCols("200,*");
		isLeftMenuShow = true;
		btnName.innerHTML = "-&nbsp;关闭菜单";		
	}
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
    String deptCode = ParamUtil.get(request, "deptCode");

	if (deptCode.equals("")) {
		out.print(SkinUtil.makeInfo(request, "请选择某个部门！"));
		return;
	}
	if (!privilege.canUserAdminDept(request, deptCode)) {
		// out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "pvg_invalid")));
		out.print(SkinUtil.makeErrMsg(request, "请选择具有管理权限的部门！"));		
		return;
	}

	com.redmoon.oa.dept.DeptDb dd = new com.redmoon.oa.dept.DeptDb();
	dd = dd.getDeptDb(deptCode);
	if (dd==null || !dd.isLoaded()) {
		out.print(StrUtil.Alert("部门" + deptCode + "不存在！"));
		return;
	}
	
	String op = ParamUtil.get(request, "op");
%>
<%@ include file="admin_dept_user_inc_menu_top.jsp"%>
<script>
o("menu1").className="current";
</script>
<div class="spacerH"></div>
<%
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

	DeptUserDb jd = new DeptUserDb();
	UserDb ud = new UserDb();

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
	String orderField = showByDeptSort ? "du.orders" : "u.orders";
   	String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ") order by du.DEPT_CODE asc, " + orderField + " asc";

	int curpage = ParamUtil.getInt(request, "CPages", 1);
	int pagesize = ParamUtil.getInt(request, "pageSize", 20);
	
	ListResult lr = jd.listResult(sql,curpage,pagesize);
	Iterator iterator = lr.getResult().iterator();
	int total = lr.getTotal();	
	Paginator paginator = new Paginator(request, total, pagesize);
	// 设置当前页数和总页数
	int totalpages = paginator.getTotalPages();
	if (totalpages==0) {
		curpage = 1;
		totalpages = 1;
	}
%>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="percent98">
  <tr>
    <td width="44%" height="26" align="left">
<div style="display:none">&nbsp;&nbsp;&nbsp;<a href="admin_dept_user_detail.jsp?deptCode=<%=StrUtil.UrlEncode(deptCode)%>">+ 详细显示</a>
<script>
//if (typeof(window.parent.leftFrame)=="object"){
//	if (window.parent.getCols()=="200,*")
//		document.write("&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">-&nbsp;关闭菜单</span></a>");
//	else {
//		isLeftMenuShow = false;
//		document.write("&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:closeLeftMenu()\"><span id=\"btnName\">+&nbsp;打开菜单</span></a>");
//	}
//}
</script>
</div>    
    </td>
    <td width="56%" align="right">找到符合条件的记录 <b><%=paginator.getTotal() %></b> 条　每页显示 <b><%=paginator.getPageSize() %></b> 条　页次 <b><%=paginator.getCurrentPage() %>/<%=paginator.getTotalPages() %></b></td>
  </tr>
</table>
<form action="" method="post" name="form1" id="form1">
  <table id="mainTable" class="tabStyle_1 percent98" width="99%"  border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" width="12%" height="24" align="center">姓名</td>
      <td class="tabStyle_1_title" width="16%" align="center">部门</td>
      <td class="tabStyle_1_title" width="46%" align="center">日报</td>
      <td class="tabStyle_1_title" width="14%" align="center">日期</td>
      <td width="12%" height="24" align="center" class="tabStyle_1_title">操作</td>
    </tr>
<%
	Vector v = lr.getResult();
	
	Iterator ir = v.iterator();
	while (ir.hasNext()) {
		DeptUserDb pu = (DeptUserDb)ir.next();
		if (!pu.getUserName().equals(""))
			ud = ud.getUserDb(pu.getUserName());	
%>
    <tr>
      <td height="22" align="center"><%=ud.getRealName()%></td>
      <td align="center">
<%
		dd = deptDb.getDeptDb(pu.getDeptCode());
		out.print(dd.getName());
			/*
			Iterator ir2 = du.getDeptsOfUser(ud.getName()).iterator();
			int k = 0;
			while (ir2.hasNext()) {
				dd = (DeptDb)ir2.next();
				String deptName = "";
				if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
					deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;";
				}
				else
					deptName = dd.getName() + "&nbsp;";
				if (k==0) {
					out.print(deptName);
				}
				else {
					out.print("，&nbsp;" + deptName);
				}
				k++;
			}
			*/
			%>	  
	  </td>
      <td align="left">
      <%
	  WorkLogDb wld = new WorkLogDb();
	  wld = wld.getLastWorkLogDb(pu.getUserName(), WorkLogDb.TYPE_NORMAL);
	  if (wld!=null) {
	  %>
      <%=HtmlUtil.getTextFromHTML( wld.getContent()) %>
      <%}%>
      </td>
      <td align="center">
      <%if (wld!=null) {%>
      <%=DateUtil.format(wld.getMyDate(), "yyyy-MM-dd")%>
      <%}%>
      </td>
    <td height="22" align="center"><a title="查看工作详情" href="javascript:;" onclick="addTab('<%=ud.getRealName()%>', '<%=request.getContextPath()%>/admin/admin_dept_user_show.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>')">查看</a></tr>
    <%
    }
%>
  </table>
</form>
<table class="percent98" width="92%" border="0" cellspacing="1" cellpadding="3" align="center">
  <tr>
    <td height="23" align="right"><%
	String querystr = "deptCode=" + StrUtil.UrlEncode(deptCode);
    out.print(paginator.getCurPageBlock("admin_dept_user.jsp?"+querystr));
%></td>
  </tr>
</table>
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
