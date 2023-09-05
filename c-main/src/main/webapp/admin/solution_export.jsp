<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="cn.js.fan.web.*"%>
<%@page import="com.redmoon.oa.flow.*"%>
<%@page import="com.redmoon.oa.visual.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@ page import="java.nio.charset.StandardCharsets" %>
<%@ page import="com.cloudwebsoft.framework.util.LogUtil" %>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv)) {
	//response.setContentType("text/html;charset=gb2312"); 
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>");
	out.println(StrUtil.makeErrMsg("权限非法！"));
	return;
}

response.setContentType(MIMEMap.get("txt"));
//response.setHeader("Content-disposition","filename="+att.getName());
response.setHeader("Content-disposition", "attachment; filename=" + StrUtil.GBToUnicode("解决方案") + ".txt");

// response.setContentType("application/octet-stream");
// response.setHeader("Content-disposition","attachment; filename="+att.getName());

// out.print(fd.getContent());

String codes = ParamUtil.get(request, "codes");
if ("".equals(codes)) {
	out.print(StrUtil.jAlert_Back("请选择表单！","提示"));
	return;
}
String content = ModuleUtil.exportSolution(codes);

BufferedInputStream bis = null;
BufferedOutputStream bos = null;
try {
	bis = new BufferedInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
	bos = new BufferedOutputStream(response.getOutputStream());
	
	byte[] buff = new byte[2048];
	int bytesRead;
	
	while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		bos.write(buff,0,bytesRead);
	}

} catch(final IOException e) {
	LogUtil.getLog(getClass()).error(e);
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
}

out.clear();
out = pageContext.pushBody();
%>