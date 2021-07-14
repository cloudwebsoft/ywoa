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
	if (op.equals("move")) {
		boolean re = false;
		int box = ParamUtil.getInt(request, "box");
		String sender = ParamUtil.get(request, "sender");
		try {
			String ids = ParamUtil.get(request, "ids");
			String[] ary = StrUtil.split(ids, ",");
			
			MailMsgMgr mmm = new MailMsgMgr();
			for (int i=0; i<ary.length; i++) {
				MailMsgDb mmd = mmm.getMailMsgDb(StrUtil.toInt(ary[i]));
				if(!sender.equals("")){
					mmd.setSender(sender);
				}
				mmd.setType(box);
				if(box == MailMsgDb.TYPE_DRAFT){
					mmd.setSendTime(null);
				}
				re = mmd.save();
			}
		} catch (ErrMsgException e) {
			//out.print(StrUtil.Alert_Back(e.getMessage()));
			out.print("移动邮件失败");
		}
		if(re) {
			if(box == MailMsgDb.TYPE_DRAFT){
				out.print("成功移动邮件到草稿箱");
			}else if(box == MailMsgDb.TYPE_SENDED){
				out.print("成功移动邮件到发件箱");
			}else if(box == MailMsgDb.TYPE_DUSTBIN){
				out.print("成功移动邮件到已删除");
			}
		}
	}
%>
