<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "com.redmoon.oa.workplan.*"%>
<%@ page import = "com.redmoon.oa.dept.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>工作计划</title>
<LINK href="../skin/default/css.css" type=text/css rel=stylesheet>
<script src="../inc/nav_top.js"></script>
</head>
<body>
<div id="navMenu">
  <div id="navItems">
    <a href="workplan_list.jsp" target="subMainFrame" title="我参与的计划" hidefocus="hidefocus"><span>我参与的计划</span></a>
    <a href="workplan_list.jsp?op=mine" target="subMainFrame" title="我拟定的计划" hidefocus="hidefocus"><span>我拟定的计划</span></a>
    <a href="workplan_add.jsp" target="subMainFrame" title="添加计划" hidefocus="hidefocus"><span>添加计划</span></a>
    <a href="workplantype_list.jsp" target="subMainFrame" title="计划类型" hidefocus="hidefocus"><span>计划类型</span></a>
  </div>
</div>
</body>
</html>
