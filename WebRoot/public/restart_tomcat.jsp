<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.licenceValidate.*"%>
<%@ page import = "org.json.*"%>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<center><div id="msgRestart" name="msgRestart" style="margin-top:300px;color:#cc0000;font-weight:700;font-size:20px;">
系统服务重新启动中，若长时间未跳转至登录页面，请手动启动tomcat。。。</div></center>
<%
RestartTomcat.restartTomcatServer();
%>
<script type="text/javascript">  
setInterval(checkPage ,4000);
var n = 0;

function checkPage(){
	$.ajax({
		type: "post",
		url: "popup_box_validate.jsp",
		data: {
			op:"restartTomcat"
		},
		dataType: "html",
		beforeSend: function(XMLHttpRequest){
		},
		success: function(data, status){
			if(data.trim()=="restartok"){
				alert("系统服务重启成功");
				window.location.href = "../index.jsp";
			} else{
			}
		},
		complete: function(XMLHttpRequest, status){
			if(n>15){
				$("#msgRestart").html("系统服务重启失败，请手动启动tomcat服务。");
			}else{
				$("#msgRestart").html("系统服务重启中。。。");
			}
		},
		error: function(XMLHttpRequest, textStatus){
			$("#msgRestart").html("系统服务重启失败，请手动启动tomcat服务。");
		}
	});	
	n++;
}
</script>  