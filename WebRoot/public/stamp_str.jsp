<%@ page contentType="text/html;charset=GB2312"%><%@page import="java.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%><%@page import = "cn.js.fan.util.*"%><%@page import="com.redmoon.oa.person.*"%><%@page import="com.redmoon.oa.stamp.*"%><jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/><%
String kind = StampDb.KIND_DEFAULT;
StampDb ld = new StampDb();
String sql = ld.getListSql(kind);
Iterator ir = ld.list(sql).iterator();
String stampNames = "";
while (ir.hasNext()) {
 	ld = (StampDb)ir.next();
	if (stampNames.equals(""))
		stampNames = ld.getTitle();
	else
		stampNames += "," + ld.getTitle();
}
out.print(stampNames);
%>