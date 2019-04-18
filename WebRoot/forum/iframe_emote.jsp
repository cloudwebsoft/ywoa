<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.forum.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<html xmlns="http://www.wk.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
<title><lt:Label res="res.label.forum.showtopic" key="emote_list"/></title>
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
String expression = ParamUtil.get(request, "expression").trim();
%>
<script>
var expr = "<%=expression%>";
function window_onload() {
	if (expr!="") {
		setRadioChecked("expression", expr);
	}
	else
		expr = getRadio("expression");
		
	var rng = document.body.createTextRange();
	var sb = "expr" + expr + "end";
	if (rng.findText(sb, 1, 6)==true) // 向前搜索大小写敏感，匹配整字
	{
		rng.scrollIntoView();
	}
}

function getRadio(radionname) {
	var radioboxs = document.all.item(radionname);
	if (radioboxs!=null)
	{
		for (i=0; i<radioboxs.length; i++)
		{
			if (radioboxs[i].type=="radio" && radioboxs[i].checked)
			{ 
				return radioboxs[i].value;
			}
		}
		return radioboxs.value
	}
	return "";
}
</script>
</head>
<body onLoad="window_onload()">
<table width="100%" align="center">
<tr>
          <%
		  int i = 0;
		  for (i=25; i<=34; i++) {%>
          <td style="padding:0px">
		  <input type="radio" value="<%=i%>" name="expression" onClick="setEmote(this)"> 
          <img src="images/brow/<%=i%>.gif"><span style="display:none">expr<%=i%>end</span>
		  </td>
          <%}%>
</tr>
<tr>
          <%for (i=35; i<=44; i++) {%>
		  <td>
          <input type="radio" value="<%=i%>" name="expression" onClick="setEmote(this)"> <img src="images/brow/<%=i%>.gif">
		  <span style="display:none">expr<%=i%>end</span>
		  </td>
          <%}%>
</tr>
<tr>
          <%for (i=1; i<=10; i++) {%>
		  <td>
          <input type="radio" value="<%=i%>" name="expression" onClick="setEmote(this)"> <img src="images/brow/<%=i%>.gif">
		  <span style="display:none">expr<%=i%>end</span>
		  </td>
          <% }%>
</tr>
<tr>
          <%for (i=11; i<=20; i++) {%>
		  <td>
          <input type="radio" value="<%=i%>" name="expression" onClick="setEmote(this)"> <img src="images/brow/<%=i%>.gif">
		  <span style="display:none">expr<%=i%>end</span> 
		  </td>
          <% } %>
</tr>
<tr>
          <%for (i=21; i<=22; i++) {%>
		  <td>
          <input type="radio" value="<%=i%>" name="expression" onClick="setEmote(this)"> <img src="images/brow/<%=i%>.gif">
		  <span style="display:none">expr<%=i%>end</span>
		  </td>
          <% } %>
		  <td>
          <input type="radio" value="<%=MsgDb.EXPRESSION_NONE%>" name="expression" onClick="setEmote(this)"> <font style="font-size:9pt"><lt:Label key="wu"/></font>
		  <span style="display:none">expr<%=MsgDb.EXPRESSION_NONE%>end</span>
		  </td>
</tr>
</table>		  
<script>
function setEmote(emoteObj) {
	window.parent.document.frmAnnounce.expression.value = emoteObj.value;
}

function setRadioChecked(myitem, val)
{
     var radioboxs = document.all.item(myitem);
     if (radioboxs!=null)
     {
	   //如果只有一个radio
	   if (radioboxs.length==null) {
			if (radioboxs.type=="radio") {
            	if (radioboxs[i].value==val) {
			 	radioboxs[i].checked = true;
				return
			 	}
			}
	   }
	   for (i=0; i<radioboxs.length; i++)
       {
            if (radioboxs[i].type=="radio")
            {
                 if (radioboxs[i].value==val) {
				 	radioboxs[i].checked = true;
					return
				 }
            }
       }
     }
	 return "";
}  
</script>
</body>
</html>
