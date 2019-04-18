<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.integration.cwbbs.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.security.*"%>
<%@ page import = "com.redmoon.forum.security.*"%>
<%@ page import = "com.redmoon.chat.ChatClient"%>
<%@ page import = "cn.js.fan.util.*" %>
<%@ page import = "cn.js.fan.web.*" %>
<%@ page import = "java.util.Properties" %>
<%@ page import = "rtx.*" %>
<%@ page import = "org.json.*"%>
<jsp:useBean id="login" scope="page" class="cn.js.fan.security.Login"/>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
  String keyId=ParamUtil.get(request, "keyid");
  String name=ParamUtil.get(request, "name");
  UserSetupDb usdDB = new UserSetupDb();
  UserSetupDb usd = usdDB.getUserSetupDbByKeyid(keyId);
  if("".equals(name)||name==null)//新增
  {
       if(usd!=null)
	  {
	      
		  JSONObject json = new JSONObject();
		  json.put("ret", "1");//已被占用
		  json.put("msg", "检测到KEY已被 "+usd.getUserName()+" 使用");
		  out.print(json); 
	  }else
	  {
	      JSONObject json = new JSONObject();
		  json.put("ret", "0");//未被占用
		  json.put("msg", "KEY未被占用");
		  out.print(json); 
	  }
  }
  else
  {
     if(usd!=null)
	  {
	      if(name.equals(usd.getUserName()))
	      {
	        JSONObject json = new JSONObject();
		    json.put("ret", "0");//未被占用
		    json.put("msg", "KEY未被占用");
		    out.print(json);
	      }
	      else
	      {
	        JSONObject json = new JSONObject();
		    json.put("ret", "1");//已被占用
		    json.put("msg", "检测到KEY已被 "+usd.getUserName()+" 使用");
		    out.print(json); 
	      }
	  }else
	  {
	      JSONObject json = new JSONObject();
		  json.put("ret", "0");//未被占用
		  json.put("msg", "KEY未被占用");
		  out.print(json); 
	  }
  }
  
  
%>