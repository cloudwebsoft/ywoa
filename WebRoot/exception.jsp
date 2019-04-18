<%@ page contentType="text/html; charset=utf-8" language="java"  isErrorPage="true" %>
<%@page import="com.redmoon.oa.kernel.*"%>
<%@page import="com.cloudwebsoft.framework.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="org.apache.commons.lang3.exception.ExceptionUtils"%>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils"%>
<%@ page import="org.apache.commons.lang3.StringUtils"%>
<%
	License license = License.getInstance();
	String enterpriseNum = license.getEnterpriseNum();
	String enterpriseName = StrUtil.UrlEncode(license.getName());
	String id = license.getId();
	String type = license.getType();
	String exceptionStr = StrUtil.trace(exception);
	
	exception.printStackTrace();
	System.out.println(exceptionStr);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>错误页</title>
<link href="<%=request.getContextPath()%>/skin/common/404_505_page.css" rel="stylesheet" type="text/css">
</head>

<body>
<div class="500box">
<img src="<%=request.getContextPath()%>/images/500.jpg" width="779" height="333" border="0" usemap="#Map">
  <map name="Map">
  	<%if (!license.isOem()) { %>
    <area shape="rect" coords="167,262,297,311" href="#" onclick="sendExceptionInfo()">
    <%} %>
  </map>
</div>
<form name="formSearch" action="http://service.yimihome.com/public/customer/exception.jsp" method="post" >
	<input type="hidden" value="<%=enterpriseNum %>" name="enterpriseNum" />
	<input type="hidden" value="<%=enterpriseName %>" name="name" />
	<input type="hidden" value="<%=id %>" name="id" />
	<input type="hidden" value="<%=type %>" name="type" />
	<textarea style="display:none" name="exception"><%=exceptionStr %></textarea>
</form>

</body>
<script type="text/javascript">
	function sendExceptionInfo(){
		document.formSearch.submit();
	}
</script>
</html>