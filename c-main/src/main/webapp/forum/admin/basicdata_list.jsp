<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "java.util.Enumeration"%>
<%@ page import = "java.util.Iterator"%>
<%@ page import = "org.jdom.*"%>
<%@ page import = "com.redmoon.forum.util.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>基础数据维护</title>
<link href="../common.css" rel="stylesheet" type="text/css">
<link href="default.css" rel="stylesheet" type="text/css"><style type="text/css">
<!--
.style2 {font-size: 14px}
.STYLE3 {color: #FFFFFF}
.STYLE4 {
	color: #000000;
	font-weight: bold;
}
.STYLE5 {color: #FF0000}
.STYLE6 {color: #000000}
-->
</style>
</head>
<body background="" leftmargin="0" topmargin="5" marginwidth="0" marginheight="0">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
	out.println(SkinUtil.makeErrMsg(request, SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
%>
<table cellSpacing="0" cellPadding="0" width="100%">
  <tbody>
    <tr>
      <td class="head">软件基础数据管理</td>
    </tr>
  </tbody>
</table>
<br>
<br>
<table width="92%" border="0" align="center" cellpadding="3" cellspacing="1" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid">
  <tr>
    <td width="14%" height="25" align="center" class="thead">基础数据名称</td>
    <td width="33%" align="center" class="thead">内容</td>
    <td height="25" colspan="3" align="center" class="thead">操作</td>
  </tr>
  <%
			  BasicDataMgr bdm = new BasicDataMgr();
              Element root = bdm.getRootElement();
			  List childs = root.getChildren();
              Iterator ir = childs.iterator();
			  int k = 0;
			  while (ir.hasNext()) {
                  Element e = (Element)ir.next();
				  k++;
              %>
  <tr>
    <form name="form<%=k%>" action="basicdata_do.jsp" method="post">
      <td width="14%" height="24" align="left" class="tbg1" style="height:24px">&nbsp;<%=e.getAttributeValue("name")%></td>
      <td width="33%" align="left" class="tbg1"><input type="hidden" name="op" value="delOption">
          &nbsp;
          <select name="<%=e.getAttributeValue("code")%>">
            <%
					String options = bdm.getOptionsStr(e.getAttributeValue("code"));
					%>
            <%=options%>
          </select>
      </td>
      <td width="17%" height="22" align="center" class="tbg1"><a href="javascript:form<%=k%>.op.value='setDefaultValue';form<%=k%>.submit()">设置默认项</a></td>
      <td width="16%" height="22" align="center" class="tbg1"><input type="hidden" name="itemCode" value="<%=e.getAttributeValue("code")%>">
        <a href="javascript:form<%=k%>.submit()">删除项</a></td>
    </form>
    <form name="formAdd<%=k%>" action="basicdata_do.jsp?op=addOption" method="post">
      <td width="20%" height="22" align="center" class="tbg1"><input name="<%=e.getAttributeValue("code")%>" value="<%=e.getAttributeValue("code")%>" type="hidden">
          <input type="hidden" name="itemCode" value="<%=e.getAttributeValue("code")%>">
          <input name="optionValue" value="" size="5">
        <a href="javascript:formAdd<%=k%>.submit()">添加</a></td>
    </form>
  </tr>
  <%}%>
</table>
</body>
</html>

