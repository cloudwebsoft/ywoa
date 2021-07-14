<%@ page contentType="text/html; charset=utf-8"%><%@ page import = "java.util.*"%><%@ page import = "cn.js.fan.db.*"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "com.redmoon.oa.*"%><%@ page import="cn.js.fan.web.*"%><%@ page import="java.io.*"%><%
String sql = ParamUtil.get(request, "sql");

com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
sql = cn.js.fan.security.ThreeDesUtil.decrypthexstr(cfg.getKey(), sql);

response.setContentType("application/vnd.ms-excel");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("操作日志.xls"));  
LogUtil.writeExcel(request, response.getOutputStream(), sql); // new FileOutputStream(fileWrite));
%>