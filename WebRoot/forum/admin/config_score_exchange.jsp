<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ page import="org.jdom.*"%>
<%@ include file="../inc/inc.jsp" %>
<%@ include file="../inc/nocache.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><lt:Label res="res.label.forum.admin.config_m" key="forum_config"/></title>
<%@ include file="../inc/nocache.jsp" %>
<LINK href="default.css" type=text/css rel=stylesheet>
</head>
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style><body  bgcolor="#FFFFFF" text="#000000">
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="scoreexchangeconfig" scope="page" class="com.redmoon.forum.plugin.ScoreExchangeConfig"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
	if (!privilege.isMasterLogin(request)) {
		out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
		return;
	}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class=head><lt:Label res="res.label.forum.admin.config_score_exchange" key="score_setting"/></TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<table width="80%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr> 
    <td width="100%" height="23" class="thead">&nbsp;<lt:Label res="res.label.forum.admin.config_m" key="forum_config"/></td>
  </tr>
  <tr> 
    <td>
<%
	Element root = scoreexchangeconfig.getRootElement();
	String name="",value = "";
	name = request.getParameter("name");
	if (name!=null && !name.equals(""))
	{
        value = ParamUtil.get(request, "value");
		if(scoreexchangeconfig.isNumeric(name,value)){
			scoreexchangeconfig.put(name,value);
		    out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_score_exchange.jsp"));
		}else{
		    out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_fail"), "config_score_exchange.jsp"));
		}
		return;
	}
	
	int k = 0;
	Iterator ir = root.getChild("score").getChildren().iterator();
	String desc = "",help = "";
	while (ir.hasNext()) {
		Element e = (Element)ir.next();
		desc = e.getAttributeValue("desc");
		help = e.getAttributeValue("help");
		name = e.getName();
		value = e.getValue();
%>
      <table width="100%" border="0" align="center" cellpadding="2" cellspacing="1">
        <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_score_exchange.jsp'>
          <tr> 
            <td width='43%'> <INPUT TYPE=hidden name=name value="<%=name%>"> 
              <strong><%=scoreexchangeconfig.getDescription(request, name)%>：</strong><br><%=scoreexchangeconfig.getDescription(request, name + "_help")%>
            <td width='35%'>
				<input type=text value="<%=value%>" name="value" style='border:1pt solid #636563;font-size:9pt' size=30>
			<td width="22%" align=center> <INPUT TYPE=submit value='<lt:Label key="op_modify"/>'></td>
          </tr>
        </FORM>
      </table>
<%
	  k++;
	}
%>
</td>
  </tr>
  <tr class=row style="BACKGROUND-COLOR: #fafafa">
    <td height="32" valign="top" bgcolor="#FFFFFF" class="thead">&nbsp;</td>
  </tr>
</table> 
</body> 
</html>
