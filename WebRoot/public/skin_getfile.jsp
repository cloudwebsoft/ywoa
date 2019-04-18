<%@ page contentType="text/html;charset=gb2312"%><%@page import="cn.js.fan.util.*"%><%@page import="com.redmoon.oa.flow.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsMgr"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsDb"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@page import="org.json.JSONObject"%><%@page import="org.json.JSONException"%>
<%
String code = ParamUtil.get(request, "code");
JSONObject result = new JSONObject(); 
if(code.equals("")){
 	try {
		result.put("res","-1");
		result.put("msg","code²»ÄÜÎª¿Õ£¡");
		out.println(result.toString());
		return;
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
response.setContentType("application/octet-stream");
MobileSkinsMgr mobileSkinMgr = new MobileSkinsMgr();
MobileSkinsDb mobileSkinDb = mobileSkinMgr.getMobileSkinsDb(code);
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(mobileSkinDb.getString("disk_name")));
String url = cn.js.fan.web.Global.getRealPath() + "/" +mobileSkinDb.getString("visual_path");
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