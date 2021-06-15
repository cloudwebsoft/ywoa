<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.*" %>
<%@ page import="cn.js.fan.db.*" %>
<%@ page import="com.redmoon.oa.dept.*" %>
<%@ page import="cn.js.fan.util.*" %>
<%@ page import = "com.redmoon.oa.person.*"%>
<%@ page import = "com.redmoon.oa.ui.*"%>
<%@ page import = "cn.js.fan.web.*"%>
<%@ page import = "cn.js.fan.cache.jcs.*"%>
<%@ page import = "org.json.*"%>
<%@ page import = "java.io.*"%>
<%@page import="com.cloudwebsoft.framework.db.JdbcTemplate"%>
<jsp:useBean id="dm" scope="page" class="com.redmoon.oa.dept.DeptMgr"/>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv = "admin.user";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String root_code = ParamUtil.get(request, "root_code");
String pageType = ParamUtil.get(request, "pageType");
try {
	com.redmoon.oa.security.SecurityUtil.antiXSS(request, privilege, "root_code", root_code, getClass().getName());
}
catch (ErrMsgException e) {
	out.print(cn.js.fan.web.SkinUtil.makeErrMsg(request, e.getMessage()));
	return;
}
if (root_code.equals("")) {
	root_code = privilege.getUserUnitCode(request);
}
DeptDb leaf = dm.getDeptDb(root_code);
DeptView dv = new DeptView(leaf);
String jsonData = dv.getJsonString(false, true);
List<String> list = dv.getAllUnit();
StringBuilder sb = new StringBuilder();
for(String str:list){
	if (sb.length()!=0)
		sb.append(",");
	sb.append(str);
}
ArrayList<String> listHided = dv.getAllHided();

int flag = ParamUtil.getInt(request,"flag");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<meta http-equiv="X-UA-Compatible" content="IE=8"/>
<meta http-equiv="pragma" content="no-cache"/> 
<meta http-equiv="Cache-Control" content= "no-cache, must-revalidate"/> 
<meta http-equiv="expires" content= "Wed, 26 Feb 1997 08:21:57 GMT"/>
<title>部门管理</title>
<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1' />
<link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath() %>/js/jstree/themes/default/style.css" />
<link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css" />
<script src="../inc/common.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery.my.js"></script>
<script src="<%=request.getContextPath() %>/js/jstree/jstree.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery.toaster.email.js"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
<script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
<link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet" type="text/css" media="screen" />
<style>
body{
	overflow-y:auto;
}
td {
	height:20px;
}
.unit {
	font-weight:bold; 
}
.deptNodeHidden {
	color:#999;
}
html {
height:100%;
margin:0;
overflow-y:hidden;
}
#d {
height:100%;
}

</style>
</head>
<body >
<div class="organize-leftbox">
  <div class="organize-btn" onclick="add()"><img src="<%=request.getContextPath()%>/skin/images/organize/btnicon-add.png"  width="20" height="20"/>新增</div>
  <div class="organize-btn" onclick="modify()"><img src="<%=request.getContextPath()%>/skin/images/organize/btnicon-alter.png"  width="20" height="20"/>修改</div>
  <div class="organize-btn" onclick="del()"><img src="<%=request.getContextPath()%>/skin/images/organize/btnicon-del.png"  alt="" width="20" height="20"/>删除</div>
</div>
<table cellSpacing=0 cellPadding=0 width="95%" align=center>
  <TBODY>
    <TR>
      <TD height=200 valign="top">
		<div id="departmentTree"></div>
		</TD>
    </TR>
  </TBODY>
</table>
</body>
<script type="text/javascript" >
var selectNodeId , selectNodeName;	
var unitStr = "<%=sb.toString()%>";
var listCode = unitStr.split(",");
var inst ,obj;
var node;
var code;
var myjsTree;

var listHided = new Array();

$(function() {
	var i = 0;
	<%	
		for(String str : listHided){
		%>
		listHided[i]= "<%=str%>";
		i++;
	<%
	}
	%>
		
	$(function () {
		myjsTree = $('#departmentTree')
		 .jstree({
		  "core" : {
			  "data" :  <%=jsonData%>,
			  "themes" : {
				 "theme" : "default" ,
				 "dots" : true,  
				 "icons" : true  
			  },
			  "check_callback" : true,	
		 },
		 "ui" : {"initially_select" : [ "root" ]  },
		 "plugins" : ["unique", "dnd", "wholerow", "themes", "ui", "contextmenu" , "types", "crrm", "state"],
		 "contextmenu": {	//绑定右击事件
		  "items": {
			  "create": {  
				  "label": "增加",
				  "icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_add.png",
				  "action": function (data) { 
					  inst = $.jstree.reference(data.reference);
					  node = inst.get_node(data.reference);
					  selectNodeId = node.id;
					  selectNodeName = node.text;
					  addDepartment(selectNodeId);
				  }
			  },  
			  "rename": {  
				  "label": "修改",  
				  "icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_alter.png",
				  "action": function (data) { 
					  inst = $.jstree.reference(data.reference);
					  node = inst.get_node(data.reference);
					  selectNodeId = node.id;
					  selectNodeName = node.text;
					  modifyDepartment(selectNodeId,selectNodeName)
				  }  
			  },   
			  "remove": {  
				  "label": "删除",
				  "icon" : "<%=request.getContextPath() %>/js/jstree/themes/default/tree_icon_close.png",
				  "action": function (data) { 
					  inst = $.jstree.reference(data.reference);
					  node = inst.get_node(data.reference);
					  selectNodeId = node.id;
					  selectNodeName = node.text;
					  deleteDepartment(selectNodeId,inst,node);
				  } 
			  }
		  }
		 }
		}).bind('move_node.jstree', function (e, data) {//绑定移动节点事件
		 //data.node.id移动节点的id
		 //data.parent移动后父节点的id
		 //data.position移动后所在父节点的位置，第一个位置为0
		 node = data.node;
		 $.ajax({
		  type: "post",
		  url: "<%=request.getContextPath() %>/admin/dept_do.jsp",
		  dataType: "json",
		  data: {
			  op: "move",
			  code: data.node.id+"",
			  parent_code: data.parent+"",
			  position : data.position+"" 
		  },
		  success: function(data, status){
			  if(data.ret == 0){
				  alert(data.msg);
				  window.location.reload(true);   
			  }  
		  },
		  complete: function(XMLHttpRequest, status){
		  },
		  error: function(XMLHttpRequest, textStatus){
			  alert("移动失败！");
			  window.location.reload(true); 
		  }
		 });	
		 for(var i=0;i<listCode.length;i++){
		  $("#"+listCode[i]+" a").first().css("font-weight","bold");
		 }
			for(var i=0;i<listHided.length;i++){
				$("#"+listHided[i]+" a").first().css("color","#999");
			}  	 		 
		}).bind('select_node.jstree', function (e, data) {     //绑定选中事件
		  node = data.node;
		  selectNodeName = data.node.text;
		  selectNodeId = data.node.id;
		  if (parent.parent) {
		  	parent.parent.setDeptCode(selectNodeId);
		  }
		  if ("list"=="<%=pageType%>") {
			  parent.userFrame.location.href = "user_list.jsp?deptCode=" + selectNodeId;
		  }
		  else {
			  try {
				  parent.userFrame.setDeptCodeAndName(selectNodeId, selectNodeName);
			  }
			  catch (e) {}
		  }
		  for(var i=0;i<listCode.length;i++){
		  $("#"+listCode[i]+" a").first().css("font-weight","bold");
		  }
			for(var i=0;i<listHided.length;i++){
				$("#"+listHided[i]+" a").first().css("color","#999");
			} 		  
		}).bind('click.jstree', function(event) {
		 // alert(event.target.inText)
		 //node = data.node;
		 //selectNodeName = data.node.text;
		 // selectNodeId = data.node.id;
		 //   var selectNodeCode = $(event.target).parents('li').attr('id');                        //获取单击节点的id 
		 //   parent.userFrame.location.href = "user_list.jsp?deptCode="+selectNodeCode;            //根据部门查询员工               
		 //   for(var i=0;i<listCode.length;i++){
		 //		$("#"+listCode[i]+" a").first().css("font-weight","bold");
		 //	}
		 for(var i=0;i<listCode.length;i++){
		  $("#"+listCode[i]+" a").first().css("font-weight","bold");
		 }
			for(var i=0;i<listHided.length;i++){
				$("#"+listHided[i]+" a").first().css("color","#999");
			} 		 
		}).bind('ready.jstree',function(){
			<%
			String curDeptCode = ParamUtil.get(request, "curDeptCode");
			if ("".equals(curDeptCode)) {
				curDeptCode = DeptDb.ROOTCODE;
			}
			if ("add".equals(pageType)) {
				%>
					try {
						parent.userFrame.setDeptCodeAndName(selectNodeId, selectNodeName);
					}
					catch (e) {}
				<%
			}
			%>
		 	positionNode("<%=curDeptCode%>");
		});
		
		// 初始化，使得单位加粗
		for(var i=0;i<listCode.length;i++){
		 $("#"+listCode[i]+" a").first().css("font-weight","bold");
		}
  
		for(var i=0;i<listHided.length;i++){
			$("#"+listHided[i]+" a").first().css("color","#999");
		}  	  
	});
});
  
  //父页面回调函数，不刷新增加部门
  function addNewNode(myId,myText,unitCode){
  		if (selectNodeId == undefined){ 
  		 	selectNodeId = "root";
  		}
        myjsTree.jstree('create_node', selectNodeId+"", {'id' : myId+"", 'text' : myText+""}, 'last');
        positionNode(myId);
        if(unitCode == "0"){
        	listCode[listCode.length] = myId+"";
        }
        for(var i=0;i<listCode.length;i++){
			$("#"+listCode[i]+" a").first().css("font-weight","bold");
		}
		for(var i=0;i<listHided.length;i++){
			$("#"+listHided[i]+" a").first().css("color","#999");
		} 		
	            	
 	}
  
	//增加部门
	function addDepartment(parent_code){
		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/ymoa/organization/addDepartment",
			dataType: "json",
			data: {
				currNodeCode: parent_code
			},
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status){
				parent.parent.addDept(parent_code,data.parentNodeName,data.newNodeCode);
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
				alert(XMLHttpRequest.responseText);
			}
		});
 	}
 	
 	//父页面回调函数，不刷新修改部门
 	function modifyTitle(name,unitCode,myId,isHide){
		//inst.set_text(node, name, "zh");
		myjsTree.jstree("set_text", node, name, "zh");
		positionNode(myId);
        for(var i=0;i<listCode.length;i++){
			if(listCode[i] == selectNodeId+""){
				listCode.splice(i,1);
				break;
			}
		}
		// 如果 isHide为true，则判断listHided中是否有，如果没有，则 加入
		// 如果isHide为false，则判断listHided中是否有，如果有，则删除
		// console.log("isHide=" + isHide + " " + name + " " + myId);
		if (isHide) {
			$("#"+myId+" a").first().css("color", "#999");		
			listHided.splice(1,0,myId);
		}
		else {
			for (var i=0; i<listHided.length; i++) {
				if (listHided[i]==myId) {
					listHided.splice(i,1);
					break;
				}
			}
			$("#"+myId+" a").first().css("color", "");
		}

        if(unitCode == "0"){
			listCode[listCode.length] = selectNodeId+"";
        }else{
        	$("#"+selectNodeId+" a").first().css("font-weight","normal");
        }
        for(var i=0;i<listCode.length;i++){
			$("#"+listCode[i]+" a").first().css("font-weight","bold");
		}
		for(var i=0;i<listHided.length;i++) {
			$("#"+listHided[i]+" a").first().css("color","#999");
		}
	}
	
	// 定位节点     myId=部门code
	function positionNode(myId) {
		myjsTree.jstree("deselect_all");
        myjsTree.jstree("select_node", myId);
	}
 	
 	// 修改部门
 	function modifyDepartment(code, name) {
 		$.ajax({
			type: "post",
			url: "<%=request.getContextPath()%>/ymoa/organization/modifyDepartment",
			dataType: "json",
			data: {
				currNodeCode: code
			},
			beforeSend: function(XMLHttpRequest){
			},
			success: function(data, status){
				parent.parent.modifyDept(code, name, data.parentNodeName, data.parentNodeCode, data.deptType, data.group, data.hide, data.description, data.shortName);
			},
			complete: function(XMLHttpRequest, status){
			},
			error: function(XMLHttpRequest, textStatus){
				alert(XMLHttpRequest.responseText);
			}
		});	
 	}
 	function deleteDepartment(code,inst,obj){
 		if("root" == code){
       		parent.parent.setToaster("根节点不能被删除");
       		return;
         }
         parent.parent.jConfirm("您确定要删除吗?","提示",function(r){
         	if(!r){return;}
         	else{
	         $.ajax({
				type: "post",
				url: "<%=request.getContextPath()%>/admin/dept_do.jsp",
				dataType: "json",
				data: {
					op: "del",
					root_code: "root",
					delcode:code+""
				},
				beforeSend: function(XMLHttpRequest){
					parent.parent.showLoading();
				},
				success: function(data, status){
					if (data.ret==1){
						parent.parent.jAlert("删除部门成功","提示");
						myjsTree.jstree('delete_node', obj);
					} else {
						parent.parent.jAlert(data.msg,"提示");
					}
					positionNode(data.selectCode);
				},
				complete: function(XMLHttpRequest, status){
					parent.parent.hiddenLoading();
					// shrink();
					for(var i=0;i<listCode.length;i++){
						$("#"+listCode[i]+" a").first().css("font-weight","bold");
					} 
					for(var i=0;i<listHided.length;i++){
						$("#"+listHided[i]+" a").first().css("color","#999");
					} 					
				},
				error: function(XMLHttpRequest, textStatus){
					alert(XMLHttpRequest.responseText);
				}
			});	
			}
         })
	}
 	function add(){
 		if (selectNodeId==undefined){
 		    parent.parent.setToaster("请选择操作节点");
 		    return;
 		 }
 		addDepartment(selectNodeId);
 	}
 	function modify(){
 		if (selectNodeId==undefined){
 			parent.parent.setToaster("请选择操作节点");
 			return;
 		}
 		modifyDepartment(selectNodeId,selectNodeName);
 	}
 	function del(){
 		if (selectNodeId==undefined){
 			 parent.parent.setToaster("请选择操作节点");
 			 return;
 		}
 		deleteDepartment(selectNodeId,inst,node);
 	}
 	$(document).ready(function (){
 	if (0==<%=flag%>){
 		parent.parent.setToaster("右键菜单可管理或拖动部门");
 	} 
 	//setTimeout('positionNode("root")',500);
  })
</script>
</html>




