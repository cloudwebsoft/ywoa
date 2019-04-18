<%@ page import="java.io.File" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.io.FileInputStream" %> 
<%@ page import="cn.js.fan.web.Global"%> 
<%@ page import="cn.js.fan.util.*" %> 
<%@ page import="javax.servlet.*" %>
<%@page import="com.redmoon.oa.person.UserDb"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="com.redmoon.oa.android.CloudConfig"%>
<%@page import="cn.js.fan.security.ThreeDesUtil"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>

<% 
JSONObject result = new JSONObject(); 
String skey = ParamUtil.get(request,"skey");
CloudConfig cfg = CloudConfig.getInstance();
skey = ThreeDesUtil.decrypthexstr(cfg.getProperty("key"), skey);
String[] ary = skey.split("&");
if (ary.length != 2) {
	return;
}

String[] ary1 = ary[0].split("=");
if (ary1.length != 2) {
	return;
}
if (!ary1[0].equals("path")) {
	return;
}

String[] ary2 = ary[1].split("=");
if (ary2.length != 2) {
	return;
}
if (!ary2[0].equals("skey")) {
	return;
}

skey = ary2[1];
String[] ary3 = skey.split("\\|");
if (ary3.length != 3) {
	return;
}
String userName = ary3[0];

if (userName.equals("")) {
	result.put("res", "-1");
	result.put("msg", "skey不存在");
	out.println(result.toString());
	return;
}

String path = ary1[1];
String file = Global.getRealPath() + File.separator + path;
FileInputStream in = null;
OutputStream o = null;

try {
	File tempFile = new File(file);
	if (!tempFile.exists()){
		tempFile = new File(Global.getAppPath() + path);
	}
	o  = response.getOutputStream();
	if (tempFile.exists()){
		in = new FileInputStream(tempFile);
		int l = 0; 
		byte[] buffer = new byte[4096]; 
		while((l = in.read(buffer)) != -1){ 
			o.write(buffer,0,l);  
		}
		o.flush(); 
	}
} catch (Exception e) {
} finally {
 	if (in != null) {
		in.close(); 
	}
	 if (o != null) {
		o.close(); 
	} 
}
out.clear(); 
out = pageContext.pushBody();

%> 

