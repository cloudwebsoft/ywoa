<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="cn.js.fan.db.ResultRecord"%>
<%@page import="cn.js.fan.db.ResultIterator"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@page import="cn.js.fan.cache.jcs.*" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
/*
一键调试：对已启用的流程开启调试模式
		问题：若父节点未启用，子节点就不需要启用
		is_debug是0表示正常，1表示调试
一键重置密码：对所有用户密码重置为123(admin,system除外)
admin:获取admin密码
		
*/

String debug = ParamUtil.get(request,"debug");
String op = ParamUtil.get(request,"op");
String czmm = ParamUtil.get(request,"czmm");
JdbcTemplate jt = new JdbcTemplate();
String adminSql = "select pwdRaw from users where name = 'admin'";
ResultIterator ri = (ResultIterator)jt.executeQuery(adminSql);
String password = "";
while(ri.hasNext()){
	ResultRecord rr = (ResultRecord)ri.next();
	password = rr.getString(1);
}
if("edit".equals(op)) {
	String sql = "update flow_directory set is_debug = " + debug +" where is_open = 1 and code = ?";
	//查出启用父节点的子节点
	String querysql = "select code from flow_directory fd where parent_code in (select code from flow_directory where layer=2 and is_open = 1)";
	
	ResultIterator ri1 = jt.executeQuery(querysql);
	while(ri1.hasNext()){
		ResultRecord rr = (ResultRecord)ri1.next();
		String code = rr.getString(1);
		//System.out.print(code);
		jt.executeUpdate(sql, new Object[]{code});
	}
	
	
}else if("reset_password".equals(op)){
	if("1".equals(czmm)){
		
		String sql = "update users set pwdRaw='123',pwd=MD5('123') where name =? and name <> 'admin' and name <> 'system'";
		String querySql = "select name from users";
		
		ResultIterator ri2 = jt.executeQuery(querySql);
		while(ri2.hasNext()){
			ResultRecord rr = (ResultRecord)ri2.next();
			String name = rr.getString(1);
			//System.out.print("name=" + name);
			jt.executeUpdate(sql,new Object[]{name});
			
		}
	}
}
RMCache rmcache = RMCache.getInstance();
rmcache.clear();

%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>工具</title>
 <%@ include file="../inc/nocache.jsp" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="../js/hopscotch/css/hopscotch.css" />
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script type="text/javascript" src="../js/hopscotch/hopscotch.js"></script>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />

  </head>
  
  <body>
    <table id="util" width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98">
    	<tr>
    		<td class="tabStyle_1_title" colspan="3">工具</td>
    	</tr>
    	<form action="debug.jsp?op=edit" method="post" id="form_debug" name="form_debug">
    	<tr class="highlight">
    		<td width="33%">一键调试</td>
    		<td width="33%">
    			<select name="debug" id="debug">
    				<option value="1">是</option>
    				<option value="0" selected ="selected">否</option>
    			</select>
    		</td>
    		<td width="33%" align="center">
    			<input type="submit" class="btn" name="edit" value="修改"/>
    		</td>
    	</tr>
    	</form>
    	<form action="debug.jsp?op=reset_password" method="post" id="reset_password" name="reset_password">
    	<tr class="highlight">
    		<td width="33%">密码重置(admin,system除外)</td>
    		<td width="33%">
    			<select name="czmm" id="czmm">
    				<option value="1">是</option>
    				<option value="0" selected ="selected">否</option>
    			</select>
    		</td>
    		<td width="33%" align="center">
    			<input type="submit" class="btn" name="edit" value="重置"/>
    		</td>
    	</tr>
    	<tr class="highlight">
    		<td width="33%">admin</td>
    		<td colspan="2">
    			<input type="text" name="admin" value="<%=password %>" readonly="readonly"/>
    		</td>
    		
    	</tr>
    	<tr class="highlight">
    		<td colspan="3" align="center">
    			<input type="button" name="return" value="返回" onclick="window.location.href('../index.jsp')"/>
    		</td>
    	</tr>
    	</form>
        
    </table>
  </body>
</html>
