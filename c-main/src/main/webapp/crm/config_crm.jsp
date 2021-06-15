<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="java.util.*"%>
<%@ page import="com.redmoon.oa.pvg.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.jdom.*"%>
<%@ page import="com.redmoon.oa.crm.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>CRM配置管理</title>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
if (!privilege.isUserPrivValid(request, PrivDb.PRIV_ADMIN)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

CRMConfig.reload();
CRMConfig myconfig = CRMConfig.getInstance();

String op = ParamUtil.get(request, "op");
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class="tdStyle_1">CRM&nbsp;配置</TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<%
Element root = myconfig.getRoot();

String name="",value = "";
name = request.getParameter("name");
if (name!=null && !name.equals("")) {
	value = ParamUtil.get(request, "value");
	myconfig.setProperty(name,value);
	// out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_crm.jsp"));
	myconfig.reload();
	myconfig = CRMConfig.getInstance();
	return;
}
%>
<table width="100%" class="tabStyle_1 percent80" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td colspan="3" class="tabStyle_1_title">配置管理</td>
  </tr>
  <%
int k = 0;
Iterator ir = root.getChildren().iterator();
String desc = "";
while (ir.hasNext()) {
  Element e = (Element)ir.next();
  name = e.getName();
  if (name.equals("levels"))
  	continue;
  	
  String isDisplay = StrUtil.getNullStr(e.getAttributeValue("isDisplay"));
  System.out.println(getClass() + " name=" + name + " isDisplay=" + isDisplay);
  if (isDisplay.equals("false")) {
  	continue;
  }
    	
  value = e.getValue();
  desc = (String)e.getAttributeValue("desc");
%>
  <form method="post" id="form<%=k%>" name="form<%=k%>" action='config_crm.jsp'>
    <tr>
      <td width='52%'><input type="hidden" name="name" value="<%=name%>" />
        &nbsp;<%=desc%> </td>
      <td width='34%'><%if (value.equals("true") || value.equals("false")) {%>
          <select name="value">
            <option value="true">
              <lt:Label key="yes"/>
            </option>
            <option value="false">
              <lt:Label key="no"/>
            </option>
          </select>
          <script>
				form<%=k%>.value.value = "<%=value%>";
				</script>
          <%} else {%>
          <input type="text" value="<%=value%>" name="value" style='border:1pt solid #636563;font-size:9pt' size="50" />
          <%}%>      </td>
      <td width="14%" align="center"><input class="btn" type="submit" name='edit' value='<lt:Label key="op_modify"/>' />
      </td>
    </tr>
  </form>
  <%
  k++;
}
%>
</table>
</body>                                 
</html>