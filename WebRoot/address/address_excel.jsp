<%@ page contentType="text/html; charset=utf-8"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "com.redmoon.oa.address.*"%><%
String sql = ParamUtil.get(request, "sql");

com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
sql = cn.js.fan.security.ThreeDesUtil.decrypthexstr(cfg.getKey(), sql);

response.setContentType("application/vnd.ms-excel");
String encode = System.getProperty("file.encoding");
if (encode != null && (encode.indexOf("utf") != -1|| encode.indexOf("UTF") != -1)){
    response.setHeader("Content-disposition","attachment; filename="+StrUtil.UTF8ToUnicode("通讯录.xls"));
}
else{
	response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("通讯录.xls"));
}
// File fileWrite = new File("f:/testWrite.xls");
// fileWrite.createNewFile();
// new FileOutputStream(fileWrite);
// ExcelHandle.writeExcel(new FileOutputStream(fileWrite));
// System.out.println(getClass().getName()+":sql:"+sql);
ExcelHandle.writeExcel(response.getOutputStream(), sql); // new FileOutputStream(fileWrite));
out.clear();
out = pageContext.pushBody();
%>