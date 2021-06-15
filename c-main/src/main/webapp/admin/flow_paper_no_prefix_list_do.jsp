<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="cn.js.fan.db.*"%>
<%@ page import="com.redmoon.oa.dept.*"%>
<%@ page import="com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="read";

String correct_result = "操作成功！";	


PaperNoPrefixMgr pnp = new PaperNoPrefixMgr();

String op = ParamUtil.get(request, "op");
String name = ParamUtil.get(request, "name");

if (op.equals("checkExist")) {
	boolean re = true;
	re = pnp.checkExist(name);
	if(re){
		out.print("true");		
	}else{
		out.print("false");			
	}
	return;
} 
%>