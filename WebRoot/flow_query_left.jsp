<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.flow.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
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
		if (imgobj.src.indexOf("i_plus-1-0.gif")!=-1)
			imgobj.src = "images/i_plus2-1-0.gif";
	}
	else
	{
		tableobj.style.display = "none";
		if (imgobj.src.indexOf("i_puls-root.gif")!=-1)
			imgobj.src = "images/i_puls-root-1.gif";
		if (imgobj.src.indexOf("i_plus2-2.gif")!=-1)
			imgobj.src = "images/i_plus-1-1.gif";
		if (imgobj.src.indexOf("i_plus2-1-0.gif")!=-1)
			imgobj.src = "images/i_plus-1-0.gif";
	}
}

function selectDir(dirCode, dirName) {
	form1.typeCode.value = dirCode;
	form1.submit();
}
</script>
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<script src="js/jquery.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
<script src="js/jstree/jstree.js"></script>
<link type="text/css" rel="stylesheet" href="js/jstree/themes/default/style.css" />
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>
<body>
<table width="100%" height="100%"  border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td width="33%" align="left" valign="top">
    <div id="flowTree"></div>
    <%
request.setAttribute("isSeeFlowType", "true");

String userName = privilege.getUser(request);
Directory dir = new Directory();
Leaf rootLeaf = dir.getLeaf(Leaf.CODE_ROOT);
DirectoryView dv = new DirectoryView(rootLeaf);
//dv.ListFunc(request, out, "_self", "selectDir", "", "" );
String jsonData = dv.getJsonStringByUser(rootLeaf, userName);
%>
	<form name="form1" action="flow_query_result.jsp" method="get" target="queryFrame">
	<input type="hidden" name="typeCode" />
    <input name="op" value="queryFlow" type="hidden" />
	</form>
	</td>
  </tr>
</table>
</body>
<script>

function bindClick() {
	$("a").bind("click", function() {
			$("a").css("color", "");
			$(this).css("color", "red");
		});
}


$(document).ready(function(){
	$('#flowTree').jstree({
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
					     //alert(e.type);
					    var eventNodeName = e.target.nodeName;               
			            if (eventNodeName == 'INS') {                   
			                return;               
			            } else if (eventNodeName == 'A') {                   
			                var $subject = $(e.target).parent();                   
		                   //选择的id值
		                   //alert($(e.target).parents('li').attr('id'));  
		                   //alert($subject.text());
		                   var code = $(e.target).parents('li').attr('id');
		                   window.open("flow/flow_list.jsp?op=search&displayMode=<%=WorkflowMgr.DISPLAY_MODE_SEARCH%>&typeCode="+code+"&myname=<%=StrUtil.UrlEncode(userName)%>","queryFrame");
			            }
   					});
   	bindClick();
});

</script>
</html>