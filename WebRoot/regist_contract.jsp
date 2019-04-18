<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.Global"%>
<%@ page import="com.redmoon.forum.person.UserSet"%>
<%@ page import="java.util.*"%>
<%@ page import="org.jdom.Element"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<%
String skinPath = SkinMgr.getSkinPath(request);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><lt:Label res="res.label.regist" key="regist"/> - <%=Global.AppName%></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link href="forum/<%=skinPath%>/css.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id="wrapper">
<%@ include file="forum/inc/header.jsp"%>
<div id="main">
<%
com.redmoon.forum.RegConfig rcfg = new com.redmoon.forum.RegConfig();
String regCompactTxt = rcfg.getProperty("regCompactTxt");
%>
<form name="form" method="post" action="regist.jsp">
<table class="tableCommon" width="81%" border="1" align="center" cellpadding="3">
<thead>
  <td align="center">注册协议</td>
</thead>
<tr>
  <td><%out.print(StrUtil.toHtml(regCompactTxt));%></td>
</tr>
<tr>
  <td align="center"><input type="submit" name="rulesubmit" value="我同意" onClick="ruleSubmit.value='true'">&nbsp;<input type="button" name="return" value="不同意" onClick="javascript:history.go(-1);">
    <input type="hidden" name="ruleSubmit"></td>
</tr>
</table>
</form>
<script type="text/javascript">
var secs = 9;
var wait = secs * 1000;
document.form.rulesubmit.value = "同 意(" + secs + ")";
document.form.rulesubmit.disabled = true;
for(i = 1; i <= secs; i++) {
        window.setTimeout("update(" + i + ")", i * 1000);
}
window.setTimeout("timer()", wait);
function update(num, value) {
        if(num == (wait/1000)) {
                document.form.rulesubmit.value = "同 意";
        } else {
                printnr = (wait / 1000) - num;
                document.form.rulesubmit.value = "同 意(" + printnr + ")";
        }
}
function timer() {
        document.form.rulesubmit.disabled = false;
        document.form.rulesubmit.value = "同 意";
}
</script>
</div>
<%@ include file="forum/inc/footer.jsp"%>
</div>
</body>
</html>