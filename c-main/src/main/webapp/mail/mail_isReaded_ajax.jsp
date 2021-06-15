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
	if (op.equals("isReaded")) {
		boolean re = false;
		int isReaded = ParamUtil.getInt(request, "isReaded");
		try {
			String ids = ParamUtil.get(request, "ids");
			String[] ary = StrUtil.split(ids, ",");
			
			MailMsgMgr mmm = new MailMsgMgr();
			for (int i=0; i<ary.length; i++) {
				MailMsgDb mmd = mmm.getMailMsgDb(StrUtil.toInt(ary[i]));
				mmd.setReaded(isReaded==1?true:false);
				re = mmd.save();
			}
		} catch (ErrMsgException e) {
			//out.print(StrUtil.Alert_Back(e.getMessage()));
			out.print("标记邮件失败");
		}
		if(re) {
			if(isReaded == 1){
				out.print("成功设置邮件为已读");
			}else{
				out.print("成功设置邮件为未读");
			}
		}
	}
%>
