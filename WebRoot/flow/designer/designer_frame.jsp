<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import = "cn.js.fan.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>设计器框架</title>
</head>
<%
String leftSrc = "";
String mainFormQueryId = ParamUtil.get(request, "mainFormQueryId");
if (!mainFormQueryId.equals(""))
	leftSrc = "designer.jsp?id=" + mainFormQueryId;
String rightSrc = "";
String subFormQueryId = ParamUtil.get(request, "subFormQueryId");
if (!subFormQueryId.equals(""))
	rightSrc = "designer.jsp?id=" + subFormQueryId;
%>
<frameset rows="*" cols="50%,*" framespacing="1">
  <frame src="<%=leftSrc%>" name="designerLeftFrame" id="designerLeftFrame" title="designerLeftFrame" />
  <frame src="<%=rightSrc%>" name="designerRightFrame" id="designerRightFrame" title="designerRightFrame" />
</frameset>
<noframes><body>
</body>
</noframes></html>
