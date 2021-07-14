<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.forum.plugin.entrance.*" %>
<%@ page import="com.redmoon.forum.*" %>
<%@ taglib uri="/WEB-INF/tlds/LabelTag.tld" prefix="lt" %>
<jsp:useBean id="fchar" scope="page" class="cn.js.fan.util.StrUtil"/>
<HTML><HEAD><TITLE><lt:Label res="res.label.forum.admin.bak_file" key="sel_board"/></TITLE>
<link rel="stylesheet" href="default.css">
<%@ include file="../../inc/nocache.jsp"%>
<META content="Microsoft FrontPage 4.0" name=GENERATOR><meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<style type="text/css">
<!--
.style1 {
	font-size: 12pt;
	font-weight: bold;
}
body {
	background-color: #F3F2F4;
}
-->
</style>
<script>
function func(code) {

}

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

function ShowChild(imgobj, name)
{
	var tableobj = findObj("childof"+name);
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus-1-1.gif")!=-1)
			imgobj.src = "images/i_plus2-2.gif";
		if (imgobj.src.indexOf("i_plus-1.gif")!=-1)
			imgobj.src = "images/i_plus2-1.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_puls-root.gif")!=-1)
			imgobj.src = "images/i_puls-root-1.gif";
		if (imgobj.src.indexOf("i_plus2-2.gif")!=-1)
			imgobj.src = "images/i_plus-1-1.gif";
		if (imgobj.src.indexOf("i_plus2-1.gif")!=-1)
			imgobj.src = "images/i_plus-1.gif";
	}
}

function init() {
   var boards = dialogArguments.getBoards();
   var ary = boards.split(",");
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

function getLeaves(){
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

function checkAll(isChecked){
   var ary = new Array();
   var j = 0;
   for(var i=0; i<form1.elements.length; i++) {
   		if (form1.elements[i].type=="checkbox"){
			form1.elements[i].checked = isChecked;
   		}
   }
   return ary;
}

function selBoards() {
	window.returnValue = getLeaves();
	window.close();
}

function handlerOnClick() {
	var obj = window.event.srcElement;
	if (obj.type=="checkbox") {
		;
	}
}

function window_onload() {
	// window.document.body.onclick = handlerOnClick;
	init();
}
</script>
</HEAD>
<BODY leftMargin=4 topMargin=8 rightMargin=0 class=menubar onLoad="window_onload()">
<table width="460" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe">
  <tr> 
    <td height="24" colspan="2" align="center" background="images/top-right.gif" class="right-title"><strong><lt:Label res="res.label.forum.admin.bak_file" key="sel_board"/></strong></td>
  </tr>
  <form id="form1" name="form1" method="post">
  <tr> 
    <td width="24" height="87">&nbsp;</td>
    <td width="249">
<%
Directory dir = new Directory();
Leaf lf = dir.getLeaf(Leaf.CODE_ROOT);
DirectoryView dv = new DirectoryView(lf);
dv.ListFuncWithCheckbox(out, "", "func", "", "");
%></td>
  </tr>
  </form>
  <tr align="center">
    <td height="28" colspan="2">
      <input type="button" name="okbtn2" value="<lt:Label res="res.label.forum.admin.bak_file" key="sel_all_board"/>" onClick="checkAll(true)">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
      <input type="button" name="okbtn3" value="<lt:Label res="res.label.forum.admin.bak_file" key="clear_sel"/>" onClick="checkAll(false)">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
<input type="button" name="okbtn" value="<lt:Label key="ok"/>" onClick="selBoards()">
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
    <input type="button" name="cancelbtn" value="<lt:Label key="cancel"/>" onClick="window.close()">    </td>
  </tr>
</table>
</BODY></HTML>
