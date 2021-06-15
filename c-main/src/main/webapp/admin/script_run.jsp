<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.basic.*" %>
<%@ page import="com.redmoon.oa.person.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="com.redmoon.oa.flow.*" %>
<%@ page import="com.redmoon.oa.flow.strategy.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="org.json.*"%>
<%@ page import="bsh.EvalError"%>
<%@ page import="bsh.Interpreter"%>
<%
String op = ParamUtil.get(request, "op");
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE>运行结果</TITLE>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script>
function run() {
	o("myscript").value = window.opener.getScript();
	o("formRun").submit();
}

<%
if (op.equals("")) {
%>
$(function() {
	run();
});
<%
}
%>
</script>
</head>
<body style="background:black; font-size:14px; color:white; line-height:1.5">
<%
String myscript = ParamUtil.get(request, "myscript");
if (!myscript.equals("")) {
	com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
	Interpreter bsh = new Interpreter();
	try {
		StringBuffer sb = new StringBuffer();
	
		// 赋值给用户
		sb.append("userName=\"" + privilege.getUser(request) + "\";");
		bsh.set("request", request);
		bsh.set("out", out);
	
		bsh.eval(sb.toString());
		
		myscript = myscript.replaceAll("System.out.println\\(", "out.print\\(\"<BR>\" + ");
		myscript = myscript.replaceAll("System.out.print\\(", "out.print\\(");
		bsh.eval(myscript);
		/*
		Object obj = bsh.get("ret");
		if (obj != null) {
			boolean ret = ((Boolean) obj).booleanValue();
			if (!ret) {
				String errMsg = (String) bsh.get("errMsg");
				LogUtil.getLog(getClass()).error(
						"bsh errMsg=" + errMsg);
			}
		}
		*/
	} catch (EvalError e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		out.print(StrUtil.toHtml(StrUtil.trace(e)));
		out.print("<BR />-----------------操作失败，以下为脚本----------------------<BR />");
		out.print(StrUtil.toHtml(myscript));
		return;
	} catch (Exception e) {
		out.print(StrUtil.toHtml(StrUtil.trace(e)));
		out.print("<BR />-----------------操作失败，以下为脚本----------------------<BR />");
		out.print(StrUtil.toHtml(myscript));
		return;
	}
	
	out.print("<BR />-----------------操作成功，以下为脚本----------------------<BR />");
	out.print(StrUtil.toHtml(myscript));
}
%>
<form id="formRun" name="formRun" method="post">
<textarea id="myscript" name="myscript" style="display:none"></textarea>
<input name="op" value="run" type="hidden" />
</form>
</body>
</html>