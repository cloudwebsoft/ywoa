<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="com.redmoon.oa.message.MessageDb"%>
<%@page import="com.redmoon.oa.android.xinge.SendNotice"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="org.json.JSONObject"%>
<html>
  <head>
  </head>
  <script type="text/javascript" src="../js/jquery.js"></script>
  <body>
  <%
  	InputStream resStream = request.getInputStream();
  	BufferedReader br = new BufferedReader(new InputStreamReader(
			resStream));
  	try{
		StringBuffer resBuffer = new StringBuffer(); 
  		String s  = "";
  		while ((s = br.readLine()) != null) {
			resBuffer.append(s);
			resBuffer.append("<br/>");
		}
  		JSONObject js = new JSONObject(resBuffer.toString());
  		String title = js.get("title").toString();
  		String content = js.get("content").toString();
	  //String title = ParamUtil.get(request,"title");
	 // String content = ParamUtil.get(request,"content");
	  MessageDb md = new MessageDb();
	  md.sendSysMsg("admin",title,content);
	  
	  //add by lichao 手机端消息推送
		SendNotice se = new SendNotice();
		se.PushNoticeToAdmin("admin", title, content); 
  		}catch( Exception e){
  			e.printStackTrace();
  		}finally{
  			br.close();
  			resStream.close();
  		}
	  
   %>
  </body>
</html>
