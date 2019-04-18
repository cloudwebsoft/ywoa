<%@ page contentType="text/html;charset=gb2312"%><%@page import="cn.js.fan.util.*"%><%@page import="cn.js.fan.web.*"%><%@page import="com.redmoon.oa.fileark.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
String ids = ParamUtil.get(request, "ids");
String fileDiskPath = "";
String realPath = Global.getRealPath() + "upfile/zip";
int id = -1;
if ("".equals(ids)) {
	id = ParamUtil.getInt(request, "id", -1);
	if (id==-1) {
		out.print(SkinUtil.makeErrMsg(request, "文章不存在！"));
		return;
	}
}

com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();

boolean isZip = true;
try {
	String attIds = ids;
	if (id!=-1) {
		attIds = String.valueOf(id);
	}
	Directory.onDownloadValidate(request, attIds, pvg.getUser(request), isZip);
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));  
	return;
}

if (id!=-1) {
	fileDiskPath = FileBakUp.zipDocFiles(id, realPath);
}
else {
	fileDiskPath = FileBakUp.zipDocsFiles(ids, realPath);	
}

// response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
response.setContentType("application/octet-stream");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode("文档下载.zip"));

BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
	bis = new BufferedInputStream(new FileInputStream(fileDiskPath));
	bos = new BufferedOutputStream(response.getOutputStream());
	
	byte[] buff = new byte[2048];
	int bytesRead;
	
	while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		bos.write(buff,0,bytesRead);
	}
} catch(final IOException e) {
	System.out.println( "IOException." + e );
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
}
	
try {
	String attIds = ids;
	if (id!=-1) {
		attIds = String.valueOf(id);
	}
	Directory.onDownload(request, attIds, pvg.getUser(request), isZip);
}
catch (ErrMsgException e) {
	out.print(SkinUtil.makeErrMsg(request, e.getMessage()));  
	return;
}
	
out.clear();
out = pageContext.pushBody();
%>