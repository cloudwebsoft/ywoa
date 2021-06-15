<%@ page contentType="text/html;charset=utf-8" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<TITLE>选择部门</TITLE>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<%@ include file="inc/nocache.jsp"%>
<script src="inc/common.js"></script>
<script src="js/jquery-1.9.1.min.js"></script>
<script src="js/jquery-migrate-1.2.1.min.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
<script src="js/jstree/jstree.js"></script>
<link type="text/css" rel="stylesheet" href="js/jstree/themes/default/style.css" />
<script src="js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
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
function selectNode(code, name) {
			window.opener.selectNode(code, name);
			window.close();
}
function ShowChild(imgobj, name) {
	var tableobj = findObj("childof"+name);
	if (tableobj==null) {
		document.frames.ifrmGetChildren.location.href = "admin/dept_ajax_getchildren.jsp?op=singleSel&parentCode=" + name;
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
</HEAD>
<BODY leftMargin=4 topMargin=8 rightMargin=0 class=menubar>
<table width="450" border="0" align="center" cellpadding="0" cellspacing="0" class="tableframe">
  <tr> 
    <td height="24" colspan="2" align="center" class="tdStyle_1"><strong>请选择部门</strong></td>
  </tr>
  <form id="form1" name="form1" method="post">
  <tr> 
    <td width="24" height="20">&nbsp;</td>
    <td width="249" valign="top">
<%
com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();

String dirCode = ParamUtil.get(request, "dirCode");
if (dirCode.equals("")) {
	dirCode = privilege.getUserUnitCode(request);
}

DeptMgr dir = new DeptMgr();
DeptDb leaf = dir.getDeptDb(dirCode);
DeptView tv = new DeptView(request, leaf);
//tv.SelectSingleAjax(out, "selectNode", "", "", true );
String jsonData = tv.getJsonString();
%></td>
  </tr>
  <tr><td colspan="2"><div ><div id="deptTree"></div></div></td></tr>
  </form>
</table>
<iframe id="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
</BODY>
<script>
function bindClick() {
	$("a").bind("click", function() {
			$("a").css("color", "");
			$(this).css("color", "red");
		});
}

$(document).ready(function(){
	$('#deptTree').jstree({
				    	"core" : {
				            "data" :  <%=jsonData%>,
				            "themes" : {
							   "theme" : "default" ,
							   "dots" : true,  
							   "icons" : true  
							},
							"check_callback" : true,	
				 		},
				 		"plugins" : ["wholerow", "themes", "ui", "types","state"],
					}).bind('click.jstree', function (e, data) {//绑定选中事件
					     //alert(data.node.id);
					     //alert(data.node.text);
						var eventNodeName = e.target.nodeName;               
			            if (eventNodeName == 'INS') {                   
			                return;               
			            } else if (eventNodeName == 'A') {                   
			                var $subject = $(e.target).parent();                   
			                if ($subject.find('ul').length > 0) {            
			                } else { 
			                  //选择的id值
			                   //alert($(e.target).parents('li').attr('id'));  
			                   //alert($subject.text());
			                   var code = $(e.target).parents('li').attr('id');
			                   var name = $subject.text(); 
			                   selectNode(code,name);    
			                }               
			            }   
   					});
   	bindClick();
});
</script>
</HTML>
