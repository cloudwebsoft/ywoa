<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>题库管理框架页</title>
	<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
	<script src="../inc/common.js"></script>
	<script type="text/javascript" src="../js/jquery1.7.2.min.js"></script>
	<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
  </head>
	<frameset id="uiframe" cols="18%,*" cols="*" frameborder="0" framespacing="0">
		<frame id="questionMenu" noresize="noresize" name="leftFrame" src="question_menu.jsp" scrolling="auto" marginwidth="0" marginheight="0" frameborder="0"></frame>
		<frame id="questionListId" noresize="noresize" src="exam_question_manage.jsp?op=search" name="rFrame" style="float: left;margin: 20px 0px;" marginwidth="0" marginheight="0"  frameborder="0"></frame>
	</frameset>
  <body>
  </body>
</html>
