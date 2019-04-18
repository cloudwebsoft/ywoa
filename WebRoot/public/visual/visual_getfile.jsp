<%@ page contentType="text/html;charset=gb2312"%><%@page import="cn.js.fan.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsMgr"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsDb"%>
<%@page import="com.redmoon.oa.visual.Attachment"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@page import="org.json.JSONObject"%><%@page import="org.json.JSONException"%>
<%
int id = ParamUtil.getInt(request, "attachId");
Attachment att =  null;
JSONObject result = new JSONObject(); 
if(id == 0){
 	try {
		result.put("res","-1");
		result.put("msg","¸½¼þ²»´æÔÚ£¡");
		out.println(result.toString());
		return;
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
att = new Attachment(id);
response.setContentType("application/octet-stream");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(att.getName()));
String url = att.getFullPath();
BufferedInputStream bis = null;
BufferedOutputStream bos = null;

try {
	bis = new BufferedInputStream(new FileInputStream(url));
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