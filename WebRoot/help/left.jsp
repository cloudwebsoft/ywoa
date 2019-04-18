<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.help.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%
String skincode = UserSet.getSkin(request);
if (skincode==null || skincode.equals(""))skincode = UserSet.defaultSkin;
SkinMgr skm = new SkinMgr();
Skin skin = skm.getSkin(skincode);
String skinPath = skin.getPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>帮助-菜单</title>
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/frame.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/<%=skinPath%>/css.css" />
<script>
function findObj(theObj, theDoc) {
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
		o("ifrmGetChildren").contentWindow.location.href = "dir_ajax_getchildren.jsp?op=simple&parentCode=" + name;
		// document.frames.ifrmGetChildren.location.href = "dir_ajax_getchildren.jsp?op=simple&parentCode=" + name;
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1) {
			imgobj.src = "images/i_minus.gif";
		}
		else
			imgobj.src = "images/i_plus.gif";
		return;
	}
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "images/i_minus.gif";
		else
			imgobj.src = "images/i_plus.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "images/i_minus.gif";
		else
			imgobj.src = "images/i_plus.gif";
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
<script src="../inc/common.js"></script>
<script src="../js/jquery.js"></script>
<script src="../js/jquery.my.js"></script>
<script src="../js/jstree/jstree.js"></script>
<link type="text/css" rel="stylesheet" href="../js/jstree/themes/default/style.css" />
</head>
<body>
<jsp:useBean id="dir" scope="page" class="com.redmoon.oa.help.Directory"/>
<table width="100%" border="0" cellpadding="0" cellspacing="0" class="frameLeftMenu">
	<tr><td>
  <%
String dirCode = ParamUtil.get(request, "dir_code");
if (dirCode.equals(""))
	dirCode = Leaf.ROOTCODE;  

Leaf leaf = dir.getLeaf(dirCode);
DirView tv = new DirView(request, leaf);
// tv.ListSimple(out, "mainFileFrame", "fileark_main.jsp", "", "" ); // "tbg1", "tbg1sel");
//tv.ListSimpleAjax(out, "mainFileFrame", "document_list_m.jsp?dir_code=", "", "", true ); // "tbg1", "tbg1sel");
String jsonData = tv.getJsonString(leaf);
%>
<div id="directoryTree"></div>    </td></tr>
</table>
<iframe id="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
</body>
<script>
function bindClick() {
	$("a").bind("click", function() {
			$("a").css("color", "");
			$(this).css("color", "red");
		});
}
//$(document).ready(bindClick);
$(document).ready(function(){
	$('#directoryTree').jstree({
				    	"core" : {
				            "data" :  <%=jsonData%>,
				            "themes" : {
							   "theme" : "default" ,
							   "dots" : true,  
							   "icons" : true  
							},
							"check_callback" : true,	
				 		},
				 		"plugins" : ["wholerow", "themes", "ui", ,"types","state"],
					}).bind('click.jstree', function (e, data) {//绑定选中事件
					     //alert(data.node.id);
						var eventNodeName = e.target.nodeName;               
			            if (eventNodeName == 'INS') {                   
			                return;               
			            } else if (eventNodeName == 'A') {                   
			                var $subject = $(e.target).parent();                   
		                   //选择的id值
		                   //alert($(e.target).parents('li').attr('id'));  
		                   //alert($subject.text());
		                   var code = $(e.target).parents('li').attr('id');
		                   window.open("document_list_m.jsp?dir_code="+code,"mainFileFrame");
			            }
   					});
   	bindClick();
});
</script>
</html>
