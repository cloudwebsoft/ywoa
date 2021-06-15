<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.jdom.*"%>
<%@ page import="com.redmoon.forum.plugin.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<html><head>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<title><lt:Label res="res.label.forum.admin.config_m" key="forum_config"/></title>
<%@ include file="../../../../inc/nocache.jsp" %>
<LINK href="../../../admin/images/default.css" type=text/css rel=stylesheet>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
body {
	margin-left: 0px;
	margin-top: 0px;
}
-->
</style>
</head>
<body bgcolor="#FFFFFF">
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

com.redmoon.forum.plugin.group.GroupConfig myconfig = com.redmoon.forum.plugin.group.GroupConfig.getInstance();

String op = ParamUtil.get(request, "op");
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class=head><lt:Label res="res.label.forum.admin.config_m" key="sys_config"/></TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr> 
    <td width="100%" height="23" class="thead">&nbsp;配置管理</td>
  </tr>
  <tr> 
    <td valign="top" bgcolor="#FFFFFF"><%
Element root = myconfig.getRoot();

String name="",value = "";
name = request.getParameter("name");
if (name!=null && !name.equals("")) {
	value = ParamUtil.get(request, "value");
	myconfig.setProperty(name,value);
	out.println(fchar.Alert_Redirect(SkinUtil.LoadString(request, "info_op_success"), "config_m.jsp"));
	myconfig.reload();
	myconfig = com.redmoon.forum.plugin.group.GroupConfig.getInstance();	
	return;
}

int k = 0;
Iterator ir = root.getChildren().iterator();
String desc = "";
while (ir.hasNext()) {
  Element e = (Element)ir.next();
  name = e.getName();
  value = e.getValue();
%>
      <table width="100%" border="0" align="center" cellpadding="2" cellspacing="1">
        <FORM METHOD=POST id="form<%=k%>" name="form<%=k%>" ACTION='config_m.jsp'>
          <tr> 
            <td bgcolor=#F6F6F6 width='52%'> <INPUT TYPE=hidden name=name value="<%=name%>"> 
              &nbsp;<%=myconfig.getDescription(request, name)%> 
            <td bgcolor=#F6F6F6 width='34%'>
			<%
			if (name.equals("create_money_code")) {%>
				<select name="value">
				<option value=""><lt:Label key="wu"/></option>
				<%	  
						ScoreMgr sm = new ScoreMgr();
						java.util.Vector v = sm.getAllScore();
						Iterator sir = v.iterator();
						String str = "";
						while (sir.hasNext()) {
							ScoreUnit su = (ScoreUnit) sir.next();
							if (su.isExchange()) {
				%>
					  <option value="<%=su.getCode()%>"><%=su.getName()%></option>
				<%	  
						  }
					  }
				%>
				</select>			
				<script>
				form<%=k%>.value.value = "<%=value%>";
				</script>
			<%}
			else if (value.equals("true") || value.equals("false")) {%>
				<select name="value"><option value="true"><lt:Label key="yes"/></option><option value="false"><lt:Label key="no"/></option></select>
				<script>
				form<%=k%>.value.value = "<%=value%>";
				</script>
			<%}else{%>	
				<input type=text value="<%=value%>" name="value" style='border:1pt solid #636563;font-size:9pt' size=30>
			<%}%>
			</td>
			<td width="14%" align=center bgcolor=#F6F6F6> <INPUT TYPE=submit name='edit' value='<lt:Label key="op_modify"/>'>            </td>
          </tr>
        </FORM>
      </table>
<%
  k++;
}
%></td>
  </tr>
  <tr class=row style="BACKGROUND-COLOR: #fafafa">
    <td valign="top" bgcolor="#FFFFFF" class="thead">&nbsp;</td>
  </tr>
</table> 
</body>
<script>
function findObj(theObj, theDoc)
{
  var p, i, foundObj;
  
  if(!theDoc) theDoc = document;
  if( (p = theObj.indexOf("?")) > 0 && parent.frames.length)
  {
    theDoc = parent.frames[theObj.substring(p+1)].document;
    theObj = theObj.substring(0,p);
  }
  if(!(foundObj = theDoc[theObj]) && theDoc.all) foundObj = theDoc.all[theObj];
  for (i=0; !foundObj && i < theDoc.forms.length; i++) 
    foundObj = theDoc.forms[i][theObj];
  for(i=0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++) 
    foundObj = findObj(theObj,theDoc.layers[i].document);
  if(!foundObj && document.getElementById) foundObj = document.getElementById(theObj);
  
  return foundObj;
}
</script>                                       
</html>