<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "com.redmoon.oa.kernel.*"%>
<%@ page import = "com.redmoon.oa.licenceValidate.*"%>
<%@ page import = "com.redmoon.oa.licenceValidate.diskno.*"%>
<%@ page import = "com.redmoon.forum.person.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "com.redmoon.oa.Config"%>
<%@ page import = "java.io.*"%>
<%@ page import = "java.net.*"%>
<%!
public String getWebIp(String strUrl) {
	try {
		URL url = new URL(strUrl);
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

		String s = "";
		StringBuffer sb = new StringBuffer("");
		String webContent = "";

		while ((s = br.readLine()) != null) {
			sb.append(s + "\r\n");

		}

		br.close();
		webContent = sb.toString();
		int start = webContent.indexOf("[") + 1;
		int end = webContent.indexOf("]");
		webContent = webContent.substring(start, end);

		return webContent;
	} catch (Exception e) {
		e.printStackTrace();
		return "";
	}
}
%>
<%
String ip ="";
ip = getWebIp("http://www.ip138.com/ip2city.asp");

Config cg = new Config();
String yimihomeURL = cg.get("yimihome_url");

String op = ParamUtil.get(request, "op");
if (op.equals("checkEntNo")) {
	String enterpriseNo= ParamUtil.get(request,"enterpriseNo");
		
	boolean flag = false;
	int re = -1;
	
	try {
		re  = HttpClientLoginValidate.HttpClientValEntNo( yimihomeURL + "/httpClientServer/httpclient_server_checkentno.jsp",enterpriseNo);
	}catch (IOException e){
		e.getMessage();
		out.print(-1);
		return;
	}
	
	out.print(re);
	    
	return;
}else if (op.equals("checkEntEmail")) {
	//String enterpriseNo = ParamUtil.get(request,"enterpriseNo");
	//String email = ParamUtil.get(request,"email");
	
	String enterpriseNo = request.getParameter("enterpriseNo");
	String email = request.getParameter("email");
	
	JSONArray messageArr = new JSONArray();
	JSONObject messageObj = new JSONObject();
	
	messageObj.put("enterpriseNo",enterpriseNo);
	messageObj.put("email",email);
	messageArr.put(messageObj);
	
	boolean flag = false;
	
	try {
		flag = HttpClientLoginValidate.HttpClientValEntEmail(yimihomeURL + "/httpClientServer/httpclient_server_check_entno_and_entemail.jsp",messageArr);
	}catch (IOException e){
		e.getMessage();
	}
	
    if (flag) {
      out.print("");
    }else{
      out.print("邮箱与企业号不匹配");
    }
    
	return;
}else if (op.equals("checkEmail")) {
	String email = ParamUtil.get(request,"email");
	
	boolean flag = false;

	try {
		flag = HttpClientLoginValidate.HttpClientVal(yimihomeURL + "/httpClientServer/httpclient_server_checkemail.jsp",email);
	}catch (IOException e){
		e.getMessage();
	}
	
    if (flag) {
      out.print("");
    }else{
      out.print("邮箱已注册过");
    }
    
	return;
} else if (op.equals("checkMobile")) {
	String mobile = ParamUtil.get(request,"mobile");
	
	boolean flag = false;
	
	try {
		flag = HttpClientLoginValidate.HttpClientVal(yimihomeURL + "/httpClientServer/httpclient_server_checkmobile.jsp",mobile);
	}catch (IOException e){
		e.getMessage();
	}
	
    if (flag) {
      out.print("");
    }else{
      out.print("手机号码已注册过");
    }
    
	return;
}else if (op.equals("enterpiseLogin")) {
	// 激活
	String enterpriseNo = ParamUtil.get(request,"entNo");
	String email = ParamUtil.get(request,"entEmail");

	JSONArray messageArr = new JSONArray();
	JSONObject messageObj = new JSONObject();
	
	messageObj.put("enterpriseNo",enterpriseNo);
	messageObj.put("email",email);
	//messageArr.put(messageObj);
	
	boolean flag = false;
	
	try {
		flag = HttpClientLoginValidate.HttpClientEntLogin(yimihomeURL + "/httpClientServer/httpclient_server_entnologin.jsp",messageObj);
	}catch (Exception e){
	    out.print("企业号激活失败");
		e.getMessage();
		return;
	}
	
	LicenseDownload ld = new LicenseDownload();
	
	try {
		if(flag){
			ld.download(yimihomeURL + "/httpClientServer/license_download_server.jsp",enterpriseNo,"officialVersion");
			License.getInstance().init();
		}else{
			// 如果验证不通过，则下载试用许可证
			ld.download(yimihomeURL + "/httpClientServer/license_download_server.jsp",enterpriseNo,"trialVersion");
		}
	}catch (Exception e){
	    out.print("证书下载失败");
		e.getMessage();
		return;
	}
	
	JSONObject json = new JSONObject();
    if (flag) {
      json.put("ret", "1");
      json.put("msg", HttpClientLoginValidate.getMsg());
    }else{
      json.put("ret", "0");    
      // out.print("企业号激活失败");
      out.print(HttpClientLoginValidate.getMsg());
    }
    out.print(json);
	return;
}else if (op.equals("regist")) {
	//String entName = ParamUtil.get(request,"entName");
	//String entEmail = ParamUtil.get(request,"entEmail");
	//String linkMan = ParamUtil.get(request,"linkMan");
	//String mobile = ParamUtil.get(request,"mobile");
	
	String entName = request.getParameter("entName");
	String entEmail = request.getParameter("entEmail");
	String linkMan = request.getParameter("linkMan");
	String mobile = request.getParameter("mobile");
	
	JSONArray messageArr = new JSONArray();
	JSONObject messageObj = new JSONObject();
		
	messageObj.put("entName",entName);
	messageObj.put("entEmail",entEmail);
	messageObj.put("linkMan",linkMan);
	messageObj.put("mobile",mobile);
	messageObj.put("ip",ip);
	//messageObj = DiskNo.GetDiskNoOrMotherboardNo(messageObj); //获取硬盘号或者主板号
		
	boolean flag = false;
	
	try {
		messageObj = HttpClientLoginValidate.HttpClientRegist(yimihomeURL + "/httpClientServer/httpclient_server_regist.jsp",messageObj);
	}catch (JSONException e){
		e.getMessage();
		out.print("");
		return;
	}catch (IOException e){
		e.getMessage();
		out.print("");
		return;
	}catch (Exception e){
		e.getMessage();
		out.print("");
		return;
	}

	int ret = messageObj.getInt("ret");
	String enterpriseNum = messageObj.getString("enterpriseNum");
	
	boolean re =false;
	if(ret==1 && !enterpriseNum.equals("")) {
		RegistInfoMgr rr = new RegistInfoMgr();	//注册信息表regist_info中插入数据
		try {
			re = rr.create(enterpriseNum,entEmail);
		}catch(ErrMsgException e){
			e.printStackTrace();
			out.print("");
		}
		out.print(messageObj.getString("msg"));
		// out.print("注册成功");
	}else if(ret==0){
		out.print("");
	}
	    
    return;
}else if (op.equals("restartTomcat")) {
	out.print("restartok");

    return;
}
%>