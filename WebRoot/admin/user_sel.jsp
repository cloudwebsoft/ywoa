<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.account.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<%
if(true){
	// request.getRequestDispatcher("../user_multi_sel.jsp?mode=single&parameterNum=2").forward(request, response);
	response.sendRedirect("../user_multi_sel.jsp?mode=single&parameterNum=2");
	return;
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="pragma" content="no-cache" />
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate" />
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>用户-选择</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script language="JavaScript">
<!--
function setPerson(userName, userRealName){
window.opener.setPerson(userName, userRealName);
window.close();
}
//-->
</script>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<table cellspacing="0" cellpadding="0" width="100%">
  <tbody>
    <tr>
      <td class="tdStyle_1">选择用户</td>
    </tr>
  </tbody>
</table>
        <form name="form1" method="post" action="user_sel.jsp?op=search" target="_self">
        <table width="90%" border="0" align="center"><tr>
              <td height="25" align="center">请输入姓名：
              <input type="text" name="realName" style="height:18px;width:100px" />
              &nbsp;
              <input type="submit" name="Submit" value="查找" class="btn" />
            </td>
            </tr>
        </table></form>
<%
/*
String priv="admin";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

		String sql;
	  	String op = ParamUtil.get(request, "op");
		// sql = "select u.name from users u, account a where u.isvalid=1 and u.name=a.userName order by a.name asc";
		sql = "select name from users where isvalid=1 order by name asc";
		String realName = ParamUtil.get(request, "realName");
	  	if (op.equals("search")) {
			sql = "select name from users where realName like " + StrUtil.sqlstr("%" + realName + "%") + " and isvalid=1 order by name asc";
		}
			  	
		int pagesize = 2000;
		UserDb userdb = new UserDb();
	    int total = userdb.getUserCount(sql);
		int curpage,totalpages;
		Paginator paginator = new Paginator(request, total, pagesize);
        // 设置当前页数和总页数
	    totalpages = paginator.getTotalPages();
		curpage	= paginator.getCurrentPage();
		if (totalpages==0)
		{
			curpage = 1;
			totalpages = 1;
		}		
%>
        <table width="90%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td align="right"><span class="title1">找到符合条件的记录 <b><%=paginator.getTotal() %></b>条</span></td>
          </tr>
        </table>
        <table class="tabStyle_1" width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
          <tr align="center">
            <td width="21%" height="24" class="tabStyle_1_title">真实姓名</td>
            <td width="43%" class="tabStyle_1_title">角色</td>
            <td width="16%" class="tabStyle_1_title">操作</td>
          </tr>
        <%
	  	// Vector v = userdb.list(sql, (curpage-1)*pagesize, curpage*pagesize);
		// Iterator ir = v.iterator();
		int start = (curpage-1)*pagesize;
		int end = curpage*pagesize;
		
        ObjectBlockIterator ir = userdb.getObjects(sql, start, end);		
		int i = 0;
		while (ir.hasNext()) {
			i++;
			UserDb user = (UserDb)ir.next();
		%>
          <tr align="left">
            <td width="21%" height="22" align="left" class="stable"><a href="user_edit.jsp?name=<%=StrUtil.UrlEncode(user.getName())%>"></a><%=user.getRealName()%></td>
            <td width="43%" align="left" class="stable">
  <%
com.redmoon.oa.pvg.RoleDb[] rld = user.getRoles();
int rolelen = 0;
if (rld!=null)
	rolelen = rld.length;
for (int m=0; m<rolelen; m++) {
	out.print(rld[m].getDesc() + "&nbsp;&nbsp;");
}
%>			</td>
            <td width="16%" align="center" class="stable"><a href="#" onClick="setPerson('<%=StrUtil.toHtml(user.getName())%>', '<%=StrUtil.toHtml(user.getRealName())%>')">选择</a></td>
          </tr>
        <%
	}
%>
        </table>
        <table width="92%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
          <tr>
            <td height="23" align="right"><%
	String querystr = "op=" + op + "&realName=" + StrUtil.UrlEncode(realName);
    out.print(paginator.getCurPageBlock("user_sel.jsp?"+querystr));
%></td>
          </tr>
        </table>
</body>
</html>                            
  