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
	if(op.equals("add")){
		try{
			QuizMgr qm = new QuizMgr();
			re = qm.create(request);
			if(re){
				out.print(StrUtil.Alert_Redirect("添加成功！", "quiz_list.jsp"));
			}else{
				out.print(StrUtil.Alert_Redirect("添加失败！", "quiz_list.jsp"));
			}
		}catch(Exception e){
			e.printStackTrace();
			out.print(StrUtil.Alert_Redirect("添加失败！", "quiz_list.jsp"));
		}
	}
	else if(op.equals("del")){
		try{
			QuizMgr qm = new QuizMgr();
			re = qm.del(request);
			if(re){
				out.print(StrUtil.Alert_Redirect("删除成功！", "quiz_list.jsp"));
			}else{
				out.print(StrUtil.Alert_Redirect("删除失败！", "quiz_list.jsp"));
			}
		}catch(Exception e){
			e.printStackTrace();
			out.print(StrUtil.Alert_Redirect("删除失败！", "quiz_list.jsp"));
		}
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
<script>
$("menu1").className="active";
</script>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<div id="newdiv" name="newdiv"><br>
  <br>
  <%
		QuizDb qd = new QuizDb();
  		String sql = "select id from " + qd.getTable().getName() + " order by id desc";
		Vector v = qd.list(sql);
		Iterator ir = null;
		if(v!=null){
			ir = v.iterator();
		}
  %>
  <TABLE width="80%" border=0 align=center cellPadding=3 cellSpacing=1 class="frame_gray">
    <TBODY>
      <TR align=center class="td_title">
        <TD width="10%" height=23 class="thead" align="center">序号</TD>
        <TD width="39%" height=23 class="thead">问题</TD>
        <TD width="31%" height=23 class="thead">答案</TD>
        <TD width="20%" height=23 class="thead">操作</TD>
      </TR>
	  <%
	  	int count = 0;
		int id = 0;
	  	while(ir.hasNext()){
			qd = (QuizDb)ir.next();
			id = qd.getInt("id");
	  %>
      <TR align=center>
        <TD height=18 align="center"><%=++count%></TD>
        <TD align="left"><%=qd.getString("question")%></TD>
        <TD align="left"><%=qd.getString("answer")%></TD>
        <TD>
		<a href="quiz_edit.jsp?id=<%=id%>">编辑</a>&nbsp;&nbsp;
		<a href="#" onClick="if(confirm('您确定要删除吗？')) window.location.href='quiz_list.jsp?op=del&id=<%=id%>'">删除</a></TD>
      </TR>
	  <%
	  	}
	  %>
    </TBODY>
  </TABLE>
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
