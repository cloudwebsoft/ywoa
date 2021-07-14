<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>智能表单设计器</title>
<link rel="STYLESHEET" type="text/css" href="edit.css">
<style>
a:link {
color:#085A7F;
text-decoration:none;
}
a:visited {
color:#085A7F;
text-decoration:none;
}
a:hover {
color:#FF0000;
text-decoration:none;
}
</style>
<%
String op = ParamUtil.get(request, "op");
String formCode = ParamUtil.get(request, "formCode");

FormDb fd = new FormDb();
fd = fd.getFormDb(formCode);

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
      <script src="flow_form_js.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>"></script>
      <input type="hidden" id="edit" name="edit" value="" /></td>
    <td width="200px" valign="top">
    <table class="small" cellSpacing="1" cellPadding="3" width="180" align="center" border="0">
        <tbody>
          <tr class="TableHeader">
            <td align="left">
            <div style="height:500px; overflow-y:auto">
            <div style="margin-bottom:5px">请选择字段</div>
            <%
			Iterator ir = fd.getFields().iterator();
			while (ir.hasNext()) {
				FormField ff = (FormField)ir.next();
				%>
				<div>&nbsp;<img src="../images/form/textfield.gif" align="absMiddle">&nbsp;
				<a href="javascript:;" onClick="addCtl('<%=ff.getName()%>', '<%=ff.getTitle()%>')"><%=ff.getTitle()%></a>
                </div>
				<%
			}
			%>
            </div>
            </td>
          </tr>
        </tbody>
      </table>
      <table class="small" cellSpacing="1" cellPadding="3" width="120" align="center" border="0">
        <tbody>
          <tr class="TableHeader">
            <td align="center"><BUTTON style="WIDTH: 120px; TEXT-ALIGN: center; height: 25px" onClick="showProperty()"> <strong>控件属性</strong></BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; HEIGHT: 25px; TEXT-ALIGN: center" onClick="saveexit()"> <b>保存并退出</b></BUTTON></td>
          </tr>
          <tr class="TableHeader">
            <td align="middle"><BUTTON style="WIDTH: 120px; HEIGHT: 25px; TEXT-ALIGN: center" onClick="window.close()"> <b>关闭设计器</b></BUTTON>
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
<script>
// mode "create" or "edit" 当为create时，obj为text，当为edit时，obj为正在编辑的控件
function makeParams(mode, obj, fieldName, fieldTitle) {
	return new Array(window.self, mode, obj, fieldName, fieldTitle);
}

function addCtl(fieldName, fieldTitle) {
	var params = makeParams('create', 'text', fieldName, fieldTitle);
	showModalDialog('view/prop.jsp', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')	
}

function CreateTxtCtl(fieldName, fieldTitle, mode, desc) {
	var content = '<input title="' + fieldTitle + '" value="' + fieldTitle + '" name="' + fieldName + '" type=text mode="' + mode + '" desc="' + desc + '">';
	insert(content);
}

function showProperty() {
	cws_selectRange();
	
	var oControlRange, obj;
	if (window.getSelection) {
		if (cws_selection.rangeCount > 0) {
			oControlRange = cws_selection.getRangeAt(0);
			obj = oControlRange.cloneContents().childNodes.item(0);
			// 如果用extractContents()，则控件会被删除掉
			// obj = oControlRange.extractContents().childNodes.item(0);
		}
	}
	else {
		oControlRange = cws_selection.createRange();     	
		obj = oControlRange.item(0);
	}
	
	if (obj==null) {
		alert("请选择控件！");
		return;
	}
	
	var tagName = obj.tagName;
	// alert(tagName + " name=" + obj.name + " value=" + obj.value + " title=" + obj.title + " kind=" + obj.kind);
	var params = makeParams('edit', obj);
	if (tagName=="INPUT") {
		showModalDialog('view/prop.jsp', params, 'dialogWidth:320px;dialogHeight:240px;status:no;help:no;')
	}
}
</script>
</html>