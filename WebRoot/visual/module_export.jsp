<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="cn.js.fan.web.*"%>
<%@page import="com.redmoon.oa.flow.*"%>
<%@page import="com.redmoon.oa.visual.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	//response.setContentType("text/html;charset=gb2312"); 
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
	out.println(fchar.makeErrMsg("权限非法！"));
	return;
}

String moduleCode = ParamUtil.get(request, "code");
ModuleSetupDb msd = new ModuleSetupDb();
msd = msd.getModuleSetupDb(moduleCode);

String formCode = ParamUtil.get(request, "formCode");
FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);
if (!fd.isLoaded()) {
	out.print(SkinUtil.makeErrMsg(request, "表单不存在！"));
	return;
}

response.setContentType(MIMEMap.get("txt"));
//response.setHeader("Content-disposition","filename="+att.getName());
response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode(msd.getString("name")) + ".txt");

// response.setContentType("application/octet-stream");
// response.setHeader("Content-disposition","attachment; filename="+att.getName());

// out.print(fd.getContent());
ModuleUtil mu = new ModuleUtil();
String content = mu.exportModule(moduleCode);
BufferedInputStream bis = null;
BufferedOutputStream bos = null;
try {
	bis = new BufferedInputStream(new ByteArrayInputStream(content.getBytes("utf-8")));
	bos = new BufferedOutputStream(response.getOutputStream());
	
	byte[] buff = new byte[2048];
	int bytesRead;
	
	while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		bos.write(buff,0,bytesRead);
	}

} catch(final IOException e) {
	System.out.println( "IOException: " + e );
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
}

out.clear();
out = pageContext.pushBody();
%>