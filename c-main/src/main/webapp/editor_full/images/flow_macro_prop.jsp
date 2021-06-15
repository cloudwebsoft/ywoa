<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "java.net.URLEncoder"%>
<%@ page import = "java.util.*"%>
<%@ page import = "cn.js.fan.util.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "com.redmoon.oa.flow.*"%>
<%@ page import = "com.redmoon.oa.flow.macroctl.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<LINK href="img_files/pop.css" type=text/css rel=stylesheet>
<%@ include file="../../inc/nocache.jsp"%>
<STYLE type=text/css>BODY {
	FONT: 9pt "宋体", Verdana, Arial, Helvetica, sans-serif
}
A {
	FONT: 9pt "宋体", Verdana, Arial, Helvetica, sans-serif
}
TABLE {
	FONT: 9pt "宋体", Verdana, Arial, Helvetica, sans-serif
}
DIV {
	FONT: 9pt "宋体", Verdana, Arial, Helvetica, sans-serif
}
SPAN {
	FONT: 9pt "宋体", Verdana, Arial, Helvetica, sans-serif
}
TD {
	FONT: 9pt "宋体", Verdana, Arial, Helvetica, sans-serif
}
TH {
	FONT: 9pt "宋体", Verdana, Arial, Helvetica, sans-serif
}
INPUT {
	FONT: 9pt "宋体", Verdana, Arial, Helvetica, sans-serif
}
SELECT {
	FONT: 9pt "宋体", Verdana, Arial, Helvetica, sans-serif
}
BODY {
	PADDING-RIGHT: 5px; PADDING-LEFT: 5px; PADDING-BOTTOM: 5px; PADDING-TOP: 5px
}
</STYLE>
<%
String mode = ParamUtil.get(request, "mode");
String ctlType = ParamUtil.get(request, "ctlType");
String editObjName = ParamUtil.get(request, "editObjName");
String formCode = ParamUtil.get(request, "formCode");
%>
<SCRIPT language=JavaScript>
var parentWin;
var mode;
var ctlType;
var editObj;

function ok() {
	if (ctlName.value=="") {
		alert("请填写标识！");
		return;
	}
	if (ctlTitle.value=="") {
		alert("请填写名称！");
		return;
	}
	if (mode=="create")
		parentWin.CreateMacroCtl(ctlType, ctlName.value, ctlTitle.value, defaultValue.value, macroType.value, "宏控件：" + macroType.options(macroType.selectedIndex).text, canNull.value);
	else {
		editObj.name = ctlName.value;
		editObj.title = ctlTitle.value;
		editObj.value = "宏控件：" + macroType.options(macroType.selectedIndex).text;
		editObj.setAttribute("macroType", macroType.value);
		if (macroType.value == 'macro_sql') {
			editObj.setAttribute("macroDefaultValue", encodeURI(defaultValue.value));
		} else {
			editObj.setAttribute("macroDefaultValue", defaultValue.value);
		}
		editObj.setAttribute("canNull", canNull.value);
	}
	window.close();
}

function window_onload() {
	parentWin =	window.opener;
	mode = "<%=mode%>";
	if (mode=="create")
		ctlType = "<%=ctlType%>";
	else {
		editObj = window.opener.IframeID.document.getElementsByName("<%=editObjName%>")[0];
		ctlName.value = editObj.name;
		ctlTitle.value = editObj.title;
		macroType.value = editObj.getAttribute("macroType");
		if (macroType.value == 'macro_sql') {
			defaultValue.value = decodeURI(editObj.getAttribute("macroDefaultValue"));
		} else {
			defaultValue.value = editObj.getAttribute("macroDefaultValue");
		}
		macroType.disabled = true;
		ctlName.disabled = true;
		
		canNull.value = editObj.getAttribute("canNull");
		if (canNull.value=="")
			canNull.value = "1";		
	}
}

function openWin(url,width,height) {
  var newwin=window.open(url,"_blank","toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes,top=50,left=120,width="+width+",height="+height);
}

function setSequence(id, name) {
	ctlTitle.value = name;
	defaultValue.value = id;
}
</SCRIPT>
<META content="MSHTML 6.00.3790.373" name=GENERATOR></HEAD>
<BODY bgColor=menu onLoad="window_onload()">
<TABLE width="293" border=0 align=center cellPadding=0 cellSpacing=0>
  <TBODY>
  <TR>
    <TD height="28" align=center>控件字段：</TD>
    <TD height="28" align=left><input name="ctlName" type="text" style="width: 200px; height:22px" maxlength="30"></TD>
  </TR>
  <TR>
<TD width="86" height="28" align=center>控件名称：</TD>
<TD width="207" height="28" align=left><input type="text" name="ctlTitle" style="width: 200px; height:22px"></TD>
  </TR>
  
  <TR>
    <TD width="86" height="28" align=center>默&nbsp;认&nbsp;值：</TD>
    <TD width="207" height="28" align=left><input type="text" name="defaultValue" style="width: 200px; height:22px"></TD>
  </TR>
  <TR>
    <TD height="28" align=center>类&nbsp;&nbsp;&nbsp;&nbsp;型：</TD>
    <TD height="28" align=left>
	<select name="macroType" id="macroType" onChange="onMacroTypeChange(this)">
<%
MacroCtlMgr mm = new MacroCtlMgr();
Iterator ir = mm.getAllMacroUnit().iterator();
while (ir.hasNext()) {
	MacroCtlUnit mu = (MacroCtlUnit)ir.next();
	// if (!mu.isDisplay())
	//	continue;
	if (!mu.isDisplay())
		continue; 
	out.print("<option value=\"" + mu.getCode() + "\">" + mu.getName() + "</option>");
}%>
	</select>    
    </TD>
  </TR>
  <TR>
    <TD height="28" align=center>必&nbsp;填&nbsp;项：</TD>
    <TD height="28" align=left>
      <select id="canNull" name="canNull">
        <option value="1">否</option>
        <option value="0">是</option>
      </select>
    </TD>
  </TR>
  <TR>
    <TD height="28" align=center>&nbsp;</TD>
    <TD height="28" align=left>&nbsp;</TD>
  </TR>
  
  <TR>
    <TD colspan="2" align=center><input name="submit" type=submit id=Ok onClick=ok() value=确定>
&nbsp;&nbsp;
<input name="button" type=button onClick=window.close(); value=取消></TD>
  </TR>
</TBODY></TABLE>
</BODY>
<script>
function onMacroTypeChange(obj) {
	if(obj.options[obj.selectedIndex].value=='macro_flow_sequence'  || obj.options[obj.selectedIndex].value=='macro_flow_number'){
		canNull.disabled = false;
		openWin('../../flow/flow_sequence_sel.jsp', 300, 40);
	}
	else if (obj.options[obj.selectedIndex].value=='macro_flow_select') {
		canNull.disabled = false;
		openWin('../../flow/basic_select_sel.jsp', 300, 40);
	}
	else if (obj.options[obj.selectedIndex].value=='nest_table') {
		canNull.disabled = true;
		canNull.value="1";
		// openWin('../../visual/module_sel.jsp', 300, 40);
		openWin('../../visual/module_field_sel_nest.jsp?nestType=nest_table&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
	}
	else if (obj.options[obj.selectedIndex].value=='nest_form') {
		canNull.disabled = true;
		canNull.value="1";
		openWin('../../visual/module_sel.jsp', 300, 40);
	}
	else if (obj.options[obj.selectedIndex].value=='nest_sheet') {
		canNull.value="1";
		canNull.disabled = true;
		openWin('../../visual/module_field_sel_nest.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
	}
	else if (obj.options[obj.selectedIndex].value=='module_field_select') {
		canNull.disabled = false;
		openWin('../../visual/module_field_sel.jsp?openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 620, 330);
	}
	else if (obj.options[obj.selectedIndex].value=='role_user_select') {
		canNull.disabled = false;
		openWin('../../flow/role_sel.jsp', 300, 40);
	}	
	else if (obj.options[obj.selectedIndex].value=='macro_form_data_map') {
		canNull.disabled = false;
		openWin('../../flow/form_data_map.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 800, 600);
	}	
	else if (obj.options[obj.selectedIndex].value=='macro_queryfield_select') {
		canNull.disabled = false;
		openWin('../../flow/macro/macro_query_field_sel.jsp?formCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 300);
	}
	else if (obj.options[obj.selectedIndex].value=='macro_detaillist_ctl') {
		canNull.disabled = false;
		openWin('../../visual/module_field_sel_nest1.jsp?nestType=nest_sheet&openerFormCode=<%=StrUtil.UrlEncode(formCode)%>', 600, 300);
	}
	else{
		canNull.disabled = false;
	}	
}
</script>
</HTML>
