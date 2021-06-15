<%@page import="com.redmoon.oa.dept.DeptView"%>
<%@page import="com.redmoon.oa.dept.DeptDb"%>
<%@page import="com.redmoon.oa.dept.DeptMgr"%>
<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page import="com.redmoon.oa.android.Privilege"%>
<%@page import="cn.js.fan.util.ParamUtil"%>
<%@page import="cn.js.fan.util.StrUtil"%>
<%@page import="com.redmoon.oa.visual.*"%>
<%@page import="com.redmoon.oa.flow.WorkflowDb"%>
<%@page import="com.redmoon.oa.flow.Leaf"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<title>嵌套表</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta name="viewport"
	content="width=device-width, initial-scale=1,maximum-scale=1,user-scalable=no">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta content="telephone=no" name="format-detection" />
<link rel="stylesheet" href="../css/mui.css">
<link rel="stylesheet" href="../css/iconfont.css" />
<link rel="stylesheet" type="text/css" href="../css/mui.picker.min.css" />
<link rel="stylesheet" href="../css/my_dialog.css" />
<link type="text/css" rel="stylesheet"
	href="../../js/jstree/themes/default/style.css" />
</head>
<script>
	function findObj(theObj, theDoc) {
		var p,
			i,
			foundObj;

		if (!theDoc)
			theDoc = document;
		if ((p = theObj.indexOf("?")) > 0 && parent.frames.length) {
			theDoc = parent.frames[theObj.substring(p + 1)].document;
			theObj = theObj.substring(0, p);
		}
		if (!(foundObj = theDoc[theObj]) && theDoc.all)
			foundObj = theDoc.all[theObj];
		for (i = 0; !foundObj && i < theDoc.forms.length; i++)
			foundObj = theDoc.forms[i][theObj];
		for (i = 0; !foundObj && theDoc.layers && i < theDoc.layers.length; i++)
			foundObj = findObj(theObj, theDoc.layers[i].document);
		if (!foundObj && document.getElementById)
			foundObj = document.getElementById(theObj);

		return foundObj;
	}

	function ShowChild(imgobj, name) {
		var tableobj = findObj("childof" + name);
		if (tableobj == null) {
			document.frames.ifrmGetChildren.location.href = "../../admin/dept_ajax_getchildren.jsp?op=singleSel&parentCode=" + name;
			if (imgobj.src.indexOf("i_puls-root-1.gif") != -1)
				imgobj.src = "../../images/i_puls-root.gif";
			if (imgobj.src.indexOf("i_plus.gif") != -1) {
				imgobj.src = "../../images/i_minus.gif";
			}
			else
				imgobj.src = "../../images/i_plus.gif";
			return;
		}
		if (tableobj.style.display == "none") {
			tableobj.style.display = "";
			if (imgobj.src.indexOf("i_puls-root-1.gif") != -1)
				imgobj.src = "../../images/i_puls-root.gif";
			if (imgobj.src.indexOf("i_plus.gif") != -1)
				imgobj.src = "../../images/i_minus.gif";
			else
				imgobj.src = "../../images/i_plus.gif";
		} else {
			tableobj.style.display = "none";
			if (imgobj.src.indexOf("i_plus.gif") != -1)
				imgobj.src = "../../images/i_minus.gif";
			else
				imgobj.src = "../../images/i_plus.gif";
		}
	}

	function insertAdjacentHTML(objId, code, isStart) {
		var obj = document.getElementById(objId);
		if (isIE())
			obj.insertAdjacentHTML(isStart ? "afterbegin" : "afterEnd", code);else {
			var range = obj.ownerDocument.createRange();
			range.setStartBefore(obj);
			var fragment = range.createContextualFragment(code);
			if (isStart)
				obj.insertBefore(fragment, obj.firstChild);
			else
				obj.appendChild(fragment);
		}
	}
</script>

<body>
	<header class="mui-bar mui-bar-nav">
	<h1 class="mui-title">部门</h1>
	</header>
	<div class="mui-content mui-scroll-wrapper">
		<div class="mui-scroll">
			<%
				com.redmoon.oa.pvg.Privilege privilege = new com.redmoon.oa.pvg.Privilege();
				String dirCode = "root";
				String formCode = ParamUtil.get(request, "formCode");
				DeptMgr dir = new DeptMgr();
				DeptDb leaf = dir.getDeptDb(dirCode);
				DeptView tv = new DeptView(request, leaf);
				String jsonData = tv.getJsonString();
			%>
			<div id="deptTree"></div>
			<iframe id="ifrmGetChildren" style="display:none" width="300"
				height="300" src=""></iframe>

		</div>
	</div>

	<script type="text/javascript" src="../js/jquery-1.9.1.min.js"></script>
	<script src="../../js/jstree/jstree.js"></script>
	<script src="../js/macro/open_window_macro.js"></script>
	<script src="../js/mui.min.js"></script>
	<script src="../js/mui.picker.min.js"></script>
	<script src="../js/mui.indexedlist.js"></script>
	<script src="../js/jq_mydialog.js"></script>
	<script src="../js/macro/macro.js"></script>
	<script type="text/javascript" src="../js/config.js"></script>
	<script type="text/javascript" src="../js/base/mui.form.js"></script>
	<script type="text/javascript" src="../js/mui.nest_sheet.js"></script>

	<script type="text/javascript" charset="utf-8">
		mui('.mui-scroll-wrapper').scroll({
			deceleration : 0.0005 //flick 减速系数，系数越大，滚动速度越慢，滚动距离越小，默认值0.0006 
		});
	</script>

	<script>
	function selectNode(formCode,code, name) {
		window.parent.selectNode(formCode,code,name);
		window.parent.closeIframe();
	}
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
			                   var formCode = '<%=formCode%>';
			                   selectNode(formCode,code,name);    
			                }               
			            }   
   					});
   	bindClick();
});
</script>
</body>
</html>
