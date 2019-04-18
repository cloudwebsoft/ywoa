<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %><%@ page import="java.util.*"%><%@ page import="cn.js.fan.util.*"%><%@ page import="cn.js.fan.module.cms.*"%><%@ page import="com.redmoon.forum.security.*"%><%
	String quizAnswer = ParamUtil.get(request,"quizAnswer");
	int id = ParamUtil.getInt(request,"qid");
	QuizDb qd = new QuizDb();
	qd = qd.getQuizDb(id);
	if (!qd.getString("answer").equals(quizAnswer)) {
		out.print("-");
	}
	else
		out.print("+");
%>