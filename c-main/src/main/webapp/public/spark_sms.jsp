<%@ page contentType="text/html;charset=utf-8"%><%@ page import="com.redmoon.oa.person.*"%><%@ page import="com.redmoon.forum.security.*"%><%@ page import="cn.js.fan.util.*" %><%@ page import="cn.js.fan.web.*" %><%@ page import="cn.js.fan.security.*" %><%@ page import="java.util.Properties" %><%@ page import = "org.json.*"%><%@ page import="com.redmoon.oa.sms.*" %><%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

String authKey = ParamUtil.get(request, "authKey");

com.redmoon.oa.sso.Config cfg = new com.redmoon.oa.sso.Config();
authKey = ThreeDesUtil.decrypthexstr(cfg.get("key"), authKey);
String[] ary = StrUtil.split(authKey, "\\|");
if (ary == null) {
	out.print(SkinUtil.makeErrMsg(request, "操作非法！"));
	return;
}

boolean re = false;
int op = ParamUtil.getInt(request, "op");

JSONObject json = new JSONObject();
String cellNo = "";

switch (op) {
case 1:
	String name = ParamUtil.get(request, "userName");
	UserMgr um = new UserMgr();
	UserDb userDb = um.getUserDb(name);
	cellNo = userDb.getMobile();
	
	// 获取用户手机号码
	json.put("ret", 1);
	json.put("cellNo", cellNo);
	break;
case 2: // 来自Spark
case 3: // 来自IMServer
	cellNo = ParamUtil.get(request, "cellNo");
	String content = ParamUtil.get(request, "content");

	System.out.println(getClass() + " cellNo=" + cellNo);
	System.out.println(getClass() + " content=" + content);
	System.out.println(getClass() + " userName=" + ary[0]);
	
	IMsgUtil iu = SMSFactory.getMsgUtil();
	iu.send(cellNo, content, ary[0]);
	json.put("ret", 1);
	
	break;
	
default:
	break;
}

out.print(json);
%>