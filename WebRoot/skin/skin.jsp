<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cloudwebsoft.framework.db.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.ui.menu.*"%>
<%@ page import="cn.js.fan.security.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="com.redmoon.forum.ui.FileViewer"%>
<%@ page import = "org.json.*"%>
<%@page import="com.redmoon.oa.netdisk.UtilTools"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
	String oldskincode = UserSet.getSkin(request);
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>皮肤选择</title>
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script language="javascript">$(function(){$("#interface_mode").css({"position":"absolute","top":"50%","left":"50%"}).css("margin-top",function(){return -$(this).height()/2;}).css("margin-left",function(){return -$(this).width()/2});});</script>
<link href="skin.css" rel="stylesheet" type="text/css" />
<script>
function selected(skinCode){
	if(skinCode != "<%=oldskincode %>"){
		$.ajax({
			url: "skin_do.jsp",
			type: "post",
			data: {
				"op":"change",
				"skinCode": skinCode
			},
			success: function(data, status){
				window.top.location.reload();
			},
			error: function(XMLHttpRequest, textStatus){
				alert('更换皮肤失败！');
			}
		});		
	}
}

function addClasses(skinCode,obj){
	if(skinCode != "<%=oldskincode %>"){
		if ($(obj).hasClass('skin-wrapper-box-img')) {
			$(obj).removeClass('skin-wrapper-box-img');
			$(obj).addClass('skin-select');
		}
	}
}

function removeClasses(skinCode,obj){
	if(skinCode != "<%=oldskincode %>"){
		if ($(obj).hasClass('skin-select')) {
			$(obj).removeClass('skin-select');
			$(obj).addClass('skin-wrapper-box-img');
		}
	}
}
</script>
</head>
<body>
<div class="skin-wrapper">
<%
  com.redmoon.oa.ui.SkinMgr sm = new com.redmoon.oa.ui.SkinMgr();
  Vector skins = sm.getAllSkin();
  int size = skins.size();
  int i = 0;
  Iterator irskin = skins.iterator();
  while (irskin.hasNext()) {
  	i++;
  	com.redmoon.oa.ui.Skin sk = (com.redmoon.oa.ui.Skin)irskin.next();
	%>
	  <div class="skin-wrapper-box" id = "<%=i %>">
	    <div class="skin-wrapper-box-img" onclick="selected('<%=sk.getCode() %>')" onmouseover="addClasses('<%=sk.getCode() %>',this)" onmouseout="removeClasses('<%=sk.getCode() %>',this)">
	    	<img src="images/<%=sk.getCode() %>.jpg" width="299" height="190" />
	    </div>
	    <div class="skin-wrapper-box-text"><%=sk.getName() %></div>
	  </div>
	<%
	if (oldskincode.equals(sk.getCode())) {
	%>
		<script>
			$('#<%=i %>').prepend('<div class="skin-wrapper-box-hook"><img src="images/skin_icon.png" width="46" height="40" /></div>');
	  		$('#<%=i %>').children(".skin-wrapper-box-img").addClass("skin-select");
	  	</script>
	<%	
	}	
  }
 %>
</div>
</body>
</html>
