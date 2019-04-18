<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.base.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.person.*"%>
<%@ page import="com.redmoon.forum.music.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="java.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
	boolean re = false;
	String op = ParamUtil.get(request,"op");
	int id = ParamUtil.getInt(request,"id",0);
	if(op.equals("edit")){
		try{
			QuizMgr qm = new QuizMgr();
			re = qm.save(request);
			if(re){
				out.print(StrUtil.Alert_Redirect("编辑成功！", "quiz_edit.jsp?id=" + id));
			}else{
				out.print(StrUtil.Alert_Back("编辑失败！"));
			}
		} catch (ErrMsgException e){
			out.print(StrUtil.Alert_Back(e.getMessage()));
		}
		return;
	}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="GENERATOR" content="Microsoft FrontPage 4.0">
<meta name="ProgId" content="FrontPage.Editor.Document">
<link href="default.css" rel="stylesheet" type="text/css">
<LINK href="../../cms/default.css" type=text/css rel=stylesheet>
<title>注册问题</title>
<style type="text/css">
<!--
body {
	margin-top: 0px;
	margin-left: 0px;
	margin-right: 0px;
}
-->
</style>
<script src="../../inc/common.js"></script>
</head>
<body>
<%@ include file="quiz_nav.jsp"%>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<div id="newdiv" name="newdiv"><br>
  <br>
    <%
		QuizDb qd = new QuizDb();
		qd = qd.getQuizDb(id);
		String question = "";
		String answer = "";
		if(qd != null){
			question = qd.getString("question");
			answer = qd.getString("answer");
		}
  %>
  <form name="form1" action="?op=edit" onSubmit="return check()" method="post" />
  <input type="hidden" name="id" value="<%=id%>" />
  <table width='60%' align="center" cellpadding='3' cellspacing='0' class="frame_gray">
    <tr>
      <td colspan="2" align="center" class="thead">编辑注册问题</td>
    </tr>
    <tr>
      <td height="22" align="right"> 问题：</td>
	  <td align="left"><input type="text" name="quizQuestion" size="50" value="<%=question%>"></td>
    </tr>
	<tr>
      <td height="22" align="right"> 答案：</td>
	  <td align="left"><input type="text" name="quizAnswer" size="50" value="<%=answer%>"></td>
    </tr>
	<tr>
	  <td align="center" colspan="2"><input type="submit" value="确定"></td>
    </tr>
  </table>
  </form>
  <br>
  <table width="98%" border="0" cellspacing="1" cellpadding="3" align="center" class="9black">
    <tr>
      <td width="2%" height="23">&nbsp;</td>
      <td height="23" valign="baseline"><div align="right">
      </div></td>
    </tr>
  </table>
</div>
<script>
function check(){
	var question = $("quizQuestion").value;
	var answer = $("quizAnswer").value;
	if(question.trim()==""){
		alert("问题不能为空！");
		return false;
	}
	if(answer.trim()==""){
		alert("答案不能为空！");
		return false;
	}
	return true;
}
</script>
</body>
</html>
