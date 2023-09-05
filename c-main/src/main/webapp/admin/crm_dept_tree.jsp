<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.ui.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.json.JSONObject"%>
<!DOCTYPE html>
<html>
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
<script src="../js/jstree/jstree.js"></script>
<link type="text/css" rel="stylesheet" href="../js/jstree/themes/default/style.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>选择部门-菜单</title>
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

function window_onload(depts, isAdmin) {
   shrink();
}

function shrink() {
   for(var i=0; i<document.images.length; i++) {
		var imgObj = document.images[i];
		try {
			if (imgObj.tableRelate!="") {
				ShowChild(imgObj, imgObj.tableRelate);
			}
		}
		catch (e) {
		}
   }
}
</script>
</head>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String operator = privilege.getUser(request);
String strDepts = "";
boolean isAdmin = privilege.isUserPrivValid(request, "admin");
if (!isAdmin) {
	java.util.Iterator ir = privilege.getUserAdminDepts(request).iterator();
	while (ir.hasNext()) {
		DeptDb dd = (DeptDb)ir.next();
		if (strDepts.equals(""))
			strDepts = dd.getCode();
		else
			strDepts += "," + dd.getCode();
	}
}
%>
<body onLoad="window_onload('<%=strDepts%>', <%=isAdmin?true:false%>)" style="background-image:url()">
<!-- <table width="100%"  border="0">
  <tr>
    <td align="left">&nbsp;请选择部门</td>
  </tr>
</table> -->
<%
DeptMgr dm = new DeptMgr();
DeptDb dd = dm.getDeptDb(privilege.getUserUnitCode(request));
DeptView tv = new DeptView(dd);
String jsonData = tv.getJsonString();
JSONObject json = tv.getNoAdminDepts(request);
//tv.ListSimple(request,out, "midFrame", "../crm/crm_dept_user.jsp", "", "" );
%>
<div id="deptTree"></div>
</body>
<script>

function bindClick() {
	$("a").bind("click", function() {
			$("a").css("color", "");
			$(this).css("color", "red");
		});
}

$(document).ready(function(){
	var data = '<%=json.toString()%>';
	data = $.parseJSON(data);
	if (data.ret == 0) {
		alert(data.data);
		return;
	}
	var isShow = data.isShow;
	var isHide = data.isHide;
	var isAdmin = data.isAdmin;
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
				 		"plugins" : ["wholerow", "themes", "ui", ,"types","state"]
					}).bind('select_node.jstree', function (e, data) {//绑定选中事件
						for(var i=0;i<isHide.length;i++){
							$("#"+isHide[i]).hide();
						}
	   					for(var i=0;i<isShow.length;i++){
							$("#"+isShow[i]+" a").first().css("color","#888");
						}
	           		}).bind('click.jstree', function (e, data) {//绑定选中事件
					     //alert(data.node.id);
	           			for(var i=0;i<isHide.length;i++){
							$("#"+isHide[i]).hide();
						}
	   					for(var i=0;i<isShow.length;i++){
							$("#"+isShow[i]+" a").first().css("color","#888");
						}
						var eventNodeName = e.target.nodeName;               
			            if (eventNodeName == 'INS') {                   
			                return;               
			            } else if (eventNodeName == 'A') {                   
			                var $subject = $(e.target).parent();                   
		                   //选择的id值
		                   //alert($(e.target).parents('li').attr('id'));  
		                   //alert($subject.text());
		                   var code = $(e.target).parents('li').attr('id');
		                   window.open("../crm/crm_dept_user.jsp?deptCode="+code,"midFrame");
			            }
   					}).bind('ready.jstree',function(){
   						myjsTree.jstree("deselect_all");
   						var initCode;
   						if (isAdmin.length > 0) {
   							initCode = isAdmin[0];
   						} else {
   							initCode = "<%=DeptDb.ROOTCODE%>";
   						}
						myjsTree.jstree("select_node", initCode);
		                window.open("../crm/crm_dept_user.jsp?deptCode="+initCode,"midFrame");
   					});
   					for(var i=0;i<isHide.length;i++){
						$("#"+isHide[i]).hide();
					}
   					for(var i=0;i<isShow.length;i++){
						$("#"+isShow[i]+" a").first().css("color","#888");
					}
   	bindClick();
});

</script>
</html>
