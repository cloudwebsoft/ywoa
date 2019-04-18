<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="org.jdom.*"%>
<%@ page import="org.jdom.output.*"%>
<%@ page import="org.jdom.input.*"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.plugin.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.plugin.base.*"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title></title>
</head>
<body>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
    String op = ParamUtil.get(request, "op");
	boolean re = true;
	if(op.equals("exchange")){
		String fromScore = ParamUtil.get(request, "fromScore");
		if (fromScore.equals("")) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.score_do", "need_from_score")));
			return;
		}
		
		String toScore = ParamUtil.get(request, "toScore");
		if (toScore.equals("")) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.score_do", "need_to_score")));
			return;
		}
		
		String value = ParamUtil.get(request, "value");
		if (value.equals("") || !StrUtil.isNumeric(value)) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.score_do", "err_value")));
			return;
		}
		
		String userName = privilege.getUser(request);
		
		ScoreMgr sm = new ScoreMgr();
		ScoreUnit su = sm.getScoreUnit(fromScore);
		IPluginScore ips = su.getScore();
		try{
			re = ips.exchange(userName, toScore, Double.parseDouble(value));
		}catch (ResKeyException e) {
			out.print(StrUtil.Alert_Back(e.getMessage(request)));
		}

		if (re)
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"score_exchange.jsp"));
		else
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_fail"),"score_exchange.jsp"));

	} else if(op.equals("transfer")) {
		String score = ParamUtil.get(request, "score");
		if (score.equals("")) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.score_do", "need_from_score")));
			return;
		}
		
		String value = ParamUtil.get(request, "value");
		if (value.equals("") || !StrUtil.isNumeric(value)) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.score_do", "err_value")));
			return;
		}
		
		String toNick = ParamUtil.get(request, "toNick");
		UserDb ud = new UserDb();
		ud = ud.getUserDbByNick(toNick);
		if (toNick.equals("") || ud == null) {
			out.print(StrUtil.Alert_Back(SkinUtil.LoadString(request, "res.label.forum.score_do", "err_to_nick")));
			return;
		}
		
		String fromUserName = privilege.getUser(request);
		String toUserName = ud.getName();

		ScoreMgr sm = new ScoreMgr();
		ScoreUnit su = sm.getScoreUnit(score);
		IPluginScore ips = su.getScore();
		try{
			re = ips.transfer(fromUserName, toUserName, Double.parseDouble(value));
		}catch (ResKeyException e) {
			out.print(StrUtil.Alert_Back(e.getMessage(request)));
		}
		if (re)
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"),"score_transfer.jsp"));
		else
			out.print(StrUtil.Alert_Redirect(SkinUtil.LoadString(request, "info_op_fail"),"score_transfer.jsp"));
	}
%>
</body>
</html>
