<%@ page contentType="text/html;charset=utf-8"%>
<%@page import="cn.js.fan.util.*"%>
<%@page import="cn.js.fan.web.Global"%>
<%@page import="com.redmoon.oa.*"%>
<%@page import="com.redmoon.oa.message.*"%>
<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<%@page import="java.net.*"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.JSONException"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
- 功能描述：手机端使用 
- 访问规则：短消息附件下载
- 过程描述： 
- 注意事项：
- 创建者：fgf 
- 创建时间：
*/
JSONObject result = new JSONObject(); 
String skey = ParamUtil.get(request,"skey");
com.redmoon.oa.android.Privilege pri = new com.redmoon.oa.android.Privilege();
String userName = pri.getUserName(skey);

if(userName.equals("")){
	try {
		result.put("res","-1");
		result.put("msg","skey不存在");
		out.println(result.toString());
		return;
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	}
}

int msgId = ParamUtil.getInt(request, "msgId");
int attId = ParamUtil.getInt(request, "attachId");

MessageMgr mm = new MessageMgr();
MessageDb md = mm.getMessageDb(msgId);
Attachment att = md.getAttachment(attId);

String s = Global.getRealPath() + att.getVisualPath() + "/" + att.getDiskName();

java.io.File f = new java.io.File(s);
response.setHeader("Content-Length", new Long(f.length()).toString()); 

response.setContentType(MIMEMap.get(StrUtil.getFileExt(att.getDiskName())));
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(att.getName()));

// response.setContentType("application/octet-stream");
// response.setHeader("Content-disposition","attachment; filename="+att.getName());

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
	System.out.println( "IOException." + e );
} finally {
	if (bis != null)
		bis.close();
	if (bos != null)
		bos.close();
}

out.clear();
out = pageContext.pushBody();
%>