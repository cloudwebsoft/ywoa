<%@ page contentType="text/html;charset=utf-8"%>  
<%@ page import="java.io.File" %>
<%@ page import="java.io.OutputStream" %>
<%@ page import="java.io.FileInputStream" %> 
<%@ page import="cn.js.fan.web.Global"%> 
<%@ page import="cn.js.fan.util.*" %> 
<%@ page import="javax.servlet.*" %>
<%@page import="cn.js.fan.web.SkinUtil"%> 
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege" />
<% 
if (!privilege.isUserLogin(request)) {
	out.print("对不起，请先登录！");
	 return;
}
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.print(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String userName = ParamUtil.get(request,"userName");
if("".equals(userName)){
	userName = privilege.getUser(request);
}

String path=ParamUtil.get(request,"path"); 
if (path.startsWith("/")){
	path = path.substring(1);
}
String file = "";
try{
	file = Global.getAppPath() + "netdisk/"+ path;
}catch(Exception e){
	e.printStackTrace();
}
FileInputStream in =null;
OutputStream o = null;
try{
	if (!file.equals("")){
		File tempFile = new File(file);
		o  = response.getOutputStream();
		if (tempFile.exists()){
			in = new FileInputStream(tempFile);
		 	int l = 0; 
		  	byte[] buffer = new byte[4096]; 
			while((l = in.read(buffer)) != -1){ 
				o.write(buffer,0,l);  
			}
			//o.flush(); 
		}
	}
 }catch(Exception e){
 	e.printStackTrace();
 }
 finally{
 	if (in != null){
 		try{
			in.close();
		}catch(Exception e1) {
			e1.printStackTrace();
		}
	}
	 if (o != null){
		try{
			o.close();
		} catch(Exception e2) {
			e2.printStackTrace();
		}
	} 
 }
out.clear(); 
out = pageContext.pushBody();

%> 

