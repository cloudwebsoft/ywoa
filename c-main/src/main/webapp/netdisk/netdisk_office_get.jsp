<%@ page contentType="text/html;charset=gb2312"%><%@page import="cn.js.fan.util.*"%><%@page import="cn.js.fan.web.*"%><%@page import="com.redmoon.oa.*"%><%@page import="com.redmoon.oa.dept.*"%><%@page import="com.redmoon.oa.netdisk.*"%><%@page import="java.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);

String priv = request.getParameter("priv");
if (priv==null)
	priv = "read";

if (!privilege.isUserPrivValid(request, priv)) {
	//response.setContentType("text/html;charset=gb2312"); 
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int id = ParamUtil.getInt(request, "id");
int attId = ParamUtil.getInt(request, "attachId");

Document mmd = new Document();
mmd = mmd.getDocument(id);
if (mmd==null || !mmd.isLoaded()) {
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
	out.println("文章不存在");
	return;
}
Attachment att = mmd.getAttachment(1, attId);
String ext = att.getExt().toLowerCase(); // diskName.substring( len-3, len );

LeafPriv lp = new LeafPriv(att.getDirCode());
if (!lp.canUserModify(privilege.getUser(request))) {
	response.setHeader("Content-disposition","filename=" + StrUtil.GBToUnicode(att.getName()));
	if (ext.equals("doc") || ext.equals("docx")){
		response.setContentType("application/msword");
	}
	else if (ext.equals("xls") || ext.equals("xlsx")){
		response.setContentType("application/vnd.ms-excel");
	}
	else if (ext.equals("wps") || ext.equals("wpt") || ext.equals("dps") || ext.equals("et")) {
	// response.setContentType("application/msword");
	response.setContentType("application/octet-stream");
	}
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String diskName = att.getDiskName();
int len = diskName.length();
response.setHeader("Content-disposition","filename=" + StrUtil.GBToUnicode(att.getName()));

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String file_netdisk = cfg.get("file_netdisk");

String realPath = Global.getRealPath() + file_netdisk + "/" + att.getVisualPath() + "/" + att.getDiskName();
File f = new File(realPath);

response.setHeader("Content-Length", new Long(f.length()).toString()); 

if (ext.equals("doc") || ext.equals("docx"))
	response.setContentType("application/msword");
else if (ext.equals("xls") || ext.equals("xlsx"))
	response.setContentType("application/vnd.ms-excel");
else if (ext.equals("ppt") || ext.equals("pptx"))
	response.setContentType("application/vnd.ms-powerpoint");
else if (ext.equals("wps") || ext.equals("wpt") || ext.equals("dps") || ext.equals("et")) {
	// response.setContentType("application/msword");
	response.setContentType("application/octet-stream");	
}
else {
	out.print("扩展名" + ext + "不支持！");
	return;
}

BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
	// System.out.println(getClass() + " " + Global.getRealPath() + file_netdisk + "/" + att.getVisualPath() + "/" + att.getDiskName());
	bis = new BufferedInputStream(new FileInputStream(realPath));
	
	bos = new BufferedOutputStream(response.getOutputStream());
	
	byte[] buff = new byte[2048];
	int bytesRead;
	
	while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		bos.write(buff,0,bytesRead);
	}
	
} catch(Exception e) {
	System.out.println("IOException." + e + "---" + att.getFullPath());
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
}

out.clear();
out = pageContext.pushBody();
%>