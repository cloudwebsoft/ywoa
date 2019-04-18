<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="cn.js.fan.web.Global"%>
<%@page import="com.redmoon.oa.*"%>
<%@page import="com.redmoon.oa.notice.*"%>
<%@page import="com.redmoon.oa.workplan.*"%>
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
if (!privilege.isUserPrivValid(request, priv))
{
	//response.setContentType("text/html;charset=gb2312"); 
	out.print("<meta http-equiv='Content-Type' content='text/html; charset=gb2312'>");
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

int noticeId = ParamUtil.getInt(request, "noticeId");
int attId = ParamUtil.getInt(request, "attachId");
NoticeAttachmentDb att = new NoticeAttachmentDb(attId);

if (!att.isLoaded()) {
	out.print("文件不存在！");
	return;
}
/*
NoticeDb td = new NoticeDb();
td = td.getNoticeDb(noticeId);
Vector v = new Vector();
v = td.getAttachs();
if(v.size()>0){
	Iterator ir = null;
	ir = v.iterator();
	while(ir.hasNext()){
		att = (NoticeAttachmentDb)ir.next();
		if(att.getId()==attId){
			break;
		}
	}
}
*/
// 用下句会使IE在本窗口中打开文件
// response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
// 使客户端直接下载，上句会使IE在本窗口中打开文件，下句也一样，�?
response.setContentType("application/octet-stream");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(att.getName()));

BufferedInputStream bis = null;
BufferedOutputStream bos = null;
//System.out.println("http://141.60.16.99/"+att.getVisualPath()+att.getDiskName());
try {
	bis = new BufferedInputStream(new FileInputStream(Global.realPath+att.getVisualPath()+"/"+att.getDiskName()));
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



