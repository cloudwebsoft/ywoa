<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="cn.js.fan.web.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.kernel.*" %>
<%@ page import="com.redmoon.oa.*" %>
<%
String formCode = ParamUtil.get(request, "formCode");

com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
String cloudUrl = "";
if (cfg.getBooleanProperty("isCloud")) {
	cloudUrl = cfg.get("cloudUrl");
	String enterpriseNum = License.getInstance().getEnterpriseNum();
	String company = License.getInstance().getCompany();
	String ver = License.getInstance().getVersion();
	
	com.redmoon.oa.Config oaCfg = new com.redmoon.oa.Config();
	SpConfig spCfg = new SpConfig();
	String sysVer = StrUtil.getNullStr(oaCfg.get("version"));
	String sp = StrUtil.getNullStr(spCfg.get("version"));
	// response.sendRedirect(cloudUrl + "/public/login_check.jsp?type=ide&sysVer=" + sysVer + "&sp=" + sp + "&ver=" + ver + "&com=" + StrUtil.UrlEncode(company) + "&entNum=" + StrUtil.UrlEncode(enterpriseNum) + "&extra=" + StrUtil.UrlEncode(Global.getFullRootPath() + "/admin/ide_left.jsp?formCode=" + StrUtil.UrlEncode(formCode)));
	
	cloudUrl += "/public/login_check.jsp?type=ide&sysVer=" + sysVer + "&sp=" + sp + "&ver=" + ver + "&com=" + StrUtil.UrlEncode(company) + "&entNum=" + StrUtil.UrlEncode(enterpriseNum) + "&extra=" + StrUtil.UrlEncode(Global.getFullRootPath() + "/admin/ide_left.jsp?formCode=" + StrUtil.UrlEncode(formCode));

	// response.sendRedirect(cloudUrl);
	// return;
}
%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>脚本设计器</title>
<script>
function setCols(cols) {
	frm.cols = cols;
}
function getCols() {
	return frm.cols;
}
</script>
</head>
<%
String priv="read";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

if (!cfg.getBooleanProperty("isCloud")) {
%>
<frameset id="frm" cols="250,*" framespacing="3" frameborder="1">
	<noframes><body></body></noframes>
	<frame src="script_left.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>" id="leftScriptFrame" name="leftScriptFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1" />
	<frame src="script_main.jsp" id="mainScriptFrame" name="mainScriptFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1" />
    </frameset>    
</frameset>
<%}else{%>
<frameset id="frm" cols="250,*" framespacing="3" frameborder="1">
	<noframes><body></body></noframes>
	<frame src="ide_left.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>" id="leftFrame" name="leftFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1" />
	<frame src="<%=cloudUrl%>" id="mainFrame" name="mainFrame" marginwidth="0" marginheight="0" scrolling="auto" frameborder="1" />
    </frameset>
</frameset>
<%}%>
</html>
