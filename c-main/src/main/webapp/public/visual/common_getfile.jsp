<%@ page contentType="text/html;charset=gb2312"%><%@page import="cn.js.fan.util.*"%><%@page import="java.io.*"%><%@page import="java.net.*"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsMgr"%>
<%@page import="com.redmoon.oa.mobileskins.MobileSkinsDb"%>
<%@page import="com.redmoon.oa.visual.Attachment"%>
<%@page import="com.redmoon.oa.android.Privilege"%>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="com.redmoon.oa.android.base.BaseAction"%>
<%@page import="com.redmoon.oa.worklog.WorkLogAttachmentDb"%>
<%@page import="cn.js.fan.web.Global"%><jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="fsecurity" scope="page" class="cn.js.fan.security.SecurityUtil"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%@page import="org.json.JSONObject"%><%@page import="org.json.JSONException"%>
<%
int id = ParamUtil.getInt(request, "attId");
int fileType = ParamUtil.getInt(request,"fileType");
String skey = ParamUtil.get(request,"skey");
Attachment att =  null;
JSONObject result = new JSONObject(); 
Privilege privMobile = new Privilege();
String userName = StrUtil.getNullStr(privMobile.getUserName(skey));
UserDb userDb = new UserDb(userName);
JSONObject res = new JSONObject();
try {
	res.put("res",BaseAction.RESULT_SUCCESS);
	if(skey.equals("") || !userDb.isLoaded()){
		result.put("returnCode",BaseAction.RESULT_SKEY_ERROR);
		res.put("result",result);
		out.println(res.toString());
		return;
	}
	
} catch (JSONException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}
String url = "";
String fileName = "";
if(fileType == BaseAction.WORK_LOG_ATT_FILE_TYPE){
	WorkLogAttachmentDb wlad = new WorkLogAttachmentDb(id);
	fileName = wlad.getName();
	url = Global.getRealPath()+"/"+wlad.getVisualPath()+wlad.getDiskName();
}

response.setContentType("application/octet-stream");
response.setHeader("Content-disposition","attachment; filename="+StrUtil.GBToUnicode(fileName));
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