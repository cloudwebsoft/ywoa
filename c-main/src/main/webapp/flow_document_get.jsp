<%@ page contentType="text/html; charset=utf-8" %>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
response.setHeader("Pragma","No-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);

String priv="read";
if (!privilege.isUserPrivValid(request,priv))
{
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

//String department_id = privilege.getDepartmentID(request);
//String document_id = request.getParameter("document_id");
//String priv = "department";//部门管理
//String user = fchar.UnicodeToGB(request.getParameter("user"));
//String pwd = request.getParameter("pwd");
//if (!document.canModifyDoc(document_id,department_id) && !privilege.isUserPrivValid(user,pwd,priv))
//{
//	out.println(fchar.makeErrMsg("警告非法用户，你无访问此页的权限！"));
//	return;
//}

int docId = ParamUtil.getInt(request, "doc_id");
int fileId = ParamUtil.getInt(request, "file_id");

DocumentMgr dm = new DocumentMgr();
Document doc = dm.getDocument(docId);
Attachment att = doc.getAttachment(1, fileId);
if (att==null) {
	out.println("取文件" + docId + "的附件" + fileId + "时，未找到！");
	return;
}


String diskName = att.getDiskName();
int len = diskName.length();
// 当att.getName()为中文时，会导致控件在解析attachment; filename=时，读取不到后面的中文，待查
// response.setHeader("Content-disposition","attachment; filename="+att.getName());
response.setHeader("Content-disposition","attachment; filename="+att.getDiskName());
// String ext = diskName.substring( len-3, len );
String ext = StrUtil.getFileExt(diskName);
if (ext.equals("doc") || ext.equals("docx"))
	response.setContentType("application/msword");
else if (ext.equals("xls") || ext.equals("xlsx")) {
	response.setContentType("application/vnd.ms-excel");
}
else {
	out.println("文件格式" + ext + "非法");
	return;
}

// System.out.println("att name=" + att.getName() + " diskName=" + att.getDiskName() + " ext=" + ext);

BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
	bis = new BufferedInputStream(new FileInputStream(att.getFullPath()));
	bos = new BufferedOutputStream(response.getOutputStream());
	
	byte[] buff = new byte[2048];
	int bytesRead;
	
	while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
		bos.write(buff,0,bytesRead);
	}
} catch(final IOException e) {
	System.out.println ( "出现IOException." + e + "---" + att.getFullPath());
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
}

out.clear();
out = pageContext.pushBody();
%>