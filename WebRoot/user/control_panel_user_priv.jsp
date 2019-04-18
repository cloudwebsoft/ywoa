<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String priv="read";
	if (!privilege.isUserPrivValid(request,priv))
	{
		out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String username = privilege.getUser(request);
	UserMgr um = new UserMgr();
	UserDb user = um.getUserDb(username);
	if (user==null || !user.isLoaded()) {
		out.print(StrUtil.Alert_Back("该用户已不存在！"));
		return;
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>我的权限</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
</head>
<body>
<div class="spacerH"></div>
<table class="tabStyle_1 percent60">
	<tbody>
		<tr>
			<td class="tabStyle_1_title">我拥有的权限</td>
		</tr>
<%
	String sql = "select priv,description,isSystem from privilege where layer=2 order by orders asc";
	JdbcTemplate jt = new JdbcTemplate();
	ResultIterator ri = jt.executeQuery(sql);
	ResultRecord rr = null;
	String desc;		  
	while (ri.hasNext()) {
		rr = (ResultRecord)ri.next();
		priv = rr.getString(1);
		if (!privilege.isUserPrivValid(request, priv)) {
			continue;
		}
		desc = rr.getString(2);
%>
		<tr>
			<td><%=desc%></td>
        </tr>
<%
	}
%>
	</tbody>
</table>
</body>
</html>