<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="java.util.*" %>
<%@ page import = "com.redmoon.oa.visual.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@page import="cn.js.fan.db.ResultIterator"%>
<%@page import="cn.js.fan.db.ResultRecord"%>
<%@ page import="com.redmoon.oa.pvg.Privilege" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>部门工作</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script>
function preview(userName,deptCode)
{
	window.parent.location.href='archive_user_modify.jsp?userName='+userName+'&deptCode='+deptCode;
}
</script>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<% 
    String deptCode = ParamUtil.get(request, "deptCode");

	if (deptCode.equals("")) { // || deptCode.equals(com.redmoon.oa.dept.DeptDb.ROOTCODE)) {
		out.print(SkinUtil.makeInfo(request, "请选择部门！"));
		return;
	}
	if (!Privilege.canUserAdminDept(request, deptCode)) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		//out.print(StrUtil.jAlert_Back(SkinUtil.LoadString(request, "pvg_invalid"),"提示"));
		return;
	}

	com.redmoon.oa.dept.DeptDb dd = new com.redmoon.oa.dept.DeptDb();
	dd = dd.getDeptDb(deptCode);
	if (dd==null || !dd.isLoaded()) {
		out.print(StrUtil.jAlert("部门" + deptCode + "不存在！","提示"));
		return;
	}
	
	String op = ParamUtil.get(request, "op");

%>
<%@ include file="crm_dept_inc_menu_top.jsp"%>
<script>
o("menu1").className="current"; 
</script>
<div class="spacerH"></div>
<form action="" method="post" name="form1" id="form1">
  <table class="tabStyle_1 percent98" width="99%"  border="0" align="center" cellpadding="0" cellspacing="0">
    <tr>
      <td class="tabStyle_1_title" width="38%" height="24" align="center">职员</td>
      <td width="62%" height="24" align="center" class="tabStyle_1_title">操作</td>
    </tr>
    <%
	//DeptUserDb jd = new DeptUserDb();
	//Vector v = jd.list(deptCode);
	//Iterator ir = v.iterator();
	
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

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean showByDeptSort = cfg.getBooleanProperty("show_dept_user_sort");
	String orderField = showByDeptSort ? "du.orders" : "u.orders";

   	String sql = "select du.ID from dept_user du, users u where du.user_name=u.name and u.isValid=1 and du.DEPT_CODE in (" + depts + ") order by du.DEPT_CODE asc, " + orderField + " asc";

   	JdbcTemplate jt = new JdbcTemplate();
   	ResultIterator ir = jt.executeQuery(sql);
   	ResultRecord rd = null;
   	int id = 0;
	while (ir.hasNext()) {
		rd = (ResultRecord)ir.next();
		id = rd.getInt(1);
		DeptUserDb dud = new DeptUserDb();
		dud = dud.getDeptUserDb(id);
		UserDb ud = new UserDb();
		if (!dud.getUserName().equals("")) {
			ud = ud.getUserDb(dud.getUserName());
		}
%>
    <tr>
      <td height="22" align="center"><%=ud.getRealName()%></td>
      <td height="22" align="center">
      <a href="javascript:;" onclick="addTab('<%=ud.getRealName()%>客户', '<%=request.getContextPath()%>/sales/customer_list.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>&op=search')">客户</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <a href="javascript:;" onclick="addTab('<%=ud.getRealName()%>联系人', '<%=request.getContextPath()%>/sales/linkman_list.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>&op=search')">联系人</a>&nbsp;&nbsp;&nbsp;&nbsp;
      <a href="javascript:;" onclick="addTab('<%=ud.getRealName()%>发现的客户', '<%=request.getContextPath()%>/sales/customer_my_find_list.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>&isShowNav=0')">发现的客户</a>&nbsp;&nbsp;&nbsp;&nbsp;
      <a href="javascript:;" onclick="addTab('<%=ud.getRealName()%>分配的客户', '<%=request.getContextPath()%>/sales/customer_my_distributed_list.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>&isShowNav=0')">分配的客户</a>&nbsp;&nbsp;&nbsp;&nbsp;
      <a href="javascript:" onclick="addTab('<%=ud.getRealName()%>销售工作', '<%=request.getContextPath()%>/sales/sales_desktop.jsp?userName=<%=StrUtil.UrlEncode(ud.getName())%>')">销售工作</a>
      </td>      
    </tr>
    <%
    }
%>
  </table>
</form>
</body>
</html>
