<%--Listfile.jsp--%>
<%@ page import="java.io.*"%>
<%@ page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.util.file.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page contentType="text/html;charset=GB2312" language="java" %>
<html>   <head><title>level</title></head>
   <body>
   <%
 
String sql = "insert into oa_user_level (levelAmount, description, levelPicPath) values (?,?,?) ";
JdbcTemplate jt = new JdbcTemplate();
jt.executeUpdate("delete from oa_user_level");
int c;
for (int i=1; i<=67; i++) {
	c = i-1;
	
	jt.executeUpdate(sql, new Object[]{new Integer(c), new Integer(i), "forum/images/group/" + i + ".gif"});
	
	c += 2;
}


   %>
   ====
   </body>
</html> 
									