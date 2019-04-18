<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.officeequip.*"%>
<%@ page import = "com.redmoon.oa.address.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String sql = ParamUtil.get(request, "sql");

//com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
//sql = cn.js.fan.security.ThreeDesUtil.decrypthexstr(cfg.getKey(), sql);
try{
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "sql", sql, getClass().getName());	
}catch(ErrMsgException e) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
    return;
}

response.setContentType("application/vnd.ms-excel");
String encode = System.getProperty("file.encoding");
if (encode != null && (encode.indexOf("utf") != -1|| encode.indexOf("UTF") != -1)){
    response.setHeader("Content-disposition","attachment; filename="+StrUtil.UTF8ToUnicode("办公用品.xls"));
}
else{
	response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("办公用品.xls"));
}

ExcelHandle.writeOfficeEquipExcel(response.getOutputStream(),sql); // new FileOutputStream(fileWrite));
out.clear();
out = pageContext.pushBody();
%>