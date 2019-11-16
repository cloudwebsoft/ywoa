<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="cn.js.fan.util.*"%>
<%@ page import="com.redmoon.oa.ui.*"%>
<jsp:useBean id="privilege" scope="page" class="com.redmoon.oa.pvg.Privilege"/>
<%
String priv="admin.user";
if (!privilege.isUserPrivValid(request,priv)) {
	out.println(cn.js.fan.web.SkinUtil.makeErrMsg(request, cn.js.fan.web.SkinUtil.LoadString(request, "pvg_invalid")));
	return;
}
String type = ParamUtil.get(request,"type");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>组织架构</title>
	<meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1' />
    <script src="<%=request.getContextPath() %>/inc/common.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery1.7.2.min.js"></script>
    <script src="<%=request.getContextPath() %>/js/jquery-ui/jquery-ui.js"></script>
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/css.css" />
    <link type="text/css" rel="stylesheet" href="<%=request.getContextPath()%>/skin/common/organize.css" />
    <link type="text/css" rel="stylesheet" href="<%=SkinMgr.getSkinPath(request)%>/jquery-ui/jquery-ui.css" />
    <script src="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.js" type="text/javascript"></script>
	<script src="<%=request.getContextPath() %>/js/jquery-alerts/cws.alerts.js" type="text/javascript"></script>
	<link href="<%=request.getContextPath() %>/js/jquery-alerts/jquery.alerts.css" rel="stylesheet"	type="text/css" media="screen" />
	<link href="<%=request.getContextPath() %>/js/jquery-showLoading/showLoading.css" rel="stylesheet" media="screen" />
	<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery-showLoading/jquery.showLoading.js"></script>
	<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.toaster.organize.js"></script>
	  <script>
		  function setDeptCode(curDeptCode) {
		  	$('#linkAdd').attr("href", "<%=request.getContextPath()%>/admin/organize/organize.jsp?type=add&curDeptCode=" + curDeptCode);
		  }
	  </script>
  </head>
  <body id="bg" style="overflow-x:hidden;overflow-y:hidden;">
	<div id="tabs1">
	     <ul>    
	       <li id="menu1" ><a href="<%=request.getContextPath()%>/admin/organize/organize.jsp?type=list" >人员资料</a></li>
	       <li id="menu2" ><a id="linkAdd" href="<%=request.getContextPath()%>/admin/organize/organize.jsp?type=add" >添加人员</a></li>
	       <li id="menu3" ><a href="<%=request.getContextPath()%>/admin/organize/organize.jsp?type=import" >导入人员</a></li>
	     </ul>
 	</div>
 	<div id="dlg" style="display:none;width:300px;overflow:hidden;">
 		<table >
 			<tr>
 				<td>名称：</td>
 				<td><input type="text" name="deptName" id="deptName" /></td>
 			</tr>
 			<tr>
 				<td>归属：</td>
 				<td><input type="text" id="parentDeptName" style="border-width:0px;" size="30" readOnly="true"/></td>
 			</tr>
 			<tr>
 				<td>隐藏：</td>
 				<td>
                <select id="isHide" name="isHide">
                <option value="0">否</option>
                <option value="1">是</option>
                </select>
                </td>
 			</tr>            
 			<tr>
 				<td>是否班组：</td>
	 				<td>
		 				<select name="isGroup" id="isGroup">
		 					<option value="0">否</option>
		 					<option value="1">是</option>
		 				</select>
	 				</td>
 			</tr>             
 			<tr>
 				<td>是否单位：</td>
	 				<td>
		 				<select name="dept_type" id="dept_type">
		 					<option value="1">否</option>
		 					<option value="0">是</option>
		 				</select>
	 				</td>
 			</tr>
 			<tr style="display: none">
 				<td>描述：</td>
		 		<td><input type="text" name="deptDesc" id="deptDesc" /></td>
 			</tr>
			<tr>
				<td>简称：</td>
				<td><input type="text" name="shortName" id="shortName" />(用于自动编号)</td>
			</tr>
 		</table>
 	</div>
 	<%if ("list".equals(type)){ %>
 	<iframe src="organize_frame_list.jsp" id="orgFrame" name="orgFrame" width="100%"  frameborder="no"></iframe>
 	<%} else if ("add".equals(type)) {
		String curDeptCode = ParamUtil.get(request, "curDeptCode");
	%>
 	<iframe src="organize_frame_add.jsp?curDeptCode=<%=curDeptCode%>" id="orgFrame" name="orgFrame" width="100%"  frameborder="no"></iframe>
 	<%} else {%>
 	<iframe src="user_import.jsp" id="orgFrame" name="orgFrame" width="100%"  frameborder="no"></iframe>
 	<%} %>
  </body>
  <script type="text/javascript">
  		function addDept(parent_code,parent_name,new_code){
  			$("#deptName").val("");
  			$("#parentDeptName").val(parent_name);
  			$("#dept_type").val("1"); 
  			$("#deptDesc").val("");                      
  			jQuery("#dlg").dialog({
			title: "新增部门",
			modal: true,
			// bgiframe:true,
			buttons: {
				"取消": function() {
					$(this).dialog("close");
				},
				"确定": function() {
					var new_name = $("#deptName").val();       //获取新增部门名称
					var dept_type = $("#dept_type").val();
					jQuery.ajax({
						type: "post",
						url: "<%=request.getContextPath()%>/admin/dept_do.jsp",
						contentType:"application/x-www-form-urlencoded; charset=iso8859-1",
						data : {    
							name:new_name,
							code:new_code,
					 		parent_code:parent_code,
					 		show:1,                      //是否显示  1：显示 0：不显示
					 		type:dept_type,				 //1：部门   0:单位
							isGroup:$('#isGroup').val(),
							isHide:$('#isHide').val(),
							description:$('#deptDesc').val(), // 部门描述, 用来做部门简写
							shortName:$('#shortName').val(),
					 		op:"AddChild"
						},
						dataType: "json",
						beforeSend: function(XMLHttpRequest){
							$('#bg').showLoading();
						},
						success: function(data, status){
							if (data.ret==1){
								jAlert("添加部门成功","提示");
								window.frames[0].frames[0].addNewNode(new_code,new_name,dept_type);
							} else {
								jAlert(data.msg,"提示");
							}
						},
						complete: function(XMLHttpRequest, status){
							$('#bg').hideLoading();	
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert("error:" + XMLHttpRequest.responseText);
						}
					});	
					jQuery(this).dialog("close");		
				}
			},
			closeOnEscape: true,
			draggable: true,
			resizable:true,
			width:300					
			});
  		}
  		
  		function modifyDept(code,name,parent_name,parent_code,deptType,isGroup,isHide,description,shortName){                         //code:当前节点code   parent_name:归属部门名称   name:当前节点名称
  			$("#parentDeptName").val(parent_name);
  			$("#deptName").val(name);
  			$("#dept_type").val(deptType);
			$("#isGroup").val(isGroup?1:0);
			$("#isHide").val(isHide?1:0);
			$("#deptDesc").val(description);
			$("#shortName").val(shortName);
  			jQuery("#dlg").dialog({
			title: "修改部门",
			modal: true,
			// bgiframe:true,
			buttons: {
				"取消": function() {
					$(this).dialog("close");
				},
				"确定": function() {
					var new_name = $("#deptName").val();       //获取新增部门名称
					var dept_type = $("#dept_type").val();
					jQuery.ajax({
						type: "post",
						url: "<%=request.getContextPath()%>/admin/dept_do.jsp",
						contentType:"application/x-www-form-urlencoded; charset=iso8859-1",						
						data : {    
							name:new_name,
							code:code,
					  		parentCode:parent_code,
					 		show:1,                      //是否显示  1：显示 0：不显示
					 		type:dept_type,                      //1：部门   0:单位
							isGroup:$('#isGroup').val(),							
							isHide:$('#isHide').val(),		
							description:$('#deptDesc').val(), // 部门描述, 用来做部门简写
							shortName:$('#shortName').val(),
					 		op:"modify"
						},
						dataType: "json",
						beforeSend: function(XMLHttpRequest){
							$('#bg').showLoading();
						},
						success: function(data, status){
							if (data.ret==1){
								//setToaster("修改部门成功");
								//window.frames[0].document.getElementById("deptFrame").src="dept_tree.jsp?root_code=root&flag=1";
								// window.location.reload();
								jAlert("修改部门成功","提示");
								window.frames[0].frames[0].modifyTitle(new_name,dept_type,code,$("#isHide").val()==1);
							} else {
								jAlert(data.msg,"提示");
							}
						},
						complete: function(XMLHttpRequest, status){
							$('#bg').hideLoading();	
						},
						error: function(XMLHttpRequest, textStatus){
							// 请求出错处理
							alert("error:" + XMLHttpRequest.responseText);
						}
					});	
					jQuery(this).dialog("close");		
				}
			},
			closeOnEscape: true,
			draggable: true,
			resizable:true,
			width:300					
			});
  		}
  		$(document).ready(function (){
  			if ("list"=="<%=type%>"){
  				$("#menu1").attr("class","current");
  			} else if ("add"=="<%=type%>"){
  				$("#menu2").attr("class","current");
  			} else {
  				$("#menu3").attr("class","current");
  			}
  			document.getElementById("orgFrame").height=document.documentElement.clientHeight-40;
  		})
  		function setToaster(mess){
  			$.toaster({priority : 'info', message : mess });   
  		}
  		function showLoading(){
  			$('#bg').showLoading();
  		}
  		function hiddenLoading(){
  			$('#bg').hideLoading();	
  		}
  		function page_refresh(){
  			window.location.href = "<%=request.getContextPath()%>/admin/organize/organize.jsp?type=list";
  		}
  </script>
</html>
