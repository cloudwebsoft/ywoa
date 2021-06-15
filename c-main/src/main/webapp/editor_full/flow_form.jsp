<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="cn.js.fan.util.*"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>智能表单设计器</title>
<link rel="STYLESHEET" type="text/css" href="edit.css">
<%
String op = ParamUtil.get(request, "op");
String formCode = ParamUtil.get(request, "formCode");
%>
<script src="../inc/common.js"></script>
<script src="../js/jquery-1.9.1.min.js"></script>
<script src="../js/jquery-migrate-1.2.1.min.js"></script>
<script>
var op = "<%=op%>";

function getContent() {
	return window.opener.getFormContent();
}

function saveexit() {
	var html;
	html = cws_getText();
	html = cws_rCode(html,"<a>　</a>","");
		
 	window.opener.setFormContent(html);
	window.opener.focus();
	
	window.close();
}

function window_onload() {
	if (op=="edit") {
		setHTML(getContent());
	}
	
	cws_Size();
}

function window_onresize() {
	// cws_Size();
}
</script>
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
</head>
<body onLoad="window_onload()" onResize="window_onresize()" style="background-color:#DEDFDE">
<table width="100%" border="0" cellpadding="0" cellspacing="0" bgcolor="#DEDFDE">
  <tr>
    <td valign="top"><%@ include file="editor.jsp"%>
<script>
// 不过滤表单中的脚本
cws_filterScript = false;
</script> 
      <Script src="flow_form_js.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>"></Script>
      <input type="hidden" id="edit" name="edit" value="" /></td>
    <td width="150px" valign="top">
    <table class="small" cellSpacing="1" cellPadding="3" width="120" align="center" border="0">
        <tbody>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left; height:30px" 
onclick=cloud_textfield()> &nbsp;<img src="../images/form/textfield.gif" align="absMiddle">&nbsp;单行输入框</BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left; height:30px" 
onclick=cloud_textarea()> &nbsp;<img src="../images/form/textarea.gif" align="absMiddle">&nbsp;多行输入框</BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left; height:30px" 
onclick=cloud_select()> &nbsp;<img src="../images/form/listmenu.gif" align="absMiddle">&nbsp;下拉菜单</BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left; height:30px" 
onclick=cloud_radio()> &nbsp;<img src="../images/form/radio.gif" align="absMiddle">&nbsp;单选框</BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left; height:30px" 
onclick=cloud_checkbox()> &nbsp;<img src="../images/form/checkbox.gif" align="absMiddle">&nbsp;选择框</BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left; height:30px" 
onclick=cloud_list()> &nbsp;<img src="../images/form/listview.gif" align="absMiddle">&nbsp;列表控件</BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left; height:30px" 
onclick=cloud_macro()> &nbsp;<img src="../images/form/auto.gif" align="absMiddle">&nbsp;宏控件</BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left; height:30px" 
onclick=cloud_calendar()> <img src="../images/form/calendar.gif" align="absMiddle">&nbsp;日历控件 </BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left" 
onclick=cloud_calculate()> <img src="../images/form/calc.gif" align="absMiddle" width="26" height="26">计算控件 </BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: left; height:30px" 
onclick=cloud_btnCtl()> &nbsp;<img src="../images/form/btn.gif" align="absMiddle">&nbsp;按  钮</BUTTON></td>
          </tr>
        </tbody>
      </table>
      <table class="small" cellSpacing="1" cellPadding="3" width="120" align="center" border="0">
        <tbody>
          <tr class="TableHeader">
            <td align="center"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: center; height: 25px" 
onclick="showProperty()"> <strong>控件属性</strong></BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; HEIGHT: 25px; TEXT-ALIGN: center" 
onclick="saveexit()"> <b>保存并退出</b></BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; HEIGHT: 25px; TEXT-ALIGN: center" 
onclick="window.close()"> <b>关闭设计器</b></BUTTON>
          <input type="hidden" name="CONTENT">
          <input type="hidden" name="CLOSE_FLAG">
          <input type="hidden" value="3" name="FORM_ID">
			</td>
          </tr>
          </tbody>
      </table></td>
  </tr>
</table>
</body>
</html>