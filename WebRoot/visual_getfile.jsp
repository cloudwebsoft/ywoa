<%@ page contentType="text/html;charset=gb2312"%><%@page import="cn.js.fan.util.*"%><%@page import="cn.js.fan.web.Global"%><%@page import="com.redmoon.oa.visual.*"%><%@page import="java.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String priv = "read";
if (!privilege.isUserPrivValid(request, priv))
{
	//response.setContentType("text/html;charset=gb2312"); 
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
	out.println(fchar.makeErrMsg("权限非法！"));
	return;
}

int id = ParamUtil.getInt(request, "attachId");
Attachment att = new Attachment(id);

response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
//response.setHeader("Content-disposition","filename="+att.getName());
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(att.getName()));

// response.setContentType("application/octet-stream");
// response.setHeader("Content-disposition","attachment; filename="+att.getName());

BufferedInputStream bis = null;
BufferedOutputStream bos = null;
// System.out.println(getClass() + " att.getFullPath():" + att.getFullPath());
try {
	bis = new BufferedInputStream(new FileInputStream(att.getFullPath()));
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