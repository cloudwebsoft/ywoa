<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.exam.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.basic.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"></meta>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, "admin.exam"))
{
	out.println(StrUtil.makeErrMsg(privilege.MSG_INVALID,"red","green"));
	return;
}
String userName = privilege.getUser(request);

%>
<title>生成试卷</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/datepicker/jquery.datetimepicker.css"/>
<script src="../js/datepicker/jquery.datetimepicker.js"></script>
<script type="text/javascript" src="../inc/livevalidation_standalone.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script language="javascript">
function checktotal(){
	var myform=document.firstform;
	var a=myform.singlecount.value;
	var b=myform.singleper.value;
	var c=myform.multicount.value;
	var d=myform.multiper.value;
	var e=myform.judgecount.value;
	var f=myform.judgeper.value;
	var g=myform.answercount.value;
	var h=myform.answerper.value;
	
	var i=myform.totalper.value;
	var j=a*b+c*d+e*f+g*h;
	if (j!=i){
	  o("tishi").innerText=" 所有题型合计总分为:"+ j +",与卷面总分不相符！";
	  return false;
	}
	else{
	  o("tishi").innerText=" 所有题型合计总分为:"+ j;
	  return true;
	}
}

function submitit(){
	var myform;
	myform=document.firstform;
	if (myform.major.value==""||myform.major.value=="<%=MajorView.ROOT_CODE%>"){
	  jAlert("请选择考试分类！","提示")
	  myform.major.focus();
	  return false;
	}
	if (myform.title.value=="") {
		jAlert("请输入试卷名称！","提示");
		return false;
	}
	if (myform.limitCount.value=="") {
		jAlert("请输入每月限制的考试次数！","提示");
		return false;
	}
	return checktotal();
}
function SelectDateTime(objName) {
    var dt = openWin("../util/calendar/time.htm?divId" + objName,"266px","185px");//showModalDialog("../util/calendar/time.htm", "" ,"dialogWidth:266px;dialogHeight:185px;status:no;help:no;");
}
function sel(dt, objName) {
    if (dt!=null && objName != "")
        //document.getEelemenById(objName).value=dt;
        o(objName).value = dt;
}
function openWin(url,width,height){
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=no,top=50,left=120,width="+width+",height="+height);
}
$(function(){
})
</script>
</head>
<body>
	<form method="post" action="exam_create_paper2.jsp?" name="firstform" onSubmit="return submitit()">
		<table class="tabStyle_1 percent98" cellspacing="0" id="AutoNumber1" height="98" cellpadding="0">
		    <tr height="30">
		      <td colspan="5" class="tabStyle_1_title">试卷基本信息</td>
		    </tr>
		    <tr height="20">
		      <td width="128" align="center" >选择专业</td>
		      <td width="855" colspan="4"><select id="major" size="1" name="major">
		        <%
		        TreeSelectDb tsd = new TreeSelectDb();
				tsd = tsd.getTreeSelectDb(MajorView.ROOT_CODE);
				MajorView mv = new MajorView(tsd);
				StringBuffer sb = new StringBuffer();
				mv.getTreeSelectByUserAsOptions(sb, tsd, 1,userName,"0");
				%>
				<%=sb %>
		      </select> 
		      </td> 
		    </tr>
		    <tr height="20">
		      <td align="center">试卷名称</td>
		      <td colspan="4"><input name="title" size="30" /></td>
		    </tr>
		    <tr height="20">
		      <td colspan="5" class="tabStyle_1_title">分值</td>
		    </tr>
		    <tr height="20">
		      <td width="128" align="center">总分(<font color="#FF0000">分</font>)</td>
		      <td >
		        <input type="text" name="totalper" size="18" onKeyUp="if(isNaN(this.value)) this.value='10';" onBlur="this.className='inputnormal'" onFocus="this.className='inputedit';this.select()" value="100"/>
		      </td>
		      <td colspan="3" id="tishi" style="font-weight: bold;color: red">　</td>
		    </tr>
		    <tr height="20">
		      <td class="tabStyle_1_title" width="128" align="center">　</td>
		      <td class="tabStyle_1_title">单选题</td>
		      <td class="tabStyle_1_title">多选题</td>
		      <td class="tabStyle_1_title">判断题</td>
		      <td class="tabStyle_1_title">问答题</td>
		    </tr>
		    <tr height="20">
		      <td width="128" align="center">数量(<font color="#FF0000">个</font>)</td>
		      <td width="195">
		      	<input type="text" name="singlecount" size="18" onKeyUp="if(isNaN(this.value)) this.value='10';" onBlur="this.className='inputnormal';checktotal();" onFocus="this.className='inputedit';this.select()" value="10"/>
		      </td>
		      <td width="330">
		      		<input type="text" name="multicount" size="18" onKeyUp="if(isNaN(this.value)) this.value='15';" onBlur="this.className='inputnormal';checktotal();" onFocus="this.className='inputedit';this.select()" value="15"/>
		      </td>
		      <td width="328">
		      	<input type="text" name="judgecount" size="18" onKeyUp="if(isNaN(this.value)) this.value='10';" onBlur="this.className='inputnormal';checktotal();" onFocus="this.className='inputedit';this.select()" value="10"/>
		      </td>
		      <td width="328">
		      	<input type="text" name="answercount" size="18" onKeyUp="if(isNaN(this.value)) this.value='10';" onBlur="this.className='inputnormal';checktotal();" onFocus="this.className='inputedit';this.select()" value="0"/>
		      </td>
		    </tr>
		    <tr height="20">
		      <td width="128" align="center"><font color="#FF0000">分</font>/个</td>
		      <td width="195">
		      	<input name="singleper" size="18" onKeyUp="if(isNaN(this.value)) this.value='2';" onBlur="this.className='inputnormal';checktotal();" onFocus="this.className='inputedit';this.select()" value="2"/>
		      </td>
		      <td width="330">
		      	<input type="text" name="multiper" size="18" onKeyUp="if(isNaN(this.value)) this.value='4';" onBlur="this.className='inputnormal';checktotal();" onFocus="this.className='inputedit';this.select()" value="4"/>
		      </td>
		      <td width="328">
		      	<input type="text" name="judgeper" size="18" onKeyUp="if(isNaN(this.value)) this.value='2';" onBlur="this.className='inputnormal';checktotal();" onFocus="this.className='inputedit';this.select()" value="2"/>
		      </td>
		      <td width="328">
		      	<input type="text" name="answerper" size="18" onKeyUp="if(isNaN(this.value)) this.value='2';" onBlur="this.className='inputnormal';checktotal();" onFocus="this.className='inputedit';this.select()" value="0"/>
		      </td>
		    </tr>
		    <tr>
		      <td width="100%" colspan="5" align="center"><input type="submit" style="font-family:'宋体'" value="下一步" class="btn"/>
		      </td>
		    </tr>
		</table>
	</form>
</body>
</html>
