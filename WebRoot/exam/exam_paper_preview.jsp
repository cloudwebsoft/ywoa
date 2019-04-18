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
<%@page import="com.redmoon.oa.basic.TreeSelectDb"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	 int id = ParamUtil.getInt(request,"paperId");
	 int paperId = ParamUtil.getInt(request,"paperId");
	 //PaperManulDb pmd = new PaperManulDb();
	 PaperDb pd = new PaperDb();
	 pd = pd.getPaperDb(paperId);
	 String majorCode = pd.getMajor();
	 TreeSelectDb tsd = new TreeSelectDb();
	 tsd = tsd.getTreeSelectDb(majorCode);
	 int testtime = pd.getTesttime();
	 String title = pd.getTitle();
	 Date starttime = pd.getStarttime();
	 Date endtime = pd.getEndtime();
	 String sqll = "select id from oa_exam_paper_question where paper_id = "+paperId;
		PaperQuestionDb pqd = new PaperQuestionDb();
		Vector v =  pqd.list(sqll);
		Iterator ir = v.iterator();
		String ids ="";
		while(ir.hasNext()){
			pqd = (PaperQuestionDb)ir.next();
			if(ids == ""){
				ids = pqd.getString("question_id");
			}
			else{
				ids +="," + pqd.getString("question_id");
			}
		}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>打印试卷</title>
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
<script src="../js/jquery.js"></script>
<body>
<div id = "printWeb">
<table style="margin-top: 20px" width="100%" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td align="center" style="font-size: 20px" class="tdStyle_1"><%=title%></td>
	</tr>
	<tr>
			<td align="center" style="font-size: 15px" class="">专业分类：&nbsp;&nbsp;<%=tsd.getName() %>&nbsp;&nbsp;&nbsp;</td>
	</tr>
</table>
<form method="post" action="" id="testform" name="testform1">
<input type="hidden" value="<%=cn.js.fan.util.DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss")%>" name="starttime" />
  <table id="AutoNumber2" class="tabStyle_1 percent80">
  	<%
  		char[] cs = "零一二三四五六七八九".toCharArray();
   		int num = 0;
  		if(pd.getSinglecount()!=0){
  			num += 1;
  		%>
 		<tr><td style="font-weight: bold;font-size: 15px;" colspan="2"><%=cs[num] %>、单选题</td></tr>  	
		<%}
  	 %>
   <%
    QuestionDb sdb = new QuestionDb();
	// String sql = ""; 
	Vector vt1 = null;
	StringBuffer sb = new StringBuffer("select id from oa_exam_database where exam_type= "+QuestionDb.TYPE_SINGLE+" and id in (");
		sb.append(ids);
		sb.append(");");
		// System.out.println(sb.toString());
	vt1 = sdb.list(sb.toString());

	if (vt1==null)
		vt1 = new Vector();
	Iterator ir1 = vt1.iterator();
    int i = 0;
	while (ir1.hasNext()) {
		 sdb= (QuestionDb)ir1.next();
		 i++;
   %>
   
    <tr>
      <td class="tabStyle_1_title" align="center" width="25px"><%=i%>、</td>
      <td class="tabStyle_1_title" style="text-align:left">
	  <%=sdb.getQuestion()%>
	  </td>
    </tr>
     <%
    	int question = sdb.getId();
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
			<td>&nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" name="<%=sdb.getId()%>" value="<%=qsd.getString("id") %>"/><%=(char)o %>、<%=qsd.getString("content").replaceAll("<p>([^<]*?)</p>","$1") %></td>
			</tr>
			<%
			k++;
		}
	}%>	
  </table>
<table id="AutoNumber2" class="tabStyle_1 percent80">
	<%if(pd.getMulticount()!=0){
		num += 1;
		%>
			<tr><td style="font-weight: bold;font-size: 15px;" colspan="2"><%=cs[num] %>、多选题</td></tr>
		<%
	} %>

    <%
	QuestionDb qdb = new QuestionDb();
	Vector vt2 = null;
	StringBuffer sb2 = new StringBuffer("select id from oa_exam_database where exam_type="+QuestionDb.TYPE_MULTI+" and id in (");
		sb2.append(ids);
		sb2.append(");");  
	vt2 = qdb.list(sb2.toString());
	if (vt2==null)
		vt2 = new Vector();	
	
	Iterator ir2 = vt2.iterator();
    int j = 0;
	while (ir2.hasNext()) {
	     j++;
		 qdb= (QuestionDb)ir2.next();
    %> 
	<tr>
	  <td class="tabStyle_1_title" align="center" width="25px"><%=j%>、</td>
      <td class="tabStyle_1_title" style="text-align:left"><%=qdb.getQuestion()%> </td>
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
		<td>&nbsp;&nbsp;&nbsp;&nbsp;<input type="checkbox" name="<%=qdb.getId()%>" value="<%=qsd.getString("id") %>"/><%=(char)o %>、<%=qsd.getString("content").replaceAll("<p>([^<]*?)</p>","$1") %></td>
		</tr>
		<%
		k++;
	}
}
  %>
  </table>
  <table id="AutoNumber2" class="tabStyle_1 percent80">
  		<%
  			if(pd.getJudgecount()!=0){
  				num += 1;
  				%>
				    <tr><td style="font-weight: bold;font-size: 15px;" colspan="2"><%=cs[num] %>、判断题</td></tr>
  				<%
  			}
  		 %>
 
  <%
    Vector vt3 = null;
	StringBuffer sb3 = new StringBuffer("select id from oa_exam_database where exam_type = "+QuestionDb.TYPE_JUDGE+" and id in (");
		sb3.append(ids);
		sb3.append(");");  
	vt3 = qdb.list(sb3.toString());
	if (vt3==null)
		vt3 = new Vector();		
	//System.out.println(sql);
	Iterator ir3 = vt3.iterator();
    j = 0;
	while (ir3.hasNext()) {
	     j++;
		 qdb= (QuestionDb)ir3.next();	
		 // System.out.println("判断题题目："+qdb.getQuestion());	   
    %> 
    <tr>
      <td class="tabStyle_1_title" align="center" width="25px"><%=j%>、</td>
      <td class="tabStyle_1_title" style="text-align:left"><%=qdb.getQuestion()%>
        </td>
    </tr>
    <tr>
      <td align="center" width="25px"></td>
      <td>&nbsp;&nbsp;&nbsp;&nbsp;
          <input type="radio" name="<%=qdb.getId()%>" value="y"/>
      正确</td>
    </tr>
    <tr>
      <td align="center" width="25px"></td>
      <td>&nbsp;&nbsp;&nbsp;&nbsp;
          <input type="radio" name="<%=qdb.getId()%>" value="n"/>
        错误</td>
    </tr>
	<%
	}%>
  </table>
  <table id="AutoNumber4" class="tabStyle_1 percent80">
  <%
	StringBuffer sb4 = new StringBuffer("select id from oa_exam_database where exam_type="+QuestionDb.TYPE_ANSWER+" and id in (");
		sb4.append(ids);
		sb4.append(");");
    Vector vt4 = null;
	vt4= qdb.list(sb4.toString());
	if (vt4 == null)
		vt4 = new Vector();		
	Iterator ir4 = vt4.iterator();
    j = 0;
    if(pd.getAnswercount()!=0){
    	num += 1;
    	%>
    	<tr><td style="font-weight: bold;font-size: 20px;" colspan="2"><%=cs[num] %>、问答题</td></tr>
    	<%
	while (ir4.hasNext()) {
	     j++;
		 qdb= (QuestionDb)ir4.next();	   
    %> 
    <tr>
      <td class="tabStyle_1_title" align="center" width="25px"><%=j%>、</td>
      <td class="tabStyle_1_title" style="text-align:left">
        <%=qdb.getQuestion()%>
      </td>
    </tr>
    <tr>
      <td width="100%" colspan="2">&nbsp;&nbsp;&nbsp;&nbsp;
          <textarea cols="118" name="<%=qdb.getId()%>" ></textarea>
      </td>
    </tr>
	<%}
	}%>
  </table>
  <div id="persistMenu" style="position: absolute; height:150px; width:230px; left:360px; top:1px;z-index: 100; visibility: hidden" class="blueborder"></div>
</form>
</div>
<script>
function showFormReport() {
	var preWin=window.open('exam_paper_preview.jsp','','left=0,top=0,width=550,height=400,resizable=1,scrollbars=1, status=1, toolbar=1, menubar=1');
	preWin.document.open();
	preWin.document.write(preWin.opener.document.getElementById('printWeb').innerHTML);
	preWin.document.close();
	preWin.document.title="表单";
	preWin.print();
	preWin.document.charset="UTF-8";
}
</script>
<p align="center"><input name="submit1" type="button" value="打印" align="middle" onclick="showFormReport()"/></p>
</body>
</html>
