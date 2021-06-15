<%@ page contentType="text/html;charset=utf-8" language="java" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.fileark.*"%>
<%@ taglib uri="/WEB-INF/tlds/DocumentTag.tld" prefix="doc" %>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
	margin-right: 0px;
	margin-bottom: 0px;
}
-->
</style>
<%
String dirCode = ParamUtil.get(request, "dirCode");
if (!dirCode.equals("")) {
%>
	<doc:DocumentTag dirCode="<%=dirCode%>">$content</doc:DocumentTag>
<%} else {
	int id = ParamUtil.getInt(request, "id");
	Document doc = new Document();
	doc = doc.getDocument(id);
%>
<title><%=doc.getTitle()%></title>
	<doc:DocumentTag id="<%="" + id%>">$content</doc:DocumentTag>
<%}%>