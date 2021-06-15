<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.plugin2.*"%>
<%@ page import="com.redmoon.forum.*"%>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="StrUtil" scope="page" class="cn.js.fan.util.StrUtil"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<meta http-equiv="pragma" content="no-cache">
<link rel="stylesheet" href="../../common.css">
<LINK href="default.css" type=text/css rel=stylesheet>
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title><lt:Label res="res.label.forum.admin.score" key="sel_plugin2"/></title>
<body bgcolor="#FFFFFF" topmargin='0' leftmargin='0' onLoad="window_onload()">
<jsp:useBean id="privilege" scope="page" class="com.redmoon.forum.Privilege"/>
<%
if (!privilege.isMasterLogin(request))
{
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}

Plugin2Mgr pm = new Plugin2Mgr();
Iterator ir = pm.getAllPlugin().iterator();
%>

<table width="100%"  border="0" align="center" cellpadding="0" cellspacing="0">
<form name="form1">
  <tr align="center" bgcolor="#F8F7F9">
    <td width="16%" height="24"><strong><lt:Label res="res.label.forum.admin.score" key="sel_plugin"/> &nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:ok()"><lt:Label key="ok"/></a></strong></td>
  </tr>
  <%
	while (ir.hasNext()) {
		Plugin2Unit pu = (Plugin2Unit)ir.next();
	%>
  <tr align="center">
    <td height="24"><input name="<%=pu.getCode()%>" value="<%=pu.getName(request)%>" type="checkbox"><%=pu.getName(request)%></td>
  </tr>
  <%}%>
</form>
</table>
</body>    
<script>
function window_onload() {
	init();
}

function ok() {
	window.opener.setPlugin2Code(getResult());
	window.close();
}

function init() {
   var depts = window.opener.getPlugin2Code();
   var ary = depts.split(",");
   for(var i=0; i<form1.elements.length; i++) {
   		if (form1.elements[i].type=="checkbox"){
			for (var j=0; j<ary.length; j++) {
				if (form1.elements[i].name==ary[j]) {
					form1.elements[i].checked = true;
					break;
				}
			}
   		}
   }
	
}

function getResult(){
   var ary = new Array();
   var j = 0;
   for(var i=0; i<form1.elements.length; i++) {
   		if (form1.elements[i].type=="checkbox"){
			if (form1.elements[i].checked) {
				ary[j] = new Array();
				ary[j][0] = form1.elements[i].name;
				ary[j][1] = form1.elements[i].value;
				j ++;
			}
   		}
   }
   return ary;
}
</script>                                    
</html>                            
  