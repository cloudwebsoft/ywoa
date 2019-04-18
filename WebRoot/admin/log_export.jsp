<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "com.redmoon.oa.account.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.worklog.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>日志-导出</title>
<link href="default.css" rel="stylesheet" type="text/css">
<%@ include file="../inc/nocache.jsp"%>
<script language="JavaScript" type="text/JavaScript">
<!--
function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}
//-->
</script>
<script language=javascript>
<!--
function openWin(url,width,height)
{
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,top=50,left=120,width="+width+",height="+height);
}
//-->
</script>
<style type="text/css">
<!--
.STYLE2 {color: #000000}
-->
</style>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<%
JdbcTemplate jt = new JdbcTemplate();
String beginDate = ParamUtil.get(request, "beginDate");
String endDate = ParamUtil.get(request, "endDate");
if (beginDate.equals(""))
	beginDate = "2008-01-01";
String s = DateUtil.toLongString(DateUtil.parse(beginDate, "yyyy-MM-dd"));	
String sql = "select user_name,log_date from log where log_type=0 and log_date>=" + StrUtil.sqlstr(s) + " order by ID desc";
ResultIterator ri = jt.executeQuery(sql);
%>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="1" class="stable">
  <tr align="center" bgcolor="#C4DAFF">
    <td width="12%" bgcolor="#ded7c1">工号</td>
    <td width="12%" height="24" bgcolor="#ded7c1"><span class="stable STYLE2">用户名</span></td>
    <td width="16%" bgcolor="#ded7c1" class="stable STYLE2">真实姓名</td>
    <td width="16%" bgcolor="#ded7c1" class="stable STYLE2">所属部门</td>
    <td width="17%" bgcolor="#ded7c1" class="stable STYLE2">访问时间</td>
  </tr>
  <%
UserMgr um = new UserMgr(); 
AccountDb acc2 = new AccountDb();
while (ri.hasNext()) {
	ResultRecord rr = (ResultRecord)ri.next();
	UserDb user = um.getUserDb(rr.getString(1));
	AccountDb acc = acc2.getUserAccount(user.getName());	
%>
  <tr align="left">
    <td width="12%" align="center" bgcolor="#ECE7E1" class="stable"><%if (acc!=null && acc.isLoaded()) {
				out.print(acc.getName());
			}%></td>
    <td width="12%" height="22" align="center" bgcolor="#ECE7E1" class="stable"><a href="user_edit.jsp?name=<%=StrUtil.UrlEncode(user.getName())%>"><%=user.getName()%></a></td>
    <td width="16%" align="center" bgcolor="#ECE7E1" class="stable"><a href="user_edit.jsp?name=<%=StrUtil.UrlEncode(user.getName())%>"><%=user.getRealName()%></a></td>
    <td width="16%" align="center" bgcolor="#ECE7E1" class="stable"><%
	DeptMgr dm = new DeptMgr();		
	DeptUserDb du = new DeptUserDb();
	Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
	int k = 0;
	while (ir2.hasNext()) {
		DeptDb dd = (DeptDb)ir2.next();
		String deptName = "";
		if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
			deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span class=STYLE1>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;";
		}
		else
			deptName = dd.getName() + "&nbsp;&nbsp;";
		if (k==0) {
			out.print("<a href='#' onClick=\"openWin('dept_user.jsp?deptCode=" + StrUtil.UrlEncode(dd.getCode()) + "', 620, 420)\">" + deptName + "</a>");
		}
		else {
			out.print("，&nbsp;" + "<a href='#' onClick=\"openWin('dept_user.jsp?deptCode=" + StrUtil.UrlEncode(dd.getCode()) + "', 620, 420)\">" + deptName + "</a>");
		}
		k++;
	} 
	%></td>
    <td width="17%" align="left" bgcolor="#ECE7E1" class="stable"><%=DateUtil.format(DateUtil.parse(rr.getString(2)), "yyyy-MM-dd HH:mm:ss")%></td>
  </tr>
  <%
}
%>
</table>
</body>
</html>
