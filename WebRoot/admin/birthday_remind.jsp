<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin"))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int showmonth = ParamUtil.getInt(request, "showMonth", -1);
if (showmonth==-1)
	showmonth = StrUtil.toInt(DateUtil.format(new java.util.Date(), "MM"), 1);
String nowDate = ""+DateUtil.format(new java.util.Date() , "dd");

UserDb userdb = new UserDb();
String sql ="select distinct name,birthday from users where " + SQLFilter.month("birthday") + " = " + showmonth + " and isValid=1 order by birthday asc ";
Iterator ir = userdb.list(sql).iterator();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>生日提醒</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<body>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">查看过生日的用户</td>
    </tr>
  </tbody>
</table><br>
<div align=center>
  <%
for (int i=1; i<=12; i++) {
	if (showmonth!=i)
		out.print("<a href='birthday_remind.jsp?showMonth=" + i + "'>"+i+"月</a>&nbsp;");
	else
		out.print("<a href='#'><font color=red>"+i+"月</font></a>&nbsp;");
}
%>
</div>
<br>
<table width="85%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent80">
  <tr>
    <td width="16%" height="22" align="center" class="tabStyle_1_title">用户名</td>
    <td width="25%" align="center" class="tabStyle_1_title">姓名</td>
    <td width="39%" align="center" class="tabStyle_1_title">部门</td>
    <td width="20%" align="center" class="tabStyle_1_title">生日</td>
  </tr>
  <%
	while (ir.hasNext()) {
		UserDb user = (UserDb)ir.next();
	%>
  <tr>
    <td height="24" align="center"><%=user.getName()%></td>
    <td align="center"><%=user.getRealName()%></td>
    <td align="center"><%
			DeptMgr dm = new DeptMgr();		
			DeptUserDb du = new DeptUserDb();
			Iterator ir2 = du.getDeptsOfUser(user.getName()).iterator();
			int k = 0;
			while (ir2.hasNext()) {
				DeptDb dd = (DeptDb)ir2.next();
				String deptName = "";
				if (!dd.getParentCode().equals(DeptDb.ROOTCODE)) {
					deptName = dm.getDeptDb(dd.getParentCode()).getName() + "<span style='font-family:宋体'>&nbsp;->&nbsp;</span>" + dd.getName() + "&nbsp;&nbsp;";
				}
				else
					deptName = dd.getName() + "&nbsp;&nbsp;";
				if (k==0) {
					out.print(deptName);
				}
				else {
					out.print("，&nbsp;" + deptName);
				}
				k++;
			} 
			%></td>
    <td align="center"><%=DateUtil.format(user.getBirthday(), "yyyy-MM-dd")%></td>
  </tr>
  <%}%>
</table>
</body>
</html>
