<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "com.redmoon.oa.integration.cwbbs.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
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
  UserSetupDb usdDB = new UserSetupDb();
  UserSetupDb usd = usdDB.getUserSetupDbByKeyid(keyId);
  Thread.sleep(1000l);
  if(usd!=null)
  {
	  JSONObject json = new JSONObject();
	  json.put("ret", "1");
	  json.put("msg", "<B>"+usd.getUserName()+"</B> KEY校验通过,请登录系统!");
	  out.print(json);
  }else
  {
      JSONObject json = new JSONObject();
	  json.put("ret", "0");
	  json.put("msg", "KEY校验失败，请尝试其它方式登录"); 
	  out.print(json);
  }
%>