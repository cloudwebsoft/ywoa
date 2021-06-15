<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.emailpop3.*"%>
<%
	String op = ParamUtil.get(request, "op");
	String str = "";
	String strDust = ParamUtil.get(request, "isDustbin");
	if (strDust.equals(""))
		strDust = "true";
	boolean isDustbin = strDust.equals("true");
	if (op.equals("del")) {
		boolean re = false;
		try {
			String ids = ParamUtil.get(request, "ids");
			String[] ary = StrUtil.split(ids, ",");
			
			MailMsgMgr mmm = new MailMsgMgr();
			for (int i=0; i<ary.length; i++) {
				re = mmm.del(StrUtil.toInt(ary[i]), isDustbin);
			}
		} catch (ErrMsgException e) {
			//out.print(StrUtil.Alert_Back(e.getMessage()));
			out.print("邮件删除失败");
		}
		if(re) {
			out.print("邮件删除成功");
		}
	}
%>
