<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.forum.ui.*"%>
<%@ page import="com.redmoon.forum.security.*"%>
<%@ page import="org.jdom.*"%>
<%@ include file="../inc/inc.jsp" %>
<%@ include file="../inc/nocache.jsp" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<title><lt:Label res="res.label.forum.admin.config_m" key="forum_config"/></title>
<%@ include file="../inc/nocache.jsp" %>
<LINK href="images/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style><body bgcolor="#FFFFFF">
<script>
function SelectDateTime(obj) {
	var dt = showModalDialog("../../util/calendar/time.jsp", "" ,"dialogWidth:266px;dialogHeight:125px;status:no;help:no;");
	if (dt!=null)
		obj.value = dt;
}
</script>
<jsp:useBean id="timeconfig" scope="page" class="com.redmoon.forum.security.TimeConfig"/>
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
      <TD class=head><lt:Label res="res.label.forum.admin.ad_list" key="config_time"/></TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<table width="85%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr> 
    <td width="100%" height="23" class="thead">&nbsp;<lt:Label res="res.label.forum.admin.config_m" key="forum_config"/></td>
  </tr>
  <tr> 
    <td>
<%
	Element root = timeconfig.getRootElement();
	String name="",value1 = "", value2="", value="";
	name = request.getParameter("name");
	if (name!=null && !name.equals(""))	{
        value1 = ParamUtil.get(request, "value1").trim();
		value2 = ParamUtil.get(request, "value2").trim();
		value = value1 + "-" + value2;
		timeconfig.put(name,value);
		out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_time.jsp"));
	}
	int k = 0;
	Iterator ir = root.getChild("forum").getChildren().iterator();
	String desc = "",help = "";
	int valindex;
	while (ir.hasNext()) {
		Element e = (Element)ir.next();
		desc = e.getAttributeValue("desc");
		help = e.getAttributeValue("help");
		name = e.getName();
		value = e.getValue();
		valindex = value.indexOf("-");
        if(valindex != -1) {
		   value1 = value.substring(0, valindex);	
		   value2 = value.substring(valindex + 1);	
		}   
%>
      <table width="100%" border="0" align="center" cellpadding="2" cellspacing="1">
        <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_time.jsp'>
          <tr> 
            <td bgcolor=#F6F6F6 width='40%'> <INPUT TYPE=hidden name=name value="<%=name%>"> 
              <strong><%=timeconfig.getDescription(request, name)%>：</strong><br><%=timeconfig.getDescription(request, name + "_help")%>
            <td bgcolor=#F6F6F6 width='38%'>
             <input style="WIDTH: 80px" value="<%=value1%>" name="value1" size="30">
             &nbsp;<img style="CURSOR: hand" onClick="SelectDateTime(form<%=k%>.value1)" src="../images/clock.gif" align="absMiddle" width="18" height="18"> &nbsp;-&nbsp;
             <input style="WIDTH: 80px" value="<%=value2%>" name="value2" size="30">
             <img style="CURSOR: hand" onClick="SelectDateTime(form<%=k%>.value2)" src="../images/clock.gif" align="absMiddle" width="18" height="18">
            <td width="22%" align=center bgcolor=#F6F6F6> <INPUT TYPE=submit value='<lt:Label key="op_modify"/>'></td>
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
    <td valign="top" bgcolor="#FFFFFF" class="thead">&nbsp;</td>
  </tr>
</table> 
</body>                                       
</html>