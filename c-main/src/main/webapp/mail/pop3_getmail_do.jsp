<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.emailpop3.pop3.*"%>
<%@ page import="java.util.*"%>
<%@ page import="javax.mail.*"%>
<%@ page import="org.json.*"%>
<%@page import="cn.js.fan.security.ThreeDesUtil"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="userservice" scope="page" class="com.redmoon.oa.person.UserService"/>
<jsp:useBean id="fnumber" scope="page" class="cn.js.fan.util.NumberUtil"/>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
/*
- 功能描述：取邮件
- 访问规则：in_box.jsp调用
- 过程描述：
- 注意事项：
- 创建者：cj 
- 创建时间：
==================
- 修改者：fgf
- 修改时间：2015.1.19
- 修改原因：优化，当登录失败时，提示错误信息
- 修改点：
*/

	int count = 0;
	if (!privilege.isUserLogin(request)) {
		out.println(SkinUtil.makeInfo(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	int id = -1;
	try {
		id = ParamUtil.getInt(request, "id");
	} catch (ErrMsgException e) {
		out.print(SkinUtil.makeInfo(request, "请选择邮箱！"));
		return;
	}
	
	EmailPop3Db epd = new EmailPop3Db();
	epd = epd.getEmailPop3Db(id);
	if (!epd.getUserName().equals(privilege.getUser(request))) {
		out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	String email = epd.getEmail();
	
	String mailserver = epd.getServerPop3();
	int port = epd.getPort();
	String email_name = epd.getEmailUser();
	String email_pwd_raw = epd.getEmailPwd();
	email_pwd_raw = ThreeDesUtil.decrypthexstr("cloudwebcloudwebcloudweb",email_pwd_raw);
	JSONObject json = new JSONObject();
	GetMail getmail = new GetMail(mailserver,port,email_name,email_pwd_raw);
	try {
		count = getmail.receive(request, email, epd.isDelete(), epd.isSsl());
		
		json.put("ret", "1");
		json.put("msg", count);
	}
	catch (ErrMsgException e) {
		json.put("ret", "0");
		json.put("msg", e.getMessage());
	}
	out.print(json.toString());
%>