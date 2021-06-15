<%@ page contentType="text/html;charset=utf-8" %><%@ page pageEncoding="UTF-8" %> <%@ page import = "com.redmoon.oa.person.*"%><%@ page import = "com.redmoon.oa.account.*"%><%@ page import = "com.redmoon.oa.dept.*"%><%@ page import = "com.redmoon.oa.kernel.*"%><%@ page import = "cn.js.fan.db.*"%><%@ page import = "cn.js.fan.base.*"%><%@ page import = "java.util.*"%><%@ page import = "cn.js.fan.util.*"%><%@ page import = "com.redmoon.oa.ui.*"%><jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/><%
/*
- 功能描述：移动手机端使用
- 访问规则：oa.jsp的框架中
- 过程描述：
- 注意事项：
- 创建者：fgf 
- 创建时间：
*/
//String result = ParamUtil.get(request, "result");
String res = (String)request.getAttribute("result");
out.print(res);
%>