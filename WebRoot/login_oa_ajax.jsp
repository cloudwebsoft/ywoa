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
<%@ page import = "com.cloudwebsoft.framework.util.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.*"%>
<%@page import="com.redmoon.oa.kernel.License"%>
<jsp:useBean id="login" scope="page" class="cn.js.fan.security.Login"/>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String userName = ParamUtil.get(request, "name");

// 防跨站点请求伪造
String callingPage = request.getHeader("Referer");
if (callingPage == null
		|| callingPage.indexOf(request.getServerName()) != -1) {
} else {
	if (callingPage.indexOf(Global.server)==-1) {
		com.redmoon.oa.LogUtil.log(userName, StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF ajax_online.jsp");
		out.print(SkinUtil.LoadString(request, "op_invalid"));
		return;
	}
}
	
boolean re = false;
com.redmoon.oa.security.Config scfg = com.redmoon.oa.security.Config.getInstance();
if (scfg.isDefendBruteforceCracking()) {
	try {
		login.canlogin(request, "redmoonoa");
	}
	catch (ErrMsgException e) {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}
}
// 如果输入密码一次就会报参数非法，请刷新页面后重试
if (!"".equals(userName)&&"".equals(ParamUtil.get(request, "keyId")))
{
	if (false && !cn.js.fan.security.Form.isTokenValid(request)) {
		String name = userName;
		
	    com.redmoon.oa.LogUtil.log(name, StrUtil.getIp(request), com.redmoon.oa.LogDb.TYPE_HACK, "CSRF login_oa_ajax.jsp");
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		// 参数非法
		json.put("msg", SkinUtil.LoadString(request, "param_invalid") + "，请刷新页面后重试！");
		out.print(json);
		return;
	}
} 

//比对是否和数据库中的版本相同
com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
com.redmoon.oa.SpConfig spCfg = new com.redmoon.oa.SpConfig();
String ver = StrUtil.getNullStr(oaCfg.get("version"));
String sp_ver = StrUtil.getNullStr(spCfg.get("version"));
try{
	JdbcTemplate jt = new JdbcTemplate();
	String sql = "select version,sp_version from oa_sys_ver";
	cn.js.fan.db.ResultIterator ri = jt.executeQuery(sql);
	String version = "";
	String sp_version = "";
	if (ri != null && ri.size() == 1){
		while(ri.hasNext()){
			cn.js.fan.db.ResultRecord rr = (cn.js.fan.db.ResultRecord) ri.next();
			version = StrUtil.getNullStr(rr.getString("version")).trim();
			sp_version = StrUtil.getNullStr(rr.getString("sp_version")).trim();
			break;
		}
	}
	if(!ver.equals(version) || !sp_ver.equals(sp_version)){
		JSONObject json = new JSONObject();
	    json.put("ret", "0");
	    json.put("msg", ver + "(" + sp_ver + ")与数据库版本" + version + "(" + sp_version + ")不匹配，请联系客服");
	    out.print(json);
	    return;
	}
}catch(Exception e){
	JSONObject json = new JSONObject();
    json.put("ret", "0");
    json.put("msg", "数据库连接错误");
    out.print(json);
    return;
}
try {
	re = privilege.login(request, response);
	if (scfg.isDefendBruteforceCracking()) {
		login.afterlogin(request,re,"redmoonoa",true);
	}
}
catch(WrongPasswordException e){
	JSONObject json = new JSONObject();
	json.put("ret", "0");
	json.put("msg", e.getMessage());
	out.print(json);
	return;
}
catch (NullPointerException e) {
    JSONObject json = new JSONObject();
    json.put("ret", "0");
    json.put("msg", "数据库连接错误！");
    out.print(json);
    return;
}
catch (InvalidNameException e) {
	JSONObject json = new JSONObject();
	json.put("ret", "0");
	json.put("msg", e.getMessage());
	out.print(json);
	return;
}
catch (ErrMsgException e) {
	String str = e.getMessage();
	if (str.startsWith("-RTX")) {
		// 传参数reverseRTXLogin=false防止重新自动反向登录
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("msg", "");
		json.put("redirect", "index.jsp?reverseRTXLogin=false");
		out.print(json);
	}
	else {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
	}
	return;
}
if (re) {
	String serverName = request.getServerName();
	// System.out.println(getClass() + " serverName=" + serverName);
	ServerIPPriv sip = new ServerIPPriv(serverName);
	if (!sip.canUserLogin(userName)) {		
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", "禁止登录！");
		out.print(json);
		return;
	}
	//删除登录云盘校验 modify by jfy 2015-06-11
    /**com.redmoon.oa.android.CloudConfig cfg = com.redmoon.oa.android.CloudConfig.getInstance();
	try {
		re = cfg.canUserLogin(request);
	}
	catch (ErrMsgException e) {
		JSONObject json = new JSONObject();
		json.put("ret", "0");
		json.put("msg", e.getMessage());
		out.print(json);
		return;
	}*/
}

String op = ParamUtil.get(request, "op");
if (re) {
	String name = userName;
	UserDb user = new UserDb();
	user = user.getUserDb(name);

	if (scfg.getIntProperty("isPwdCanReset")==1) {
		// 检查用户的邮箱信息是否已完善
		String email = user.getEmail();
		if ("".equals(email)) {
			JSONObject json = new JSONObject();
			String url = "user/user_info_setup.jsp";
			json.put("ret", "1");
			json.put("msg", "");
			json.put("redirect", url);
			out.print(json);
			return;
		}
	}

	if (scfg.isForceChangeInitPassword()) {
		// 判断是否初始密码
		String pwd = ParamUtil.get(request, "pwd");
		if (pwd.equals(scfg.getInitPassword())) {
			// response.sendRedirect("oa_change_initpwd.jsp");
			JSONObject json = new JSONObject();
			String url = "oa_change_initpwd.jsp";
			json.put("ret", "1");
			json.put("msg", "");
			json.put("redirect", url);
			out.print(json);
			return;
		}
	}

	com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
	boolean isRTXUsed = cfg.get("isRTXUsed").equals("true");

	CWBBSConfig ccfg = CWBBSConfig.getInstance();
	if (ccfg.getBooleanProperty("isUse")) {
		PassportRemoteUtil pru = new PassportRemoteUtil();
		request.setAttribute("uid", user.getName());
		request.setAttribute("desc", user.getAddress());
		request.setAttribute("pwd", user.getPwdMD5());
		request.setAttribute("realname", user.getRealName());
		pru.remoteSuperLogin(request, response, ccfg.getProperty("url"), ccfg.getProperty("key"), Global.getFullRootPath(request) + "/oa.jsp");
	}
	else {
		String mainTitle = ParamUtil.get(request, "mainTitle");
		String mainPage = ParamUtil.get(request, "mainPage");
		String queryStr = "";
		if (!mainPage.equals("")) {
			mainPage = cn.js.fan.security.AntiXSS.antiXSS(mainPage);
		}
		queryStr = "?mainTitle=" + StrUtil.UrlEncode(mainTitle) + "&mainPage=" + mainPage;
		
		UserSetupDb usd = new UserSetupDb();
		// 注意不能用name作为参数，因为可能是用工号登录的
		// usd = usd.getUserSetupDb(name);
		usd = usd.getUserSetupDb(privilege.getUser(request));
		
		boolean isSpecified = cfg.get("styleMode").equals("2"); 
		// 指定风格
		if (isSpecified) {
			int styleSpecified = StrUtil.toInt(cfg.get("styleSpecified"), -1);
			if (styleSpecified!=-1) {
				String url = "";
				if (styleSpecified==UserSetupDb.UI_MODE_PROFESSION) {
					url = "oa.jsp" + queryStr;
				} 
				else if (styleSpecified==UserSetupDb.UI_MODE_FLOWERINESS) {
					url = "mydesktop.jsp" + queryStr;					
				} 
				else if (styleSpecified==UserSetupDb.UI_MODE_FASHION) {
					url = "main.jsp" + queryStr;
				} 
				else if (styleSpecified == UserSetupDb.UI_MODE_PROFESSION_NORMAL) {
					url = "oa_main.jsp" + queryStr;// 经典型传统菜单
				}
				else if (styleSpecified == UserSetupDb.UI_MODE_LTE) {
					url = "lte/index.jsp" + queryStr;
				}				
				else {
					if (usd.getMenuMode()==UserSetupDb.MENU_MODE_NEW) {				
						url = "oa.jsp" + queryStr;
					}
					else {
						url = "oa_main.jsp" + queryStr;
					}
				}
				
				JSONObject json = new JSONObject();
				json.put("ret", "1");
				json.put("msg", "");
				json.put("redirect", url);
				out.print(json);
				
				return;
			}
		}
		
		String os = ParamUtil.get(request, "os");
		// Safari
		if (os.equals("4")) {			
			JSONObject json = new JSONObject();
			json.put("ret", "1");
			json.put("msg", "");
			json.put("redirect", "main.jsp" + queryStr);
			out.print(json);
			return;
		}
		
		String url = "";
		if (usd.getUiMode()==UserSetupDb.UI_MODE_NONE) {
			// response.sendRedirect("ui_mode_guide.jsp" + queryStr);
			url = "ui_mode_guide.jsp" + queryStr;
			com.redmoon.oa.kernel.License license = com.redmoon.oa.kernel.License.getInstance();    
			if(license.isVip()) {
				url = "ui_mode_guide.jsp" + queryStr;
			}else{
				if (usd.getMenuMode()==UserSetupDb.MENU_MODE_NEW) {
					url = "oa.jsp" + queryStr;
				}
				else {
					url = "oa_main.jsp" + queryStr;
				}
			}
		} else if (usd.getUiMode()==UserSetupDb.UI_MODE_PROFESSION) {
			// response.sendRedirect("oa.jsp" + queryStr);
			if (usd.getMenuMode()==UserSetupDb.MENU_MODE_NEW) {					
				url = "oa.jsp" + queryStr;
			}
			else {
				url = "oa_main.jsp" + queryStr;
			}
		} else if (usd.getUiMode()==UserSetupDb.UI_MODE_FLOWERINESS) {
			// response.sendRedirect("mydesktop.jsp" + queryStr);
			url = "mydesktop.jsp" + queryStr;
		} else if (usd.getUiMode()==UserSetupDb.UI_MODE_FASHION) {
			// response.sendRedirect("main.jsp" + queryStr);
			url = "main.jsp" + queryStr;
		} 
		else if (usd.getUiMode() == UserSetupDb.UI_MODE_LTE) {
			url = "lte/index.jsp" + queryStr;
		}			
		else {
			if (usd.getMenuMode()==UserSetupDb.MENU_MODE_NEW) {		
				url = "oa.jsp" + queryStr;
			}
			else {
				url = "oa_main.jsp";
			}
		}
		JSONObject json = new JSONObject();
		json.put("ret", "1");
		json.put("msg", "");
		json.put("redirect", url);
		out.print(json);
		return;
	}
}
else {
	JSONObject json = new JSONObject();
	json.put("ret", "0");
	json.put("msg", "登录失败，请检查用户名或密码是否正确！");
	out.print(json);
}
%>