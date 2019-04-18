<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.fileark.*" %>
<%@ page import="cn.js.fan.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<style>
a.column:link {
font-weight:bold;
}
a.column:visited {
font-weight:bold;
}
a.subsite:link {
font-weight:bold;
color:#0066FF;
}
a.subsite:visited {
font-weight:bold;
color:#0066FF;
}
a.link:link {
font-weight:bold;
}
a.link:visited {
font-weight:bold;
}
</style>
<title>WIKI-菜单</title>
<script src="../../../../inc/common.js"></script>
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

function ShowChild(imgobj, name) {
	var tableobj = findObj("childof"+name);
	if (tableobj==null) {
		document.frames.ifrmGetChildren.location.href = "../../../dir_ajax_getchildren.jsp?op=simple&parentCode=" + name;
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "../../../../fileark/images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1) {
			imgobj.src = "../../../../fileark/images/i_minus.gif";
		}
		else
			imgobj.src = "../../../../fileark/images/i_plus.gif";
		return;
	}
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "../../../../fileark/images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "../../../../fileark/images/i_minus.gif";
		else
			imgobj.src = "../../../../fileark/images/i_plus.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "../../../../fileark/images/i_minus.gif";
		else
			imgobj.src = "../../../../fileark/images/i_plus.gif";
	}	
}

function insertAdjacentHTML(objId,code,isStart){ 
	var obj = document.getElementById(objId);
	if(isIE()) 
		obj.insertAdjacentHTML(isStart ? "afterbegin" : "afterEnd",code); 
	else{ 
		var range=obj.ownerDocument.createRange(); 
		range.setStartBefore(obj); 
		var fragment = range.createContextualFragment(code); 
		if(isStart) 
			obj.insertBefore(fragment,obj.firstChild); 
		else 
			obj.appendChild(fragment); 
	}
}
</script>
</head>
<body>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.fileark.Directory"/>
<table width="100%"  border="0">
  <tr>
    <td align="left">&nbsp;请选择目录项</td>
  </tr>
</table>
<table width="100%"  border="0">
  <tr>
    <td width="5" align="left">&nbsp;</td>
  <td align="left"><%
Leaf leaf = dir.getLeaf(Leaf.CODE_WIKI);
if (leaf==null) {
	out.print("wiki目录不存在！");
	return;
}
DirView tv = new DirView(request, leaf);
tv.ListSimpleAjax(out, "mainFileFrame", "wiki_update_list.jsp?dir_code=", "", "", true ); // "tbg1", "tbg1sel");
%></td>
  </tr>
</table>
<iframe id="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
</body>
</html>
