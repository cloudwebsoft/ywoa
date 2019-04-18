<%@ page contentType="text/html;charset=gb2312"%><%@page import="cn.js.fan.util.*"%>
<%@page import="cn.js.fan.web.*"%>
<%@page import="com.redmoon.oa.*"%>
<%@page import="com.redmoon.oa.dept.*"%>
<%@page import="com.redmoon.oa.netdisk.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = request.getParameter("priv");
if (priv==null)
	priv = "read";
/*
if (!privilege.isUserPrivValid(request, priv)) {
	//response.setContentType("text/html;charset=gb2312"); 
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
*/

int id = ParamUtil.getInt(request, "id");
int attId = ParamUtil.getInt(request, "attachId");

Document mmd = new Document();
mmd = mmd.getDocument(id);
if (mmd==null || !mmd.isLoaded()) {
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
	out.println("文件不存在！");
	return;
}
Attachment att = mmd.getAttachment(1, attId);

LeafPriv lp = new LeafPriv(att.getDirCode());
if (!lp.canUserSee(privilege.getUser(request))) {
	// 如果没有共享的权限，则检查文件是否为已发布
	String[] depts = StrUtil.split(att.getPublicShareDepts(), ",");
	int len = 0;
	if (depts!=null)
		len = depts.length;
	boolean isValid = false;
	if (len==0)
		isValid = true;
	else {	
		DeptUserDb du = new DeptUserDb();
		for (int i=0; i<len; i++) {
			if (du.isUserOfDept(privilege.getUser(request), depts[i])) {
				isValid = true;
				break;
			}
		}
	}
	if (!isValid) {
		out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
}

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String file_netdisk = cfg.get("file_netdisk");

String s = Global.getRealPath() + file_netdisk + "/" + att.getVisualPath() + "/" + att.getDiskName();

java.io.File f = new java.io.File(s);
response.setHeader("Content-Length", new Long(f.length()).toString()); 

	// response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
	response.setContentType(MIMEMap.get(att.getExt()));
	response.setHeader("Content-disposition","attachment; filename=" + StrUtil.GBToUnicode(att.getName()));
	
	// 以询问下载的方式打开，会覆盖父窗口
	// response.setContentType("application/octet-stream");
	// response.setHeader("Content-disposition","attachment; filename=" + StrUtil.GBToUnicode(att.getName()));
	
	BufferedInputStream bis = null;
	BufferedOutputStream bos = null;
	
	try {
		bis = new BufferedInputStream(new FileInputStream(s));
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