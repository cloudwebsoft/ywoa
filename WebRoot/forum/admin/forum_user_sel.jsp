<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.base.*"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<title><lt:Label res="res.label.forum.admin.forum_user_sel" key="select_user"/></title>
<link href="default.css" rel="stylesheet" type="text/css">
<script language="JavaScript">
<!--
function setPerson(userName, userNick) {
window.opener.setPerson(userName, userNick);
window.close();
}
//-->
</script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-right: 0px;
	margin-bottom: 0px;
}
.STYLE2 {color: #000000}
-->
</style>
<body bgcolor="#FFFFFF" leftmargin='0' topmargin='5'>
<%
String groupCode = ParamUtil.get(request, "groupCode");
String nick = ParamUtil.get(request, "nick");
String op = ParamUtil.get(request, "op");

com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();	
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "groupCode", groupCode, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "nick", nick, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, pvg, "op", op, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}

%>
<TABLE 
style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" 
cellSpacing=0 cellPadding=3 width="100%" align=center>
  <TBODY>
    <TR>
      <TD class=thead style="PADDING-LEFT: 10px" noWrap width="70%"><font size="-1"><b><lt:Label res="res.label.forum.admin.forum_user_sel" key="select_user"/></b></font> </TD>
    </TR>
    <TR>
      <TD height="175" align="center" bgcolor="#FFFFFF" style="PADDING-LEFT: 10px">
        <table width="90%" border="0" align="center">
          <form name="form1" method="get" action="?"><tr>
            <td height="25" align="center">
			用户组：
			<select name="groupCode">
			<option value="">全部</option>
			<%
			UserGroupDb ugroup = new UserGroupDb();
			Vector result = ugroup.list();
			Iterator irR = result.iterator();
			while (irR.hasNext()) {
				ugroup = (UserGroupDb)irR.next();
				if (ugroup.getCode().equals(UserGroupDb.GUEST))
					continue;
			%>
			<option value="<%=ugroup.getCode()%>"><%=ugroup.getDesc()%></option>
			<%
			}		
			%>
			</select>
			<script>
			form1.groupCode.value = "<%=groupCode%>";
			</script>
              <lt:Label res="res.label.forum.admin.forum_user_sel" key="input_nick"/>：
              <input type="text" name="nick" style="height:18px;width:100px" value="<%=nick%>">
			  <input name="op" value="search" type="hidden" />
              &nbsp;
              <input type="submit" name="Submit" value="<lt:Label res="res.label.forum.admin.forum_user_sel" key="search"/>">
            </td>
            </tr></form>
        </table>
<%
		String sql;
		sql = "select name from sq_user";
	  	if (op.equals("search")) {
			sql = "select name from sq_user where nick like " + StrUtil.sqlstr("%" + nick + "%");
			if (!groupCode.equals("") && !groupCode.equals(UserGroupDb.EVERYONE)) {
				sql += " and group_code=" + StrUtil.sqlstr(groupCode);
			}
		}
		else {
			if (!groupCode.equals("") && !groupCode.equals(UserGroupDb.EVERYONE)) {
				sql += " where group_code=" + StrUtil.sqlstr(groupCode);
			}
		}
	  	sql += " order by regdate asc";
		
		// out.print(sql);
		
		int pagesize = 10;
		UserDb user = new UserDb();

		Paginator paginator = new Paginator(request);
		int curpage = paginator.getCurPage();
		
        ListResult lr = user.listResult(sql, curpage, pagesize);
	    int total = lr.getTotal();
		Iterator ir = lr.getResult().iterator();

		paginator.init(total, pagesize);
		// 设置当前页数和总页数
		int totalpages = paginator.getTotalPages();
		if (totalpages==0) {
			curpage = 1;
			totalpages = 1;
		}	
			
		int i = 0;
		
%>
        <table width="90%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td align="right"><span class="title1"><%=paginator.getPageStatics(request)%></span></td>
          </tr>
        </table>
        <table width="98%" border="0" align="center" cellpadding="0" cellspacing="1">
          <tr align="center" bgcolor="#C4DAFF">
            <td width="21%" height="24" bgcolor="#EFEBDE" class="stable STYLE2"><lt:Label res="res.label.forum.admin.forum_user_sel" key="nick"/></td>
            <td width="32%" bgcolor="#EFEBDE" class="stable STYLE2"><lt:Label res="res.label.forum.admin.forum_user_sel" key="reg_time"/></td>
            <td width="31%" bgcolor="#EFEBDE" class="stable STYLE2"><lt:Label res="res.label.forum.admin.forum_user_sel" key="level"/></td>
            <td width="16%" bgcolor="#EFEBDE" class="stable STYLE2"><lt:Label key="op"/></td>
          </tr>
        <%
		while (ir.hasNext()) {
			i++;
			user = (UserDb)ir.next();
		%>
          <tr align="left">
            <td width="21%" height="22" align="center" bgcolor="#EEEDF3" class="stable"><%=user.getNick()%></td>
            <td width="32%" align="center" bgcolor="#EEEDF3" class="stable"><%=ForumSkin.formatDateTime(request, user.getRegDate())%></td>
            <td width="31%" align="center" bgcolor="#EEEDF3" class="stable"><%=user.getLevelDesc()%></td>
            <td width="16%" align="center" bgcolor="#EEEDF3" class="stable"><a href="#" onClick="setPerson('<%=user.getName()%>', '<%=user.getNick()%>')"><lt:Label res="res.label.forum.admin.forum_user_sel" key="select"/></a></td>
          </tr>
        <%}%>
        </table>
        <br>
        <table width="92%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
          <tr>
            <td height="23" align="right"><%
	String querystr = "op=" + op + "&nick=" + StrUtil.UrlEncode(nick) + "&groupCode=" + StrUtil.UrlEncode(groupCode);
    out.print(paginator.getCurPageBlock("?"+querystr));
%></td>
          </tr>
        </table>
        <br>
      <p> </TD>
    </TR>
    <!-- Table Body End -->
    <!-- Table Foot -->
    <TR>
      <TD class=tfoot align=right><DIV align=right> </DIV></TD>
    </TR>
    <!-- Table Foot -->
  </TBODY>
</TABLE>
</body>
</html>                            
  