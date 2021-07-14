<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.netdisk.Attachment"%>
<%@page import="cn.js.fan.web.Global"%>
<%@page import="java.io.*"%><%
String priv = request.getParameter("priv");
if (priv==null)
	priv = "read";
int op  = ParamUtil.getInt(request,"op",0);
if(op==0){
	return;
}
int attId = ParamUtil.getInt(request,"id",0);
if(attId==0){
	return;
}
Attachment att = new Attachment(attId);
com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String realPath = Global.getRealPath() + "/"
	+ cfg.get("file_netdisk") + "/" + att.getVisualPath() + "/"
	+ att.getName();
File file = new File(realPath);
if (!file.exists()) {
	out.print("文件不存在！");
	return;
}

response.setContentType("application/"+att.getExt());
response.setHeader("Content-disposition","attachment; filename="+att.getName());

BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
bis = new BufferedInputStream(new FileInputStream(file));
bos = new BufferedOutputStream(response.getOutputStream());

byte[] buff = new byte[2048];
int bytesRead;

while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
bos.write(buff,0,bytesRead);
}

} catch(final IOException e) {
} finally {
if (bis != null)
bis.close();
if (bos != null)
bos.close();
}
%>



