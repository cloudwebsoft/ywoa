<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.db.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.exam.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<title>sip-三级安全教育考试管理外部人员考试</title>
<style>
.loginlink a {color: white;}
table
{
border-collapse:separate;
border-spacing:10px 30px;
}
.input_out{ font:14px '微软雅黑' ;background-color: 33CCFF;height:60px;width:230px;border-radius:25px; border-width:1px;font-size: 20px}

.input_move{font:14px '微软雅黑' ;color: red;background-color: 33CCFF;height:60px;width:230px;border-radius:25px; border-width:1px;font-size: 20px}
</style>
</head>
<body style="overflow:-Scroll;overflow-y:hidden">
	<div style="width:100%; height:100%; background:url(../skin/bluethink/images/login/login_bgimg.jpg);background-repeat: no-repeat;background-size:cover ;">
	    <div style="position:absolute; z-index:1"><img src="<%=SkinMgr.getSkinPath(request)%>/images/login/login_logo.png"/></div>
		<div style="width:650px; height:520px; background:url(../skin/bluethink/images/login/login_computer.png);background-repeat: no-repeat;float: left;margin-top: 200px;"></div>
		<div style="width:500px; height:300px;float: left;margin-top: 30px; background:url(../skin/bluethink/images/login/exam_inde.jpg); ">
			<br/>
			<br/>
			<form method="post" name="form1" action="" >
				<h1 align="center" style="color: white;font-size: 45">三级安全教育在线考试</h1>
				<br/>
				<br/>
				<br/>
				<br/>
				<br/>
				<br/>
				<br/>
				<br/>
				<br/>
				<table width="560" align="center">
					<tr>
						<td align="center" class="loginlink" width="200"><input class="input_out" onmousemove="this.className='input_move';this.style.cursor='hand'" onmouseout="this.className='input_out'"  type="button" value ="内部人员考试" onclick="examLogin(1)"/></td>
					</tr> 
					<tr>
						<td align="center" class="loginlink" width="200"><input class="input_out" onmousemove="this.className='input_move';this.style.cursor='hand'" onmouseout="this.className='input_out'" type="button" value ="外部人员考试" onclick="examLogin(2)"/></td>
					</tr>
				</table>
			</form>
			<br/>
		</div>
	</div>
	<script>
	$(document).ready(function() {
		$(".loginlink a").hover(function(){
			$(this).css({'color':'red'});
		},
	    function(){  
            $(this).css({'color':'white'});  
       })
	});
	function examLogin(kind){
		if(kind=="1"){
			window.location.href ="exam_manual_login.jsp";
		}else{
			window.location.href ="prj_exam_login.jsp";
		}
	}
	</script>
</body>
</html>
