<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.emailpop3.*"%>
<%@ page import="com.redmoon.oa.emailpop3.pop3.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="java.util.*"%>
<%@ page import="javax.mail.*"%>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/><jsp:useBean id="userservice" scope="page" class="com.redmoon.oa.person.UserService"/><jsp:useBean id="fnumber" scope="page" class="cn.js.fan.util.NumberUtil"/><jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
	if (!privilege.isUserLogin(request)) {
		out.println(SkinUtil.makeInfo(request, SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
	
	boolean re = false;
	String op = ParamUtil.get(request, "op");
	if (op.equals("del")) {
		try {
			EmailPop3Mgr epm = new EmailPop3Mgr();
			re = epm.del(request);
		}
		catch (ErrMsgException e) {
			out.print("删除失败：" + e.getMessage());
			return;
		}
		if(re) {
			out.print("删除成功！");
			return;
		} else {
			out.print("删除失败！");
			return;
		}
	}
%>
