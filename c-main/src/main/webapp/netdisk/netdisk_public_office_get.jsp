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

PublicAttachment patt = new PublicAttachment();
patt = patt.getPublicAttachment(id);

if (patt.getAttId()!=0) {
	out.println(SkinUtil.makeErrMsg(request, "链接的文件不能被编辑"));
	return;
}

String ext = patt.getExt().toLowerCase(); // diskName.substring( len-3, len );

PublicLeafPriv lp = new PublicLeafPriv(patt.getPublicDir());
if (!lp.canUserModify(privilege.getUser(request))) {
	response.setHeader("Content-disposition","filename=" + StrUtil.GBToUnicode(patt.getName()));
	if (ext.equals("doc") || ext.equals("docx")){
		response.setContentType("application/msword");
	}
	else if (ext.equals("xls") || ext.equals("xlsx")){
		response.setContentType("application/vnd.ms-excel");
	}
	else if (ext.equals("wps") || ext.equals("wpt") || ext.equals("dps") || ext.equals("et")) {
	// response.setContentType("application/msword");
		response.setContentType("application/octet-stream");
	}else if (ext.equals("ppt") || ext.equals("pptx")){
		response.setContentType("application/vnd.ms-powerpoint");
	}else{
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

String diskName = patt.getDiskName();
int len = diskName.length();
response.setHeader("Content-disposition","filename=" + StrUtil.GBToUnicode(patt.getName()));

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String file_netdisk = cfg.get("file_netdisk_public");

String realPath = Global.getRealPath() + file_netdisk + "/" + patt.getVisualPath() + "/" + patt.getDiskName();
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
	// System.out.println(getClass() + " " + Global.getRealPath() + file_netdisk + "/" + patt.getVisualPath() + "/" + patt.getDiskName());
	bis = new BufferedInputStream(new FileInputStream(realPath));
	
	bos = new BufferedOutputStream(response.getOutputStream());
	
	byte[] buff = new byte[2048];
	int bytesRead;
	
	while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		bos.write(buff,0,bytesRead);
	}
	
} catch(Exception e) {
	e.printStackTrace();
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
}
%>