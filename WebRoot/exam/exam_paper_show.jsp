<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "java.text.SimpleDateFormat" %>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.exam.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.cloudwebsoft.framework.db.JdbcTemplate"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<title>预览试卷</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style type="text/css">
<!--
.inputborder {color: #00FF00; text-align: center; border-style: solid; border-width: 1; 
               background-color: #333333 }
.outborder {border-left: 1px solid #333333; border-right: 1px solid #000000; 
               border-top: 1px solid #99CCFF; border-bottom: 1px solid #000000; 
               background-color: #336699 }
.STYLE1 {
	font-size: medium;
	font-weight: bold;
}
.STYLE2 {
	font-size: 14;
	font-weight: bold;
}
-->
</style>

</head>
<body>
<%
	String singleIds = ParamUtil.get(request, "singleIds");
  	String multiIds = ParamUtil.get(request, "multiIds");
  	String judgeIds = ParamUtil.get(request, "judgeIds");
  	String answerIds = ParamUtil.get(request, "answerIds");
  	String singleArr[] = singleIds.split(",");
  	String multiArr[] = multiIds.split(",");
  	String judgeArr[] = judgeIds.split(",");
  	String answerArr[] = answerIds.split(",");
  	QuestionDb qdb = new QuestionDb();
 %>
<table style="" width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td align="center" style="font-size: 15px" class="tdStyle_1">预览试卷</td>
	</tr>
</table>
<div id="printWeb">
<form method="post" action="" id="testform" name="testform1">
  <table id="single" class="tabStyle_1 percent80">
    <%
    	int a =0;
    	char[] cs = "零一二三四五六七八九".toCharArray();
   		int num = 0;
    	if(singleIds!=""){
    		num += 1;
    	%>
    	 <tr><td style="font-weight: bold;font-size: 15px;" colspan="2"><%=cs[num] %>、单选题</td></tr>
    	<%
    	for(int i=0;i<singleArr.length;i++){
    		a++;
    		qdb= qdb.getQuestionDb(Integer.parseInt(singleArr[i]));
    		//去除左右标签的正则表达式：</?[a-zA-Z]+[^><]*>
    		//保留img标签的正则表达式：</?[^/?(img)][^><]*>
    	%>
    		<tr>
    			<td align="center" width="25px"><%=a %>.</td>
    			<td><%=qdb.getQuestion() %></td>
    		</tr>
    		<%
	    	int question = qdb.getId();
	    	String selSelectOptionSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(question)) + "order by orders";
	    	QuestionSelectDb qsd = new QuestionSelectDb();
	    	Iterator optionIt = qsd.list(selSelectOptionSql).iterator();
			int k = 0;
			while(optionIt.hasNext()){
				int o = (int)'A';
				o = o + k;
				qsd = (QuestionSelectDb)optionIt.next();
				%>
				<tr>
				<td align="center" width="25px">&nbsp;&nbsp;<%=(char)o %>、</td>
				<td>&nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" name="<%=qdb.getId()%>" value="<%=qsd.getString("id") %>"/><%=qsd.getString("content").replaceAll("<p>([^<]*?)</p>","$1") %></td>
				</tr>
				<%
				k++;
			}
    	}
   	}
     %>
  </table>
<table id="multi" class="tabStyle_1 percent80">
	<%
		if(multiIds!=""){
			num += 1;
			%>
			   <tr><td style="font-weight: bold;font-size: 15px;" colspan="2"><%=cs[num] %>、多选题</td></tr>
			<%
		}
	 %>
   		<%
   			int b=0;
   			if(multiIds!=""){
	   			for(int i=0;i<multiArr.length;i++){
	   				b++;
	   				qdb= qdb.getQuestionDb(Integer.parseInt(multiArr[i]));
	   				%>
	   				<tr>
	   					<td align="center" width="25px"><%=b %>.</td>
		    			<td><%=qdb.getQuestion() %></td>
	    			</tr>
	    			<%
					int question = qdb.getId();
				   	String selSelectOptionSql = "select id from oa_exam_database_option where question_id = " + StrUtil.sqlstr(String.valueOf(question)) + "order by orders";
				   	QuestionSelectDb qsd = new QuestionSelectDb();
				   	Iterator optionIt = qsd.list(selSelectOptionSql).iterator();
					int k = 0;
					while(optionIt.hasNext()){
						int o = (int)'A';
						o = o + k;
						qsd = (QuestionSelectDb)optionIt.next();
						%>
						<tr>
						<td align="center" width="25px">&nbsp;&nbsp;<%=(char)o %>、</td>
						<td>&nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" name="<%=qdb.getId()%>" value="<%=qsd.getString("id") %>"/><%=qsd.getString("content").replaceAll("<p>([^<]*?)</p>","$1") %></td>
						</tr>
						<%
						k++;
					}
				}
   			}
   		 %>
  </table>
  <table id="judge" class="tabStyle_1 percent80">
  	<%
  		if(judgeIds!=""){
  			num += 1;
  			%>
  			  	<tr><td style="font-weight: bold;font-size: 15px;" colspan="2"><%=cs[num] %>、判断题</td></tr> 
  			<%
  		}
  	 %>
   	<%
   			int c=0;
   			if(judgeIds!=""){
	   			for(int i=0;i<judgeArr.length;i++){
	   				c++;
	   				qdb= qdb.getQuestionDb(Integer.parseInt(judgeArr[i]));
	   				%>
	   				<tr>
	   					<td align="center" width="25px"><%=c %>.</td>
		    			<td><%=qdb.getQuestion() %></td>
	    			</tr>
	    			<tr>
	    				<td></td>
		    			<td><input type="radio" id ="answer" name = "answer" value="正确"/>正确&nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" id ="answer" name = "answer" value="不正确"/>不正确
		    			</td>
	    			</tr>
	   				
	   				<%
   				}
   			}
   		 %>
  </table>
  <table id="answer" class="tabStyle_1 percent80">
  	<%
  		if(answerIds!=""){
  			num += 1;
  			%>
  			  	<tr><td style="font-weight: bold;font-size: 15px;" colspan="2"><%=cs[num] %>、问答题</td></tr> 
  			<%
  		}
  	 %>
   	<%
   			int d=0;
   			if(answerIds!=""){
	   			for(int i=0;i<answerArr.length;i++){
	   				c++;
	   				qdb= qdb.getQuestionDb(Integer.parseInt(answerArr[i]));
	   				%>
	   				<tr>
	   					<td align="center" width="25px"><%=c %>.</td>
		    			<td><%=qdb.getQuestion() %></td>
	    			</tr>
	    			<tr>
	    				<td width="100%" colspan="2">&nbsp;&nbsp;&nbsp;&nbsp;
					    	<textarea cols="118" name="<%=qdb.getId()%>" ></textarea>
					    </td>
	    			</tr>
	   				
	   				<%
   				}
   			}
   		 %>
  </table>
   </form>
</div>
</body>

</html>
