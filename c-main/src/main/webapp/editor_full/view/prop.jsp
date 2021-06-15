<%@ page contentType="text/html;charset=utf-8"%>
<%@ page import = "cn.js.fan.util.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD>
<META http-equiv=Content-Type content="text/html; charset=utf-8">
<LINK href="img_files/pop.css" type=text/css rel=stylesheet>
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
<script src="../../inc/common.js"></script>
<SCRIPT language=JavaScript>
var parentWin;
var mode;
var fieldName, fieldTitle;
var editObj;

function ok() {
	if (mode=="create") {
		parentWin.CreateTxtCtl(fieldName, fieldTitle, o("mode").value, o("desc").value);
	}
	else {
		if (isIE11) {
			// ie11下，如下两种方式无法删除
		    // editObj.parentNode.removeChild(editObj);
			// $(editObj).remove();
			
			if (parentWin.IframeID.getSelection) {
				if (parentWin.IframeID.getSelection().rangeCount > 0) {
					var oControlRange = parentWin.IframeID.getSelection().getRangeAt(0);
					oControlRange.extractContents();
				}
			}
			parentWin.CreateTxtCtl(fieldName, fieldTitle, o("mode").value, o("desc").value);
		}
		else {
			editObj.setAttribute("mode", o("mode").value);
			editObj.setAttribute("desc", o("desc").value);
		}
	}
	window.close();
}

function window_onload() {
	parentWin =	dialogArguments[0];
	mode = dialogArguments[1];
	if (mode=="create") {
		fieldName = dialogArguments[3];
		fieldTitle = dialogArguments[4];
		
		fieldName.value = fieldName;
		spanFieldName.innerHTML = fieldName;
		spanFieldTitle.innerHTML = fieldTitle;
		
	}
	else {
		editObj = dialogArguments[2];
		fieldName = editObj.name;
		fieldTitle = editObj.value;
		spanFieldName.innerHTML = editObj.name;
		spanFieldTitle.innerHTML = editObj.value;
		desc.value = editObj.getAttribute("desc");
		o("mode").value = editObj.getAttribute("mode");
	}
}
</SCRIPT>
</HEAD>
<BODY bgColor=menu onLoad="window_onload()">
<TABLE width="293" border=0 align=center cellPadding=0 cellSpacing=0>
  <TBODY>
  <TR>
    <TD height="28" align=center>字段：</TD>
    <TD height="28" align=left><span id="spanFieldName"></span></TD>
  </TR>
  <TR>
    <TD height="28" align=center>名称：</TD>
    <TD height="28" align=left><span id="spanFieldTitle"></span>
    </TD>
  </TR>
  <TR style="display:none">
    <TD width="86" height="28" align=center>模式：</TD>
    <TD width="207" height="28" align=left>
      <select id="mode" name="mode">
        <option value="1">编辑</option>
        <option value="0">查看</option>
      </select>
      </TD>
  </TR>
  <TR style="display:none">
    <TD height="28" align=center>描述：</TD>
    <TD height="28" align=left><input type="text" name="desc" style="width: 200px; height:22px"></TD>
  </TR>
  <TR>
    <TD colspan="2" align=center><input name="submit" type=submit id=Ok onClick=ok() value=确定>
  &nbsp;&nbsp;
  <input name="button" type=button onClick=window.close(); value=取消></TD>
  </TR>
</TBODY></TABLE>
</BODY></HTML>
