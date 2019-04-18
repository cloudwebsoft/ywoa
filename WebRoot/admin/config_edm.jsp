<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="java.util.Enumeration"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="cn.js.fan.module.cms.kernel.*"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<%@ page import="com.redmoon.edm.*"%>
<%@ page import="org.jdom.*"%>
<%@ page import="cn.js.fan.web.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="expires" content="wed, 26 Feb 1997 08:21:57 GMT">
<title><lt:Label res="res.label.cms.config" key="config_mgr"/></title>
<%@ include file="../inc/nocache.jsp" %>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script src="../js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="../js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="../js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<script language="JavaScript">
function validate() {
	if  (document.addform.name.value=="") {
		alert('<lt:Label res="res.label.cms.config" key="type_cannot_null"/>');
		document.addform.name.focus();
		return false ;
	}
}

function checkdel(frm) {
 if(!confirm('<lt:Label res="res.label.cms.config" key="is_confirm_del"/>'))
	 return;
 frm.op.value="del";
 frm.submit();
}
</script>
<body>
<jsp:useBean id="cfgparser" scope="page" class="cn.js.fan.util.CFGParser"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "admin";
if (!privilege.isUserPrivValid(request,priv)) {
    out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

String op = ParamUtil.get(request, "op");
if ("send".equals(op)) {
	EDMJob job = new EDMJob();
	job.send();
}
com.redmoon.edm.Config myconfig = com.redmoon.edm.Config.getInstance();	  
Element root = myconfig.getRootElement();

String name="",value = "";
name = request.getParameter("name");
if (name!=null && !name.equals("")) {
	value = ParamUtil.get(request, "value");
	myconfig.put(name,value);
	out.println(StrUtil.jAlert_Redirect(SkinUtil.LoadString(request,"info_op_success"), "提示", "config_edm.jsp"));
	return;
}
%>
<TABLE cellSpacing=0 cellPadding=0 width="100%">
  <TBODY>
    <TR>
      <TD class="tdStyle_1">配置管理</TD>
    </TR>
  </TBODY>
</TABLE>
<br>
<table width="98%" border="0" align="center" cellpadding="0" cellspacing="0" class="tabStyle_1 percent98" style="BORDER-RIGHT: #a6a398 1px solid; BORDER-TOP: #a6a398 1px solid; BORDER-LEFT: #a6a398 1px solid; BORDER-BOTTOM: #a6a398 1px solid" >
  <tr> 
    <td width="100%" height="23" class="tabStyle_1_title">&nbsp;配置管理
    <input type="button" style="display:none" value="发送" onClick="window.location.href='config_edm.jsp?op=send'" />
    </td>
  </tr>
  <tr class=row > 
    <td valign="top">
      <br>
      <%
int k = 0;
Iterator ir = root.getChild("root").getChildren().iterator();
String desc = "";
while (ir.hasNext()) {
  Element e = (Element)ir.next();
  desc = e.getAttributeValue("desc");
  String type = e.getAttributeValue("type");
  String isDisplay = e.getAttributeValue("isDisplay");
  if (isDisplay!=null && "false".equals(isDisplay)) {
  	continue;
  }
  name = e.getName();
  value = e.getValue();
%>
      <form method="post" id="form<%=k%>" name="form<%=k%>" action="config_edm.jsp">
      <table width="98%" border="0" align="center" cellpadding="2" cellspacing="1">
          <tr> 
            <td width='20%'>
			<input type=hidden name=name value="<%=name%>"> 
              &nbsp;<%=myconfig.getDescription(name)%> 
            <td width='69%'>
			<%if (value.equals("true") || value.equals("false")) {%>
				<select name="value"><option value="true"><lt:Label res="res.label.cms.config" key="yes"/></option><option value="false"><lt:Label res="res.label.cms.config" key="no"/></option></select>
				<script>
				form<%=k%>.value.value = "<%=value%>";
				</script>
			<%}	else{
				if ("textarea".equals(type)) {
					%>
					<textarea id="value<%=k%>" name="value"><%=StrUtil.HtmlEncode(value)%></textarea>
					
<script type="text/javascript" src="../ckeditor365/ckeditor.js" mce_src="../ckeditor365/ckeditor.js"></script>
<script>
CKEDITOR.replace("value<%=k%>", 
	{
		height:500,
		enterMode : CKEDITOR.ENTER_BR,
        shiftEnterMode: CKEDITOR.ENTER_P,
		fullPage : true
		// skin : 'kama',
		// toolbar : 'Basic',
	});
</script>
					
					<%
				}
				else {
				%>
				<input type=text value="<%=StrUtil.HtmlEncode(value)%>" name="value" style='border:1pt solid #636563;font-size:9pt;width:500px;' size=30>
            <%	}
			}%>
            <td width="11%" align=center>
			<input type="submit" value='<lt:Label res="res.label.cms.config" key="modify"/>' />
			</td>
          </tr>
      </table>
      </form>
<%
  k++;
}
%>
<br></td>
  </tr>
  <tr class=row>
    <td valign="top" class="thead">&nbsp;</td>
  </tr>
</table> 
</body>                                        
</html>                            
  