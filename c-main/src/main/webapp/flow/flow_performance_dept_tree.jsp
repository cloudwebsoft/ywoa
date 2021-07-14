<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>选择部门-菜单</title>
<script src="../inc/common.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
<script src="../js/jstree/jstree.js"></script>
<link type="text/css" rel="stylesheet" href="../js/jstree/themes/default/style.css" />
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
		document.frames.ifrmGetChildren.location.href = "../admin/dept_ajax_getchildren.jsp?op=simple&parentCode=" + name + "&actionUrl=<%=request.getContextPath()%>/flow/flow_performance_user_list.jsp";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "../admin/images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1) {
			imgobj.src = "../admin/images/i_minus.gif";
		}
		else
			imgobj.src = "../admin/images/i_plus.gif";
		return;
	}
	if (tableobj.style.display=="none")
	{
		tableobj.style.display = "";
		if (imgobj.src.indexOf("i_puls-root-1.gif")!=-1)
			imgobj.src = "../admin/images/i_puls-root.gif";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "../admin/images/i_minus.gif";
		else
			imgobj.src = "../admin/images/i_plus.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_plus.gif")!=-1)
			imgobj.src = "../admin/images/i_minus.gif";
		else
			imgobj.src = "../admin/images/i_plus.gif";
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

function window_onload(depts, isAdmin) {
   if (!isAdmin) {
	   try {
		   if (depts!="") {
			   var ary = depts.split(",");
			   var isFinded = false;
			   var len = document.all.tags('A').length;
			   for(var i=0; i<len; i++) {
					try {
						var aObj = document.all.tags('A')[i];
						var canSel = false;
						for (var j=0; j<ary.length; j++) {
							if (aObj.outerHTML.split("=")[2].split("\"")[0] == ary[j]) {
								canSel = true;
								break;
							}
						}
						if (!canSel) {
							aObj.outerHTML = "<a onClick=><font color='#888888'>" + aObj.innerText + "</font></a>"; 
						}					
						isFinded = true;
					}
					catch (e) {}
			   }
		   }
		   else{
			   var len = document.all.tags('A').length;
			   for(var i=0; i<len; i++) {
				   var aObj = document.all.tags('A')[i];
				   aObj.outerHTML = "<a onClick=><font color='#888888'>" + aObj.innerText + "</font></a>";
			   }	  
		   }
	   }
	   catch (e) {}
   }
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
<body>
<%
DeptMgr dm = new DeptMgr();
DeptDb dd = dm.getDeptDb(privilege.getUserUnitCode(request));
DeptView tv = new DeptView(dd);
//tv.ListSimpleAjax(request,out, "midFrame", "flow_performance_user_list.jsp", "", "", true);
String jsonData = tv.getJsonString();
%>
<div id="deptTree"></div>
<iframe id="ifrmGetChildren" style="display:none" width="300" height="300" src=""></iframe>
</body>
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
			                   window.open("flow_performance_user_list.jsp?deptCode="+code,"midFrame");
				            }
   					});
   	bindClick();
});
</script>
</html>
