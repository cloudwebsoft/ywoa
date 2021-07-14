<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%
String curDeptCode = ParamUtil.get(request, "curDeptCode");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title></title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
</head>
<frameset id="allFrame" rows="" cols="21%,*" border="0">
	  <noframes>你需要 Internet Explorer 版本 3.0 或更高版本才能查看框架!</noframes>
	  <frame src="dept_tree.jsp?root_code=root&flag=0&pageType=add&curDeptCode=<%=curDeptCode%>" id="deptFrame" name="deptFrame">
  	  <frame src="user_add.jsp?curDeptCode=<%=curDeptCode%>" id="userFrame" name="userFrame">
</frameset>
</html>
